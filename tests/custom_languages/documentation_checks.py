"""Verify Word/README fidelity and the offline help (standard library only)."""
from pathlib import Path
import importlib.util
import re
import tempfile
import unittest
from html.parser import HTMLParser
from xml.etree import ElementTree as ET
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('readme_converter', ROOT / 'tools/readme.py')
converter = importlib.util.module_from_spec(spec)
spec.loader.exec_module(converter)
W = converter.W

class Help(HTMLParser):
    def __init__(self):
        super().__init__(); self.ids = []; self.targets = []; self.stack = []
    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if 'id' in attrs: self.ids.append(attrs['id'])
        if attrs.get('href', '').startswith('#'): self.targets.append(attrs['href'][1:])
        if tag not in {'area','base','br','col','embed','hr','img','input','link','meta','param','source','track','wbr'}:
            self.stack.append(tag)
    def handle_endtag(self, tag):
        if not self.stack or self.stack.pop() != tag: raise AssertionError('Unbalanced HTML: ' + tag)

class DocumentationChecks(unittest.TestCase):
    def test_word_readme_reproducible(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / 'README.md'
            converter.docx_to_markdown(ROOT / 'tools/漢字音典介紹.docx', output)
            # Git history may retain trailing Word spaces and different line endings.
            normalized = lambda text: '\n'.join(line.rstrip() for line in text.splitlines()).strip()
            self.assertEqual(normalized(output.read_text(encoding='utf-8')),
                             normalized((ROOT / 'README.md').read_text(encoding='utf-8')))

    def test_no_source_text_or_hyperlinks_lost(self):
        markdown = (ROOT / 'README.md').read_text(encoding='utf-8')
        plain = re.sub(r'\[([^\]]+)\]\([^\n]+?\)', r'\1', markdown).replace('*', '')
        normalize = lambda s: re.sub(r'\s+', '', s).replace('●', '')
        with ZipFile(ROOT / 'tools/漢字音典介紹.docx') as archive:
            root = ET.fromstring(archive.read('word/document.xml'))
            for p in root.iter(W + 'p'):
                text = ''.join(t.text or '' for t in p.iter(W + 't'))
                if text.strip(): self.assertIn(normalize(text), normalize(plain))
            codes = ''.join(n.text or '' for n in root.iter(W + 'instrText'))
            for match in re.finditer(r'HYPERLINK\s+(?:"([^"]+)"|(\S+))', codes, re.I):
                self.assertIn(match.group(1) or match.group(2), markdown)
        self.assertNotIn('****', markdown)
        self.assertEqual(markdown.count('| --- | --- |'), 1)

    def test_split_run_field_and_relationship_links(self):
        xml = f'''<w:p xmlns:w="{W[1:-1]}" xmlns:r="{converter.R[1:-1]}">
          <w:r><w:fldChar w:fldCharType="begin"/></w:r>
          <w:r><w:instrText> HYPERLINK "https://example.org/field" </w:instrText></w:r>
          <w:r><w:fldChar w:fldCharType="separate"/></w:r>
          <w:r><w:rPr><w:b/></w:rPr><w:t>前</w:t></w:r><w:r><w:rPr><w:b/></w:rPr><w:t>端</w:t></w:r>
          <w:r><w:fldChar w:fldCharType="end"/></w:r>
          <w:hyperlink r:id="r1"><w:r><w:t>後端</w:t></w:r></w:hyperlink></w:p>'''
        self.assertEqual(converter.inline(ET.fromstring(xml), {'r1':'https://example.org/relationship'}),
                         '[**前端**](https://example.org/field)[後端](https://example.org/relationship)')

    def test_help_structure_anchors_and_published_scope(self):
        text = (ROOT / 'app/src/main/assets/help/index.htm').read_text(encoding='utf-8')
        parser = Help(); parser.feed(text)
        self.assertFalse(parser.stack)
        self.assertEqual(len(parser.ids), len(set(parser.ids)))
        self.assertTrue(set(parser.targets) <= set(parser.ids))
        for anchor in ['sec_contents','sec_interface','sec_example','sec_romanization','custom','merged-comments']:
            self.assertIn(anchor, parser.ids)
        for feature in ['數據庫正在建設中……','編輯方案','編輯分區','默認關閉','#RRGGBB','單表','注釋結果合併顯示']:
            self.assertIn(feature, text)
        self.assertNotIn('獨立管理頁', text)

if __name__ == '__main__':
    unittest.main()
