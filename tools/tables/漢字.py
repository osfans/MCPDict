#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

def get_note():
	note = open("../README.md", encoding="U8").read()
	note = re.sub(r"^#.*$", "", note, flags=re.M).strip()
	note = re.sub(r"\[(.*?)\]\((.*?)\)", "<a href=\\2>\\1</a>", note)
	note = re.sub(r"^- (.+)$", "<li>\\1</li>", note, flags=re.M)
	note = note.replace("</li>\n", "</li>")
	note = re.sub(r"(<li>.+</li>)", "<ul>\\1</ul>", note)
	return note

class 表(_表):
	color = "#9D261D"
	site = "漢字音典在線版"
	url = "https://mcpdict.sourceforge.io/cgi-bin/search.py?hz=%s"
	note = get_note()


	def read(self):
		return dict()
