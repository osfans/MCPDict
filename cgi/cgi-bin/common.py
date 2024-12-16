#!/usr/bin/env python2
# -*- coding: utf-8 -*-
import sys, os, re
import sqlite3
import xml.etree.ElementTree as ET

reload(sys)
sys.setdefaultencoding('utf-8')

def getString(name):
	l = root.findall("string[@name='%s']" % name)[0]
	return l.text

def getStrings(name):
	l = root.findall("string-array[@name='%s']/*" % name)
	return [getString(i.text.split("/")[1]) if "@string" in i.text else i.text for i in l]

def getStringFromFile(fname, *args):
	template = open(fname)
	s = template.read() % args
	template.close()
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
			if i[k]:
				s += "%s：%s<br/>" % (k, i[k])
		if i["說明"]:
			s += i["說明"]
	else:
		for k in ("地點","經緯度", "作者", "錄入人", "維護人","來源", "參考文獻","文件名","版本","字數","□數", "音節數","不帶調音節數"):
			if i[k]:
				s += "%s：%s<br/>" % (k, i[k])
		if s: s += "<br/>"
		if i["說明"]:
			s += i["說明"]
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
COMMENT = "注釋"
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
s = "<br><h2>已收錄語言</h2><table border=1 cellspacing=0>"
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