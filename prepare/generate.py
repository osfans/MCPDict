#!/usr/bin/env python3

import sqlite3, re, json
from collections import defaultdict
import logging
from time import time
import ruamel.yaml
from itertools import chain
import unicodedata
from openpyxl import load_workbook

logging.basicConfig(format='[%(asctime)s] %(message)s', level=logging.INFO)
start = time()
start0 = start

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
  ('hz', '漢字', '漢字', '#9D261D', '字海', 'http://yedict.com/zscontent.asp?uni=%2$s',"更新：2021-06-23<br>說明：本程序支持多種方式查詢漢字在古今中外多種語言中的讀音。如輸入𰻞（漢字）、30EDE或U+30EDE（Unicode編碼）、biang2（普通話拼音）、43（總筆畫數）、辵39（部首餘筆），均可查詢到“𰻞”的讀音。"),
  #('unicode', '統一碼', '統一碼', '#808080', 'Unihan', 'https://www.unicode.org/cgi-bin/GetUnihanData.pl?codepoint=%s'),
  ('sg', '上古（鄭張尚芳）', '鄭張', '#9A339F', '韻典網（上古音系）', 'https://ytenx.org/dciangx/dzih/%s',"名稱：上古音鄭張尚芳擬音<br>來源：<a href=https://ytenx.org/dciangx/>韻典網</a>"),
  ('ba', '上古（白一平沙加爾）', '白沙2015', '#9A339F', None, None, "更新：2015-10-13<br>名稱：上古音白一平沙加爾2015年擬音<br>來源：<a href=http://ocbaxtersagart.lsait.lsa.umich.edu/>http://ocbaxtersagart.lsait.lsa.umich.edu/</a>"),
  ('mc', '廣韻', '廣韻', '#9A339F', '韻典網', "http://ytenx.org/zim?kyonh=1&dzih=%s", "名稱：廣韻擬音<br>來源：<a href=https://ytenx.org/kyonh/>韻典網</a>、<a href=https://github.com/biopolyhedron/rime-middle-chinese>中古全拼輸入法</a><br>說明：灰色讀音來自中古全拼輸入法。括號中注明了《廣韻》中的聲母、韻攝、韻目、等、呼、聲調，以及《平水韻》中的韻部。對於“支脂祭真仙宵侵鹽”八個有重紐的韻，僅在聲母爲脣牙喉音時標註A、B類。廣韻韻目中缺少冬系上聲、臻系上聲、臻系去聲和痕系入聲，“韻典網”上把它們補全了，分別作“湩”、“𧤛”、“櫬”、“麧”。由於“𧤛”字不易顯示，故以同韻目的“齔”字代替。"),
  ('yt', '韻圖', '韻圖', '#9A339F', None, None, "名稱：韻圖擬音<br>來源：QQ共享文檔<a href=https://docs.qq.com/sheet/DYk9aeldWYXpLZENj>韻圖音系同音字表</a>"),
  ('zy', '中原音韻', '中原音韻', '#9A339F', '韻典網（中原音韻）', 'https://ytenx.org/trngyan/dzih/%s', "名稱：中原音韻擬音<br>來源：<a href=https://ytenx.org/trngyan/>韻典網</a><br>說明：下標“入”表明是古入聲字"),
  ('pu', '普通話', '普通話', '#FF00FF', '漢典網', "http://www.zdic.net/hans/%s", "名稱：普通話、國語<br>來源：<a href=https://www.zdic.net/>漢典</a>、<a href=http://yedict.com/>字海</a>、<a href=https://www.moedict.tw/>萌典</a><br>說明：灰色讀音來自<a href=https://www.moedict.tw/>萌典</a>"),
  ('nt', '南通話', '南通', '#0000FF', '南通方言網', "http://nantonghua.net/search/index.php?hanzi=%s", "更新：2018-01-08<br>名稱：南通話<br>來源：<a href=http://nantonghua.net/archives/5127/南通话字音查询/>南通方言網</a>"),
  ('tr', '泰如方言', '泰如', '#0000FF', '泰如小字典', "http://taerv.nguyoeh.com/query.php?table=泰如字典&簡體=%s", "更新：2021-06-22<br>名稱：泰如方言<br>來源：<a href=http://taerv.nguyoeh.com/>泰如小字典</a>"),
  ('ic', '鹽城話', '鹽城', '#0000FF', '淮語字典', "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s", "名稱：鹽城話<br>來源：<a href=http://huae.nguyoeh.com/>類音字彙</a>，《鹽城縣志》等"),
  #('lj', '南京話', '南京', '#0000FF', '南京官話拼音方案', "https://uliloewi.github.io/LangJinPinIn/PinInFangAng"),
  ('td', '通東談話', '通東', '#7C00FF', None, None, "更新：2021-06-23<br>名稱：通東談話<br>來源：網友<u>正心修身</u>"),
  ('sz', '蘇州話', '蘇州', '#1E90FF', '吳語學堂（蘇州）', "https://www.wugniu.com/search?table=suzhou_zi&char=%s", "名稱：蘇州話<br>來源：<a href=https://github.com/NGLI/rime-wugniu_soutseu>蘇州吳語拼音輸入方案</a>、<a href=https://www.wugniu.com/>吳語學堂</a>"),
  ('sh', '上海話', '上海', '#1E90FF', '吳音小字典（上海）', "http://www.wu-chinese.com/minidict/search.php?searchlang=zaonhe&searchkey=%s", "名稱：上海話<br>來源：《上海市區方言志》（1988年版），蔡子文錄入<br>說明：該書記錄的是中派上海話音系（使用者多出生於20世紀40至70年代），與<a href=http://www.wu-chinese.com/minidict/>吳音小字典</a>記錄的音系並不完全相同。"),
  ('ra', '瑞安城關', '瑞安', '#1E90FF', None, None, "更新：2021-05-24<br>名稱：瑞安城關讀音<br>來源：網友<u>落橙</u>"),
  ('nc', '南昌話', '南昌', '#00ADAD', None, None, "名稱：南昌話<br>來源：網友<u>澀口的茶</u>"),
  ('hk', '客家話綜合口音', '綜合客語', '#008000', '薪典', "https://www.syndict.com/w2p.php?item=hak&word=%s", "更新：2019-04-19<br>名稱：客家話綜合口音<br>來源：<a href=https://github.com/syndict/hakka/>客語輸入法</a>、<a href=https://www.syndict.com/>薪典</a>"),
  ('hl', '客家話海陸腔', '海陸客語', '#008000', '客語萌典', "https://www.moedict.tw/:%s", "名稱：客家話海陸腔<br>來源：<a href=https://www.moedict.tw/>客語萌典</a>"),
  ('sx', '客家話四縣腔', '四縣客語', '#008000', '客語萌典', "https://www.moedict.tw/:%s", "名稱：客家話四縣腔<br>來源：<a href=https://www.moedict.tw/>客語萌典</a>"),
  ('ct', '廣州話', '廣州', '#FFAD00', '粵語審音配詞字庫', "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s", "名稱：廣州話<br>來源：<a href=http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/>粵語審音配詞字庫</a>、<a href=http://www.unicode.org/charts/unihan.html>Unihan</a><br>說明：括號中的爲異讀讀音"),
  ('mn', '閩南語', '閩南', '#FF6600', '臺灣閩南語常用詞辭典', "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s", "更新：2018-07-05<br>名稱：閩南語<br>來源：<a href=https://twblg.dict.edu.tw/holodict_new/>臺灣閩南語常用詞辭典</a><br>說明：下標“俗”表示“俗讀音”，“替”表示“替代字”，指的是某個字的讀音其實來自另一個字，比如“人”字的lang5音其實來自“儂”字。有些字會有用斜線分隔的兩個讀音（如“人”字的jin5/lin5），前者爲高雄音（第一優勢腔），後者爲臺北音（第二優勢腔）。"),
  ('vn', '越南語', '越南', '#DB7093', '漢越辭典摘引', "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s", "名稱：越南語<br>來源：<a href=http://www.vanlangsj.org/hanviet/>漢越辭典摘引</a>"),
  ('kr_mid', '中世紀朝鮮語', '中世朝鮮', '#BA55D3', None, None, "名稱：中世紀朝鮮語<br>來源：<a href=https://github.com/nk2028/sino-korean-readings>韓國漢字音歷史層次研究</a>"),
  ('kr', '朝鮮語', '朝鮮', '#BA55D3', 'Naver漢字辭典', "http://hanja.naver.com/hanja?q=%s", "名稱：朝鮮語、韓語<br>來源：<a href=http://hanja.naver.com/>Naver漢字辭典</a>"),
  ('jp_go', '日語吳音', '日語吳音', '#FF0000', None, None, "名稱：日語吳音<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。"),
  ('jp_kan', '日語漢音', '日語漢音', '#FF0000', None, None, "名稱：日語漢音<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。"),
  ('jp_tou', '日語唐音', '日語唐音', '#FF0000', None, None, "名稱：日語<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。"),
  ('jp_kwan', '日語慣用音', '日語慣用', '#FF0000', None, None, None),
  ('jp_other', '日語其他讀音', '日語其他', '#FF0000', None, None, None),
  ('bh', '總筆畫數', '總筆畫數', '#808080', None, None, None),
  ('bs', '部首餘筆', '部首餘筆', '#808080', None, None, None),
]
ZHEADS = list(zip(*HEADS))
KEYS = ZHEADS[0]
FIELDS = ", ".join(["%s "%i for i in KEYS])
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
  del row["pu"]
  row["hz"] = i
  unicodes[i] = row
