#!/usr/bin/env python3

import sqlite3, re, json
from collections import defaultdict
import logging
from time import time

logging.basicConfig(format='[%(asctime)s] %(message)s', level=logging.INFO)
start = time()

def timeit():
    global start
    end = time()
    passed = end - start
    start = end
    return passed

def hex2chr(uni):
    "把unicode轉換成漢字"
    if uni.startswith("U+"): uni = uni[2:]
    return chr(int(uni, 16))

HEADS = [
  ('hz', '漢字', '漢字', '#9D261D', '字海', 'http://yedict.com/zscontent.asp?uni=%2$s'),
  #('unicode', '統一碼', '統一碼', '#808080', 'Unihan', 'https://www.unicode.org/cgi-bin/GetUnihanData.pl?codepoint=%s'),
  ('bh', '總筆畫數', '筆畫', '#808080', None, None),
  ('bs', '部首餘筆', '部首', '#808080', None, None),
  ('sg', '上古', '上古', '#9A339F', '韻典網（上古音系）', 'https://ytenx.org/dciangx/dzih/%s'),
  ('mc', '中古', '中古', '#9A339F', '韻典網', "http://ytenx.org/zim?kyonh=1&dzih=%s"),
  ('zy', '中原音韻', '近古', '#9A339F', '韻典網（中原音韻）', 'https://ytenx.org/trngyan/dzih/%s'),
  ('pu', '普通話', '普語', '#FF00FF', '漢典網', "http://www.zdic.net/hans/%s"),
  ('nt', '南通話', '南通', '#0000FF', '南通方言網', "http://nantonghua.net/search/index.php?hanzi=%s"),
  ('tr', '泰如方言', '泰如', '#0000FF', '泰如小字典', "http://taerv.nguyoeh.com/"),
  ('ic', '鹽城話', '鹽城', '#0000FF', '淮語字典', "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s"),
  #('lj', '南京話', '南京', '#0000FF', '南京官話拼音方案', "https://uliloewi.github.io/LangJinPinIn/PinInFangAng"),
  ('sz', '蘇州話', '蘇州', '#1E90FF', '吳語學堂（蘇州）', "https://www.wugniu.com/search?table=suzhou_zi&char=%s"),
  ('sh', '上海話', '上海', '#1E90FF', '吳音小字典（上海）', "http://www.wu-chinese.com/minidict/search.php?searchlang=zaonhe&searchkey=%s"),
  ('nc', '南昌話', '南昌', '#00ADAD', None, None),
  ('hk', '客家話綜合口音', '客語', '#008000', '薪典', "https://www.syndict.com/w2p.php?item=hak&word=%s"),
  ('hl', '客家話海陸腔', '海陸', '#008000', '客語萌典', "https://www.moedict.tw/:%s"),
  ('sx', '客家話四縣腔', '四縣', '#008000', '客語萌典', "https://www.moedict.tw/:%s"),
  ('ct', '粵語', '粵語', '#FFAD00', '粵語審音配詞字庫', "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s"),
  ('mn', '閩南語', '閩南', '#FF6600', '臺灣閩南語常用詞辭典', "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s"),
  ('vn', '越南語', '越南', '#DB7093', '漢越辭典摘引', "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s"),
  ('kr', '朝鮮語', '朝鮮', '#BA55D3', 'Naver漢字辭典', "http://hanja.naver.com/hanja?q=%s"),
  ('jp_go', '日語吳音', '日吳', '#FF0000', None, None),
  ('jp_kan', '日語漢音', '日漢', '#FF0000', None, None),
  ('jp_tou', '日語唐音', '日唐', '#FF0000', None, None),
  ('jp_kwan', '日語慣用音', '日慣', '#FF0000', None, None),
  ('jp_other', '日語其他讀音', '日他', '#FF0000', None, None),
]
ZHEADS = list(zip(*HEADS))
KEYS = ZHEADS[0]
FIELDS = ", ".join(["%s TEXT"%i for i in KEYS])
COUNT = len(KEYS)
INSERT = 'INSERT INTO mcpdict VALUES (%s)'%(','.join('?'*COUNT))

#db
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

# patch
unicodes["冇"]["kr"]=None
logging.info("讀取數據庫 %.3f" % timeit())

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

pq = dict()
for line in open("../../ytenx/ytenx/sync/kyonh/PrengQim.txt"):
    line = line.strip()
    fs = line.split(" ")
    pq[fs[0]] = fs[1].replace("'", "0")
