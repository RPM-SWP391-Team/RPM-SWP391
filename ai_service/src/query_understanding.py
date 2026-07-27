import json
import logging
import re
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field, ValidationError

logger = logging.getLogger(__name__)


class QueryPlan(BaseModel):
    is_medical: bool = Field(description="True if the query is a medical or health-related query.")
    intent: str = Field(description="Intent category: 'greeting', 'off_topic', 'medical_question', 'patient_question', or 'clinical_summary'.")
    need_retrieval: bool = Field(description="Whether retrieval of medical guidelines is required.")
    need_patient_context: bool = Field(description="Whether patient-specific context is required to answer accurately.")
    need_summary: bool = Field(description="Whether this query requests an overall clinical summary report.")
    domains: List[str] = Field(default_factory=list, description="Medical domains involved, e.g. glycemic_control, hypertension, ckd, retinopathy, etc.")
    abnormal_findings: List[str] = Field(default_factory=list, description="Abnormal findings mentioned or detected, e.g. high_blood_pressure, low_TIR, high_CV.")
    evidence_needed: List[str] = Field(default_factory=list, description="Clinical guidelines needed, e.g. 'ADA glycemic targets', 'ESC blood pressure targets'.")
    search_queries: List[str] = Field(default_factory=list, description="Targeted English search queries for retrieval.")


