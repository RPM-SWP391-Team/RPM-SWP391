import pytest
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.prompt_builder import PromptBuilder, PromptBudgetExceededError, SYSTEM_INSTRUCTION
from src.context_builder import ExpandedContext, ContextChunk
from src.tokenizer import WhitespaceTokenizer

def test_prompt_builder_structure_and_citations():
    tokenizer = WhitespaceTokenizer()
    builder = PromptBuilder(tokenizer=tokenizer, max_prompt_tokens=1000)
    
    chunks = [
        ContextChunk(node_id="n1", text="Insulin liều 10 unit.", source="doc1", title_path=["A"], citation="url1", metadata={}),
        ContextChunk(node_id="n2", text="Metformin 500mg.", source="doc2", title_path=["B"], citation="url2", metadata={})
    ]
    expanded_ctx = ExpandedContext(chunks=chunks)
    
    patient_data = {"age": 62, "gender": "Male"}
    question = "Bệnh nhân nên dùng thuốc gì?"
    
    result = builder.build(question, expanded_ctx, patient_data)
    
    prompt = result.prompt_text
    
    # Check structure
    assert SYSTEM_INSTRUCTION in prompt
    assert "--- PATIENT CONTEXT ---" in prompt
    assert "- Age: 62" in prompt
    assert "--- RETRIEVED MEDICAL CONTEXT ---" in prompt
    assert "Question: Bệnh nhân nên dùng thuốc gì?" in prompt
    
    # Check citations
    assert "[Source 1]" in prompt
    assert "Insulin liều 10 unit." in prompt
    assert "[Source 2]" in prompt
    assert "Metformin 500mg." in prompt
    
    assert "[Source 1]" in result.citation_map
    assert result.citation_map["[Source 1]"].node_id == "n1"

def test_prompt_budget_exceeded():
    tokenizer = WhitespaceTokenizer()
    # Set limit cực nhỏ
    builder = PromptBuilder(tokenizer=tokenizer, max_prompt_tokens=10)
    
    expanded_ctx = ExpandedContext(chunks=[
        ContextChunk(node_id="n1", text="Rất dài " * 100, source="doc", title_path=[], citation="", metadata={})
    ])
    
    with pytest.raises(PromptBudgetExceededError):
        builder.build("Hỏi", expanded_ctx, {"age": 50})

def test_system_prompt_locked():
    # Kiểm chứng rằng system prompt không tự ý bị thay đổi vì nó chứa quy tắc an toàn y tế.
    assert "KHÔNG tự suy diễn hoặc lấy kiến thức ngoài" in SYSTEM_INSTRUCTION
    assert "không đủ thông tin" in SYSTEM_INSTRUCTION.lower()