conn.close()
logging.info("讀取數據庫 %.3f" % timeit())

kCompatibilityVariants = dict()
def update(k, d):
  global kCompatibilityVariants
  for i,v in d.items():
    i = kCompatibilityVariants.get(i, i)
    if i not in unicodes:
      unicodes[i] = {"hz": i, "unicode": "%04X"%ord(i)}
    if unicodes[i].get(k, None): continue
    unicodes[i][k] = ",".join(v)

d=defaultdict(list)

#kCompatibilityVariant
for line in open("../app/src/main/res/raw/orthography_hz_compatibility.txt"):
    han, val = line.rstrip()
    kCompatibilityVariants[han] = val
logging.info("處理兼容字 %.3f" % timeit())

#hz grade
g1 = set(open("一级字").read().strip().split("\n"))
g2 = set(open("二级字").read().strip().split("\n"))

def get_missing(g, k):
    return print("".join(set(g)-set(k)))

#mc
d.clear()
for line in open("../../RIME/rime-middle-chinese/zyenpheng.dict.yaml"):
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
logging.info("處理廣韻 %.3f" % timeit())

#yt
import yt
d.clear()
d = yt.get_dict()
update("yt", d)
logging.info("處理韻圖 %.3f" % timeit())

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

#ba
#http://ocbaxtersagart.lsait.lsa.umich.edu/BaxterSagartOC2015-10-13.xlsx
d.clear()
for sheet in load_workbook("BaxterSagartOC2015-10-13.xlsx"):
  for row in sheet.rows:
    if row:
      hz = row[0].value
      py = row[4].value
      if len(hz) == 1:
        if py not in d[hz]:
          d[hz].append(py)
