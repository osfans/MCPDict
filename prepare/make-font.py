#!/usr/bin/env python3

import fontforge

notoext = set(map(ord,open("NotoSansCJK-Regular.txt").read().strip()))

font = fontforge.open("98WB-U.ttf")
for g in font.glyphs():
    u = g.unicode
    if u in notoext or u < 0x3400:
        font.removeGlyph(g)
font.generate("../app/src/main/res/font/han.ttf")
font.close()
