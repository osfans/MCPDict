import re, sys
from io import StringIO
from pdfminer.high_level import extract_text_to_fp

with StringIO() as out:
    extract_text_to_fp(inf=open(sys.argv[1], "rb"), output_type="html", outfp=out, codec=None)
    out.seek(0)
    html = out.read()
html = re.sub(r"(b'.*?')", lambda x:eval(x.group(1)).decode("gb18030"), html)
html = re.sub(r'<span style="position:absolute;.*?></span>', "", html, flags=re.M|re.S)
html = re.sub(r'<a .*?>.*?</a>', "", html, flags=re.M|re.S)
html = re.sub("</div>", "", html, flags=re.M|re.S)
html = re.sub("<div.*?>", "", html, flags=re.M|re.S)
html = re.sub("\n{2,}", "\n", html, flags=re.M|re.S)
html = re.sub(r'<span style="[^"]+:8px">(.*?)</span>', "", html, flags=re.M|re.S)
html = re.sub("(\n<br>)([^<])", "\\2", html, flags=re.M|re.S)
html = re.sub(r'(\n<br>)(</span><span style="font-family: 方)', "\\2", html, flags=re.M|re.S)
html = re.sub(r'(\n<br>)(</span><span style="font-family: [^"]+7px)', "\\2", html, flags=re.M|re.S)
html = re.sub(r'(\n<br>)(</span><span style="font-family: [^"]+font-size:10px">□)', "\\2", html, flags=re.M|re.S)
d1 = {
    "":"1",
    "":"3",
    "":"5",
    "":"7",
    "":"ʰ",
    "": "ʐ",
    "": "ɕ",
    "": "ŋ",
    "": "∅",
    "": "ɿ",
    "": "ʅ",
    "": "ə",
    "": "ɤ",
    "": "æ",
    "": "ɔ",
    "": "ʔ",
    "": "꜔",
    "": "˩",
    "": "꜒꜔",
    "": "꜔꜒",
    "?": "ɒ",
    "": "˙",
}
d = {
    "":"1",
    "":"ə",
    "":"ɔ",
    "":"∅",
    "":"",
    "":"2",
    "":"ȵ",
    "":"ɛ",
    "":"ʐ",
    "":"5",
    "":"",
    "":"ŋ",
    "":"3",
    "":"ɕ",
    "":"ʰ",
    "":"ɤ",
    "":"ʅ",
    "":"ʔ",
    "":"ɛ̃",
    "":"ɿ",
    "":"7",
    "":"ʂ",
    "":"",
    "":"˙",
}
for i,j in d.items():
    if j:
        html = html.replace(i, j)
html = re.sub(r'(<span style="font-family: ZIeFfH-\d; font-size:10px">[^\d□])', '\n<br>\\1', html, flags=re.M|re.S)

puas = set(re.findall("[\ue000-\uefff]", html))
for i in puas:
    print(f'"{i}":"",')
open(sys.argv[2], "w", encoding="U8").write(html)