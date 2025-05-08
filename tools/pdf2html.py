import re, sys, os
from io import StringIO
from pdfminer.high_level import extract_text_to_fp
from pdfminer.layout import LAParams

os.system("del /f /q Im*.jpg")
laparams=None#LAParams(char_margin=2.0, line_margin=0.5, word_margin=0.1)
with StringIO() as out:
    extract_text_to_fp(inf=open(sys.argv[1], "rb"), output_type="html", outfp=out, codec=None, output_dir=".", laparams=None)
    html = out.getvalue()
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
html = re.sub(r'(<span style="font-family: [^"]+7px">)(.*?)(</span>)', "{\\2}", html, flags=re.M|re.S).replace("}{", "").replace("\n<br>}", "}")
d = {
    "":"ə",
    "":"ɔ",
    "":"∅",
    "":"ȵ",
    "":"ɛ",
    "":"ʐ",
    "":"ŋ",
    "":"3",
    "":"ɕ",
    "":"ʰ",
    "":"ɤ",
    "":"ʅ",
    "":"ʔ",
    "":"ɛ̃",
    "":"ɿ",
    "":"ʂ",
    "?":"ʯ",
    "":"˙",
    "":"æ",
    "":"㊂",
    "":"ʌ",
    "":"ɭ",
    "":"㊂",
    "":"ɣ",
    "":"᪶",
    "":"㊀",
    "":"㊁",
    "":"|",
    "":"7",
    "":"3",
    "":"ɐ",
    "":"ɡ",
    "":"ɦ",
    "":"1",
    "":"5",
    "":"",
    "":"",
    "":"ɑ",
    "":"",
    "":"",
    "":"ɠ",
    "":"○",
    "":"2",
    "":"ɓ",
    "":"",
    "":"8",
    "":"ʑ",
    "":"̃",
    "":"2",
    "":"ʃ",
    "":"",
    "":"ẽ",
    "":"7",
    "":"ɯ",
    "":"ɷ",
    "":"",
    "":"˨˩",
    "":"6",
    "":"1",
    "":"3",
    "":"5",
    "":"2",
    "":"ã",
    "":"ɔ̃",
    "":"ʒ",
    "":"ɪ̃",
    "":"ʊ",
    "":"",
    "":"˨",
    "":"",
    "":"",
    "":"",
    "":"",
    "":"",
    "":"",
    "":"",
    "":"",
}
for i,j in d.items():
    if j:
        html = html.replace(i, j)

puas = re.findall("[\ue000-\uefff]", html)
puad = dict()
for i in puas:
    if i not in puad:
        puad[i] = 1
    else:
        puad[i] += 1
for i in sorted(puad.keys(), key=lambda x: puad[x], reverse=True):
    print(f'\t"{i}":"",')
open(sys.argv[2], "w", encoding="U8").write(html)