dzih = defaultdict(list)
for line in open("../../ytenx/ytenx/sync/kyonh/Dzih.txt"):
  line = line.strip()
  fs = line.split(" ")
  dzih[fs[0]].append(pq[fs[1]])
for hz in unicodes.keys():
  if "mc" in unicodes[hz]:
    py = unicodes[hz]["mc"]
    if py:
      if hz in dzih:
        pys = [py if py in dzih[hz] else "|%s|" % py for py in py.split(",")]
        for py in dzih[hz]:
          if py not in pys:
            pys.append(py)
        unicodes[hz]["mc"] = ",".join(pys)
      else:
        unicodes[hz]["mc"] = "|%s|"%(py.replace(",", "|,|"))
logging.info("處理中古音 %.3f" % timeit())

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
logging.info("處理上古音 %.3f" % timeit())

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
sds = {'去': '5', '入平': '2', '入去': '5', '入上': '3', '上': '3','陽平': '2', '陰平': '1'}

for line in open("../../ytenx/ytenx/sync/trngyan/TriungNgyanQimYonh.txt"):
  line = line.strip()
  if line.startswith('#'): continue
  fs = line.split(' ')
  hzs = fs[1]
  sd = fs[2]
  py = sms[fs[4]]+yms[fs[5]]+sds[sd]
  if "ɿ" in py:
    py = re.sub("([ʂɽ].*?)ɿ", "\\1ʅ", py)
  if sd.startswith("入"):
    py = "_%s_"%py
  for hz in hzs:
    if py not in d[hz]:
      d[hz].append(py)
update("zy", d)
logging.info("處理近古音 %.3f" % timeit())

#nt
#http://nantonghua.net
d.clear()
for line in open("nt.txt"):
  fs = line.strip().split(',')
  if fs[1]=='"hanzi"': continue
  hz = fs[1].strip('"')[0]
  py = fs[-6].strip('"') + fs[-4]
  if '白读' in line:
    py = "%s`白`" % py
  elif '文读' in line:
    py = "%s`文`" % py
  elif '又读' in line:
    py = "%s`又`" % py
  if py not in d[hz]:
    d[hz].append(py)
update("nt", d)
logging.info("處理南通話 %.3f" % timeit())

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
  c = fs[6]
  if fs[3] == fs[4] == 'v': fs[3] = ''
  py = trsm[fs[3]]+trym[fs[4]]+fs[5]
  if '白' in c or '口' in c or '常' in c or '古' in c or '舊' in c or '未' in c:
    py = "%s`白`" % py
  elif '正' in c or '本' in c:
    py = "%s`本`" % py
  elif '異' in c or '訓' in c or '避' in c or '又' in c:
    py = "%s`又`" % py
  elif '文' in c or '新' in c or '齶化' in c:
    py = "%s`文`" % py
  
  if py not in d[hz]:
    d[hz].append(py)
  jt = fs[2].replace('"','')
  if jt!=hz and py not in d[jt]:
    d[jt].append(py)
update("tr", d)
logging.info("處理泰如話 %.3f" % timeit())

#ic
#https://github.com/osfans/xu/blob/master/docs/xu.csv
d.clear()
icsm = {'g': 'k', 'd': 't', '': '', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 't': 'tʰ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ', 'ng': 'ŋ'}
icym = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'i', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iəu', 'u': 'u', 'v': 'v', 'en': 'ən', 'a': 'a', 'on': 'ɔŋ', 'an': 'ã', 'oh': 'ɔʔ', 'i': 'j', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'ui': 'ui', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'uʔ', 'aen': 'ɛ̃', 'er': 'ɚ', 'eu': 'əu', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iuʔ', 'yen': 'yn', 'ian': 'iã', 'iun': 'iũ', 'un': 'ũ', 'o': 'ɔ', 'uan': 'uã', 'ua': 'ua', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya'}
for line in open("ic"):
  line = line.strip()
  if not line: continue
  fs = line.split('\t')
  py,hzs = fs
  sm = re.findall("^[^aeiouvy]?", py)[0]
  sd = py[-1]
  ym = py[len(sm):-1]
  py = icsm[sm]+icym[ym]+sd
  hzs = re.findall("(.)([+-=*?]?)(（.*?）)?", hzs)
  for hz, c, m in hzs:
    p = ""
    if c and c in '-+=*?':
      if c == '-':
        p = "白"
      elif c == '+':
        p = "又"
      elif c == '=':
        p = "文"
      elif c == '*' or c == '?':
        p = "俗"
    p = p + m
    if p:
      p = "`%s`" % p
    p = py + p
    if p not in d[hz]:
      if c == '-':
        d[hz].insert(0, p)
      else:
        d[hz].append(p)
