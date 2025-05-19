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
		字, 音, 註 = 列[自.列序[0]], 列[自.列序[1]], 列[自.列序[2]]
		if not 音: return
		音 = 音.lstrip("又").replace("gv", "gw")
		音, sd = 自.分音(音)
		if sd.isdigit():
			if 音[-1] in "ptk":
				if sd == "1": sd = "7"
				elif sd == "3": sd = "8"
				elif sd == "6": sd = "9"
		else:
			sd = ""
		for ym in sorted(自.yms.keys(), key=lambda x:-len(x)):
			if 音.endswith(ym):
				sm = 音[:-len(ym)]
				break
		音 = 自.sms[sm+"#"] + 自.yms[ym] + sd
		return 字, 音, 註
