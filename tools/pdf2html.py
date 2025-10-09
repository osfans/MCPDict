import re, sys, os
from io import StringIO
from pdfminer.high_level import extract_text_to_fp
# from pdfminer.layout import LAParams

os.system("del /f /q Im*.jpg Im*.bmp")
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
html = re.sub(r'(<span style="font-family: ZJLKl\d-\d; font-size:10px">[^□])', "\n<br>\\1", html, flags=re.M|re.S)
html = re.sub(r'(\n<br>)(</span><span style="font-family: 方)', "\\2", html, flags=re.M|re.S)
html = re.sub(r'(\n<br>)(</span><span style="font-family: [^"]+7px)', "\\2", html, flags=re.M|re.S)
html = re.sub(r'(\n<br>)(</span><span style="font-family: [^"]+font-size:10px">□)', "\\2", html, flags=re.M|re.S)
html = re.sub(r'(<span style="font-family: [^"]+7px">)(.*?)(</span>)', "{\\2}", html, flags=re.M|re.S).replace("}{", "").replace("\n<br>}", "}")
html = re.sub(r'style="position:absolute;.*?"', "", html, flags=re.M|re.S)
html = html.replace("--", "=")
d = {
    "■23":"33",
    "■52":"52",
    "■35":"44",
    "■4B":"213",
    "|":"3",
    "■45": "tʂ",
    "■28": "ɐ",
    "■4D3": "ɭ\u0329",
    "":"钁",
    "":"䝼",

}
for i,j in d.items():
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
#([dtʈsʰ‘bpmfvɕtØkɡnȵŋhxl]+［)