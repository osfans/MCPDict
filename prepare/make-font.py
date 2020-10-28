#!/usr/bin/env python3

import fontforge

hz = ""
for line in open("缺字"):
    hz+=line.strip()
missing = set(map(ord,hz))

font = fontforge.open("98WB-U.ttf")
has = set()
for g in font.glyphs():
    u = g.unicode
    has.add(u)
    if u not in missing:
        font.removeGlyph(g)
font.generate("../app/src/main/res/font/han.ttf")
font.close()
print("".join(map(chr,missing-has)))
