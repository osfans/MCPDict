#!/usr/bin/env python3
"""Convert introduction Word to README, preserving Word field hyperlinks.

Run from any directory: python tools/readme.py [source.docx] [output.md].
The database builder consumes README. Only the standard library is needed.
"""
from pathlib import Path
import re
import sys
from xml.etree import ElementTree as ET
from zipfile import ZipFile

W = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
R = "{http://schemas.openxmlformats.org/officeDocument/2006/relationships}"


def inline(paragraph, relationships):
    chunks, fields = [], []

    def append(text, bold=False, italic=False, link=""):
        if not text:
            return
        if chunks and chunks[-1][1:] == (bold, italic, link):
            chunks[-1] = (chunks[-1][0] + text, bold, italic, link)
        else:
            chunks.append((text, bold, italic, link))

    def walk(node, link=""):
        if node.tag == W + "hyperlink":
            link = relationships.get(node.get(R + "id"), link)
        if node.tag == W + "fldSimple":
            match = re.search(r'HYPERLINK\s+(?:"([^"]+)"|(\S+))', node.get(W + "instr", ""), re.I)
            if match:
                link = match.group(1) or match.group(2)
        if node.tag == W + "r":
            props = node.find(W + "rPr")
            def enabled(tag):
                value = props.find(W + tag) if props is not None else None
                return value is not None and value.get(W + "val", "1") not in ("0", "false", "off")
            for child in node:
                if child.tag == W + "fldChar":
                    kind = child.get(W + "fldCharType")
                    if kind == "begin":
                        fields.append({"code": "", "visible": False, "link": ""})
                    elif kind == "separate" and fields:
                        field = fields[-1]
                        field["visible"] = True
                        match = re.search(r'HYPERLINK\s+(?:"([^"]+)"|(\S+))', field["code"], re.I)
                        if match:
                            field["link"] = match.group(1) or match.group(2)
                    elif kind == "end" and fields:
                        fields.pop()
                elif child.tag == W + "instrText" and fields:
                    fields[-1]["code"] += child.text or ""
                elif child.tag in (W + "t", W + "tab", W + "br"):
                    if fields and not all(f["visible"] for f in fields):
                        continue
                    url = next((f["link"] for f in reversed(fields) if f["link"]), link)
                    text = (child.text or "") if child.tag == W + "t" else " "
                    append(text, enabled("b"), enabled("i"), url)
            return
        for child in node:
            walk(child, link)

    walk(paragraph)
    # Coalesce Word's split runs and emit one link around its whole display text.
    result, index = [], 0
    while index < len(chunks):
        url, group = chunks[index][3], []
        while index < len(chunks) and chunks[index][3] == url:
            text, bold, italic, _ = chunks[index]
            if text.strip():
                leading = text[:len(text) - len(text.lstrip())]
                trailing = text[len(text.rstrip()):]
                value = text.strip()
                if bold:
                    value = "**" + value + "**"
                if italic:
                    value = "*" + value + "*"
                text = leading + value + trailing
            group.append(text)
            index += 1
        value = "".join(group)
        target = "<" + url + ">" if any(c.isspace() for c in url) else url
        result.append(f"[{value}]({target})" if url else value)
    return "".join(result).strip()


def docx_to_markdown(docx_file, output_md):
    with ZipFile(docx_file) as archive:
        root = ET.fromstring(archive.read("word/document.xml"))
        styles = ET.fromstring(archive.read("word/styles.xml"))
        names = {s.get(W + "styleId"): s.find(W + "name").get(W + "val", "").lower()
                 for s in styles if s.find(W + "name") is not None}
        relationships = {}
        if "word/_rels/document.xml.rels" in archive.namelist():
            relationships = {r.get("Id"): r.get("Target", "")
                             for r in ET.fromstring(archive.read("word/_rels/document.xml.rels"))}
        blocks = []
        for block in root.find(W + "body"):
            if block.tag == W + "p":
                text = inline(block, relationships)
                if not text:
                    continue
                style = block.find(W + "pPr/" + W + "pStyle")
                name = names.get(style.get(W + "val"), "") if style is not None else ""
                heading = re.match(r"heading\s+(\d+)", name)
                if name == "title":
                    text = "# " + text
                elif heading:
                    text = "#" * min(6, int(heading.group(1)) + 1) + " " + text
                elif text.startswith("●"):
                    text = "- " + text[1:].lstrip()
                elif "list" in name:
                    text = "- " + text
                blocks.append(text)
            elif block.tag == W + "tbl":
                table = []
                for row in block.findall(W + "tr"):
                    cells = [" ".join(inline(p, relationships) for p in cell.findall(W + "p"))
                             .replace("|", "\\|") for cell in row.findall(W + "tc")]
                    table.append("| " + " | ".join(cells) + " |")
                    if len(table) == 1:
                        table.append("| " + " | ".join("---" for _ in cells) + " |")
                blocks.append("\n".join(table))
    Path(output_md).write_text("\n\n".join(blocks) + "\n", encoding="utf-8", newline="\n")


if __name__ == "__main__":
    here = Path(__file__).resolve().parent
    docx_to_markdown(sys.argv[1] if len(sys.argv) > 1 else here / "漢字音典介紹.docx",
                     sys.argv[2] if len(sys.argv) > 2 else here.parent / "README.md")
