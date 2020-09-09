#!/usr/bin/env python3

import sqlite3, re
from collections import defaultdict

HEADS = [
  ('hz', '漢字', '漢字', '#9D261D', '字海', 'http://yedict.com/zscontent.asp?uni=%2$s'),
  ('unicode', '統一碼', '統一碼', '#808080', 'Unihan', 'https://www.unicode.org/cgi-bin/GetUnihanData.pl?codepoint=%s'),
  ('sg', '上古', '上古', '#9A339F', '韻典網（上古音系）', 'https://ytenx.org/dciangx/dzih/%s'),
  ('mc', '中古', '中古', '#9A339F', '韻典網', "http://ytenx.org/zim?kyonh=1&dzih=%s"),
  ('zy', '中原音韻', '近古', '#9A339F', '韻典網（中原音韻）', 'https://ytenx.org/trngyan/dzih/%s'),
  ('pu', '普通話', '普語', '#FF00FF', '漢典網', "http://www.zdic.net/hans/%s"),
  ('tr', '泰如方言', '泰如', '#6161FF', '泰如小字典', "http://taerv.nguyoeh.com/"),
  ('nt', '南通話', '南通', '#6161FF', '南通方言網', "http://nantonghua.net/search/index.php?hanzi=%s"),
  ('ic', '鹽城話', '鹽城', '#6161FF', '淮語字典', "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s"),
  #('lj', '南京話', '南京', '#6161FF', '南京官話拼音方案', "https://uliloewi.github.io/LangJinPinIn/PinInFangAng"),
  ('sh', '上海話', '上海', '#00ADAD', '吳音小字典（上海）', "http://www.wu-chinese.com/minidict/search.php?searchlang=zaonhe&searchkey=%s"),
  ('sz', '蘇州話', '蘇州', '#00ADAD', '吳音小字典（蘇州）', "http://www.wu-chinese.com/minidict/search.php?searchlang=suceu&searchkey=%s"),
  ('ct', '粵語', '粵語', '#269A26', '粵語審音配詞字庫', "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s"),
  ('mn', '閩南語', '閩南', '#FFAD00', '臺灣閩南語常用詞辭典', "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s"),
  ('vn', '越南語', '越南', '#FF6600', '漢越辭典摘引', "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s"),
  ('kr', '朝鮮語', '朝鮮', '#3366FF', 'Naver漢字辭典', "http://hanja.naver.com/hanja?q=%s"),
  ('jp_go', '日語吳音', '日吳', '#FF0000', None, None),
  ('jp_kan', '日語漢音', '日漢', '#FF0000', None, None),
  ('jp_tou', '日語唐音', '日唐', '#FF0000', None, None),
  ('jp_kwan', '日語慣用音', '日慣', '#FF0000', None, None),
  ('jp_other', '日語其他讀音', '日他', '#FF0000', None, None)]
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
    if unicodes[i].get(k, None): continue
    unicodes[i][k] = ",".join(v)

d=defaultdict(list)