update("ic", d)
logging.info("處理鹽城話 %.3f" % timeit())

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
def sz2ipa(s):
  tone = s[-1]
  if tone.isdigit():
    s = s[:-1]
  else:
    tone = ""
  s = re.sub("y$", "ɿ", s)
  s = re.sub("yu$", "ʮ", s)
  s = re.sub("q$", "h", s)
  s = s.replace("y", "ghi").replace("w", "ghu").replace("ii", "i").replace("uu", "u")
  s = re.sub("(c|ch|j|sh|zh)u", "\\1iu", s)
  s = re.sub("iu$", "yⱼ", s)
  s = s.replace("au", "æ").replace("ieu", "y").replace("eu", "øʏ").replace("oe", "ø")\
          .replace("an", "ã").replace("aon", "ɑ̃").replace("en", "ən")\
          .replace("iuh", "yəʔ").replace("iu", "y").replace("ou", "əu").replace("aeh", "aʔ").replace("ah", "ɑʔ").replace("ih", "iəʔ").replace("eh", "əʔ").replace("on", "oŋ")
  s = re.sub("h$", "ʔ", s)
  s = re.sub("er$", "əl", s)
  s = s.replace("ph", "pʰ").replace("th", "tʰ").replace("kh", "kʰ").replace("tsh", "tsʰ")\
          .replace("ch", "tɕʰ").replace("c", "tɕ").replace("sh", "ɕ").replace("j", "dʑ").replace("zh", "ʑ")\
          .replace("gh", "ɦ").replace("ng", "ŋ").replace("gn", "ȵ")
  s = re.sub("i$", "iⱼ", s)
  s = re.sub("ie$", "i", s)
  s = re.sub("e$", "ᴇ", s)
  s = s + tone
  return s
