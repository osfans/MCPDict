#!/usr/bin/env python3

import sqlite3, re, json
from collections import defaultdict
import logging
from time import time
import ruamel.yaml
from itertools import chain
import unicodedata
import variant

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
  ('hz', '漢字', '漢字', '#9D261D', '字海', 'http://yedict.com/zscontent.asp?uni=%2$s',"更新：2021-07-16<br>說明：<br>　　本程序源自“<a href=https://github.com/MaigoAkisame/MCPDict>漢字古今中外讀音查詢</a>”，收錄了更多漢字、更多語言、更多讀音，當然錯誤也更多，可去<a href=https://github.com/osfans/MCPDict>漢字音典</a>或進<a href=mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D-hNzAQCgZQL-uIlhFrxWJ56umCexsmBi>QQ群</a>提出寶貴意見。<br>　　本程序將多種語言的漢字讀音集成於本地數據庫，默認用國際音標注音，可用於比較各語言讀音的異同，也能給學習本程序所收的語言提供有限的幫助。<br>　　本程序支持多種方式查詢漢字及其讀音，如輸入𰻞（漢字）、30EDE（Unicode編碼）、biang2（普通話拼音）、43（總筆畫數）、辵39（部首餘筆），均可查到“𰻞”及其讀音。音節末尾的“?”可匹配任何聲調。<br>"),
  #('unicode', '統一碼', '統一碼', '#808080', 'Unihan', 'https://www.unicode.org/cgi-bin/GetUnihanData.pl?codepoint=%s'),
  ('och_sg', '上古（鄭張尚芳）', '鄭張', '#9A339F', '韻典網（上古音系）', 'https://ytenx.org/dciangx/dzih/%s',"名稱：上古音鄭張尚芳擬音<br>來源：<a href=https://ytenx.org/dciangx/>韻典網</a>"),
  ('och_ba', '上古（白一平沙加爾）', '白沙2015', '#9A339F', None, None, "更新：2015-10-13<br>名稱：上古音白一平沙加爾2015年擬音<br>來源：<a href=http://ocbaxtersagart.lsait.lsa.umich.edu/>http://ocbaxtersagart.lsait.lsa.umich.edu/</a>"),
  ('ltc_mc', '廣韻', '廣韻', '#9A339F', '韻典網', "http://ytenx.org/zim?kyonh=1&dzih=%s", "名稱：廣韻擬音<br>來源：<a href=https://ytenx.org/kyonh/>韻典網</a>、<a href=https://github.com/biopolyhedron/rime-middle-chinese>中古全拼輸入法</a><br>說明：灰色讀音來自中古全拼輸入法。括號中注明了《廣韻》中的聲母、韻攝、韻目、等、呼、聲調，以及《平水韻》中的韻部。對於“支脂祭真仙宵侵鹽”八個有重紐的韻，僅在聲母爲脣牙喉音時標註A、B類。廣韻韻目中缺少冬系上聲、臻系上聲、臻系去聲和痕系入聲，“韻典網”上把它們補全了，分別作“湩”、“𧤛”、“櫬”、“麧”。由於“𧤛”字不易顯示，故以同韻目的“齔”字代替。"),
  ('ltc_yt', '韻圖', '韻圖', '#9A339F', None, None, "名稱：韻圖擬音<br>來源：QQ共享文檔<a href=https://docs.qq.com/sheet/DYk9aeldWYXpLZENj>韻圖音系同音字表</a>"),
  ('ltc_zy', '中原音韻', '中原音韻', '#9A339F', '韻典網（中原音韻）', 'https://ytenx.org/trngyan/dzih/%s', "名稱：中原音韻擬音<br>來源：<a href=https://ytenx.org/trngyan/>韻典網</a><br>說明：平聲分陰陽，入聲派三聲。下標“入”表示古入聲字"),
  ('cmn', '普通話', '普通話', '#FF00FF', '漢典網', "http://www.zdic.net/hans/%s", "更新：2021-07-08<br>名稱：普通話、國語<br>來源：<a href=https://www.zdic.net/>漢典</a>、<a href=http://yedict.com/>字海</a>、<a href=https://www.moedict.tw/>萌典</a><br>說明：灰色讀音來自<a href=https://www.moedict.tw/>萌典</a>。可使用漢語拼音、注音符號查詢漢字。在輸入漢語拼音時，可以用數字1、2、3、4代表聲調，放在音節末尾，“?”可代表任何聲調；字母ü可用v代替。例如查詢普通話讀lüè的字時可輸入lve4。在輸入注音符號時，聲調一般放在音節末尾，但表示輕聲的點（˙）既可以放在音節開頭，也可以放在音節末尾，例如“的”字的讀音可拼作“˙ㄉㄜ”或“ㄉㄜ˙”。"),
  ('cmn_xn_yzll', '永州零陵話', '永州零陵', '#C600FF', None, None, "更新：2021-07-15<br>名稱：永州零陵話<br>來源：<a href=https://github.com/shinzoqchiuq/yongzhou-homophony-syllabary>永州官話同音字表</a>、《湖南省志·方言志》<br>說明：本同音字表描寫的是屬於山北片區的永州零陵區口音，整理自《湖南省志·方言志》，有脣齒擦音 /f/，無全濁塞擦音 /dz/ 和 /dʒ/，「彎」「汪」不同韻，區分陰去和陽去"),
  ('cmn_hy_hc_fdgc', '肥東古城話', '肥東古城', '#0000FF', None, None, "更新：2021-07-12<br>名稱：肥東古城話<br>來源：安徽肥東古城方言同音字匯"),
  ('cmn_hy_hc_ic', '鹽城話', '鹽城', '#0000FF', '淮語字典', "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s", "更新：2021-07-12<br>名稱：鹽城話<br>來源：<a href=http://huae.nguyoeh.com/>類音字彙</a>、鹽城縣志、鹽城方言研究（步鳳）等"),
  ('cmn_hy_tt_nt', '南通話', '南通', '#0000FF', '南通方言網', "http://nantonghua.net/search/index.php?hanzi=%s", "更新：2018-01-08<br>名稱：南通話<br>來源：<a href=http://nantonghua.net/archives/5127/南通话字音查询/>南通方言網</a>"),
  ('cmn_hy_tt_tr', '泰如方言', '泰如', '#0000FF', '泰如小字典', "http://taerv.nguyoeh.com/query.php?table=泰如字典&簡體=%s", "更新：2021-06-22<br>名稱：泰如方言<br>來源：<a href=http://taerv.nguyoeh.com/>泰如小字典</a>"),
  ('cmn_hy_tt_xh', '興化話', '興化', '#0000FF', None, None, "更新：2021-07-15<br>名稱：興化話<br>來源：江蘇興化方言音系、興化方言詞典"),
  #('lj', '南京話', '南京', '#0000FF', '南京官話拼音方案', "https://uliloewi.github.io/LangJinPinIn/PinInFangAng"),
  ('wuu_td', '通東談話', '通東', '#7C00FF', None, None, "更新：2021-07-16<br>名稱：通東談話<br>來源：網友<u>正心修身</u>"),
  ('wuu', '標準吳語', '吳語', '#1E90FF', '標準吳語字典', "http://nguyoeh.com/query.php?table=吳語字典&簡體=%s", "更新：2021-07-09<br>名稱：標準吳語<br>來源：<a href=http://nguyoeh.com/>標準吳語字典</a>"),
  ('wuu_sz', '蘇州話', '蘇州', '#1E90FF', '吳語學堂（蘇州）', "https://www.wugniu.com/search?table=suzhou_zi&char=%s", "名稱：蘇州話<br>來源：<a href=https://github.com/NGLI/rime-wugniu_soutseu>蘇州吳語拼音輸入方案</a>、<a href=https://www.wugniu.com/>吳語學堂</a>"),
  ('wuu_sh', '上海話', '上海', '#1E90FF', '吳音小字典（上海）', "http://www.wu-chinese.com/minidict/search.php?searchlang=zaonhe&searchkey=%s", "名稱：上海話<br>來源：《上海市區方言志》（1988年版），蔡子文錄入<br>說明：該書記錄的是中派上海話音系（使用者多出生於20世紀40至70年代），與<a href=http://www.wu-chinese.com/minidict/>吳音小字典</a>記錄的音系並不完全相同。"),
  ('wuu_jy', '縉雲話', '縉雲', '#1E90FF', None, None, "更新：2021-07-08<br>名稱：縉雲話<br>來源：由東甌組<u>老虎</u>、<u>林奈安</u>提供"),
  ('wuu_oj_rads', '瑞安東山話', '瑞安東山', '#1E90FF', None, None, "更新：2021-07-08<br>名稱：瑞安東山話<br>來源：由東甌組<u>落橙</u>、<u>老虎</u>提供"),
  ('wuu_oj_yqyc', '樂清樂成話', '樂清樂成', '#1E90FF', None, None, "更新：2021-07-08<br>名稱：樂清樂成話<br>來源：由東甌組<u>落橙</u>、<u>老虎</u>、<u>阿纓</u>提供"),
  ('wuu_oj_wzcd', '溫州城底話', '溫州城底', '#1E90FF', None, None, "更新：2021-07-08<br>名稱：溫州城底話<br>來源：由東甌組<u>落橙</u>、<u>小小溫州人(up)</u>、<u>老虎</u>提供"),
  ('gan_nc', '南昌話', '南昌', '#00ADAD', None, None, "名稱：南昌話<br>來源：網友<u>澀口的茶</u>"),
  ('hak', '客家話綜合口音', '客語', '#008000', '薪典', "https://www.syndict.com/w2p.php?item=hak&word=%s", "更新：2019-04-19<br>名稱：客家話綜合口音<br>來源：<a href=https://github.com/syndict/hakka/>客語輸入法</a>、<a href=https://www.syndict.com/>薪典</a>"),
  ('hak_hl', '客家話海陸腔', '海陸客語', '#008000', '客語萌典', "https://www.moedict.tw/:%s", "名稱：客家話海陸腔<br>來源：<a href=https://www.moedict.tw/>客語萌典</a>"),
  ('hak_sx', '客家話四縣腔', '四縣客語', '#008000', '客語萌典', "https://www.moedict.tw/:%s", "名稱：客家話四縣腔<br>來源：<a href=https://www.moedict.tw/>客語萌典</a>"),
  ('yue_gz', '廣州話', '廣州', '#FFAD00', '粵語審音配詞字庫', "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s", "名稱：廣州話<br>來源：<a href=http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/>粵語審音配詞字庫</a>、<a href=http://www.unicode.org/charts/unihan.html>Unihan</a><br>說明：括號中的爲異讀讀音"),
  ('nan', '閩南語', '閩南', '#FF6600', '臺灣閩南語常用詞辭典', "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s", "更新：2020-05-17<br>名稱：閩南語<br>來源：<a href=https://github.com/tauhu-tw/tauhu-taigi>豆腐台語詞庫</a>、<a href=https://twblg.dict.edu.tw/holodict_new/>臺灣閩南語常用詞辭典</a><br>說明：下標“俗”表示“俗讀音”，“替”表示“替代字”，指的是某個字的讀音其實來自另一個字，比如“人”字的lang5音其實來自“儂”字。有些字會有用斜線分隔的兩個讀音（如“人”字的jin5/lin5），前者爲高雄音（第一優勢腔），後者爲臺北音（第二優勢腔）。"),
  ('nan_pn', '普寧話', '普寧', '#FF6600', None, None, "更新：2021-07-08<br>名稱：普寧話<br>來源：由<u>阿纓</u>提供"),
  ('nan_st', '汕頭話', '汕頭', '#FF6600', None, None, "更新：2021-07-14<br>名稱：汕頭話<br>來源：由<u>Kiattan</u>提供"),
  ('vi', '越南語', '越南', '#DB7093', '漢越辭典摘引', "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s", "名稱：越南語<br>來源：<a href=http://www.vanlangsj.org/hanviet/>漢越辭典摘引</a>"),
  ('ko_okm', '中世紀朝鮮語', '中世朝鮮', '#BA55D3', None, None, "名稱：中世紀朝鮮語<br>來源：<a href=https://github.com/nk2028/sino-korean-readings>韓國漢字音歷史層次研究</a>"),
  ('ko_kor', '朝鮮語', '朝鮮', '#BA55D3', 'Naver漢字辭典', "http://hanja.naver.com/hanja?q=%s", "名稱：朝鮮語、韓語<br>來源：<a href=http://hanja.naver.com/>Naver漢字辭典</a><br>說明：括號前的讀音爲漢字本來的讀音，也是朝鮮的標準音，而括號內的讀音爲韓國應用<a href=http://zh.wikipedia.org/wiki/%E9%A0%AD%E9%9F%B3%E6%B3%95%E5%89%87>頭音法則</a>之後的讀音。"),
  ('ja_go', '日語吳音', '日語吳音', '#FF0000', None, None, "名稱：日語吳音<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。<br>　　日語每個漢字一般具有吳音、漢音兩種讀音，個別漢字還具有唐音和慣用音。這四種讀音分別用“日吳”、“日漢”、“日唐”、“日慣”表示。另外，對於一些生僻字，《漢字源》中沒有註明讀音的種類，也沒有提供“歷史假名遣”寫法，這一類“其他”讀音用“日他”表示。<br>　　 有的讀音會帶有括號，括號前的讀音爲“現代假名遣”寫法，括號內的讀音爲對應的“歷史假名遣”寫法。<br>　　 讀音的顏色和粗細代表讀音的常用程度。<b>黑色粗體</b>爲“常用漢字表”內的讀音；黑色細體爲《漢字源》中列第一位，但不在“常用漢字表”中的讀音；<span class=dim>灰色細體</span>爲既不在“常用漢字表”中，也不在《漢字源》中列第一位的讀音。"),
  ('ja_kan', '日語漢音', '日語漢音', '#FF0000', None, None, "名稱：日語漢音<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。<br>　　日語每個漢字一般具有吳音、漢音兩種讀音，個別漢字還具有唐音和慣用音。這四種讀音分別用“日吳”、“日漢”、“日唐”、“日慣”表示。另外，對於一些生僻字，《漢字源》中沒有註明讀音的種類，也沒有提供“歷史假名遣”寫法，這一類“其他”讀音用“日他”表示。<br>　　 有的讀音會帶有括號，括號前的讀音爲“現代假名遣”寫法，括號內的讀音爲對應的“歷史假名遣”寫法。<br>　　 讀音的顏色和粗細代表讀音的常用程度。<b>黑色粗體</b>爲“常用漢字表”內的讀音；黑色細體爲《漢字源》中列第一位，但不在“常用漢字表”中的讀音；<span class=dim>灰色細體</span>爲既不在“常用漢字表”中，也不在《漢字源》中列第一位的讀音。"),
  ('ja_tou', '日語唐音', '日語唐音', '#FF0000', None, None, "名稱：日語<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。<br>　　日語每個漢字一般具有吳音、漢音兩種讀音，個別漢字還具有唐音和慣用音。這四種讀音分別用“日吳”、“日漢”、“日唐”、“日慣”表示。另外，對於一些生僻字，《漢字源》中沒有註明讀音的種類，也沒有提供“歷史假名遣”寫法，這一類“其他”讀音用“日他”表示。<br>　　 有的讀音會帶有括號，括號前的讀音爲“現代假名遣”寫法，括號內的讀音爲對應的“歷史假名遣”寫法。<br>　　 讀音的顏色和粗細代表讀音的常用程度。<b>黑色粗體</b>爲“常用漢字表”內的讀音；黑色細體爲《漢字源》中列第一位，但不在“常用漢字表”中的讀音；<span class=dim>灰色細體</span>爲既不在“常用漢字表”中，也不在《漢字源》中列第一位的讀音。"),
  ('ja_kwan', '日語慣用音', '日語慣用', '#FF0000', None, None, None),
  ('ja_other', '日語其他讀音', '日語其他', '#FF0000', None, None, None),
  ('bh', '總筆畫數', '總筆畫數', '#808080', None, None, None),
  ('bs', '部首餘筆', '部首餘筆', '#808080', None, None, None),
  ('va', '異體字', '異體字', '#808080', None, None, None),
  ('fl', '分類', '分類', '#808080', None, None, None),
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
  row["ltc_mc"] = row.pop("mc")
  row["wuu_sh"] = row.pop("sh")
  row["yue_gz"] = row.pop("ct")
  row["nan"] = row.pop("mn")
  row["vi"] = row.pop("vn")
  row["ko_kor"] = row.pop("kr")
  row["ja_go"] = row.pop("jp_go")
  row["ja_kan"] = row.pop("jp_kan")
  row["ja_tou"] = row.pop("jp_tou")
  row["ja_kwan"] = row.pop("jp_kwan")
  row["ja_other"] = row.pop("jp_other")
  unicodes[i] = row
