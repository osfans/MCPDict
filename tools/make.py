#!/usr/bin/env python3

import sqlite3, os, sys, json
from collections import defaultdict
from time import time
from tables import *
import argparse

parser = argparse.ArgumentParser(description='Create mcpdict database')
parser.add_argument('-c', action='store_true', help='檢查同音字', required=False)
parser.add_argument('-s', action='store_true', help='計算相似度', required=False)
parser.add_argument('-省', help='province to include', required=False)
parser.add_argument('-o', '--output', help='output tsv', required=False)
parser.add_argument('-j', '--json', action='store_true', help='output json', required=False, default=False)
parser.add_argument('-l', '--html', action='store_true', help='output html', required=False, default=False)
args, argv = parser.parse_known_args()
start = time()

字數 = 0

def getMarkerSize(size):
	if size >= 4: return "large"
	if size == 3: return "medium"
	return "small"

def dumpJson(langs):
	FeatureCollection = {
		"type": "FeatureCollection",
		"features": []
	}
	for lang in langs:
		if not lang.info.get("經緯度", ""): continue
		Feature = {
			"type": "Feature",
			"properties": {
				"marker-color": lang.info["音典顏色"].split(",")[0],
				"marker-size": getMarkerSize(int(lang.info["地圖級別"])),
				"marker-symbol": lang.info["音典排序"][0].lower() if lang.info["音典排序"] else "",
				"title": lang.info["簡稱"],
			},
			"geometry": {
				"type": "Point",
				"coordinates": eval(lang.info["經緯度"])
			}
		}
		for i in ["語言", "地點", "地圖集二分區", "音典分區", "陳邡分區", '方言島', '版本', '作者', '錄入人', '維護人', '字表來源', '參考文獻', '補充閲讀', '字數', '□數', '音節數', '不帶調音節數']:
			if lang.info[i]:
				Feature["properties"][i] = lang.info[i]
		FeatureCollection["features"].append(Feature)
	curdir = os.path.dirname(__file__)
	geojsonpath = os.path.join(curdir, "info.geojson")
	if os.path.exists(geojsonpath):
		json.dump(FeatureCollection, fp=open(geojsonpath, "w",encoding="U8",newline="\n"),ensure_ascii=False,indent=2)

def dumpHtml(langs):
	lines = list()
	lines.append("""<html lang="ko">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<title>漢字音典已收錄語言</title>
		<script>
			let currentSort = { column: null, asc: true };

			function sortTable(table, column, asc = true) {
				const tbody = table.querySelector('tbody');
				const rows = Array.from(tbody.querySelectorAll('tr'));
				
				// 排序规则
				const sortedRows = rows.sort((a, b) => {
					const aText = a.querySelector(`td:nth-child(${column + 1})`).textContent.trim();
					const bText = b.querySelector(`td:nth-child(${column + 1})`).textContent.trim();
					
					// 数字排序
					if (!isNaN(aText) && !isNaN(bText)) {
						return asc ? aText - bText : bText - aText;
					}
					
					// 文本排序
					return asc ? aText.localeCompare(bText) : bText.localeCompare(aText);
				});
				
				// 重新插入排序后的行
				sortedRows.forEach(row => tbody.appendChild(row));
			}

			function sortTableByColumn(column) {
				const table = document.getElementById('sortable-table');
				const isAsc = currentSort.column === column ? !currentSort.asc : false;
				
				sortTable(table, column, isAsc);
				
				// 更新排序状态
				currentSort = { column, asc: isAsc };
				
				// 更新表头样式
				updateHeaderStyles(column, isAsc);
			}

			function updateHeaderStyles(column, asc) {
				const headers = document.querySelectorAll('th');
				headers.forEach((header, index) => {
					header.classList.remove('sorted-asc', 'sorted-desc');
					if (index === column) {
						header.classList.add(asc ? 'sorted-asc' : 'sorted-desc');
					}
				});
			}
		</script>
		<style>
			th { cursor: pointer; }
			th.sorted-asc::after { content: " ↑"; }
			th.sorted-desc::after { content: " ↓"; }
		</style>
	</head>
	<body onload="sortTableByColumn(8);">
		<table id="sortable-table" border="1" cellspacing="0" cellpadding="5">
			<thead>
				<tr>
""")
	headers = ["序號","語言", "簡稱", "地點", "經緯度", "地圖集二分區", "音典分區", "陳邡分區", "版本","字表來源", "參考文獻", "補充閲讀", "字數", "□數", "音節數", "不帶調音節數"]
	for header in headers:
		lines.append(f"\t\t\t\t\t<th onclick='sortTableByColumn({headers.index(header)})'>{header}</th>\n")
	lines.append("\t\t\t\t</tr>\n\t\t\t</thead>\n\t\t\t<tbody>\n")
	count = 0
	for lang in langs[1:]:
		if not lang.info.get("地圖集二分區", ""): continue
		lines.append("\t\t\t\t<tr>\n")
		for i in headers:
			v = lang.info.get(i, "")
			if i == "序號":
				count += 1
				v = str(count)
			if v is None: v = ""
			lines.append(f"\t\t\t\t\t<td>{v}</td>\n")
		lines.append("\t\t\t\t</tr>\n")
	lines.append("\t\t\t</tbody>\n\t\t</table>\n\t</body>\n</html>")
	curdir = os.path.dirname(__file__)
	curpath = os.path.join(curdir, "info.html")
	open(curpath, "w",encoding="U8",newline="\n").writelines(lines)