#mc
#https://github.com/biopolyhedron/rime-middle-chinese/blob/master/zyenpheng.dict.yaml
d.clear()
for line in open("zyenpheng.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) < 2: continue
  hz, py = fs[:2]
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("mc", d)

#sg
#https://github.com/BYVoid/ytenx/blob/master/ytenx/sync/dciangx/DrienghTriang.txt
d.clear()
for line in open("../../ytenx/ytenx/sync/dciangx/DrienghTriang.txt"):
  line = line.strip()
  if line.startswith('#'): continue
  fs = line.split(' ')
  hz = fs[0]
  py = fs[12]
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("sg", d)

#zy
#https://github.com/BYVoid/ytenx/blob/master/ytenx/sync/trngyan
d.clear()

def getIPA(name):
  yms = dict()
  for line in open(name):
    line = line.strip('\n')
    if line.startswith('#'): continue
    fs = line.split(' ')
    ym, ipa = fs
    yms[ym] = ipa
  return yms
sms = getIPA("../../ytenx/ytenx/sync/trngyan/CjengMuxNgixQim.txt")
yms = getIPA("../../ytenx/ytenx/sync/trngyan/YonhMuxNgixQim.txt")
sds = {'去': '5', '入平': '<u>2</u>', '入去': '<u>5</u>', '入上': '<u>3</u>', '上': '3','陽平': '2', '陰平': '1'}

for line in open("../../ytenx/ytenx/sync/trngyan/TriungNgyanQimYonh.txt"):
  line = line.strip()
  if line.startswith('#'): continue
  fs = line.split(' ')
  hzs = fs[1]
  py = sms[fs[4]]+yms[fs[5]]+sds[fs[2]]
  if "ɿ" in py:
    py = re.sub("([ʂɽ].*?)ɿ", "\\1ʅ", py)
  for hz in hzs:
    if py not in d[hz]:
      d[hz].append(py)
update("zy", d)

#tr
#http://taerv.nguyoeh.com/
d.clear()
trsm = {'g': 'k', 'd': 't', '': '', 'sh': 'ʂ', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 'r': 'ʐ', 'zh': 'tʂ', 't': 'tʰ', 'v': 'v', 'ng': 'ŋ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'ch': 'tʂʰ', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ'}
trym = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'i', 'r': 'ʅ', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iəu', 'u': 'u', 'v': 'v', 'en': 'əŋ', 'a': 'a', 'on': 'ɔŋ', 'ei': 'əi', 'an': 'aŋ', 'oh': 'ɔʔ', 'i': 'j', 'ien': 'iəŋ', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'uei': 'uəi', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'uʔ', 'aen': 'ɛ̃', 'er': 'ɚ', 'eu': 'əu', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iuʔ', 'yen': 'yəŋ', 'ian': 'iaŋ', 'iun': 'iũ', 'un': 'ũ', 'o': 'ɔ', 'uan': 'uaŋ', 'ua': 'ua', 'uen': 'uəŋ', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya'}
for line in open("cz6din3.csv"):
  fs = line.strip().split(',')
  if fs[0]=='"id"': continue
  hz = fs[1].replace('"','')
  fs[3] = fs[3].strip('"')
  fs[4] = fs[4].strip('"')
  fs[5] = fs[5].strip('"')
  if fs[3] == fs[4] == 'v': fs[3] = ''
  py = trsm[fs[3]]+trym[fs[4]]+fs[5]
  if py not in d[hz]:
    d[hz].append(py)
  jt = fs[2].replace('"','')
  if jt!=hz and py not in d[jt]:
    d[jt].append(py)
update("tr", d)

#nt
#http://nantonghua.net
d.clear()
for line in open("nt.txt"):
  fs = line.strip().split(',')
  if fs[1]=='"hanzi"': continue
  hz = fs[1].strip('"')[0]
  py = fs[-6].strip('"') + fs[-4]
  if py not in d[hz]:
    d[hz].append(py)
update("nt", d)

#ic
#https://github.com/osfans/xu/blob/master/docs/xu.csv
d.clear()
icsm = {'g': 'k', 'd': 't', '': '', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 't': 'tʰ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ', 'ng': 'ŋ'}
icym = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'i', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iəu', 'u': 'u', 'v': 'w', 'en': 'ən', 'a': 'a', 'on': 'ɔŋ', 'an': 'ã', 'oh': 'ɔʔ', 'i': 'j', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'ui': 'ui', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'uʔ', 'aen': 'ɛ̃', 'er': 'ɚ', 'eu': 'əu', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iuʔ', 'yen': 'yn', 'ian': 'iã', 'iun': 'iũ', 'un': 'ũ', 'o': 'ɔ', 'uan': 'uã', 'ua': 'ua', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya'}
for line in open("xu.csv"):
  line = line.strip()
  fs = line.split(',')
  hzs = fs[1].replace('"','')
  py = fs[2].replace('"','')
  sm = re.findall("^[^aeiouvy]?", py)[0]
  sd = py[-1]
  ym = py[len(sm):-1]
  py = icsm[sm]+icym[ym]+sd
  for hz in hzs.split(" "):
    if len(hz) == 1:
      if py not in d[hz]:
        d[hz].append(py)
update("ic", d)

#lj
#https://github.com/uliloewi/lang2jin1/blob/master/langjin.dict.yaml
# ~ d.clear()
# ~ for line in open("langjin.dict.yaml"):
  # ~ line = line.strip()
  # ~ fs = line.split('\t')
  # ~ if len(fs) != 2: continue
  # ~ hz, py = fs
  # ~ if len(hz) == 1:
    # ~ if py not in d[hz]:
      # ~ d[hz].append(py)
# ~ update("lj", d)

#sz
#https://github.com/NGLI/rime-wugniu_soutseu/blob/master/wugniu_soutseu.dict.yaml
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

#pu
def hex2chr(uni):
    "把unicode轉換成漢字"
    return chr(int(uni[2:], 16))
def norm(py):
    if py == "wòng": py= "weng4"
    tones=['ā', 'á', 'ǎ', 'à', 'ē', 'é', 'ě', 'è', 'ī', 'í', 'ǐ', 'ì', 'ō', 'ó', 'ǒ', 'ò', 'ū', 'ú', 'ǔ', 'ù', 'ǘ', 'ǚ', 'ǜ', 'ń', 'ň', 'ǹ', 'm̄', 'ḿ', 'm̀','ê̄','ế','ê̌','ề']
    toneb=["a1","a2","a3","a4","e1","e2","e3","e4","i1","i2","i3","i4","o1","o2","o3","o4","u1","u2","u3","u4","ü2","ü3","ü4","n2","n3","n4","m1","m2", "m4",'ea1','ea2','ea3','ea4']
    for i in tones:
        py=py.replace(i,toneb[tones.index(i)])
    py=py.replace('ü','v')
    py=re.sub("(\d)(.*)$", r'\2\1', py)
    return py

d.clear()
for line in open("/usr/share/unicode/Unihan_Readings.txt"):
    fields = line.strip().split("\t", 2)
    if len(fields) != 3:
        continue
    han, typ, yin = fields
    han = hex2chr(han)
    if typ == "kMandarin":
        yin = yin.strip().split(" ")
        for y in yin:
            d[han].append(norm(y))
update("pu", d)

#ct
#https://github.com/rime/rime-cantonese/blob/master/jyut6ping3.dict.yaml
d.clear()
for line in open("jyut6ping3.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) < 2: continue
  hz, py = fs[:2]
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
for line in open("/usr/share/unicode/Unihan_Readings.txt"):
  fields = line.strip().split("\t", 2)
  if len(fields) != 3: continue
  han, typ, yin = fields
  han = hex2chr(han)
  if typ == "kCantonese":
    yin = yin.strip().split(" ")
    for y in yin:
      if y not in d[han]:
        d[han].append(y)
update("ct", d)

#hk
#https://github.com/g0v/moedict-webkit/blob/master/h.txt

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
print(len(unicodes))
