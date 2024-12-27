#!/usr/bin/env python2
# -*- coding: utf-8 -*-
from common import *

options_search = []
for i in KEYS:
	name = LANGUAGES[i]
	selected = "selected" if i == lang else ""
	s = "<option value=%s %s>%s</option>"%(i, selected, name)
	options_search.append(s)
options_search = "\n".join(options_search)

options_dict = ["<option>%s</option>" % (DICT_HEAD)]
for i in KEYS_DICT:
	name = LANGUAGES[i]
	selected = "selected" if i == dict else ""
	s = "<option value=%s %s>%s</option>"%(i, selected, name)
	options_dict.append(s)
options_dict = "\n".join(options_dict)

types=getStrings("pref_entries_type")
options_types="\n".join(["<option value=%s %s>%s</option>"%(i, "selected" if i == searchType else "", i) for i in types])

filters=getStrings("pref_entries_filters")
options_filter = []
for i in filters:
	selected = "selected" if i == filter else ""
	s = "<option value=%s %s>%s</option>"%(i, selected, i)
	options_filter.append(s)
options_filters="\n".join(options_filter)

charset_values = getStrings("pref_values_charset")
options_charset = []
for i,j in enumerate(getStrings("pref_entries_charset")):
	selected = "selected" if charset_values[i] == charset else ""
	s = "<option value=%s %s>%s</option>"%(charset_values[i], selected, j)
	options_charset.append(s)
options_charsets = "\n".join(options_charset)

tones = getStrings("pref_entries_tone_display")
options_tone = "\n".join(["<option value=%s>%s</option>"%(i, j) for i,j in enumerate(tones)])

tvs = getStrings("pref_entries_tone_value_display")
options_tv = "\n".join(["<option value=%s>%s</option>"%(i, j) for i,j in enumerate(tvs)])

print(getStringFromFile("template.html",APP,APP,
options_types,
getString("search_hint"),hzs,getString("clear"),"查詢",
options_search,options_dict,options_filters,getString("hz_option"),
options_charsets,"checked" if variant else "",getString("option_allow_variants")))

if hzs:
	hzs = hzs.decode("U8").strip()
if not hzs:
	print(INTROS[HZ if filter == "僅漢字" else orgLang])
	conn.close()
	exit()

word = "MATCH"
if searchType == HZ or searchType == YIN: 
	if isUnicode(hzs):
		hzs = toUnicode(hzs)
	if isHZ(hzs):
		lang = HZ
	if lang == HZ and re.match("[a-zA-Zü]+[0-5?]?", hzs):
		lang = "普通話"
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

if searchType == DICT or searchType == COMMENT:
	value = "'%s'" % (" ".join(hzs))
else:
	value = " OR ".join(hzs)

output = ""
for r in getSqls(value, word):
	hz = r[HZ]
	output += "<p><div class=hz>%s</div>"%(hz)
	if variant:
		va = getVariant(hzs, r[VA])
		if va: output += "<div class=variant>（%s）</div>"%(va)
	output += "<div class=y>U+%04X</div>" % (ord(hz))
	for k in KEYS_DICT:
		if not r[k]: continue
		output += "<div class=y>%s</div>" % (k)
	output += "</div>\n"
	last = ""
	for k in getVisibleColumns(filter):
		if not r[k]: continue
		color = COLORS[k].split(",")[0]
		output += ("<ul><div class=place style='border:1px %s solid;'>%s</div><div class=ipa>%s</div></ul>"%(color,getColorName(k),rich(r, k)))
	output+="</ul></details>\n"
if not output:
	output = getString("no_matches")
print(output)
conn.close()
