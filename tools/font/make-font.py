#!/usr/bin/env python3

import fontforge

notoext = set(map(ord,open("NotoSansCJK-Regular.txt").read().strip()))

font = fontforge.open("TH-Feon.sfd")
for g in font.glyphs():
    u = g.unicode
    if u in notoext or u < 0x3400 or u >= 0x2A700 or 0xa000<= u < 0x20000:
        font.removeGlyph(g)
font.generate("../..//app/src/main/res/font/hanb.ttf")
font.close()

font = fontforge.open("TH-Feon.sfd")
for g in font.glyphs():
    u = g.unicode
    if u in notoext or u < 0x2A700 or u >= 0x2B820:
        font.removeGlyph(g)
font.generate("../..//app/src/main/res/font/hand.ttf")
font.close()

font = fontforge.open("TH-Feon.sfd")
for g in font.glyphs():
    u = g.unicode
    if u in notoext or u < 0x2B820 or u >= 0x2CEB0:
        font.removeGlyph(g)
font.generate("../..//app/src/main/res/font/hane.ttf")
font.close()

font = fontforge.open("TH-Feon.sfd")
for g in font.glyphs():
    u = g.unicode
    if u in notoext or u < 0x2CEB0 or u >= 0x30000:
        font.removeGlyph(g)
font.generate("../..//app/src/main/res/font/hanf.ttf")
font.close()

font = fontforge.open("TH-Feon.sfd")
for g in font.glyphs():
    u = g.unicode
    if u in notoext or u < 0x30000 or u >= 0x31350:
        font.removeGlyph(g)
font.generate("../..//app/src/main/res/font/hang.ttf")
font.close()

font = fontforge.open("ipa.sfd")
font.generate("../../app/src/main/res/font/ipa.ttf")
font.close()
