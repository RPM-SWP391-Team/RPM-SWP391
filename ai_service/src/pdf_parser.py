import os
from dataclasses import dataclass
from typing import List
import fitz  # PyMuPDF

@dataclass
class Page:
    page_number: int
    text: str

@dataclass
class ParsedDocument:
    document_name: str
    pages: List[Page]

class PdfParser:
    def __init__(self):
        pass

    def parse(self, file_path: str) -> ParsedDocument:
        doc = fitz.open(file_path)
        pages = []
        document_name = os.path.basename(file_path)
        
        for i, page in enumerate(doc):
            text = page.get_text()
            pages.append(Page(page_number=i+1, text=text))
            
        return ParsedDocument(document_name=document_name, pages=pages)
