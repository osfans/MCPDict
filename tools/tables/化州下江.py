#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_wh_hzxj"
	tones = "55 1 1a 陰平 ꜀,35 3 2a 陰上 ꜂,33 5 3a 陰去 ꜄,214 2 1b 陽平 ꜁,214 4 2b 陽上 ꜃,21 6 3b 陽去 ꜅,5 7a 4a 上陰入 ꜆,33 7b 4b 下陰入 ꜀,22 8 4c 陽入 ꜇"
	_file = "化州下江字表*.tsv"
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
"n":"n̩"
}

	def parse(self, fs):
		hz, sm, ym, sd, js = fs[0], fs[8], fs[9], fs[10], fs[11]
		if not sm and not sd: return
		sm = self.sms.get(sm+"#", sm)
		ym = self.yms.get(ym, ym)
		if ym and ym.endswith("̚"):
			if sd == "1": sd = "7"
			elif sd == "3": sd = "8"
			elif sd == "6": sd = "9"
		yb = sm + ym + sd
		return hz, yb, js
