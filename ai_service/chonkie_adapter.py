import json
import hashlib
from pathlib import Path
from src.chunking import ChunkNode

def convert_chonkie_batch(chonkie_chunks: list[dict], source_filename: str) -> list[ChunkNode]:
    """
    Chuyển đổi một danh sách các chunk từ Chonkie sang list các đối tượng ChunkNode.
    
    Lưu ý quan trọng về previous_sibling_node / next_sibling_node:
    Đây là node_id của các chunk liền kề (ngay trước/sau) theo thứ tự xuất hiện tuyến tính
    trong tài liệu, KHÔNG phải là quan hệ cây phân cấp (hierarchical tree) thực sự.
    """
    nodes = []
    
    for idx, chunk in enumerate(chonkie_chunks):
        node_id = chunk["id"]
        text = chunk["text"]
        
        # Determine previous and next sibling nodes based on linear index
        prev_node = chonkie_chunks[idx - 1]["id"] if idx > 0 else None
        next_node = chonkie_chunks[idx + 1]["id"] if idx < len(chonkie_chunks) - 1 else None
        
        chunk_hash = hashlib.sha256(text.encode('utf-8')).hexdigest()
        
        node = ChunkNode(
            node_id=node_id,
            text=text,
            source_file=source_filename,
            document_name=source_filename,
            source_document=source_filename,
            title_path=[],
            parent_node_id=None,
            previous_sibling_node=prev_node,
            next_sibling_node=next_node,
            source_url=None,
            chunk_hash=chunk_hash,
            chunk_type=chunk.get("chunk_type", "text")
        )
        # Token count doesn't exist as a field in ChunkNode, so we can't safely inject it 
        # without breaking the dataclass contract or causing issues with index_builder, 
        # but we could put it in an extra metadata dict if one existed. 
        # Since ChunkNode doesn't have an extensible `metadata` dict attribute (it only generates it in to_dict),
        # we strictly adhere to the defined dataclass fields.
        
        nodes.append(node)
        
    return nodes

def convert_chonkie_file(json_path: str) -> list[ChunkNode]:
    """
    Đọc file JSON chứa output của Chonkie và chuyển thành list các ChunkNode.
    Tự động suy luận tên file PDF gốc từ tên file JSON.
    """
    path_obj = Path(json_path)
    # Tự suy tên file PDF (ví dụ: dc26s008_chunks.json -> dc26s008.pdf)
    source_filename = path_obj.name.replace("_chunks.json", ".pdf")
    
    with open(path_obj, "r", encoding="utf-8") as f:
        chonkie_chunks = json.load(f)
        
    return convert_chonkie_batch(chonkie_chunks, source_filename)
