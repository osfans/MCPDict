#!/usr/bin/env python2
# -*- coding: utf-8 -*-
import sys, os, re
import sqlite3
import xml.etree.ElementTree as ET

reload(sys)
sys.setdefaultencoding('utf-8')

def rich(r, k):
	s = r[k]
	if k == "白-沙上古": return s
	s = s.replace("  ", "　").replace(" ", "").replace("　", " ")
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

def getCharsetSQL(charset):
	sql = ""
	if charset == HZ:
		pass
	elif charset in KEYS_DICT or charset == GY:
		sql = "AND %s IS NOT NULL" % charset
	else:
		sql = "AND 分類 LIKE '%%%s%%'" % charset
	return sql

def getKeys(key, variant):
	keys = [key]
	if variant:
		keys.append(HZ)
		keys.append("異體字")
	elif key in KEYS_JA: keys = KEYS_JA
	return keys

def getSelect(key, value, word):
	return 'SELECT *,offsets(mcpdict) AS vaIndex FROM mcpdict where (`%s` %s "%s") %s' % (key, word, value, getCharsetSQL(charset))

def getSqls(value, word):
	sqls = list(map(lambda x: getSelect(x, value, word), getKeys(lang, variant)))
	sqls = (' UNION '.join(sqls)) + ' ORDER BY vaIndex LIMIT 10'
	return c.execute(sqls)

def getVisibleColumns(filter):
	if filter == "當前語言": return [orgLang]
	if filter == "僅方言島": return ISLANDS
	if filter == "僅漢字": return []
	return KEYS

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
	if not vars: return ""
	for i in hzs:
		if i in vars:
			return i
	return ""

def getString(name):
	l = root.findall("string[@name='%s']" % name)[0]
	return l.text

def getStrings(name):
	l = root.findall("string-array[@name='%s']/*" % name)
	return [getString(i.text.split("/")[1]) if "@string" in i.text else i.text for i in l]

def getStringFromFile(fname, *args):
	template = open(fname)
	s = template.read()
	template.close()
	s = re.sub(r"(\d+)%", "\\1%%", s)
	s = s % args
	return s

def isHZ(c):
	uni = ord(c[0])
	return (uni >= 0x4E00 and uni <= 0x9FFF)\
		 or uni == 0x25A1\
		 or uni == 0x3007\
		 or (uni >= 0x3400 and uni <= 0x4DBF)\
		 or (uni >= 0x20000 and uni <= 0x2A6DF)\
		 or (uni >= 0x2A700 and uni <= 0x2B73F)\
		 or (uni >= 0x2B740 and uni <= 0x2B81F)\
		 or (uni >= 0x2B820 and uni <= 0x2CEAF)\
		 or (uni >= 0x2CEB0 and uni <= 0x2EBEF)\
		 or (uni >= 0x30000 and uni <= 0x3134F)\
		 or (uni >= 0x31350 and uni <= 0x323AF)\
		 or (uni >= 0x2EBF0 and uni <= 0x2EE5F)\
		 or (uni >= 0xF900 and uni <= 0xFAFF)\
		 or (uni >= 0x2F800 and uni <= 0x2FA1F)

def formatIntro(i):
	s = ""
	if i["簡稱"].encode() == HZ:
		for k in ("版本","字數"):
			if i[k]: s += "%s：%s<br/>" % (k, i[k])
		if i["說明"]: s += i["說明"]
	else:
		for k in ("地點","經緯度", "作者", "錄入人", "維護人","字表來源", "參考文獻","補充閲讀","文件名","版本","字數","□數", "音節數","不帶調音節數"):
			if i[k]: s += "%s：%s<br/>" % (k, i[k])
		if s: s += "<br/>"
		if i["說明"]: s += i["說明"]
		for k in ("音系說明", "解析日志", "同音字表"):
			if i[k]: s += "<h2>%s</h2>%s" % (k, i[k])
	s = s.replace("\n", "<br/>").replace("href=", "target='_blank' href=")
	return s

cur = os.path.abspath(os.path.dirname(__file__))
os.chdir(cur)
xmlname = "strings.xml"
root = ET.parse(xmlname).getroot()

APP = getString("app_name")
HZ = "漢字"
GY = "廣韻"
VA = "異體字"
YIN = "讀音"
DICT = "辭典"
COMMENT = "註釋"
DICT_HEAD = getString("dict")
TABLE_NAME = "mcpdict"

import cgitb
cgitb.enable()

import cgi
print("Content-type: text/html; charset=UTF-8\n")
form = cgi.FieldStorage()
searchType = form.getvalue("type", HZ)
lang = form.getvalue("lang", "普通話")
dict = form.getvalue("dict", "")
orgLang = form.getvalue("lang", HZ)
charset = form.getvalue("charset", HZ)
hzOptionChecked = form.getvalue("漢字選項", False)
variant = form.getvalue("variant", True) if searchType == HZ or searchType == YIN else False
filter = form.getvalue("filter", "顯示全部")
tone = form.getvalue("tone", 0)
hzs = form.getvalue(HZ, sys.argv[1] if len(sys.argv) == 2 else "")

dbname = "mcpdict.db"
conn = sqlite3.connect(dbname)
conn.row_factory = sqlite3.Row
c = conn.cursor()
c.execute("SELECT * FROM info order by 地圖集二排序")
info = c.fetchall()
KEYS_ALL = [i["簡稱"].encode() for i in info]
KEYS = [i["簡稱"].encode() for i in info if i["音節數"]]
KEYS_DICT = ("說文", "康熙", "漢大", "匯纂")
KEYS_JA = [i for i in KEYS if i.startswith("日語")]
LANGUAGES = {i["簡稱"].encode():i["語言"] for i in info}
ISLANDS = [i["簡稱"].encode() for i in info if i["方言島"]]

INTROS = {i["簡稱"].encode():formatIntro(i) for i in info}
s = "<br><h2>已收錄語言</h2><table border=1 cellSpacing=0>"
fields = ("語言", "字數", "□數", "音節數", "不帶調音節數")
s += "<tr><th>%s</th></tr>" % "</th><th>".join(fields)
for i in info:
	if not i["音節數"]: continue
	s += "<tr><td>%s</td></tr>" % "</td><td>".join([str(i[k]) if i[k] else "" for k in fields])
s += "</table>"
INTROS[HZ] += s

COLORS = {i["簡稱"].encode():i["地圖集二顏色"] for i in info}
TYPES = {i["簡稱"].encode():i["地圖集二分區"] for i in info}
#KEYS.sort(key=lambda x: TYPES.get(x, None))