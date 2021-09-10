#!/usr/bin/env python2
# -*- coding: utf-8 -*-
import sys
reload(sys)
sys.setdefaultencoding('utf-8')

import re, os
import xml.etree.ElementTree as ET
xmlname = "strings.xml"
if not os.path.exists(xmlname):
	xmlname = "../../app/src/main/res/values/strings.xml"
tree = ET.parse(xmlname)
root = tree.getroot()
def getStrings(name):
	l = root.findall("string-array[@name='%s']/*" % name)
	return [i.text for i in l]

def getString(name):
	l = root.findall("string[@name='%s']" % name)[0]
	return l.text

import sqlite3
dbname = 'mcpdict.db'
if not os.path.exists(dbname):
	dbname = '../../app/src/main/assets/databases/mcpdict.db'
conn = sqlite3.connect(dbname)
conn.row_factory = sqlite3.Row
c = conn.cursor()
c.execute("SELECT * FROM mcpdict where rowid<=7")
result = c.fetchall()
SEARCH_AS_NAMES,NAMES,COLORS,DICT_NAMES,DICT_LINKS,INTROS,TONE_NAMES = map(dict, result)
KEYS = [i[0] for i in c.description]

import cgitb
cgitb.enable()

import cgi
print("Content-type: text/html; charset=UTF-8\n")
form = cgi.FieldStorage()
key = form.getvalue("key", "hz")
charset = form.getvalue("charset", "hz")
variant = form.getvalue("variant", False)
language = form.getvalue("language", ".+")
tone = form.getvalue("tone", 0)
hzs = form.getvalue("hz", sys.argv[1] if len(sys.argv) == 2 else "")

print("""<html lang=ko>
<head>
	<title>%s</title>
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=1">
	<style>
		@font-face {
			font-family: ipa;
			src: url(/ipa.ttf);
		}
		div {
			display:inline-block;
			align: left;
		}
		.place {
			border: 1px black solid;
			padding: 0 3px;
			border-radius: 5px;
			text-align: center;
			transform-origin: right;
			font-size: 80%%;
		}
		body {
			font-family: ipa, sans;
		}
		.ipa {
			padding: 0 5px;
		}
		.desc {
			font-size: 70%%;
		}
		.hz {
			font-size: 300%%;
			color: #9D261D;
		}
		.variant {
			color: #808080;
		}
		.y {
			color: #1E90FF;
			margin: 0 5px;
		}
		p {
			margin: 0.2em 0;
		}
		td {
			vertical-align: top;
			align: left;
		}
	</style>
</head><body>
"""%getString("app_name"))

KEYS_READING = list(filter(lambda k: "_" in k, KEYS))
KEYS_Y = ("sw", "kx", "hd")

def rich(r, k):
	s = r[k]
	if k == "och_ba": return s
	s = re.sub(", ?", ", ", s)
	s = s.replace("\n", "<br>")
	s = re.sub("`(.*?)`", "<div class=desc>\\1</div>", s)
	s = re.sub("\|(.*?)\|", "<font color='#808080'>\\1</font>", s)
	s = re.sub("\*(.*?)\*", "<b>\\1</b>", s)
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
		 or (uni >= 0xF900 and uni <= 0xFAFF)\
		 or (uni >= 0x2F800 and uni <= 0x2FA1F)

def isUnicode(c):
	return re.match("^(U\\+)?[0-9A-Fa-f]{4,5}$", c) != None

def toUnicode(c):
	c = c.upper()
	if c.startswith("U+"): c = c[2:]
	return unichr(int(c, 16))

def getCharsetSQL():
	sql = ""
	if charset == "hz":
		pass
	elif charset in ("ltc_mc", "sw", "kx", "hd"):
		sql = "AND %s IS NOT NULL" % charset
	else:
		sql = "AND fl MATCH '%s'" % charset
	return sql

if hzs:
	hzs = hzs.decode("U8")[:10].strip()
else:
	print(INTROS.get(key, INTROS["hz"]))
	conn.close()
	exit()

if not language: language = key
word = "MATCH"
s = ""
if (key == "hz" or "_" in key) and isUnicode(hzs):
	hzs = toUnicode(hzs)
	key = "hz"
if "_" in key and isHZ(hzs):
	key = "hz"
if key == "hz" and re.match("[a-zA-Zü]+[0-5?]?", hzs):
	key = "cmn_"
if key in KEYS_Y:
	if len(hzs) == 1 and isHZ(hzs):
		key = "hz"
	else:
		word = "LIKE"
		hzs = "%%%s%%" % hzs
if key != "hz":
	if not isHZ(hzs):
		variant = False
	hzs = (hzs,)

def getKeys(key):
	keys = [key]
	if variant:
		keys.append("va")
	elif key == "wbh":
		keys = list(filter(lambda k: k.startswith("wb"), KEYS))
	elif key == "ja_any":
		keys = list(filter(lambda k: k.startswith("ja_"), KEYS))
	return keys

def getSelect(key, value):
	return 'SELECT *,offsets(mcpdict) AS vaIndex FROM mcpdict where (`%s` %s "%s") AND rowid > 7 %s' % (key, word, value, getCharsetSQL())

for value in hzs:
	sqls = list(map(lambda x: getSelect(x, value), getKeys(key)))
	sqls = (' UNION '.join(sqls)) + 'ORDER BY vaIndex LIMIT 10'
	for r in c.execute(sqls):
		hz = r["hz"]
		s += "<p><div class=hz>%s</div>"%(hz)
		if hz != value and variant:
			s += "<div class=variant>（%s）</div>"%(value)
		s += "<div class=y>U+%04X</div>" % (ord(hz))
		for k in KEYS_Y:
			if r[k]:
				s += "<div class=y>%s</div>" % (NAMES[k])
		s += "</div>\n"
		s += "<table>"
		for k in KEYS_READING:
			if not re.match(language, k): continue
			if r[k]:
				s += ("<tr><td align=right><div class=place style='border:1px %s solid;'><font color=%s>%s</font></div></td><td class=ipa>%s</td></tr>\n"%(COLORS[k],COLORS[k],NAMES[k],rich(r, k)))
		if s:
			s += "</table>\n"
if not s:
	s = getString("no_matches")
print(s)
conn.close()