update("ba", d)
logging.info("處理白沙 %.3f" % timeit())

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
    py = "%s`入`"%py
  for hz in hzs:
    if py not in d[hz]:
      d[hz].append(py)
update("zy", d)
logging.info("處理中原音韻 %.3f" % timeit())

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
  if not hz: continue
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
  if not jt: continue
  if jt!=hz and py not in d[jt]:
    d[jt].append(py)
update("tr", d)
logging.info("處理泰如話 %.3f" % timeit())

#ic
#https://github.com/osfans/xu/blob/master/docs/xu.csv
d.clear()
icsm = {'g': 'k', 'd': 't', '': '', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 't': 'tʰ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ', 'ng': 'ŋ'}
icym = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'i', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iəu', 'u': 'u', 'v': 'v', 'en': 'ən', 'a': 'a', 'on': 'ɔŋ', 'an': 'ã', 'oh': 'ɔʔ', 'i': 'j', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'ui': 'ui', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'uʔ', 'aen': 'ɛ̃', 'er': 'ɚ', 'eu': 'əu', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iuʔ', 'yen': 'yn', 'ian': 'iã', 'iun': 'iũ', 'un': 'ũ', 'o': 'ɔ', 'uan': 'uã', 'ua': 'ua', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya', '': ''}
for line in open("ic"):
  line = line.strip()
  if not line: continue
  fs = line.split('\t')
  py,hzs = fs
  sm = re.findall("^[^aeiouvy]?g?", py)[0]
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

