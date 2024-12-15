#!/usr/bin/env python2
# -*- coding: utf-8 -*-
from common import *

charset = form.getvalue("charset", HZ)
variant = form.getvalue("variant", False) if searchType == HZ else False
filter = form.getvalue("filter", "顯示全部")
tone = form.getvalue("tone", 0)
hzs = form.getvalue(HZ, sys.argv[1] if len(sys.argv) == 2 else "")

print(getStringFromFile("search.html", APP))

def rich(r, k):
	s = r[k]
	if k == "白-沙": return s
	s = s.replace(" ", "")
	s = re.sub(", ?", ", ", s)
	s = s.replace("\n", "<br>")
	s = re.sub("\{(.*?)\}", "<div class=desc>\\1</div>", s)
	s = re.sub("\|(.*?)\|", "<font color='#808080'>\\1</font>", s)
	s = re.sub("\*(.*?)\*", "<b>\\1</b>", s)
	return s

def isUnicode(c):
	return re.match("^(U\\+)?[0-9A-Fa-f]{4,5}$", c) != None

def toUnicode(c):
	c = c.upper()
	if c.startswith("U+"): c = c[2:]
	return unichr(int(c, 16))

def getCharsetSQL():
	sql = ""
	if charset == HZ:
		pass
	elif charset in KEYS_DICT or charset == GY:
		sql = "AND %s IS NOT NULL" % charset
	else:
		sql = "AND 分類 LIKE '%%%s%%'" % charset
	return sql

if hzs:
	hzs = hzs.decode("U8").strip()
else:
	print(INTROS.get(lang, INTROS[HZ]))
	conn.close()
	exit()

if not filter: filter = lang
word = "MATCH"
s = ""
if searchType == HZ or searchType == YIN: 
	if isUnicode(hzs):
		hzs = toUnicode(hzs)
	if isHZ(hzs):
		lang = HZ
	if lang == HZ and re.match("[a-zA-Zü]+[0-5?]?", hzs):
		lang = "cmn_"
	if lang in KEYS_DICT:
		if len(hzs) == 1 and isHZ(hzs):
			lang = HZ
		else:
			word = "LIKE"
			hzs = "%%%s%%" % hzs
	if lang != HZ:
		if not isHZ(hzs):
			variant = False
		hzs = (hzs,)
elif searchType == DICT:
	lang = dict
	if lang == DICT_HEAD: lang = TABLE_NAME

def getKeys(key):
	keys = [key]
	if variant:
		keys.append(HZ)
		keys.append("異體字")
	elif key in KEYS_JA: keys = KEYS_JA
	return keys

def getSelect(key, value):
	return 'SELECT *,offsets(mcpdict) AS vaIndex FROM mcpdict where (`%s` %s "%s") %s' % (key, word, value, getCharsetSQL())

def getVisibleColumns(filter):
	if filter == "當前語言": return [orgLang]
	if filter == "僅方言島": return ISLANDS
	if filter == "僅漢字": return []
	return KEYS

regions={
	'och_':'歷史音',
	'ltc_':'歷史音',
	'cmn_':'官話',
	'cmn_xn_':'西南官話',
	'cmn_jh_':'江淮官話',
	'cmn_jil_':'冀魯官話',
	'cmn_zho_':'中原官話',
	'cmn_ly_':'蘭銀官話',
	'cmn_fyd_':'官話方言島',
	'cjy_':'晉語',
	'wuu_':'吳語',
	'czh_':'徽語',
	'gan_':'贛語',
	'hak_':'客語',
	'hsn_':'湘語',
	'yue_':'粵語',
	'csp_':'南部平話',
	'nan_':'閩南語',
	'cdo_':'閩東語',
	'mnp_':'閩北語',
	'xxx_':'土話',
	'wxa_':'鄉話',
	'vi_':'域外方音',
	'ko_':'域外方音',
	'ja_':'域外方音',
}

rks = sorted(regions.keys(),key=lambda k:-k.count('_'))

def getRegion(k):
	for i in rks:
		if k.startswith(i):
			return regions[i]
	return ""

def getRegionDiff(k, last):
	return k.count("-") - last.count("-")

def getColorName(k):
	name = k
	color = COLORS[k]
	fmt = "<font color=%s>%s</font>"
	if "," in color:
		colors = color.split(",")
		m = len(name)//2
		names = name[:m],name[m:]
		s = ""
		for i in range(2):
			s += fmt % (colors[1 - i], names[i])
		return s
	return fmt % (color, name)
def getVariant(hzs, vars):
	for i in hzs:
		if i in vars:
			return i
	return ""
if searchType == DICT or searchType == COMMENT:
	value = "'%s'" % (" ".join(hzs))
else:
	value = " OR ".join(hzs)
sqls = list(map(lambda x: getSelect(x, value), getKeys(lang)))
sqls = (' UNION '.join(sqls)) + ' ORDER BY vaIndex LIMIT 10'
#print(sqls)
for r in c.execute(sqls):
	hz = r[HZ]
	s += "<p><div class=hz>%s</div>"%(hz)
	if variant:
		va = getVariant(hzs, r[VA])
		if va: s += "<div class=variant>（%s）</div>"%(va)
	s += "<div class=y>U+%04X</div>" % (ord(hz))
	for k in KEYS_DICT:
		if r[k]:
			s += "<div class=y>%s</div>" % (k)
	s += "</div>\n"
	last = ""
	for k in getVisibleColumns(filter):
		if r[k]:
			region = getRegion(k)
			if region != last:
				if last:
					diff = getRegionDiff(region, last)
					if diff <= 0:
						s += "</ul></details>\n" * (1 - diff)
					else:
						n = region.count("-")
						start = 1 if region.startswith(last) else 0
						for i in range(start, n):
							if i == 0:
								s += "</ul></details>\n"
							s += "<details open><summary>%s</summary><ul>"%region.split("-")[i]
				s +="<details open><summary>%s</summary><ul>"%region.rsplit("-", 1)[-1]
				last = region
			color = COLORS[k].split(",")[0]
			s += ("<ul><div class=place style='border:1px %s solid;'>%s</div><div class=ipa>%s</div></ul>"%(color,getColorName(k),rich(r, k)))
	s+="</ul></details>\n"
if not s:
	s = getString("no_matches")
print(s)
conn.close()
