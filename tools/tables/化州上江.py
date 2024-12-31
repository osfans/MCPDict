#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	sms = {"b#":"p",
"p#":"pʰ",
"bb#":"ɓ",
"m#":"m",
"f#":"f",
"d#":"t",
"t#":"tʰ",
"dd#":"ɗ",
"n#":"n",
"l#":"l",
"z#":"ts",
"c#":"tsʰ",
"s#":"s",
"sl#":"ɬ",
"g#":"k",
"k#":"kʰ",
"ng#":"ŋ",
"gw#":"kʋ",
"kw#":"kʰʋ",
"ngw#":"ŋʋ",
"h#":"h",
"w#":"ʋ",
"j#":"j",
"nj#":"ȵ",
"#":"ʔ"}
	yms = {"aa":"aː",
"aai":"aːj",
"aau":"aːw",
"aan":"aːn",
"aang":"aːŋ",
"aam":"aːm",
"aat":"at̚",
"aak":"ak̚",
"aap":"ap̚",
"ai":"ɐj",
"au":"ɐw",
"an":"ɐn",
"ang":"ɐŋ",
"am":"ɐm",
"at":"ɐt̚",
"ak":"ɐk̚",
"ap":"ɐp̚",
"e":"ɛ",
"ei":"ɛj",
"eu":"e̯ɛw",
"en":"e̯ɛn",
"eng":"e̯ɛŋ",
"em":"e̯ɛm",
"et":"e̯ɛt̚",
"ek":"e̯ɛk̚",
"ep":"e̯ɛp̚",
"i":"i",
"iu":"iw",
"in":"in",
"ing":"iŋ",
"im":"im",
"it":"ɪt̚",
"ik":"ɪk̚",
"ip":"ɪp̚",
"o":"ɔ",
"oi":"u̯ɔj",
"eoi":"ɵj",
"ou":"u̯ɔw",
"ooau":"u̯ɔɒw",
"on":"u̯ɔn",
"ong":"u̯ɔŋʷ",
"ot":"u̯ɔt̚",
"ok":"u̯ɔk̚",
"u":"ʋ̩",
"ui":"ʋ̩j",
"un":"un",
"ung":"uŋʷ",
"ut":"ʊt̚",
"uk":"ʊk̚",
"ng":"ŋ̍",
"m":"m̩",
"n":"n̩",
"":""
}

	def 析(自, 列):
		字, yb, js = 列[0], 列[2], 列[3]
		if not yb: return
		yb = yb.lstrip("又").replace("gv", "gw")
		yb, sd = 自.splitSySd(yb)
		if sd.isdigit():
			if yb[-1] in "ptk":
				if sd == "1": sd = "7"
				elif sd == "3": sd = "8"
				elif sd == "6": sd = "9"
		else:
			sd = ""
		for ym in sorted(自.yms.keys(), key=lambda x:-len(x)):
			if yb.endswith(ym):
				sm = yb[:-len(ym)]
				break
		yb = 自.sms[sm+"#"] + 自.yms[ym] + sd
		return 字, yb, js
