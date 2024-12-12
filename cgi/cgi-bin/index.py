#!/usr/bin/env python2
# -*- coding: utf-8 -*-
import sys, re, os
reload(sys)
sys.setdefaultencoding('utf-8')

import xml.etree.ElementTree as ET
cur = os.path.abspath(os.path.dirname(__file__))
xmlname = os.path.join(cur, "strings.xml")
tree = ET.parse(xmlname)
root = tree.getroot()
def getStrings(name):
	l = root.findall("string-array[@name='%s']/*" % name)
	return [i.text for i in l]

def getString(name):
	l = root.findall("string[@name='%s']" % name)[0]
	return l.text

import sqlite3
dbname = os.path.join(cur, 'mcpdict.db')
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
key = form.getvalue("key")
if key not in KEYS: key = "漢字"

APP = getString("app_name")
options_search = []
for i in KEYS:
	name = SEARCH_AS_NAMES[i]
	selected = "selected" if i == key else ""
	s = "<option value=%s %s>%s</option>"%(i, selected, name)
	options_search.append(s)
	if i.startswith("ja_kan"):
		s = "<option value=%s>%s</option>"%("ja_any", getString("search_as_ja_any"))
		options_search.append(s)
		break
options_search = "\n".join(options_search)

languages=getStrings("pref_entries_filters")
#language_values=getStrings("pref_values_show_languages")
options_language="\n".join(["<option value=%s>%s</option>"%(languages[i], j) for i,j in enumerate(languages)])

charsets = getStrings("pref_entries_charset")
charset_values = getStrings("pref_values_charset")
options_charset = "\n".join(["<option value=%s>%s</option>"%(charset_values[i], j) for i,j in enumerate(charsets)])

tones = getStrings("pref_entries_tone_display")
options_tone = "\n".join(["<option value=%s>%s</option>"%(i, j) for i,j in enumerate(tones)])

tvs = getStrings("pref_entries_tone_value_display")
options_tv = "\n".join(["<option value=%s>%s</option>"%(i, j) for i,j in enumerate(tvs)])

print("""<html lang=ko>
<head>
	<title>%s</title>
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=1">
	<style>
		@font-face {
			font-family: ipa;
			src: url(/ipa.ttf);
		}
		body, input[type="text"] {
			font-family: ipa, sans;
		}
	</style>
</head>
<body>
	<p><big>%s</big>&nbsp;&nbsp;<a href="/help" target="_blank">幫助</a>&nbsp;&nbsp;更快更強的安卓離線版<a href="https://github.com/osfans/MCPDict/releases">下載</a></p>
	<form id=mcp method=post target="receiver" action="/cgi-bin/search.py">
		<table>
			<tr><td><label>%s</label></td><td><input type="text" name="hz" placeholder="%s"></input>
			<input type="button" onclick="hz.value='';" value=%s />
			<button>查詢</button></td></tr>
			<tr><td><label>%s</label></td><td><select name="key">%s</select></tr>
			<tr><td><label>%s</label></td><td><select name="charset">%s</select><input type="checkbox" name="variant" checked="checked">%s</input></td></tr>
			<tr><td><label>%s</label></td><td><select name="language">%s</select></td></tr>
		</table>
	</form>
	<iframe name="receiver" id="receiver" width=100%% height=70%% frameBorder="0"></iframe>
	<script>
		document.getElementById('receiver').srcdoc="<html lang=ko>%s";
	</script>
</body>
</html>
""" % (APP,APP,
getString("search_for"),
getString("search_hint"),
getString("clear"),
getString("search_as"),options_search,
getString("search_options"),options_charset,getString("option_allow_variants"),
getString("show_languages"),options_language,INTROS[key]))

conn.close()