#db
if not args.output:
	NAME = os.path.join(WORKSPACE, '..', 'app/src/main/assets/databases/mcpdict.db')
	DIR = os.path.dirname(NAME)
	if os.path.exists(NAME): os.remove(NAME)
	if not os.path.exists(DIR): os.mkdir(DIR)
	conn = sqlite3.connect(NAME)
	c = conn.cursor()
	items = list()
	langs, 高頻字 = getLangs(items, argv, args)
	keys = [f"{lang.簡稱}" for lang in langs]
	for i in keys:
		if keys.count(i) > 1:
			print(f"{i}重名")
			sys.exit(1)
	fields = ["字組", "語言", "讀音", "註釋"]
	tokens = "□－〈〉［］（）"
	CREATE = 'CREATE VIRTUAL TABLE langs USING fts5 (%s, columnsize=0, tokenize="unicode61 remove_diacritics 0 tokenchars \'%s\' ")' % (",".join(fields), tokens)
	INSERT = 'INSERT INTO langs VALUES (%s)'% (','.join('?' * len(fields)))
	c.execute(CREATE)
	c.executemany(INSERT, items)
	del items
	字書 = None
	if len(argv) != 1:
		dicts = defaultdict(dict)
		fields, 字書 = getDicts(dicts)
		CREATE = 'CREATE VIRTUAL TABLE mcpdict USING fts5 (%s, columnsize=0, tokenize="unicode61 tokenchars \'%s\'")' % (",".join(fields), tokens)
		INSERT = 'INSERT INTO mcpdict VALUES (%s)'% (','.join('?' * len(fields)))
		c.execute(CREATE)
		c.executemany(INSERT, (list(map(dicts[i].get, fields)) for i in sorted(dicts.keys(), key=lambda x:((高頻字.index(x) if x in 高頻字 else 0xffff),-len(dicts[x]),cjkorder(x)))))
		字數 = len(dicts)
		del dicts
	langs[0].info["字數"] = 字數
	if 字書:
		langs.extend(字書)
	keys = list(langs[1].info.keys())
	keys.remove("字表格式")
	keys.remove("跳過行數")
	keys.remove("字表使用調值")
	keys.remove("字聲韻調註列名")
	CREATE = 'CREATE VIRTUAL TABLE info USING fts5 (%s, columnsize=0)' % (",".join(keys))
	INSERT = 'INSERT INTO info VALUES (%s)'% (','.join('?' * len(keys)))
	c.execute(CREATE)
	c.executemany(INSERT, (list(map(lang.info.get, keys)) for lang in langs))

	conn.commit()
	conn.close()

if args.json:
	dumpJson(langs)

if args.html:
	dumpHtml(langs)

passed = time() - start
print(f"({字數:5d}) {passed:6.3f} 保存")