#td
d.clear()
for line in open("通東談話.csv"):
  line = line.strip('\n').replace('"','')
  fs = line.split('\t')
  hz, jt, py = fs[:3]
  sd = fs[4]
  if py == "IPA": continue
  sd = sd[-1]
  if sd == "0": sd = ""
  elif sd == "¹": sd = "8"
  elif sd == "²": sd = "9"
  if sd: py += sd
  js = fs[6].replace("~", "～")
  if js: py += "`%s`"%js
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
  if len(jt) == 1:
    if py not in d[jt]:
      d[jt].append(py)
update("td", d)
logging.info("處理通東話 %.3f" % timeit())

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
  tones = {"1": "⁴⁴1", "2": "²²³2", "3":"⁵¹3", "5": "⁵²³5", "6":"²³¹6", "7":"⁴³7", "8":"²³8"}
  s = s + tones.get(tone, tone)
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
d.clear()
for line in open("pu.txt"):
  line = line.strip()
  if not line or line.startswith("#"):
    continue
  hzs,py = line.split("\t")[:2]
  for hz in hzs:
    d[hz] = py.split(",")
update("pu",d)
logging.info("處理普通話 %.3f" % timeit())

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
logging.info("處理廣州話 %.3f" % timeit())

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

#ra
def ra2ipa(s):
  s = re.sub("([^aeiouy])u([1-8])", "\\1ʋʷ\\2", s)
  s = s.replace("y", "J").replace("io","yo").replace("Jo","yo").replace("ae", "ɛ").replace("ao", "ɔ").replace("eu", "əʉ").replace("ou", "əu").replace("oe", "ø").replace("yu", "y").replace("iu", "y")\
          .replace("an", "aŋ").replace("en", "əŋ").replace("on", "oŋ")
  s = s.replace("ph", "pʰ").replace("th", "tʰ").replace("kh", "kʰ").replace("tsh", "tsʰ")\
          .replace("ch", "tɕʰ").replace("c", "tɕ").replace("sh", "ɕ").replace("j", "dʑ").replace("J","j").replace("zh", "ʑ")\
          .replace("gh", "ɦ").replace("ng", "ŋ")
  return s
d.clear()
for line in open("同音字表-瑞安.csv"):
  fs = line.strip().split('\t')
  hz = fs[0]
  py = fs[2]
  if len(hz) == 1:
    py = ra2ipa(py)
    if py not in d[hz]:
      d[hz].append(py)
update("ra", d)
logging.info("處理瑞安話 %.3f" % timeit())

#mn
for i in unicodes.keys():
  if "mn" in unicodes[i]:
    py = unicodes[i]["mn"]
    if py:
      py = re.sub("\|(.*?)\|", "\\1`白`", py)
      py = re.sub("\*(.*?)\*", "\\1`文`", py)
      py = re.sub("\((.*?)\)", "\\1`俗`", py)
      py = re.sub("\[(.*?)\]", "\\1`替`", py)
      unicodes[i]["mn"] = py
