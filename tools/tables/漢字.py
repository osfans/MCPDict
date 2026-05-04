#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables.__init__ import WORKSPACE
import re, os

def get_note():
	fname = os.path.join(WORKSPACE, "..", "README.md")
	if not os.path.exists(fname): return ""
	說明 = open(fname, encoding="U8").read()
	說明 = re.sub(r"^#### (.*)$", "<h4>\\1</h4>", 說明, flags=re.M).strip()
	說明 = re.sub(r"^### (.*)$", "<h3>\\1</h3>", 說明, flags=re.M).strip()
	說明 = re.sub(r"^## (.*)$", "<h2>\\1</h2>", 說明, flags=re.M).strip()
	說明 = re.sub(r"^#.*$", "", 說明, flags=re.M).strip()
	說明 = re.sub(r"\[(.*?)\]\((.*?)\)", "<a href=\\2>\\1</a>", 說明)
	說明 = re.sub(r"^- (.+)$", "<li>\\1</li>", 說明, flags=re.M)
	說明 = 說明.replace("</li>\n", "</li>")
	說明 = re.sub(r"(<li>.+</li>)", "<ul>\\1</ul>", 說明)
	說明 = re.sub(r"\*\*(.+?)\*\*", "<b>\\1</b>", 說明)
	# md table to html
	說明 = re.sub(r"^\|(.+?)\|(.+?)\|$", "<tr><td>\\1</td><td>\\2</td></tr>", 說明, flags=re.M)
	說明 = re.sub(r"<tr><td>(.*?)</td><td>(.*?)</td></tr>\n(<tr>.*?</tr>\n)(<tr>.+</tr>)", "<table style=\"border: 1px solid;\">\n<tr><th>\\1</th><th>\\2</th></tr>\n\\4</table>", 說明, flags=re.DOTALL)
	說明 = re.sub(r"\n{2,}", "<br>\n", 說明).strip()
	說明 = re.sub(r"(\d>)<br>", "\\1", 說明)
	#open(os.path.join(WORKSPACE, "說明.html"), "w", encoding="U8").write(說明)
	return 說明

class 表(_表):
	顏色 = "#9D261D"
	網站 = "漢字音典在線版"
	網址 = "https://mcpdict.sourceforge.io/cgi-bin/search.py?字=%s"
	說明 = get_note()
	簡稱 = "漢字"

	def 讀(自, 更新=False):
		自.音表.clear()
		自.音節典.clear()
		自.字音典.clear()
		自.聲韻典.clear()
		自.d.clear()
		return dict()
