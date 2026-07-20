import pytest
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.llm_client import DummyRetryLLMClient

def test_llm_retry_success():
    # Fail 2 lần, max retry 3 lần -> Lần thứ 3 sẽ pass
    client = DummyRetryLLMClient(max_retries=3, fail_times=2)
    res = client.generate("test")
    assert "thành công" in res

def test_llm_retry_timeout_fail():
    # Fail 4 lần, nhưng chỉ cho phép retry 2 lần -> Bắn lỗi
    client = DummyRetryLLMClient(max_retries=2, fail_times=4)
    with pytest.raises(ConnectionError):
        client.generate("test")
