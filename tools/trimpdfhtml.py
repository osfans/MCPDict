import re, os, sys

html = open(sys.argv[1], "r", encoding="U8").read()

html = re.sub("</div>", "", html, flags=re.M|re.S)
html = re.sub("<div.*?>", "", html, flags=re.M|re.S)
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
    "■39": "1",
    "■29": "2",
    "■34": "3",
    "■26": "6",
    "■54": "7a",
    "■5d": "7b",
    "■3f": "8",
    "■63": "ŋ̍",
}
for i,j in d.items():
    html = html.replace(i, j)
html = re.sub(r'(<span style="font-family: ZIeFfH-\d; font-size:10px">[^\d□])', '\n<br>\\1', html, flags=re.M|re.S)
open(sys.argv[2], "w", encoding="U8").write(html)