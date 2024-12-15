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
options_filters="\n".join(["<option>%s</option>"%(i) for i in filters])

charsets = getStrings("pref_entries_charset")
charset_values = getStrings("pref_values_charset")
options_charset = "\n".join(["<option value=%s>%s</option>"%(charset_values[i], j) for i,j in enumerate(charsets)])

tones = getStrings("pref_entries_tone_display")
options_tone = "\n".join(["<option value=%s>%s</option>"%(i, j) for i,j in enumerate(tones)])

tvs = getStrings("pref_entries_tone_value_display")
options_tv = "\n".join(["<option value=%s>%s</option>"%(i, j) for i,j in enumerate(tvs)])

print(getStringFromFile("template.html",APP,APP,
options_types,
getString("search_hint"),getString("clear"),"查詢",
options_search,options_dict,options_filters,getString("hz_option"),
options_charset,getString("option_allow_variants"),
INTROS[orgLang].replace("\n", "")))
conn.close()
