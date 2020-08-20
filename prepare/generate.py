#!/usr/bin/env python3

import sqlite3
from collections import defaultdict

HEADS = [
  ('hz', '漢字', '漢字', '#9D261D', '字海', 'http://yedict.com/zscontent.asp?uni=%2$s'),
  ('unicode', '編碼', '編碼', '#808080', 'Unihan', 'https://www.unicode.org/cgi-bin/GetUnihanData.pl?codepoint=%s'),
  ('mc', '中古拼音', '中古', '#9A339F', '韻典網', "http://ytenx.org/zim?kyonh=1&dzih=%s"),
  ('tr', '泰如拼音', '泰如', '#0000FF', '泰如小字典', "http://taerv.nguyoeh.com/"),
  ('nt', '南通話', '南通', '#0000FF', '南通方言網', "http://nantonghua.net/search/index.php?hanzi=%s"),
  ('ic', '鹽城話', '鹽城', '#0000FF', '淮語字典', "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s"),
  ('lj', '南京話', '南京', '#0000FF', None, None),
  ('sh', '上海話', '上海', '#00ADAD', '吳音小字典（上海）', "http://www.wu-chinese.com/minidict/search.php?searchlang=zaonhe&searchkey=%s"),
  ('sz', '蘇州話', '蘇州', '#00ADAD', '吳音小字典（蘇州）', "http://www.wu-chinese.com/minidict/search.php?searchlang=suceu&searchkey=%s"),
  ('pu', '普通話', '普', '#FF00FF', '漢典網', "http://www.zdic.net/hans/%s"),
  ('ct', '粵語', '粵', '#269A26', '粵語審音配詞字庫', "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s"),
  ('mn', '閩南語', '閩南', '#FFAD00', '臺灣閩南語常用詞辭典', "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s"),
  ('kr', '朝鮮語', '朝鮮', '#3366FF', 'Naver漢字辭典', "http://hanja.naver.com/hanja?q=%s"),
  ('vn', '越南語', '越南', '#FF6600', '漢越辭典摘引', "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s"),
  ('jp_go', '日語吳音', '日·吳', '#FF0000', None, None),
  ('jp_kan', '日語漢音', '日·漢', '#FF0000', None, None),
  ('jp_tou', '日語唐音', '日·唐', '#FF0000', None, None),
  ('jp_kwan', '日語慣用音', '日·慣', '#FF0000', None, None),
  ('jp_other', '日語其他讀音', '日·他', '#FF0000', None, None)]
ZHEADS = list(zip(*HEADS))
KEYS = ZHEADS[0]
FIELDS = ", ".join(["%s TEXT"%i for i in KEYS])
COUNT = len(KEYS)
INSERT = 'INSERT INTO mcpdict VALUES (%s)'%(','.join('?'*COUNT))

unicodes=defaultdict(dict)
conn = sqlite3.connect('mcpdict.db')
conn.row_factory = sqlite3.Row
c = conn.cursor()
for r in c.execute('SELECT * FROM mcpdict'):
  i = chr(int(r["unicode"],16))
  row = dict(r)
  row["hz"] = i
  unicodes[i] = row
conn.close()

def update(k, d):
  for i,v in d.items():
    if i not in unicodes:
      unicodes[i] = {"hz": i, "unicode": "%04X"%ord(i)}
    unicodes[i][k] = ",".join(v)

d=defaultdict(list)
#tr
d.clear()
for line in open("cz6din3.csv"):
  fs = line.strip().split(',')
  if fs[0]=='"id"': continue
  hz = fs[1].replace('"','')
  py = (fs[3]+fs[4]+fs[5]).replace('"','').replace('vv','v')
  if py not in d[hz]:
    d[hz].append(py)
  jt = fs[2].replace('"','')
  if jt!=hz and py not in d[jt]:
    d[jt].append(py)
update("tr", d)

#nt
d.clear()
for line in open("nt.txt"):
  fs = line.strip().split(',')
  if fs[1]=='"hanzi"': continue
  hz = fs[1].strip('"')[0]
  py = fs[-1].strip('"')
  if py not in d[hz]:
    d[hz].append(py)
update("nt", d)

#ic
d.clear()
for line in open("xu.csv"):
  line = line.strip()
  fs = line.split(',')
  hzs = fs[1].replace('"','')
  py = fs[2].replace('"','')
  for hz in hzs.split(" "):
    if len(hz) == 1:
      if py not in d[hz]:
        d[hz].append(py)
update("ic", d)

#lj
d.clear()
for line in open("langjin.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) != 2: continue
  hz, py = fs
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("lj", d)

#sz
d.clear()
for line in open("wugniu_soutseu.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) != 2: continue
  hz, py = fs
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("sz", d)

conn = sqlite3.connect('../app/src/main/assets/databases/mcpdict.db')
c = conn.cursor()
c.execute("DROP TABLE IF EXISTS mcpdict")
c.execute("CREATE VIRTUAL TABLE mcpdict USING fts3 (%s)"%FIELDS)
c.executemany(INSERT, ZHEADS[1:])
for i in sorted(unicodes.keys()):
  d = unicodes[i]
  v = list(map(d.get, KEYS))
  c.execute(INSERT, v)
conn.commit()
conn.close()