conn.close()
logging.info("讀取數據庫 %.2f" % timeit())

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
logging.info("處理兼容字 %.2f" % timeit())

#fl
d.clear()
for line in open("/usr/share/unicode/Unihan_DictionaryIndices.txt"):
  if not line.startswith("U"): continue
  han, typ, val = line.strip().split("\t", 2)
  han = hex2chr(han)
  if typ == "kIRGKangXi" and val.endswith("0"):
    d[han].append(typ)
for line in open("/usr/share/unicode/Unihan_OtherMappings.txt"):
  if not line.startswith("U"): continue
  han, typ, val = line.strip().split("\t", 2)
  han = hex2chr(han)
  if typ == "kTGH":
    order = int(val.split(":")[1])
    if order <= 3500: level = 1
    elif order <= 6500: level = 2
    else: level = 3    
    d[han].append("%s%d"%(typ, level))
  elif typ in ("kGB0", "kBigFive"):
    d[han].append(typ)
for line in open("/usr/share/unicode/Unihan_DictionaryLikeData.txt"):
  if not line.startswith("U"): continue
  han, typ, val = line.strip().split("\t", 2)
  if typ == "kHKGlyph":
    han = hex2chr(han)
    d[han].append(typ)
for line in open("/usr/share/unicode/Unihan_IRGSources.txt"):
  if not line.startswith("U"): continue
  han, typ, val = line.strip().split("\t", 2)
  if typ == "kIRG_TSource" and val.startswith("T1-"):
    han = hex2chr(han)
    d[han].append("T1")
