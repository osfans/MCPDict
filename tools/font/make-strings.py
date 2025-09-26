#!/usr/bin/env python3

import os
from fontTools.ttLib import TTFont

def get_font_display_name_simple(ttf_path):
	font = TTFont(ttf_path, fontNumber=0)
	name_table = font['name']
	ret = "Unknown"
	for name_id in [4, 1]:
		for record in name_table.names:
			if record.nameID == name_id:
				ret = record.toUnicode()
				print(ret, hex(record.langID))
				if record.langID in (0x0404,): return ret
	return ret

def is_serif_font(path):
	name = get_font_display_name_simple(path)
	return any(keyword in name.lower() or keyword in path.lower() for keyword in ["serif", "song", "sung", "ming", "kai", "fang", "楷", "宋", "明", "仿"])

def normalize(name):
	return name.lower().rsplit(".", 1)[0].replace(".", "_").replace("-", "_").split("_regular", 1)[0] + "." + name.rsplit(".", 1)[1]

l = list()
for i in os.listdir("../../font/src/main/res/font"):
	out = os.path.join("../../font/src/main/res/font", normalize(i))
	os.rename(os.path.join("../../font/src/main/res/font", i), out)
	name = get_font_display_name_simple(out)
	is_serif = is_serif_font(out)
	l.append((os.path.basename(out).rsplit(".", 1)[0], get_font_display_name_simple(out), is_serif))
l = sorted(l)
f = open("../../font/src/main/res/values/strings.xml", "w", encoding="utf-8")
print("<resources>", file=f)
print(f'\t<string name="font_app_name">漢字音典字體({l[0][1]})</string>', file=f)
print("\t<string-array name=\"names\">", file=f)
for filename, name, is_serif in l:
	print(f"\t\t<item>{name}</item>", file=f)
print("\t</string-array>", file=f)
print("\t<string-array name=\"fonts\">", file=f)
for filename, name, is_serif in l:
	print(f"\t\t<item>{filename},{'serif' if is_serif else 'sans'}</item>", file=f)
print("\t</string-array>", file=f)
print("</resources>", file=f)
f.close()