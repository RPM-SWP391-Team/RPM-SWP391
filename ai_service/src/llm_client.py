import time
import logging
from typing import Protocol

logger = logging.getLogger(__name__)

class LLMClient(Protocol):
    """
    Protocol giao tiếp với LLM.
    Trách nhiệm: Nhận prompt thuần, trả về text thuần. Xử lý retry ngầm.
    Cấm build prompt hoặc parse output tại đây.
    """
    def generate(self, prompt: str, api_key: str = None) -> str: ...


import requests
import json
import os
from dotenv import load_dotenv
from . import config

load_dotenv()

class GeminiLLMClient:
    """
    Real Gemini LLM Client that uses requests to bypass SDK deprecation.
    """
    def __init__(self, api_key: str = None):
        self.api_key = api_key or os.environ.get("GEMINI_API_KEY")
        if not self.api_key:
            raise ValueError("GEMINI_API_KEY is not set.")
        self.model_name = config.LLM.model_name
        self.url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model_name}:generateContent?key={self.api_key}"

    def generate(self, prompt: str) -> str:
        t0 = time.time()
        payload = {
            "contents": [{"parts": [{"text": prompt}]}],
            "generationConfig": {
                "temperature": config.LLM.temperature,
                "maxOutputTokens": config.LLM.max_output_tokens,
            }
        }
        
        max_retries = 3
        for attempt in range(max_retries):
            try:
                response = requests.post(self.url, json=payload, headers={"Content-Type": "application/json"})
                response.raise_for_status()
                data = response.json()
                
                text_response = data["candidates"][0]["content"]["parts"][0]["text"]
                latency = time.time() - t0
                logger.info(f"[LLM] Gemini generate success. Latency: {latency:.3f}s. Prompt len: {len(prompt)}")
                return text_response
            except requests.exceptions.HTTPError as e:
                status_code = e.response.status_code
                if status_code in (503, 500, 429) and attempt < max_retries - 1:
                    wait_time = 2 ** attempt
                    print(f"[*] Gemini API {status_code} Error. Retrying in {wait_time}s...")
                    time.sleep(wait_time)
                else:
                    logger.error(f"[LLM] Gemini API error: {e}")
                    raise
            except Exception as e:
                logger.error(f"[LLM] Gemini API error: {e}")
                raise


class GroqLLMClient:
    """
    Real Groq LLM Client that uses requests to call Groq API.
    """
    def __init__(self, api_key: str = None, model_name: str = "llama-3.3-70b-versatile"):
        self.api_key = api_key or os.environ.get("GROQ_API_KEY")
        if not self.api_key:
            raise ValueError("GROQ_API_KEY is not set.")
        self.model_name = model_name
        self.url = "https://api.groq.com/openai/v1/chat/completions"

    def generate(self, prompt: str, api_key: str = None) -> str:
        t0 = time.time()
        payload = {
            "model": self.model_name,
            "messages": [{"role": "user", "content": prompt}],
            "temperature": config.LLM.temperature,
            "max_tokens": config.LLM.max_output_tokens,
        }
        
        active_key = api_key if api_key else self.api_key
        if not active_key:
            raise ValueError("No API Key available for GroqLLMClient.")
            
        headers = {
            "Authorization": f"Bearer {active_key}",
            "Content-Type": "application/json"
        }
        
        max_retries = 3
        for attempt in range(max_retries):
            try:
                response = requests.post(self.url, json=payload, headers=headers)
                response.raise_for_status()
                data = response.json()
                
                text_response = data["choices"][0]["message"]["content"]
                latency = time.time() - t0
                logger.info(f"[LLM] Groq generate success. Latency: {latency:.3f}s. Prompt len: {len(prompt)}")
                return text_response
            except requests.exceptions.HTTPError as e:
                status_code = e.response.status_code
                if status_code in (503, 500, 429) and attempt < max_retries - 1:
                    wait_time = 2 ** attempt
                    print(f"[*] Groq API {status_code} Error. Retrying in {wait_time}s...")
                    time.sleep(wait_time)
                else:
                    logger.error(f"[LLM] Groq API error: {e.response.text}")
                    raise
            except Exception as e:
                logger.error(f"[LLM] Groq API error: {e}")
                raise