for line in open("方言調查字表"):
  han = line.strip()
  if han:
    d[han].append("FD")
update("fl", d)
logging.info("漢字分類 %.2f" % timeit())

#mc
d.clear()
for line in open("zyenpheng.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) < 2: continue
  hz, py = fs[:2]
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("ltc_mc", d)

pq = dict()
for line in open("PrengQim.txt"):
    line = line.strip()
    fs = line.split(" ")
    pq[fs[0]] = fs[1].replace("'", "0")
dzih = defaultdict(list)
for line in open("Dzih.txt"):
  line = line.strip()
  fs = line.split(" ")
  dzih[fs[0]].append(pq[fs[1]])
for hz in unicodes.keys():
  if "ltc_mc" in unicodes[hz]:
    py = unicodes[hz]["ltc_mc"]
    if py:
      if hz in dzih:
        pys = [py if py in dzih[hz] else "|%s|" % py for py in py.split(",")]
        for py in dzih[hz]:
          if py not in pys:
            pys.append(py)
        unicodes[hz]["ltc_mc"] = ",".join(pys)
      else:
        unicodes[hz]["ltc_mc"] = "|%s|"%(py.replace(",", "|,|"))
logging.info("處理廣韻 %.2f" % timeit())

#yt
import yt
d.clear()
d = yt.get_dict()
update("ltc_yt", d)
logging.info("處理韻圖 %.2f" % timeit())

#sg
#https://github.com/BYVoid/ytenx/blob/master/ytenx/sync/dciangx/DrienghTriang.txt
d.clear()
for line in open("DrienghTriang.txt"):
  line = line.strip()
  if line.startswith('#'): continue
  fs = line.split(' ')
  hz = fs[0]
  py = fs[12]
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("och_sg", d)
logging.info("處理上古音 %.2f" % timeit())

#ba
#http://ocbaxtersagart.lsait.lsa.umich.edu/BaxterSagartOC2015-10-13.xlsx
d.clear()
for line in open("BaxterSagartOC2015-10-13.csv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz = fs[0]
  py = fs[4]
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("och_ba", d)
logging.info("處理白沙 %.2f" % timeit())

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
sms = getIPA("CjengMuxNgixQim.txt")
yms = getIPA("YonhMuxNgixQim.txt")
sds = {'去': '5', '入平': '2', '入去': '5', '入上': '3', '上': '3','陽平': '2', '陰平': '1'}

for line in open("TriungNgyanQimYonh.txt"):
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
update("ltc_zy", d)
logging.info("處理中原音韻 %.2f" % timeit())

#yzll
d.clear()
tones = {"1": "¹³", "2": "³³", "3":"⁵⁵", "5": "²⁴", "6":"³²⁴"}
for line in open("永州官話同音字表.tsv"):
  line = line.strip("\n")
  fs = line.split("\t")
  hz,jt,py,bz = fs
  if len(hz)!=1: continue
  sd = py[-1].replace("5","6").replace("4","5")
  py = py[:-1]
  py = py.replace("w","u").replace("uu", "u")
  py = re.sub("^(ts|tsh|s|z)i", "\\1ɿ", py)
  py = re.sub("^y(?=[^u])", "i", py).replace("ii","i")
  py = re.sub("^(c|ch|sh|zh)u", "\\1yu", py)
  py = py.replace("iu", "iou").replace("ui", "uei").replace("yun", "yn").replace("un", "uen")
  ipa = py.replace("ou", "əu").replace("ao", "au").replace("ang", "ã").replace("an", "ẽ").replace("yu", "y")
  ipa = re.sub("^h", "x", ipa).replace("gh", "ɣ").replace("sh", "ɕ").replace("zh", "ʑ").replace("h", "ʰ")\
      .replace("ts", "ʦ").replace("c", "ʨ").replace("ng", "ŋ")
  ipa = ipa + tones[sd] + sd
  if bz:
    ipa += "`%s`"%bz
  d[hz].append(ipa)
  if jt != hz and ipa not in d[jt]:
    d[jt].append(ipa)
update("cmn_xn_yzll", d)
logging.info("處理永州零陵話 %.2f" % timeit())

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
update("cmn_hy_tt_nt", d)
logging.info("處理南通話 %.2f" % timeit())

#tr
#http://taerv.nguyoeh.com/
d.clear()
trsm = {'g': 'k', 'd': 't', '': '', 'sh': 'ʂ', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 'r': 'ʐ', 'zh': 'tʂ', 't': 'tʰ', 'v': 'v', 'ng': 'ŋ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'ch': 'tʂʰ', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ'}
trym = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'i', 'r': 'ʅ', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iəu', 'u': 'u', 'v': 'v', 'en': 'əŋ', 'a': 'a', 'on': 'ɔŋ', 'ei': 'əi', 'an': 'aŋ', 'oh': 'ɔʔ', 'i': 'j', 'ien': 'iəŋ', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'uei': 'uəi', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'uʔ', 'aen': 'ɛ̃', 'er': 'ɚ', 'eu': 'əu', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iuʔ', 'yen': 'yəŋ', 'ian': 'iaŋ', 'iun': 'iũ', 'un': 'ũ', 'o': 'ɔ', 'uan': 'uaŋ', 'ua': 'ua', 'uen': 'uəŋ', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya'}
trsd = {'0':'','1':'²¹','2':'³⁵','3':'²¹³', '5':'⁴⁴', '6':'²²','7':'⁴', '8':'³⁵'}
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
  py = trsm[fs[3]]+trym[fs[4]]+trsd[fs[5]]+fs[5]
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
update("cmn_hy_tt_tr", d)
logging.info("處理泰如話 %.2f" % timeit())

#xh
d.clear()
xhsd = {'1':'³²⁴', "2":'³⁵',"3":"²¹³", "5":"⁵³","6":"²¹","7":"⁴","8":"⁵"}
for line in open("興化同音字表.tsv"):
  line = line.strip()
  if line.startswith("#"):
    ym = line[1:]
  else:
    fs = line.split("\t")
    sm = fs[0].replace("ø", "")
    for sd,hzs,n in re.findall("［(\d)］(.*?)((?=［)|$)", fs[1]):
      py = sm + ym + xhsd[sd]+sd
      hzs = re.findall("(.)\d?([+-=*/?]?)\d?(\{.*?\})?", hzs)
      for hz, c, m in hzs:
        m = m.strip("{}")
        p = ""
        if c and c in '-+=*?':
          if c == '-':
            p = "白"
          elif c == '+':
            p = "又"
          elif c == '=':
            p = "文"
          elif c == '*':
            p = "俗"
          elif c == '/':
            p = "书"
          elif c == '?':
            p = "待考"
        if p and m:
          p = p + "：" + m
        else:
          p = p + m
        if p:
          p = "`%s`" % p
        p = py + p
        if p not in d[hz]:
          if c == '-':
            d[hz].insert(0, p)
          else:
            d[hz].append(p)
update("cmn_hy_tt_xh", d)
logging.info("處理興化話 %.2f" % timeit())

#ic
#https://github.com/osfans/xu/blob/master/docs/xu.csv
d.clear()
icsm = {'g': 'k', 'd': 't', '': '', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 't': 'tʰ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ', 'ng': 'ŋ'}
icym = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'i', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iəu', 'u': 'u', 'v': 'v', 'en': 'ən', 'a': 'a', 'on': 'ɔŋ', 'an': 'ã', 'oh': 'ɔʔ', 'i': 'j', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'ui': 'ui', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'uʔ', 'aen': 'ɛ̃', 'er': 'ɚ', 'eu': 'əu', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iuʔ', 'yen': 'yn', 'ian': 'iã', 'iun': 'iũ', 'un': 'ũ', 'o': 'ɔ', 'uan': 'uã', 'ua': 'ua', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya', '': ''}
icsd = {'1':'³¹', "2":'²¹³',"3":"⁵⁵", "5":"³⁵","7":"⁵"}
for line in open("鹽城同音字表.tsv"):
  line = line.strip()
  if not line: continue
  fs = line.split('\t')
  py,hzs = fs
  sm = re.findall("^[^aeiouvy]?g?", py)[0]
  sd = py[-1]
  if sd not in "12357": sd = ""
  ym = py[len(sm):len(py)-len(sd)]
  py = icsm[sm]+icym[ym]+icsd.get(sd,"")+sd
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
      elif c == '*':
        p = "俗"
      elif c == '?':
        p = "待考"
    p = p + m
    if p:
      p = "`%s`" % p
    p = py + p
    if p not in d[hz]:
      if c == '-':
        d[hz].insert(0, p)
      else:
        d[hz].append(p)
update("cmn_hy_hc_ic", d)
logging.info("處理鹽城話 %.2f" % timeit())

#fdgc
d.clear()
tones = {"1":"³¹",'2':"³⁵",'3':"²¹³","5":"⁵³","7":"⁴⁴"}
for line in open("肥東古城同音字表.tsv"):
  line = line.strip()
  if line.startswith("#"): continue
  ipa,hzs = line.split("\t")
  sd = ipa[-1]
  if sd.isdigit():
    ipa = ipa[:-1] + tones[sd] + sd
  else:
    sd = ""
  hzs = re.findall("(.)(\d)?\*?(\(.*?\))?", hzs)
  for hz,index,m in hzs:
    p = ipa
    if m:
      m = m.strip("()")
      p = "%s`%s`" % (p, m)
    if index == "1":
      d[hz].insert(0, p)
    else:
      d[hz].append(p)    
update("cmn_hy_hc_fdgc", d)
logging.info("處理肥東古城話 %.2f" % timeit())

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
for line in open("通東談話 字表.tsv"):
  line = line.strip('\n')
  fs = [i.strip(' "') for i in line.split('\t')]
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
update("wuu_td", d)
logging.info("處理通東話 %.2f" % timeit())

#wuu
d.clear()
sms={'dz':'ʣ', 'zh':'ʑ', 'th':'tʰ', 'sh':'ɕ', 'lh':'ʔl', 'ts':'ʦ', 'tsh':'ʦʰ', 'c':'ʨ', 'ph':'pʰ', 'kh':'kʰ', 'nh':'ʔn', 'j':'ʥ', 'ng':'ŋ', 'gh':'ɦ', 'ngh':'ʔŋ', 'ch':'ʨʰ', 'mh':'ʔm'}
yms={'ae': 'æ', 'aeh': 'æʔ', 'ai': 'ai', 'an': 'aŋ', 'au': 'au', 'ah': 'aʔ', 'a': 'ɑ', 'ee': 'e', 'ei': 'ei', 'eeh': 'eʔ', 'en': 'əŋ', 'eu': 'əu', 'eh': 'əʔ', 'iae': 'iæ', 'iaeh': 'iæʔ', 'ian': 'iaŋ', 'iau': 'iau', 'iah': 'iaʔ', 'ia': 'iɑ', 'ie': 'ie', 'ieh': 'ieʔ', 'ieu': 'iəu', 'i': 'i', 'ih': 'iɪʔ', 'in': 'iŋ', 'ion': 'ioŋ', 'ioh': 'ioʔ', 'iaon': 'iɔŋ', 'iaoh': 'iɔʔ', 'ieon': 'iʌŋ', 'ieoh': 'iʌʔ', 'm': 'm', 'n': 'n', 'ng': 'ŋ', 'on': 'oŋ', 'o': 'o', 'oe': 'ø', 'ou': 'ou', 'oeh': 'øʔ', 'oh': 'oʔ', 'aon': 'ɔŋ', 'aoh': 'ɔʔ', 'y': 'ɿ', 'uae': 'uæ', 'uaeh': 'uæʔ', 'uan': 'uaŋ', 'uah': 'uaʔ', 'ua': 'uɑ', 'uei': 'uei', 'uen': 'uəŋ', 'ueh': 'uəʔ', 'uon': 'uoŋ', 'uo': 'uo', 'uoe': 'uø', 'uoeh': 'uøʔ', 'uoh': 'uoʔ', 'uaon': 'uɔŋ', 'uaoh': 'uɔʔ', 'u': 'u', 'eon': 'ʌŋ', 'eoh': 'ʌʔ', 'iu': 'y', 'iuin': 'yɪŋ', 'iuih': 'yɪʔ', 'io': 'yo', 'ioe': 'yø', 'ioeh': 'yøʔ', '':''}
for line in open("標準吳語.csv"):
  line = line.strip('\n')
  fs = [i.strip(' "') for i in line.split('\t')]
  hz, jt, js = fs[1], fs[2], fs[-1]
  if len(hz) != 1: continue
  sm,ym,sd=fs[3:6]
  if sd:
    sd = int(sd) * 2 - 1
    if sm in ("b","d","dz","j","g","m","n","ng","v","l","z","zh","gh"): sd += 1
  sm = sms.get(sm, sm)
  ym = yms.get(ym, ym)
  py = sm+ym+str(sd)
  if js: py += "`%s`"%js
  if py not in d[hz]:
    d[hz].append(py)
  if len(jt) == 1:
    if py not in d[jt]:
      d[jt].append(py)
update("wuu", d)
logging.info("處理標準吳語 %.2f" % timeit())

#sz
#https://github.com/NGLI/rime-wugniu_soutseu/blob/master/wugniu_soutseu.dict.yaml
def sz2ipa(s):
  tone = s[-1]
  if tone.isdigit():
    s = s[:-1]
  else:
    tone = ""
  s = re.sub("y$", "ɿ", s)
  s = re.sub("^yu$", "ghiu", s)
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
  tones = {"1": "⁴⁴", "2": "²²³", "3":"⁵¹", "5": "⁵²³", "6":"²³¹", "7":"⁴³", "8":"²³"}
  s = s + tones.get(tone, "") + tone
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
update("wuu_sz", d)
logging.info("處理蘇州話 %.2f" % timeit())

#cmn
d.clear()
for line in open("cmn.txt"):
  line = line.strip()
  if not line or line.startswith("#"):
    continue
  hzs,py = line.split("\t")[:2]
  for hz in hzs:
    d[hz] = py.split(",")
update("cmn",d)
logging.info("處理普通話 %.2f" % timeit())

#gz
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
update("yue_gz", d)
logging.info("處理廣州話 %.2f" % timeit())

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
  if "wuu_sh" in unicodes[i]:
    sh = unicodes[i]["wuu_sh"]
    if sh:
      fs = sh.split(",")
      fs = map(sh2ipa, fs)
      sh = ",".join(fs)
      unicodes[i]["wuu_sh"] = sh
logging.info("處理上海話 %.2f" % timeit())

#ra
d.clear()
for line in open("方言调查字表-瑞安東山.csv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,py,yb,zs = fs[:5]
  if not py: continue
  sd = py[-1] if py[-1].isdigit() else ""
  if len(hz) == 1:
    yb = yb.strip("0").replace('˩','¹')\
                      .replace('˨','²')\
                      .replace('˧','³')\
                      .replace('˦','⁴')\
                      .replace('˥','⁵')
    js = yb + sd + ("`%s`"% zs if zs else "")
    if js not in d[hz]:
      d[hz].append(js)
    if jt != hz and len(jt) == 1:
      if js not in d[jt]:
        d[jt].append(js)
update("wuu_oj_rads", d)
logging.info("處理瑞安東山話 %.2f" % timeit())

#yqyc
d.clear()
for line in open("乐清乐成字表.csv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,py,yb = fs
  if not py: continue
  sd = py[-1] if py[-1].isdigit() else ""
  if len(hz) == 1:
    yb = yb.strip("0").replace('˩','¹')\
                      .replace('˨','²')\
                      .replace('˧','³')\
                      .replace('˦','⁴')\
                      .replace('˥','⁵')
    js = yb + sd
    if js not in d[hz]:
      d[hz].append(js)
    if jt != hz and len(jt) == 1 and js not in d[jt]:
      d[jt].append(js)
update("wuu_oj_yqyc", d)
logging.info("處理樂清樂成話 %.2f" % timeit())

#wzcd
d.clear()
for line in open("温州城底字表.csv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,py,yb = fs
  if not py: continue
  sd = py[-1] if py[-1].isdigit() else ""
  if len(hz) == 1:
    yb = yb.strip("0").replace('˩','¹')\
                      .replace('˨','²')\
                      .replace('˧','³')\
                      .replace('˦','⁴')\
                      .replace('˥','⁵')
    js = yb + sd
    if js not in d[hz]:
      d[hz].append(js)
    if jt != hz and len(jt) == 1 and js not in d[jt]:
      d[jt].append(js)
update("wuu_oj_wzcd", d)
logging.info("處理溫州城底話 %.2f" % timeit())

#jy
tones = {'阳入':8,'阴上':3,'阳平':2,'阴入':7,'阳去':6,'阴平':1,'阴去':5,'阳上':4}
d.clear()
for line in open("缙云字表.csv"):
  fs = [i.strip('" ') for i in line.strip('\n').replace("ロ","□").split('\t')]
  hz,jt,sd,zs,yb = fs[0],fs[1],fs[4],fs[5],fs[7]
  if not py: continue
  if len(hz) == 1:
    sd = str(tones[sd])
    yb = yb.strip("0").replace('˩','¹')\
                      .replace('˨','²')\
                      .replace('˧','³')\
                      .replace('˦','⁴')\
                      .replace('˥','⁵')
    js = yb + sd + ("`%s`"% zs if zs and not zs.isdigit() else "")
    if js not in d[hz]:
      d[hz].append(js)
    if jt != hz and len(jt) == 1:
      if js not in d[jt]:
        d[jt].append(js)
update("wuu_jy", d)
logging.info("處理縉雲話 %.2f" % timeit())

#nan
d.clear()
for line in open("豆腐台語詞庫.csv"):
  fs = line.strip().split(',')
  hz = fs[1]
  py = fs[0]
  if len(hz) == 1:
    if py not in d[hz]:
      d[hz].append(py)
update("nan", d)
for i in unicodes.keys():
  if "nan" in unicodes[i]:
    py = unicodes[i]["nan"]
    if py:
      py = re.sub("\|(.*?)\|", "\\1`白`", py)
      py = re.sub("\*(.*?)\*", "\\1`文`", py)
      py = re.sub("\((.*?)\)", "\\1`俗`", py)
      py = re.sub("\[(.*?)\]", "\\1`替`", py)
      unicodes[i]["nan"] = py
logging.info("處理閩南話 %.2f" % timeit())

#nan_pn
d.clear()
tones = {'˥˦':8,'˥˧':3,'˦˦':2,'˧˨':7,'˧˩˩':6,'˨˨˧':1,'˨˩':5,'˨˩˧':4}
for line in open("普宁字表初稿.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,yb,zs = fs[:3]
  if not yb: continue
  sd = fs[-1]
  sd = "%s"%tones.get(sd, "")
  if len(hz) == 1:
    if hz in "?？": hz = "□"
    yb = yb.strip("0").replace('˩','¹')\
                      .replace('˨','²')\
                      .replace('˧','³')\
                      .replace('˦','⁴')\
                      .replace('˥','⁵')
    js = yb + sd + ("`%s`"% zs if zs else "")
    if js not in d[hz]:
      d[hz].append(js)
update("nan_pn", d)
logging.info("處理普寧話 %.2f" % timeit())

#nan_st
d.clear()
tones = [1, 5, 2, 6, 3, 7, 4, 8]
for line in open("方言调查字表 （汕头）.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,py,yb,zs = fs[:4]
  if not yb: continue
  sd = py[-1]
  if sd.isdigit():
    sd = str(tones.index(int(sd)) + 1)
  else:
    sd = ""
  if len(hz) == 1:
    yb = yb.strip("0").replace('˩','¹')\
                      .replace('˨','²')\
                      .replace('˧','³')\
                      .replace('˦','⁴')\
                      .replace('˥','⁵')
    js = yb + sd + ("`%s`"% zs if zs else "")
    if js not in d[hz]:
      d[hz].append(js)
update("nan_st", d)
logging.info("處理汕頭話 %.2f" % timeit())

#hak
#https://github.com/syndict/hakka/blob/master/hakka.dict.yaml
hktones = {"⁴⁴":"1", "³³": "1", "¹¹":"2", "³¹":"3", "¹³":"4", "⁵²":"5", "⁵³":"5", "²¹":"6", "¹":"7", "⁵":"8", "³":"8"}
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
update("hak", d)

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
update("hak_hl", d)
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
update("hak_sx", d)
logging.info("處理客家話 %.2f" % timeit())

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
update("gan_nc", d)
logging.info("處理南昌話 %.2f" % timeit())

#ko
#https://github.com/nk2028/sino-korean-readings/blob/main/woosun-sin.csv
d.clear()
for line in open("woosun-sin.csv"):
  fs = line.strip().split(',')
  hz = fs[0]
  if len(hz) == 1:
    py = "".join(fs[1:4])
    if py not in d[hz]:
      d[hz].append(py)
update("ko_okm", d)
logging.info("處理中世朝鮮 %.2f" % timeit())

#patch
patch = ruamel.yaml.load(open("patch.yaml"), Loader=ruamel.yaml.Loader)
for lang in patch:
  for hz in patch[lang]:
    for i in hz:
      if i not in unicodes:
        unicodes[i]["hz"] = i
      unicodes[i][lang] = patch[lang][hz]
logging.info("修正漢字音 %.2f" % timeit())

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
logging.info("處理總畫數 %.2f" % timeit())

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
logging.info("部首檢字法 %.2f" % timeit())

#variant
variants = variant.get()
for i in list(unicodes.keys()):
  d = unicodes[i]
  if d.get("cmn"):
    d["va"] = " ".join(variants.get(i, i))
logging.info("處理異體字 %.2f" % timeit())

#stat
counts = dict()
for lang in KEYS:
    count = 0
    for i in unicodes:
        if unicodes[i].get(lang, None): count+=1
        elif lang == "ja_tou": #所有日語音
            if unicodes[i].get("ja_go", None)\
                or unicodes[i].get("ja_kan", None)\
                or unicodes[i].get("ja_kwan", None)\
                or unicodes[i].get("ja_other", None):
                count+=1
    counts[lang] = count
ZHEADS[6] = list(ZHEADS[6])
for i,lang in enumerate(KEYS):
    desc = ZHEADS[6][i]
    ZHEADS[6][i]="收字：%d個<br>%s"%(counts[lang], desc if desc else "")

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
  if not d.get("cmn"):
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
logging.info("保存數據庫 %.2f" % timeit())
logging.info("處理總時間 %.2f" % (time() - start0))
logging.info("音典總字數 %d" % len(unicodes))