logging.info("處理閩南話 %.3f" % timeit())

#hk
#https://github.com/syndict/hakka/blob/master/hakka.dict.yaml
hktones = {"⁴⁴":"1", "³³": "1", "¹¹":"2", "³¹":"3", "¹³":"4", "⁵²":"5", "⁵³":"5", "²¹":"6", "⁵":"7", "¹":"8", "³":"8"}
sxtones = {"²⁴":"1", "¹¹": "2", "³¹":"3", "⁵³":"3", "⁵⁵":"5", "²":"7", "⁵":"8"}
hltones = {"⁵³":"1", "⁵⁵": "2", "²⁴":"3", "¹¹":"5", "³³":"6", "⁵":"7", "²":"8"}
def hk2ipa(s, tones):
  c = s[-1]
  if c in "文白":
    s = s[:-1]
  else:
    c = ""
  s = s.replace("er","ə").replace("ae","æ").replace("ii", "ɿ").replace("e", "ɛ").replace("o", "ɔ")
  s = s.replace("sl", "ɬ").replace("nj", "ɲ").replace("t", "tʰ").replace("zh", "t∫").replace("ch", "t∫ʰ").replace("sh", "∫").replace("p", "pʰ").replace("k", "kʰ").replace("z", "ts").replace("c", "tsʰ").replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ").replace("rh", "ʒ").replace("r", "ʒ").replace("ng", "ŋ").replace("?", "ʔ").replace("b", "p").replace("d", "t").replace("g", "k")
  tone = re.findall("[¹²³⁴⁵\d]+$", s)
  if tone:
    tone = tone[0]
    s = s + tones[tone]
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
      py = py.replace("1","¹").replace("2","²").replace("3","³").replace("4","⁴").replace("5","⁵")
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

#kr_mid
#https://github.com/nk2028/sino-korean-readings/blob/main/woosun-sin.csv
d.clear()
for line in open("woosun-sin.csv"):
  fs = line.strip().split(',')
  hz = fs[0]
  if len(hz) == 1:
    py = "".join(fs[1:4])
    if py not in d[hz]:
      d[hz].append(py)
update("kr_mid", d)
logging.info("處理中世朝鮮 %.3f" % timeit())

#patch
patch = ruamel.yaml.load(open("patch.yaml"), Loader=ruamel.yaml.Loader)
for lang in patch:
  for hz in patch[lang]:
    for i in hz:
      if i not in unicodes:
        unicodes[i]["hz"] = i
      unicodes[i][lang] = patch[lang][hz]
logging.info("修正汉字音 %.3f" % timeit())

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

f = open("缺字","w")
fpy = open("缺音", "w")
notoext = set(open("NotoSansCJK-Regular.txt").read().strip())
for i in sorted(unicodes.keys(), key=cjkorder):
  n = ord(i)
  if (n<0x3400 and n not in (0x25A1, 0x3007)) or 0xA000<=n<0xF900 or 0xFB00<=n<0x20000 or n>=0x31350:
    print(i, unicodes[i])
    continue
  #SMP2 and unicode 13
  if n >= 0x20000 or 0x9FD1<=n<=0x9FFF or 0x4DB6<=n<=0x4DBF:
    if i not in notoext:
      f.write(i)
  d = unicodes[i]
  v = list(map(d.get, KEYS))
  c.execute(INSERT, v)
  if not d.get("pu"):
    fpy.write(i)
f.close()

for i in chain(range(0x3400,0xa000),range(0x20000,0x31350)):
  c = chr(i)
  if c not in unicodes:
    try:
      name = unicodedata.name(c)
      if name.startswith("CJK UNIFIED"):
        fpy.write(c)
    except:
      pass
fpy.close()

conn.commit()
conn.close()
logging.info("保存數據庫 %.3f" % timeit())
logging.info("處理總時間 %.3f" % (time() - start0))
logging.info("音典總字數 %d" % len(unicodes))
