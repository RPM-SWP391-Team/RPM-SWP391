from typing import Protocol, List

class Tokenizer(Protocol):
    def tokenize(self, text: str) -> List[str]:
        """Tách từ (tokenize) cho văn bản, trả về list các từ."""
        ...

class WhitespaceTokenizer:
    """
    Tách từ đơn giản bằng khoảng trắng (default fallback).
    Dành riêng cho BM25 (Phase 2), không phụ thuộc thư viện ngoài.
    """
    def tokenize(self, text: str) -> List[str]:
        if not text:
            return []
        import string
        # Bỏ dấu câu cơ bản để tránh dính "insulin."
        text = text.translate(str.maketrans('', '', string.punctuation))
        return text.lower().split()