d.clear()
for line in open("wugniu_soutseu.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) != 2: continue
  hz, py = fs
  if len(hz) == 1:
    py = sz2ipa(py)
    if py not in d[hz]:
      d[hz].append(py)
update("sz", d)
logging.info("處理蘇州話 %.3f" % timeit())

#pu
def norm(py):
    if py == "wòng": py= "weng4"
    py = py.replace('ɑ', 'a').replace('ɡ', 'g').replace('ü','v')
    tonea=['ā', 'á', 'ǎ', 'à', 'ē', 'é', 'ě', 'è', 'ī', 'í', 'ǐ', 'ì', 'ō', 'ó', 'ǒ', 'ò', 'ū', 'ú', 'ǔ', 'ù', 'ǘ', 'ǚ', 'ǜ', 'ń', 'ň', 'ǹ', 'm̄', 'ḿ', 'm̀','ê̄','ế','ê̌','ề']
    toneb=["a1","a2","a3","a4","e1","e2","e3","e4","i1","i2","i3","i4","o1","o2","o3","o4","u1","u2","u3","u4","v2","v3","v4","n2","n3","n4","m1","m2", "m4",'ea1','ea2','ea3','ea4']
    for i in tonea:
      if i in py:
        py=py.replace(i, toneb[tonea.index(i)])
        break
    py=re.sub("(\d)(.*)$", r'\2\1', py)
    return py

d.clear()
for line in open("/usr/share/unicode/Unihan_Readings.txt"):
    if not line.startswith("U"): continue
    fields = line.strip().split("\t", 2)
    han, typ, yin = fields
    if typ == "kMandarin":
      han = hex2chr(han)
      yin = yin.strip().split(" ")
      for y in yin:
        d[han].append(norm(y))
update("pu", d)
logging.info("處理普通話 %.3f" % timeit())

#https://github.com/g0v/moedict-data-csld/blob/master/中華語文大辭典全稿-20160620.csv
def update_twpy(hz, py):
  if len(hz) != 1 or len(py) == 0: return
  k = "pu"
  v = unicodes[hz].get(k, None)
  pyu = "_%s_" % py
  if v:
    vl = v.split(",")
    if py not in vl and pyu not in vl:
      unicodes[hz][k] = v + "," + pyu
  else:
    unicodes[hz] = {"hz": hz, "unicode": "%04X"%ord(hz)}
    unicodes[hz][k] = pyu

for line in open("中華語文大辭典全稿-20160620.csv"):
  line = line.strip()
  fs = line.split(",")
  if len(fs) <= 13: continue
  if fs[1]!='終定稿': continue
  cht = fs[5]
  chs = fs[6]
  py = fs[11]
  if not py: continue
  if len(chs) == 1 or len(cht)==1:
    py = norm(py)
    update_twpy(cht, py)
    if chs != cht:
      update_twpy(chs, py)
logging.info("處理大辭典 %.3f" % timeit())

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
  line = line.strip()
  if not line.startswith("U"): continue
  fields = line.strip().split("\t", 2)
  han, typ, yin = fields
  if typ == "kCantonese":
    yin = yin.strip().split(" ")
    han = hex2chr(han)
    for y in yin:
      if y not in d[han]:
        d[han].append(y)
update("ct", d)
logging.info("處理廣東話 %.3f" % timeit())

#sh
def sh2ipa(s):
  tag = s[0]
  isTag = tag == "|"
  if isTag:
    s = s[1:-1]
  b = s
  tone = s[-1]
  if tone.isdigit():
    s = s[:-1]
  else:
    tone = ""
  s = re.sub("y$", "ɿ", s)
  s = s.replace("y", "ghi").replace("w", "ghu").replace("ii", "i").replace("uu", "u")
  s = re.sub("(c|ch|j|sh|zh)u", "\\1iu", s)
  s = s.replace("au", "ɔ").replace("eu", "ɤ").replace("oe", "ø")\
          .replace("an", "ã").replace("aon", "ɑ̃").replace("en", "ən")\
          .replace("iuh", "yiʔ").replace("iu", "y").replace("eh", "əʔ").replace("on", "oŋ")
  s = re.sub("h$", "ʔ", s)
  s = re.sub("r$", "əl", s)
  s = s.replace("ph", "pʰ").replace("th", "tʰ").replace("kh", "kʰ").replace("tsh", "tsʰ")\
          .replace("ch", "tɕʰ").replace("c", "tɕ").replace("sh", "ɕ").replace("j", "dʑ").replace("zh", "ʑ")\
          .replace("gh", "ɦ").replace("ng", "ŋ")
  s = re.sub("e$", "ᴇ", s)
  s = s + tone
  if isTag:
    s = "%s`白`" % s
  return s

for i in unicodes.keys():
  if "sh" in unicodes[i]:
    sh = unicodes[i]["sh"]
    if sh:
      fs = sh.split(",")
      fs = map(sh2ipa, fs)
      sh = ",".join(fs)
      unicodes[i]["sh"] = sh
logging.info("處理上海話 %.3f" % timeit())

#mn
for i in unicodes.keys():
  if "mn" in unicodes[i]:
    py = unicodes[i]["mn"]
    if py:
      py = re.sub("\|(.*?)\|", "\\1`白`", py)
      py = re.sub("\*(.*?)\*", "\\1`文`", py)
      py = re.sub("\((.*?)\)", "\\1`俗`", py)
      py = re.sub("\[(.*?)\]", "\\1`訓`", py)
      unicodes[i]["mn"] = py
logging.info("處理閩南話 %.3f" % timeit())

#hk
#https://github.com/syndict/hakka/blob/master/hakka.dict.yaml
hktones = {"44":"1", "33": "1", "11":"2", "31":"3", "13":"4", "52":"5", "53":"5", "21":"6", "5":"7", "1":"8", "3":"8"}
sxtones = {"²⁴":"1", "¹¹": "2", "³¹":"3", "⁵³":"3", "⁵⁵":"5", "²":"7", "⁵":"8"}
hltones = {"⁵³":"1", "⁵⁵": "2", "²⁴":"3", "¹¹":"5", "³³":"6", "⁵":"7", "²":"8"}
def hk2ipa(s, tones):
  c = s[-1]
  if c in "文白":
    s = s[:-1]
  else:
    c = ""
  s = s.replace("er","ə").replace("ae","æ").replace("ii", "ɿ").replace("e", "ɛ")
  s = s.replace("sl", "ɬ").replace("nj", "ɲ").replace("t", "tʰ").replace("zh", "t∫").replace("ch", "t∫ʰ").replace("sh", "∫").replace("p", "pʰ").replace("k", "kʰ").replace("z", "ts").replace("c", "tsʰ").replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ").replace("rh", "ʒ").replace("r", "ʒ").replace("ng", "ŋ").replace("?", "ʔ").replace("b", "p").replace("d", "t").replace("g", "k")
  tone = re.findall("[¹²³⁴⁵\d]+$", s)
  if tone:
    tone = tone[0]
    s = s.replace(tone, tones[tone])
  if c == "文" or c == "白":
    s = "%s`%s`"%(s,c)
  return s

d.clear()
for line in open("hakka.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) < 2: continue
  hz, py = fs[:2]
  if len(hz) == 1:
    if py:
      py = hk2ipa(py, hktones)
      if py not in d[hz]:
        d[hz].append(py)
update("hk", d)

#https://github.com/g0v/moedict-data-hakka/blob/master/dict-hakka.json
tk = json.load(open("dict-hakka.json"))
d.clear()
for line in tk:
    hz = line["title"]
    heteronyms = line["heteronyms"]
    if len(hz) == 1:
      for i in heteronyms:
        pys = i["pinyin"]
        py = re.findall("海⃞(.+?)\\b", pys)
        if py:
          d[hz].append(hk2ipa(py[0], hltones))
update("hl", d)
d.clear()
for line in tk:
    hz = line["title"]
    heteronyms = line["heteronyms"]
    if len(hz) == 1:
      for i in heteronyms:
        pys = i["pinyin"]
        py = re.findall("四⃞(.+?)\\b", pys)
        if py:
          d[hz].append(hk2ipa(py[0], sxtones))
update("sx", d)
logging.info("處理客家話 %.3f" % timeit())

#nc
readings = "白文又"
d.clear()
def get_readings(index):
	if not index: return ''
	return "`%s`"%readings[int(index)-1]
def readorder(py):
	index = readings.index(py[-2])+1 if py.endswith('`') else 0
	return "%d%s"%(index,py)
def sub(m):
	ret = ''
	for i in m.group(1):
		if i in "12345":continue
		ret += i + m.group(2)
	return ret
for line in open("nc"):
	line = line.strip().replace(" ", "").replace("(", "（").replace(")","）")
	if line.startswith("#") or not line: continue
	line = re.sub('（(.*?)）(\d)', sub, line)
	fs = re.findall("^([^\u3400-\U0003134f]+ ?)(.*)$", line)[0]
	py, hzs = fs
	if not hzs: continue
	for hz,r in re.findall("(.)(\d?)", hzs):
		item = py+get_readings(r)
		if item not in d[hz]:
			d[hz].append(item)
		if py.endswith('k7') or py.endswith('k8'):
			item = re.sub('k([78])', 'ʔ\\1', py)+get_readings('3')
			if item not in d[hz]:
				d[hz].append(item)
for hz in d:
	d[hz] = sorted(d[hz], key=readorder)
update("nc", d)
logging.info("處理南昌話 %.3f" % timeit())

#bh
d.clear()
for line in open("/usr/share/unicode/Unihan_IRGSources.txt"):
    if not line.startswith("U"): continue
    fields = line.strip().split("\t", 2)
    han, typ, val = fields
    if typ == "kTotalStrokes":
      han = hex2chr(han)
      if han in unicodes:
        d[han].append(val)
update("bh", d)
logging.info("處理總畫數 %.3f" % timeit())

#bs
bs = dict()
for line in open("/usr/share/unicode/CJKRadicals.txt"):
    line = line.strip()
    if not line or line.startswith("#"): continue
    fields = line.split("; ", 2)
    order, radical, han = fields
    han = hex2chr(han)
    bs[order] = han
d.clear()
for line in open("/usr/share/unicode/Unihan_IRGSources.txt"):
    if not line.startswith("U"): continue
    fields = line.strip().split("\t", 2)
    han, typ, vals = fields
    if typ != "kRSUnicode": continue
    han = hex2chr(han)
    if han not in unicodes: continue
    for val in vals.split(" "):
      fs = val.split(".")
      order, left = fs
      left = left.replace('-', 'f')
      d[han].append(bs[order]+left)
update("bs", d)
logging.info("部首檢字法 %.3f" % timeit())

#all hz readings
def cjkorder(s):
  n = ord(s)
  return n + 0x10000 if n < 0x4E00 else n

conn = sqlite3.connect('../app/src/main/assets/databases/mcpdict.db')
c = conn.cursor()
c.execute("DROP TABLE IF EXISTS mcpdict")
c.execute("CREATE VIRTUAL TABLE mcpdict USING fts3 (%s)"%FIELDS)
c.executemany(INSERT, ZHEADS[1:])

f = open("han.txt","w")
for i in sorted(unicodes.keys(), key=cjkorder):
  n = ord(i)
  if 0xE000<=n<=0xF8FF or 0xF0000<=n<=0xFFFFD or 0x100000<=n<=0x10FFFD:
    continue
  d = unicodes[i]
  v = list(map(d.get, KEYS))
  c.execute(INSERT, v)
  if n >= 0x20000:
    f.write(i)
f.close()
conn.commit()
conn.close()
logging.info("保存數據庫 %.3f" % timeit())

print(len(unicodes))