class QueryUnderstandingService:
    """
    Service responsible for Query Understanding & Query Planning.
    Replaces heuristic keyword lists and naive query translation.
    Outputs a validated QueryPlan Pydantic model.
    """
    def __init__(self, llm_client: Any = None):
        self.llm_client = llm_client

    def _build_system_prompt(self) -> str:
        return (
            "You are a Senior Medical Query Understanding & Query Planning AI for a Clinical Decision Support System (CDSS).\n"
            "Your job is to analyze the user's input query and optional patient context, and produce a structured JSON QueryPlan.\n\n"
            "CRITICAL CATEGORIZATION RULES:\n"
            "1. 'greeting': Friendly greetings (e.g. 'Xin chào', 'Hello', 'Hi', 'Alo', 'Chào bác sĩ').\n"
            "   -> is_medical: false, intent: 'greeting', need_retrieval: false, need_patient_context: false, need_summary: false, search_queries: []\n\n"
            "2. 'off_topic': Non-medical questions (e.g. weather, sports, 'Messi', translation requests, general trivia, songs, math).\n"
            "   -> is_medical: false, intent: 'off_topic', need_retrieval: false, need_patient_context: false, need_summary: false, search_queries: []\n\n"
            "3. 'clinical_summary': Requests to summarize patient condition/data (e.g. 'Tổng hợp tình trạng bệnh nhân', 'Báo cáo lâm sàng').\n"
            "   -> is_medical: true, intent: 'clinical_summary', need_retrieval: false, need_patient_context: true, need_summary: true, search_queries: []\n\n"
            "4. 'patient_question': Patient-specific health queries requiring patient vital signs/data (e.g. 'Đường huyết của tôi có ổn không?', 'Huyết áp 150/90 có cao không?').\n"
            "   -> is_medical: true, intent: 'patient_question', need_retrieval: true, need_patient_context: true, need_summary: false\n\n"
            "5. 'medical_question': General medical guideline / drug / disease questions without needing patient data (e.g. 'TIR là gì?', 'Liều Metformin thế nào?', 'Mục tiêu HbA1c người cao tuổi').\n"
            "   -> is_medical: true, intent: 'medical_question', need_retrieval: true, need_patient_context: false, need_summary: false\n\n"
            "QUERY PLANNING RULES (For search_queries):\n"
            "- Each search query MUST be in English.\n"
            "- Each search query MUST target ONE specific clinical guideline section (ADA / ESC).\n"
            "- DO NOT create long generic queries containing full patient demographic data.\n"
            "- DO NOT answer the question.\n"
            "- Examples of targeted query mapping:\n"
            "  * TIR / TAR / TBR / Glucose targets -> 'ADA glycemic targets TIR TAR TBR adults'\n"
            "  * Glucose variability (CV%) -> 'ADA glucose variability recommendations'\n"
            "  * High Blood Pressure (SBP/DBP) -> 'ESC hypertension blood pressure target diabetes'\n"
            "  * Kidney / Nephropathy / CKD -> 'ADA chronic kidney disease diabetes'\n"
            "  * Retinopathy / Eye -> 'ADA diabetic retinopathy guideline'\n"
            "  * Neuropathy / Foot -> 'ADA diabetic neuropathy foot care'\n"
            "  * Insulin therapy / Metformin -> 'ADA insulin therapy recommendation'\n"
            "  * Elderly diabetes -> 'ADA diabetes management older adults'\n"
            "  * Pregnancy / Gestational -> 'ADA diabetes in pregnancy guideline'\n\n"
            "JSON OUTPUT FORMAT:\n"
            "Return ONLY a valid JSON object matching this schema:\n"
            "{\n"
            '  "is_medical": boolean,\n'
            '  "intent": string,\n'
            '  "need_retrieval": boolean,\n'
            '  "need_patient_context": boolean,\n'
            '  "need_summary": boolean,\n'
            '  "domains": [string],\n'
            '  "abnormal_findings": [string],\n'
            '  "evidence_needed": [string],\n'
            '  "search_queries": [string]\n'
            "}"
        )

    def analyze(self, question: str, patient_context: Optional[Dict[str, Any]] = None, api_key: str = None) -> QueryPlan:
        q_clean = question.strip()
        
        # Direct static shortcut for common greetings
        q_lower = q_clean.lower()
        greetings = ["chào", "chào bạn", "chào buổi sáng", "chào bác sĩ", "chào em", "chào ad", "hi", "hello", "xin chào", "good morning", "good afternoon", "good evening", "alo"]
        if q_lower in greetings or any(q_lower == g for g in greetings):
            return QueryPlan(
                is_medical=False,
                intent="greeting",
                need_retrieval=False,
                need_patient_context=False,
                need_summary=False,
                domains=[],
                abnormal_findings=[],
                evidence_needed=[],
                search_queries=[]
            )

        prompt = f"User Query: {q_clean}\n"
        if patient_context:
            prompt += f"Patient Context Available: {patient_context}\n"

        system_prompt = self._build_system_prompt()

        if not self.llm_client:
            return self._fallback_query_plan(q_clean)

        try:
            raw_response = self.llm_client.generate(
                prompt=prompt,
                system_prompt=system_prompt,
                api_key=api_key,
                max_tokens=400,
                temperature=0.1
            ).strip()

            # Extract JSON block if surrounded by markdown fence
            json_str = raw_response
            if "```json" in json_str:
                json_str = json_str.split("```json")[1].split("```")[0].strip()
            elif "```" in json_str:
                json_str = json_str.split("```")[1].split("```")[0].strip()

            data = json.loads(json_str)
            plan = QueryPlan(**data)
            logger.info(f"[QueryUnderstanding] Analyzed query successfully. Intent: {plan.intent}, Search Queries: {plan.search_queries}")
            return plan
        except (json.JSONDecodeError, ValidationError, Exception) as e:
            logger.warning(f"[QueryUnderstanding] LLM response parsing/validation failed: {e}. Falling back to default query plan.")
            return self._fallback_query_plan(q_clean)

    def _fallback_query_plan(self, question: str) -> QueryPlan:
        """
        Fallback logic when LLM fails or is unavailable.
        """
        q_lower = question.lower()
        is_summary = any(k in q_lower for k in ["tổng hợp", "báo cáo lâm sàng", "tóm tắt tình trạng"])
        
        return QueryPlan(
            is_medical=True,
            intent="clinical_summary" if is_summary else "medical_question",
            need_retrieval=not is_summary,
            need_patient_context=True if is_summary else False,
            need_summary=is_summary,
            domains=["glycemic_control"],
            abnormal_findings=[],
            evidence_needed=["ADA guideline"],
            search_queries=[question] if not is_summary else []
        )
