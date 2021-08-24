#!/usr/bin/env python3

import sqlite3, re, json, os
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
  ('hz', '漢字', '漢字', '#9D261D', '字海', 'http://yedict.com/zscontent.asp?uni=%2$s',"版本：V4.9 (2021-08-27)<br>說明：<br>　　本程序源自“<a href=https://github.com/MaigoAkisame/MCPDict>漢字古今中外讀音查詢</a>”，收錄了更多語言、更多讀音，錯誤也更多，可去<a href=https://github.com/osfans/MCPDict>Github</a>和<a href=mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D-hNzAQCgZQL-uIlhFrxWJ56umCexsmBi>QQ群</a>提出意見与建議。<br>　　本程序將多種語言的漢字讀音集成於本地數據庫，默認用國際音標注音，可用於比較各語言讀音的異同，也能給學習本程序所收的語言提供有限的幫助。<br>　　本程序收錄了Unicode13全部漢字（不含偏旁部首及兼容區漢字）、〇（同“星”或“零”）、□（有音無字、本字不明）。支持形音義等多種查詢方式，可輸入𰻞（漢字）、30EDE（Unicode編碼）、biang2（普通話拼音，音節末尾的“?”可匹配任何聲調）、43（總筆畫數）、辵39（部首餘筆），均可查到“𰻞”字，也可以切到兩分、五筆畫等輸入漢字字形的編碼進行查詢，還可以切到康熙字典、漢語大字典等通過釋義中出現的詞語搜索到相應的漢字。<br>",None),
  ('lf', '兩分', '兩分', '#1E90FF', None, None, "名稱：兩分<br>來源：<a href=http://yedict.com/zslf.htm>兩分查字</a><br>說明：可以輸入“雲龍”或“yunlong”查到“𱁬”",None),
  ('wbh', '五筆畫', '五筆畫', '#1E90FF', None, None, "名稱：五筆畫<br>來源：<a href=https://github.com/CNMan/UnicodeCJK-WuBi>Github</a><br>說明：12345分別代表橫豎撇捺折，可以輸入“12345”查到“札”。也可以輸入五筆字型的編碼查詢漢字，比如輸入“snn”查詢“扎”。",None),
  ('kx', '康熙字典', '康熙', '#1E90FF', None, None, "名稱：康熙字典<br>來源：<a href=https://github.com/7468696e6b/kangxiDictText/>GitHub</a>",None),
  ('hd', '漢語大字典', '漢大', '#1E90FF', None, None, "名稱：漢語大字典<br>來源：<a href=https://github.com/zi-phoenicia/hydzd/>GitHub</a>",None),
  ('och_sg', '上古（鄭張尚芳）', '鄭張', '#9A339F', '韻典網（上古音系）', 'https://ytenx.org/dciangx/dzih/%s',"名稱：上古音鄭張尚芳擬音<br>來源：<a href=https://ytenx.org/dciangx/>韻典網</a><br>說明：在擬音後面的括號中注明了《上古音系》中的反切、聲符、韻部。",None),
  ('och_ba', '上古（白一平沙加爾）', '白沙2015', '#9A339F', None, None, "更新：2015-10-13<br>名稱：上古音白一平沙加爾2015年擬音<br>來源：<a href=http://ocbaxtersagart.lsait.lsa.umich.edu/>http://ocbaxtersagart.lsait.lsa.umich.edu/</a>",None),
  ('ltc_mc', '廣韻', '廣韻', '#9A339F', '韻典網', "http://ytenx.org/zim?kyonh=1&dzih=%s", "名稱：廣韻<br>來源：<a href=https://ytenx.org/kyonh/>韻典網</a><br>說明：括號中注明了《廣韻》中的聲母、韻攝、韻目、等、呼、聲調，以及《平水韻》中的韻部。對於“支脂祭真仙宵侵鹽”八個有重紐的韻，僅在聲母爲脣牙喉音時標註A、B類。廣韻韻目中缺少冬系上聲、臻系上聲、臻系去聲和痕系入聲，“韻典網”上把它們補全了，分別作“湩”、“𧤛”、“櫬”、“麧”。由於“𧤛”字不易顯示，故以同韻目的“齔”字代替。"," 1 1 平 ꜀, 3 2 上 ꜂, 5 3 去 ꜄, 7 4 入 ꜆"),
  ('ltc_yt', '韻圖', '韻圖', '#9A339F', None, None, "名稱：韻圖擬音<br>來源：QQ共享文檔<a href=https://docs.qq.com/sheet/DYk9aeldWYXpLZENj>韻圖音系同音字表</a>"," 1 1 平 ꜀, 3 2 上 ꜂, 5 3 去 ꜄, 7 4 入 ꜆"),
  ('ltc_zy', '中原音韻', '中原音韻', '#9A339F', '韻典網（中原音韻）', 'https://ytenx.org/trngyan/dzih/%s', "名稱：中原音韻擬音<br>來源：<a href=https://ytenx.org/trngyan/>韻典網</a><br>說明：平聲分陰陽，入聲派三聲。下標“入”表示古入聲字","33 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,214 3 2 上 ꜂,51 5 3 去 ꜄"),
  ('cmn', '普通話', '普通話', '#FF00FF', '漢典網', "http://www.zdic.net/hans/%s", "更新：2021-08-23<br>名稱：普通話、國語<br>來源：漢語大字典、<a href=https://www.zdic.net/>漢典</a>、<a href=http://yedict.com/>字海</a>、<a href=https://www.moedict.tw/>萌典</a><br>說明：灰色讀音來自<a href=https://www.moedict.tw/>萌典</a>。可使用漢語拼音、注音符號查詢漢字。在輸入漢語拼音時，可以用數字1、2、3、4代表聲調，放在音節末尾，“?”可代表任何聲調；字母ü可用v代替。例如查詢普通話讀lüè的字時可輸入lve4。在輸入注音符號時，聲調一般放在音節末尾，但表示輕聲的點（˙）既可以放在音節開頭，也可以放在音節末尾，例如“的”字的讀音可拼作“˙ㄉㄜ”或“ㄉㄜ˙”。","55 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,215 3 2 上 ꜂,51 5 3 去 ꜄"),
  ('cmn_xn_yzll', '永州零陵話', '零陵', '#C600FF', None, None, "更新：2021-07-15<br>名稱：永州零陵話<br>來源：<a href=https://github.com/shinzoqchiuq/yongzhou-homophony-syllabary>永州官話同音字表</a>、《湖南省志·方言志》<br>說明：本同音字表描寫的是屬於山北片區的永州零陵區口音，整理自《湖南省志·方言志》，有脣齒擦音 /f/，無全濁塞擦音 /dz/ 和 /dʒ/，「彎」「汪」不同韻，區分陰去和陽去","13 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,55 3 2 上 ꜂,24 5 3a 陰去 ꜄,324 6 3b 陰去 ꜅"),
  ('cmn_hy_hc_fdgc', '肥東古城話', '肥東古城', '#0000FF', None, None, "更新：2021-07-12<br>名稱：肥東古城話<br>來源：安徽肥東古城方言同音字匯","31 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,213 3 2 上 ꜂,,53 5 3 去 ꜄,,44 7 4 入 ꜆"),
  ('cmn_hy_hc_lj', '南京話', '南京', '#0000FF', None,None, "更新：2021-08-04<br>名稱：南京話<br>來源：<a href=https://github.com/uliloewi/lang2jin1>南京話拼音输入法</a>", "31 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,212 3 2 上 ꜂,44 5 3 去 ꜄,5 7 4 入 ꜆"),
  ('cmn_hy_hc_yz', '揚州話', '揚州', '#0000FF', None,None, "更新：2021-08-26<br>名稱：揚州話<br>來源：<u>慕禃</u>", "21 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,42 3 2 上 ꜂,,55 5 3 去 ꜄,,4 7 4 入 ꜆,2 8 4b 陽入 ꜇"),
  ('cmn_hy_hc_bf', '濱阜方言', '濱阜', '#0000FF', None, None, "版本：V3.0 (2021-08-12)<br>名稱：濱阜方言<br>來源：<u>清竮塵</u>整理自《濱海縣志》","52 1 1a 陰平 ꜀,25 2 1b 陽平 ꜁,211 3 2 上 ꜂,,334 5 3 去 ꜄,,4 7 4 入 ꜆"),
  ('cmn_hy_hc_ic', '鹽城話', '鹽城', '#0000FF', '淮語字典', "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s", "更新：2021-08-26<br>名稱：鹽城話<br>來源：<a href=http://huae.nguyoeh.com/>類音字彙</a>、鹽城縣志、鹽城方言研究（步鳳）等","31 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,55 3 2 上 ꜂,,35 5 3 去 ꜄,,5 7 4 入 ꜆"),
  ('cmn_hy_tt_xh', '興化話', '興化', '#0000FF', None, None, "更新：2021-07-15<br>名稱：興化話<br>來源：江蘇興化方言音系、興化方言詞典","324 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,213 3 2 上 ꜂,,53 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"),
  ('cmn_hy_tt_tr', '泰如方言', '泰如', '#0000FF', '泰如小字典', "http://taerv.nguyoeh.com/query.php?table=泰如字典&簡體=%s", "更新：2021-08-01<br>名稱：泰如方言<br>來源：<a href=http://taerv.nguyoeh.com/>泰如小字典</a>","21 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,213 3 2 上 ꜂,,44 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,35 8 4b 陽入 ꜇"),
  ('cmn_hy_tt_nt', '南通話', '南通', '#0000FF', '南通方言網', "http://nantonghua.net/search/index.php?hanzi=%s", "更新：2018-01-08<br>名稱：南通話<br>來源：<a href=http://nantonghua.net/archives/5127/南通话字音查询/>南通方言網</a>","21 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,55 3 2 上 ꜂,,42 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,42 7 4a 陰入 ꜆,55 8 4b 陽入 ꜇"),
  ('wuu_td', '通東談話', '通東', '#7C00FF', None, None, "更新：2021-08-26<br>名稱：通東談話<br>來源：<u>正心修身</u>","44 1 1a 陰平 ꜀,113 2 1b 陽平 ꜁,51 3 2a 陰上 ꜂,231 4 2b 陽上 ꜃,334 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,34 7 4a 陰入 ꜆,23 8a 4b 陽入 ꜇,5 8b 4c 次濁入 ꜁"),
  ('wuu_sz', '蘇州話', '蘇州', '#1E90FF', '吳語學堂（蘇州）', "https://www.wugniu.com/search?table=suzhou_zi&char=%s", "名稱：蘇州話<br>來源：<a href=https://github.com/NGLI/rime-wugniu_soutseu>蘇州吳語拼音輸入方案</a>、<a href=https://www.wugniu.com/>吳語學堂</a>、<u>樛木</u><br>說明：灰色的爲老派翹舌音，是樛木整理自《一百年前的蘇州話》、《鄉音字類》，加*的漢字表示[ʐ][dʐ]歸屬、陽上陽去歸屬缺乏直接資料，以區分這兩者的蘇州行政區劃內的方吳語爲基準。對於《一百年前的蘇州話》有而《鄉音字類》中沒有的字，因《一百年前的蘇州話》記音不含聲調，則其聲調以今音爲準。","44 1 1a 陰平 ꜀,223 2 1b 陽平 ꜁,51 3 2 上 ꜂,31 4 2b 陽上 ꜃,523 5 3a 陰去 ꜄,231 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,23 8 4b 陽入 ꜇"),
  ('wuu_sh', '上海話', '上海', '#1E90FF', '吳音小字典（上海）', "http://www.wu-chinese.com/minidict/search.php?searchlang=zaonhe&searchkey=%s", "名稱：上海話<br>來源：《上海市區方言志》（1988年版），蔡子文錄入<br>說明：該書記錄的是中派上海話音系（使用者多出生於20世紀40至70年代），與<a href=http://www.wu-chinese.com/minidict/>吳音小字典</a>記錄的音系並不完全相同。","53 1 1 平 ꜀,,,,34 5 3a 陰去 ꜄,23 6 3b 陽去 ꜅,55 7 4a 陰入 ꜆,12 8 4b 陽入 ꜇"),
  ('wuu_tz_bztz', '標準台州話', '標準台州', '#1E90FF', '標準吳語字典', "http://nguyoeh.com/query.php?table=吳語字典&簡體=%s", "更新：2021-07-09<br>名稱：標準吳語<br>來源：<a href=http://nguyoeh.com/>標準吳語字典</a>","53 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,44 3 2a 陰上 ꜂,22 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('wuu_tz_lh', '臨海話', '臨海', '#1E90FF', None, None, "更新：2021-08-25<br>名稱：臨海話<br>來源：<u>落橙</u>","33 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,42 3 2 上 ꜂,,55 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('wuu_tz_xj', '仙居話', '仙居', '#1E90FF', None, None, "更新：2021-08-25<br>名稱：仙居話<br>來源：<u>落橙</u>","334 1 1a 陰平 ꜀,312 2 1b 陽平 ꜁,423 3 2 上 ꜂,,55 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('wuu_sl_sc', '遂昌話', '遂昌', '#1E90FF', None, None, "更新：2021-08-25<br>名稱：遂昌話<br>來源：<u>落橙</u>、<u>阿纓</u>","55 1 1a 陰平 ꜀,221 2 1b 陽平 ꜁,52 3 2a 陰上 ꜂,13 4 2b 陽上 ꜃,334 5 3a 陰去 ꜄,212 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,23 8 4b 陽入 ꜇"),
  ('wuu_sl_yh', '雲和話', '雲和', '#1E90FF', None, None, "更新：2021-08-17<br>名稱：雲和話<br>來源：<u>落橙</u>、<u>阿纓</u>","324 1 1a 陰平 ꜀,423 2 1b 陽平 ꜁,53 3 2a 陰上 ꜂,21 4 2b 陽上 ꜃,55 5 3a 陰去 ꜄,223 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,24 8 4b 陽入 ꜇"),
  ('wuu_sl_tsly', '泰順羅陽話', '泰順羅陽', '#1E90FF', None, None, "更新：2021-08-13<br>名稱：泰順羅陽話<br>來源：<u>落橙</u>、<u>阿纓</u>","224 1 1a 陰平 ꜀,42 2 1b 陽平 ꜁,51 3 2a 陰上 ꜂,21 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,11 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,1 8 4b 陽入 ꜇,33 0 0 小稱 0"),
  ('wuu_sl_sy', '松陽話', '松陽', '#1E90FF', None, None, "更新：2021-08-20<br>名稱：松陽話<br>來源：<u>落橙</u>","51 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,214 3 2a 陰上 ꜂,22 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('wuu_oj_wzcd', '溫州話', '溫州', '#1E90FF', None, None, "更新：2021-08-13<br>名稱：溫州話<br>來源：由東甌組<u>落橙</u>、<u>小小溫州人(up)</u>、<u>老虎</u>提供","33 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2 上 ꜂,,52 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,213 7 4 入 ꜆"),
  ('wuu_oj_rads', '瑞安東山話', '瑞安東山', '#1E90FF', None, None, "更新：2021-08-26<br>名稱：瑞安東山話<br>來源：由東甌組<u>落橙</u>、<u>老虎</u>提供","44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,13 4 2b 陽上 ꜃,52 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,322 7 4a 陰入 ꜆,211 8 4b 陽入 ꜇"),
  ('wuu_oj_yqyc', '樂清樂成話', '樂清樂成', '#1E90FF', None, None, "更新：2021-08-25<br>名稱：樂清樂成話<br>來源：由東甌組<u>落橙</u>、<u>老虎</u>、<u>阿纓</u>提供","44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,34 4 2b 陽上 ꜃,52 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,323 7 4a 陰入 ꜆,212 8 4b 陽入 ꜇"),
  ('wuu_oj_cnpm', '蒼南蒲門甌語方言島', '蒼南蒲門', '#1E90FF', None, None, "更新：2021-08-17<br>名稱：蒼南蒲門甌語方言島<br>來源：<u>落橙</u>","44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,45 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,42 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,323 7 4a 陰入 ꜆,212 8 4b 陽入 ꜇"),
  ('wuu_jy', '縉雲話', '縉雲', '#1E90FF', None, None, "更新：2021-08-26<br>名稱：縉雲話<br>來源：由東甌組<u>老虎</u>、<u>林奈安</u>提供","334 1 1a 陰平 ꜀,231 2 1b 陽平 ꜁,53 3 2a 陰上 ꜂,31 4 2b 陽上 ꜃,554 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,423 7 4a 陰入 ꜆,35 8 4b 陽入 ꜇"),
  ('gan_nc', '南昌話', '南昌', '#00ADAD', None, None, "名稱：南昌話<br>來源：<u>澀口的茶</u>","42 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,213 3 2 上 ꜂,,45 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,21 8 4b 陽入 ꜇"),
  ('hak', '客家話綜合口音', '綜合客語', '#008000', '薪典', "https://www.syndict.com/w2p.php?item=hak&word=%s", "更新：2019-04-19<br>名稱：客家話綜合口音<br>來源：<a href=https://github.com/syndict/hakka/>客語輸入法</a>、<a href=https://www.syndict.com/>薪典</a>","44 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,31 3 2a 陰上 ꜂,13 4 2b 陽上 ꜃,53 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,1 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"),
  ('hak_whhb', '五華橫陂客家話', '五華橫陂', '#008000', None, None,"更新：2021-08-12<br>名稱：五華橫陂客家話<br>來源：《廣東五華客家話比較研究》，徐汎平，2010","44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,31 3 2 上 ꜂,,53 5 3 去 ꜄,,1 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"),
  ('hak_whsz', '五華水寨客家話', '五華水寨', '#008000', None, None,"更新：2021-08-12<br>名稱：五華水寨客家話<br>來源：《廣東五華客家話比較研究》，徐汎平，2010","44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,31 3 2 上 ꜂,,53 5 3 去 ꜄,,2 7 4a 陰入 ꜆,4 8 4b 陽入 ꜇"),
  ('hak_hl', '客家話海陸腔', '海陸客語', '#008000', '客語萌典', "https://www.moedict.tw/:%s", "名稱：客家話海陸腔<br>來源：<a href=https://www.moedict.tw/>客語萌典</a>","53 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,24 3 2 上 ꜂,,11 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('hak_sx', '客家話四縣腔', '四縣客語', '#008000', '客語萌典', "https://www.moedict.tw/:%s", "名稱：客家話四縣腔<br>來源：<a href=https://www.moedict.tw/>客語萌典</a>","24 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,31 3 2 上 ꜂,,55 5 3 去 ꜄,,2 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"),
  ('yue_gz', '香港粵語', '香港', '#FFAD00', '粵語審音配詞字庫', "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s", "名稱：香港粵語<br>來源：<a href=http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/>粵語審音配詞字庫</a>、<a href=http://www.unicode.org/charts/unihan.html>Unihan</a><br>說明：括號中的爲異讀讀音","55 1 1a 陰平 ꜀,35 3 2a 陰上 ꜂,33 5 3a 陰去 ꜄,11 2 1b 陽平 ꜁,23 4 2b 陽上 ꜃,22 6 3b 陽去 ꜅,55 7a 4a 上陰入 ꜆,33 7b 4b 下陰入  ꜀,22 8 4c 陽入 ꜇"),
  ('yue_yl', '鬱林話', '鬱林', '#FFAD00', None, None, "更新：2021-07-10<br>名稱：鬱林話<br>來源：<u>赤鬚夜蜂虎</u>","54 1 1a 陰平 ꜀,33 3 2a 陰上 ꜂,52 5 3a 陰去 ꜄,32 2 1b 陽平 ꜁,13 4 2b 陽上 ꜃,21 6 3b 陽去 ꜅,5 7a 4a 上陰入 ꜆,3 7b 4b 下陰入  ꜀,2 8a 4c 上陽入 ꜇,1 8b 4d 下陽入 ꜁,44 0 0 上陰小 0,45 0 0 下陰小 0,24 0 0 陽小 0"),
  ('nan', '臺灣閩南語', '臺灣', '#FF6600', '臺灣閩南語常用詞辭典', "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s", "更新：2020-05-17<br>名稱：臺灣閩南語<br>來源：<a href=https://github.com/tauhu-tw/tauhu-taigi>豆腐台語詞庫</a>、<a href=https://twblg.dict.edu.tw/holodict_new/>臺灣閩南語常用詞辭典</a><br>說明：下標“俗”表示“俗讀音”，“替”表示“替代字”，指的是某個字的讀音其實來自另一個字，比如“人”字的lang5音其實來自“儂”字。有些字會有用斜線分隔的兩個讀音（如“人”字的jin5/lin5），前者爲高雄音（第一優勢腔），後者爲臺北音（第二優勢腔）。","55 1 1a 陰平 ꜀,51 3 2 上 ꜂,31 5 3a 陰去 ꜄,3 7 4a 陰入 ꜆,24 2 1b 陽平 ꜁,,33 6 3b 陽去 ꜅,5 8 4b 陽入 ꜇"),
  ('nan_lz_lz', '雷州黎話', '雷州', '#FF6600', None, None, "版本：V2.0 (2021-08-23)<br>名稱：雷州黎話<br>來源：<u>Kiattan</u>","24 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,41 3 2a 陰上 ꜂,33 4 2b 陽上 ꜃,21 5 3a 陰去 ꜄,453 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('nan_lz_wcls', '吳川蘭石東話', '吳川蘭石', '#FF6600', None, None, "版本：V2.0 (2021-08-23)<br>名稱：吳川蘭石東話<br>來源：<u>Kiattan</u>","33 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,31 3 2a 陰上 ꜂,42 4 2b 陽上 ꜃,44 5 3a 陰去 ꜄,453 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('nan_lz_dbls', '電白龍山海話', '電白龍山', '#FF6600', None, None, "版本：V2.0 (2021-08-23)<br>名稱：電白龍山海話<br>來源：<u>Kiattan</u>","33 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,31 3 2a 陰上 ꜂,42 4 2b 陽上 ꜃,44 5 3a 陰去 ꜄,453 6 3b 陽去 ꜅,53 6b 3c 陽去b ꜃,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('nan_lz_ldzz', '羅定漳州話', '羅定漳州', '#FF6600', None, None, "版本：V2.0 (2021-08-23)<br>名稱：羅定漳州話<br>來源：<u>Kiattan</u>","45 1a 1a 陰平a ꜀,55 1b 1b 陰平b ꜆,24 2a 1c 陽平a ꜁,22 2b 1d 陽平b ꜇,21 2c 1e 陽平c ꜇,451 3 2a 陰上 ꜂,443 4 2b 陽上 ꜃,31 5 3 去 ꜄,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"),
  ('nan_cs_pn', '普寧話', '普寧', '#FF6600', None, None, "版本：V2.0 (2021-08-18)<br>名稱：普寧話<br>來源：<u>阿纓</u>","223 1 1a 陰平 ꜀,44 2 1b 陽平 ꜁,53 3 2a 陰上 ꜂,213 4 2b 陽上 ꜃,21 5 3a 陰去 ꜄,311 6 3b 陽去 ꜅,32 7 4a 陰入 ꜆,54 8 4b 陽入 ꜇"),
  ('nan_cs_st', '汕頭話', '汕頭', '#FF6600', None, None, "版本：V2.0 (2021-08-05)<br>名稱：汕頭話<br>來源：<u>Kiattan</u>","33 1 1a 陰平 ꜀,52 3 2a 陰上 ꜂,212 5 3a 陰去 ꜄,2 7 4a 陰入 ꜆,55 2 1b 陽平 ꜁,35 4 2b 陽上 ꜃,31 6 3b 陽去 ꜅,54 8 4b 陽入 ꜇"),
  ('nan_cs_rp', '饒平話', '饒平', '#FF6600', None, None, "版本：V1.3 (2021-07-28)<br>名稱：饒平話<br>來源：<u>四方麻東</u>","33 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,51 3 2a 陰上 ꜂,25 4 2b 陽上 ꜃,212 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,2 7 4a 陰入 ꜆,54 8 4b 陽入 ꜇"),
  ('nan_cdo_nd', '寧德話', '寧德', '#FF6600', None, None, "更新：2021-08-12<br>名稱：寧德話<br>來源：<u>落橙</u>、<u>阿纓</u>","44 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,42 3 2 上 ꜂,,35 5 3a 陰去 ꜄,332 6 3b 陽去 ꜅,2 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"),
  ('vi', '越南語', '越南', '#DB7093', '漢越辭典摘引', "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s", "名稱：越南語<br>來源：<a href=http://www.vanlangsj.org/hanviet/>漢越辭典摘引</a>","33 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,313 3 2a 陰上 ꜂,35 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,35 7 4a 陰入 ꜆,21 8 4b 陽入 ꜇"),
  ('ko_okm', '中世紀朝鮮語', '中世朝鮮', '#BA55D3', None, None, "名稱：中世紀朝鮮語<br>來源：<a href=https://github.com/nk2028/sino-korean-readings>韓國漢字音歷史層次研究</a>",None),
  ('ko_kor', '朝鮮語', '朝鮮', '#BA55D3', 'Naver漢字辭典', "http://hanja.naver.com/hanja?q=%s", "名稱：朝鮮語、韓語<br>來源：<a href=http://hanja.naver.com/>Naver漢字辭典</a><br>說明：括號前的讀音爲漢字本來的讀音，也是朝鮮的標準音，而括號內的讀音爲韓國應用<a href=http://zh.wikipedia.org/wiki/%E9%A0%AD%E9%9F%B3%E6%B3%95%E5%89%87>頭音法則</a>之後的讀音。",None),
  ('ja_go', '日語吳音', '日語吳音', '#FF0000', None, None, "名稱：日語吳音<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。<br>　　日語每個漢字一般具有吳音、漢音兩種讀音，個別漢字還具有唐音和慣用音。這四種讀音分別用“日吳”、“日漢”、“日唐”、“日慣”表示。另外，對於一些生僻字，《漢字源》中沒有註明讀音的種類，也沒有提供“歷史假名遣”寫法，這一類“其他”讀音用“日他”表示。<br>　　 有的讀音會帶有括號，括號前的讀音爲“現代假名遣”寫法，括號內的讀音爲對應的“歷史假名遣”寫法。<br>　　 讀音的顏色和粗細代表讀音的常用程度。<b>黑色粗體</b>爲“常用漢字表”內的讀音；黑色細體爲《漢字源》中列第一位，但不在“常用漢字表”中的讀音；<span class=dim>灰色細體</span>爲既不在“常用漢字表”中，也不在《漢字源》中列第一位的讀音。",None),
  ('ja_kan', '日語漢音', '日語漢音', '#FF0000', None, None, "名稱：日語漢音<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。<br>　　日語每個漢字一般具有吳音、漢音兩種讀音，個別漢字還具有唐音和慣用音。這四種讀音分別用“日吳”、“日漢”、“日唐”、“日慣”表示。另外，對於一些生僻字，《漢字源》中沒有註明讀音的種類，也沒有提供“歷史假名遣”寫法，這一類“其他”讀音用“日他”表示。<br>　　 有的讀音會帶有括號，括號前的讀音爲“現代假名遣”寫法，括號內的讀音爲對應的“歷史假名遣”寫法。<br>　　 讀音的顏色和粗細代表讀音的常用程度。<b>黑色粗體</b>爲“常用漢字表”內的讀音；黑色細體爲《漢字源》中列第一位，但不在“常用漢字表”中的讀音；<span class=dim>灰色細體</span>爲既不在“常用漢字表”中，也不在《漢字源》中列第一位的讀音。",None),
  ('ja_tou', '日語唐音', '日語唐音', '#FF0000', None, None, "名稱：日語<br>來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。<br>　　日語每個漢字一般具有吳音、漢音兩種讀音，個別漢字還具有唐音和慣用音。這四種讀音分別用“日吳”、“日漢”、“日唐”、“日慣”表示。另外，對於一些生僻字，《漢字源》中沒有註明讀音的種類，也沒有提供“歷史假名遣”寫法，這一類“其他”讀音用“日他”表示。<br>　　 有的讀音會帶有括號，括號前的讀音爲“現代假名遣”寫法，括號內的讀音爲對應的“歷史假名遣”寫法。<br>　　 讀音的顏色和粗細代表讀音的常用程度。<b>黑色粗體</b>爲“常用漢字表”內的讀音；黑色細體爲《漢字源》中列第一位，但不在“常用漢字表”中的讀音；<span class=dim>灰色細體</span>爲既不在“常用漢字表”中，也不在《漢字源》中列第一位的讀音。",None),
  ('ja_kwan', '日語慣用音', '日語慣用', '#FF0000', None, None, None,None),
  ('ja_other', '日語其他讀音', '日語其他', '#FF0000', None, None, None,None),
  ('bh', '總筆畫數', '總筆畫數', '#1E90FF', None, None, None,None),
  ('bs', '部首餘筆', '部首餘筆', '#808080', None, None, None,None),
  ('cj3', '倉頡三代', '倉三', '#808080', None, None, None,None),
  ('cj5', '倉頡五代', '倉五', '#808080', None, None, None,None),
  ('cj6', '倉頡六代', '倉六', '#808080', None, None, None,None),
  ('wb86', '五筆86版', '五筆86', '#808080', None, None, None,None),
  ('wb98', '五筆98版', '五筆98', '#808080', None, None, None,None),
  ('wb06', '五筆06版', '五筆06', '#808080', None, None, None,None),
  ('va', '異體字', '異體', '#808080', None, None, None,None),
  ('fl', '分類', '分類', '#808080', None, None, None,None),
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
  del row["mc"]
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

#ST
stVariants = dict()
for line in open("STCharacters.txt"):
  line = line.strip()
  fs = line.split("\t")
  stVariants[fs[0]] = fs[1].split(" ")

#fl
d.clear()
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
pq = dict()
for line in open("PrengQim.txt"):
    line = line.strip()
    fs = line.split(" ")
    pq[fs[0]] = fs[1].replace("'", "0")
last = ""
for line in open("Dzih.txt"):
  line = line.strip()
  fs = line.split(" ")
  if len(fs[0]) == 1:
    js = fs[3]
    if "上同" in js: js = js.replace("上同", "同" + last)
    else: last = fs[0]
    d[fs[0]].append("%s`%s`"%(pq[fs[1]], js))
update("ltc_mc", d)
logging.info("處理廣韻 %.2f" % timeit())

#yt
import yt
ytd = yt.get_dict()
update("ltc_yt", ytd)
logging.info("處理韻圖 %.2f" % timeit())

#sg
#https://github.com/BYVoid/ytenx/blob/master/ytenx/sync/dciangx/DrienghTriang.txt
d.clear()
for line in open("DrienghTriang.txt"):
  line = line.strip('\n')
  if line.startswith('#'): continue
  fs = line.split(' ')
  hz = fs[0]
  if len(hz) != 1: continue
  zs = fs[16]
  if zs: zs = "`%s`" % zs
  js = ("%s (%s%s切 %s聲 %s%s)%s"%(fs[12], fs[7],fs[8],fs[9],fs[10],fs[11],zs))
  if js not in d[hz]:
    d[hz].append(js)
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
sds = {'去': '4', '入平': '2', '入去': '4', '入上': '3', '上': '3','陽平': '2', '陰平': '1'}

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
for line in open("永州官話同音字表.tsv"):
  line = line.strip("\n")
  fs = line.split("\t")
  hz,jt,py,bz = fs
  if len(hz)!=1: continue
  sd = py[-1]
  py = py[:-1]
  py = py.replace("w","u").replace("uu", "u")
  py = re.sub("^(ts|tsh|s|z)i", "\\1ɿ", py)
  py = re.sub("^y(?=[^u])", "i", py).replace("ii","i")
  py = re.sub("^(c|ch|sh|zh)u", "\\1yu", py)
  py = py.replace("iu", "iou").replace("ui", "uei").replace("yun", "yn").replace("un", "uen")
  ipa = py.replace("ou", "əu").replace("ao", "au").replace("ang", "ã").replace("an", "ẽ").replace("yu", "y")
  ipa = re.sub("^h", "x", ipa).replace("gh", "ɣ").replace("sh", "ɕ").replace("zh", "ʑ").replace("h", "ʰ")\
      .replace("ts", "ts").replace("c", "tɕ").replace("ng", "ŋ")
  ipa = ipa + sd
  if bz:
    ipa += "`%s`"%bz
  d[hz].append(ipa)
  if jt != hz and ipa not in d[jt]:
    d[jt].append(ipa)
update("cmn_xn_yzll", d)
logging.info("處理永州零陵話 %.2f" % timeit())

#bf
d.clear()
for line in open("阜寧同音字表3.0.tsv"):
  line = line.strip().replace("~", "～").replace('"','').replace(' ','')
  if not line: continue
  if line.startswith("#"):
    ym = line[1:]
  else:
    fs = line.split("\t")
    sm = fs[0].replace("Ø", "")
    if len(fs) != 2: continue
    for sd,hzs,n in re.findall("\[(\d)\](.*?)((?=\[)|$)", fs[1]):
      py = sm + ym +sd
      hzs = re.findall("(.)\d?([+-=*/?]?)\d?(\{.*?\})?", hzs)
      for hz, c, m in hzs:
        m = m.strip("{}")
        if "}" in m: print(m)
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
update("cmn_hy_hc_bf", d)
logging.info("處理滨阜方言 %.2f" % timeit())

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
    py = "%s`本音`" % py
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
for line in open("興化同音字表.tsv"):
  line = line.strip()
  if line.startswith("#"):
    ym = line[1:]
  else:
    fs = line.split("\t")
    sm = fs[0].replace("ø", "")
    for sd,hzs,n in re.findall("［(\d)］(.*?)((?=［)|$)", fs[1]):
      py = sm + ym +sd
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
icym = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'i', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iɤɯ', 'u': 'u', 'v': 'v', 'en': 'ən', 'a': 'a', 'on': 'ɔŋ', 'an': 'ã', 'oh': 'ɔʔ', 'i': 'j', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'ui': 'ui', 'uae': 'uɛ', 'aeh': 'æʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'uʔ', 'aen': 'ɛ̃', 'eu': 'ɤɯ', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iuʔ', 'yen': 'yn', 'ian': 'iã', 'iun': 'iũ', 'un': 'ũ', 'o': 'ɔ', 'uan': 'uã', 'ua': 'ua', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uæʔ', 'iaeh': 'iæʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya', '': ''}
for line in open("鹽城同音字表.tsv"):
  line = line.strip()
  if not line: continue
  fs = line.split('\t')
  py,hzs = fs
  sm = re.findall("^[^aeiouvy]?g?", py)[0]
  sd = py[-1]
  if sd not in "12357": sd = ""
  ym = py[len(sm):len(py)-len(sd)]
  py = icsm[sm]+icym[ym]+sd
  hzs = re.findall("(.)([+-=*?]?)(（.*?）)?", hzs)
  for hz, c, m in hzs:
    p = ""
    m = m.strip("（）")
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
    if p and m:
        p = "%s：%s" % (p, m)
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
update("cmn_hy_hc_ic", d)
logging.info("處理鹽城話 %.2f" % timeit())

#fdgc
d.clear()
for line in open("肥東古城同音字表.tsv"):
  line = line.strip()
  if line.startswith("#"): continue
  ipa,hzs = line.split("\t")
  ipa = ipa.rstrip("0")
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
# ~ #https://github.com/uliloewi/lang2jin1/blob/master/langjin.dict.yaml
ljsm = {'g': 'k', 'd': 't', '': '', 'sh': 'ʂ', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 'r': 'ʐ', 'zh': 'ʈʂ', 't': 'tʰ', 'v': 'v', 'ng': 'ŋ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'ch': 'ʈʂʰ', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ'}
def lj2ipa(py):
  py = re.sub("r([1-5])$", "ʅ\\1", py)
  if py.startswith("ʅ"): py = "r" + py
  fs = re.findall("^([^aäüeiouyʅ1-9]+)?(.*)(\d)?$", py)
  if len(fs)<1:
    print(py, fs)
  sm,ym,sd = fs[0]
  sm = ljsm[sm]
  ym = ym.replace("y", "ɿ").replace("ü", "y").replace("än", "ẽ").replace("ä", "ɛ")\
    .replace("ao", "ɔ").replace("ei", "əi").replace("ou", "əɯ")\
    .replace("en", "ə̃").replace("er", "ɚ").replace("ng", "̃").replace("n", "̃")
  return sm + ym + sd
d.clear()
for line in open("langjin.dict.yaml"):
  line = line.strip()
  fs = line.split('\t')
  if len(fs) < 2: continue
  hz, py = fs[:2]
  if len(hz) != 1 or py in "vw": continue
  if py not in d[hz]:
    d[hz].append(lj2ipa(py))
update("cmn_hy_hc_lj", d)
logging.info("處理南京話 %.2f" % timeit())

#yz
d.clear()
for line in open("揚州同音字表.tsv"):
  line = line.strip("\n").replace('"', '').replace("##","#").replace('“','').replace('〆', '乄')
  py,hzs = line.split("\t")
  for c,hz,m in re.findall("([？#\+])?(.)(（.*?（.*?）.*）|（.*?）)?", hzs):
    p = ""
    if c == '+':
      p = "書"
    elif c == '#':
      p = "俗"
    elif c == '？':
      p = "存疑"
    if m:
      p += " " + m.strip("（）")
    p = p.strip()
    if p:
      p = "`%s`" % p
    p = py + p
    if p not in d[hz]:
      d[hz].append(p)
update("cmn_hy_hc_yz", d)
logging.info("處理揚州話 %.2f" % timeit())

#td
d.clear()
for line in open("通東談話 字表.tsv"):
  line = line.strip('\n')
  fs = [i.strip(' "') for i in line.split('\t')]
  hz, jt, py, ipa = fs[:4]
  sd = fs[4]
  if py == "IPA": continue
  sd = sd[-1]
  if sd == "0": sd = ""
  elif sd == "¹": sd = "8"
  elif sd == "²": sd = "9"
  if sd: ipa += sd
  js = fs[6].replace("~", "～")
  if js: ipa += "`%s`"%js
  if len(hz) == 1:
    if ipa not in d[hz]:
      d[hz].append(ipa)
  if len(jt) == 1:
    if ipa not in d[jt]:
      d[jt].append(ipa)
update("wuu_td", d)
logging.info("處理通東話 %.2f" % timeit())

#wuu
d.clear()
sms={'dz':'dz', 'zh':'ʑ', 'th':'tʰ', 'sh':'ɕ', 'lh':'ʔl', 'ts':'ts', 'tsh':'tsʰ', 'c':'tɕ', 'ph':'pʰ', 'kh':'kʰ', 'nh':'ʔn', 'j':'dʑ', 'ng':'ŋ', 'gh':'ɦ', 'ngh':'ʔŋ', 'ch':'tɕʰ', 'mh':'ʔm'}
yms={'ae': 'æ', 'aeh': 'æʔ', 'ai': 'ai', 'an': 'aŋ', 'au': 'au', 'ah': 'aʔ', 'a': 'ɑ', 'ee': 'e', 'ei': 'ei', 'eeh': 'eʔ', 'en': 'əŋ', 'eu': 'əu', 'eh': 'əʔ', 'iae': 'iæ', 'iaeh': 'iæʔ', 'ian': 'iaŋ', 'iau': 'iau', 'iah': 'iaʔ', 'ia': 'iɑ', 'ie': 'ie', 'ieh': 'ieʔ', 'ieu': 'iəu', 'i': 'i', 'ih': 'iɪʔ', 'in': 'iŋ', 'ion': 'ioŋ', 'ioh': 'ioʔ', 'iaon': 'iɔŋ', 'iaoh': 'iɔʔ', 'ieon': 'iʌŋ', 'ieoh': 'iʌʔ', 'm': 'm', 'n': 'n', 'ng': 'ŋ', 'on': 'oŋ', 'o': 'o', 'oe': 'ø', 'ou': 'ou', 'oeh': 'øʔ', 'oh': 'oʔ', 'aon': 'ɔŋ', 'aoh': 'ɔʔ', 'y': 'ɿ', 'uae': 'uæ', 'uaeh': 'uæʔ', 'uan': 'uaŋ', 'uah': 'uaʔ', 'ua': 'uɑ', 'uei': 'uei', 'uen': 'uəŋ', 'ueh': 'uəʔ', 'uon': 'uoŋ', 'uo': 'uo', 'uoe': 'uø', 'uoeh': 'uøʔ', 'uoh': 'uoʔ', 'uaon': 'uɔŋ', 'uaoh': 'uɔʔ', 'u': 'u', 'eon': 'ʌŋ', 'eoh': 'ʌʔ', 'iu': 'y', 'iuin': 'yɪŋ', 'iuih': 'yɪʔ', 'io': 'yo', 'ioe': 'yø', 'ioeh': 'yøʔ', '':''}
for line in open("標準吳語.tsv"):
  line = line.strip('\n')
  fs = [i.strip(' "') for i in line.split('\t')]
  hz, jt, js = fs[1], fs[2], fs[-1]
  if len(hz) != 1: continue
  sm,ym,sd=fs[3:6]
  if ym.endswith("h") and not sd:
      sd = 4
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
update("wuu_tz_bztz", d)
logging.info("處理標準台州 %.2f" % timeit())

#lh
d.clear()
tones = {'33':1,'31':2,'42':3,'55':5,'13':6,'5':7,'2':8}
for line in open("临海方言字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd,zs = fs[:5]
  if not yb or len(hz)!=1: continue
  sd = str(tones[sd])
  js = yb + sd
  if zs: js += "`%s`"%zs
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and js not in d[jt]:
    d[jt].append(js)
update("wuu_tz_lh", d)
logging.info("處理临海話 %.2f" % timeit())

#xj
d.clear()
tones = {'334':1,'312':2,'423':3,'55':5,'22':6,'5':7,'2':8}
for line in open("仙居方言字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd,zs = fs[:5]
  if not yb or len(hz)!=1: continue
  sd = str(tones[sd])
  js = yb + sd
  if zs: js += "`%s`"%zs
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and js not in d[jt]:
    d[jt].append(js)
update("wuu_tz_xj", d)
logging.info("處理仙居話 %.2f" % timeit())

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
  s = s + tone
  return s
d.clear()
firstline = False
for line in open("苏州话翘舌音字表.tsv"):
  line = line.replace('"', '').strip()
  fs = line.split("\t")
  if fs[0] == '#ən':
    firstline = True
  if firstline:
    if len(fs) == 1:
      ym = fs[0][1:]
    else:
      sm = fs[0][1:]
      if "原" in sm:
        sm = re.sub("^.+?\(原(.+?)\)", "\\1", sm)
      for sd,hzs,n in re.findall("\[(\d)\](.*?)((?= )|$)", fs[1]):
        py = sm + ym +sd
        hzl = re.findall("(.)([=*]?)(\{.*?\})?", hzs)
        for hz, c, m in hzl:
          if hz in '、()': continue
          p = ''
          if c == '=':
            p = "書"
          elif c == '*':
            p = "＊"
          m = m.strip('{}')
          if m:
            p += " " + m
          p = p.strip()
          if p:
            p = "`%s`" % p
          p = "|%s%s|" % (py,p)
          d[hz].append(p)
          if hz in stVariants and (hz + "(") not in hzs:
            ft = stVariants[hz][0]
            if p not in d[ft]:
              d[ft].append(p)
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
for line in open("cmn.tsv"):
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

#yl
ipas={'b∅': 'p', 'p∅': 'pʰ', 'bb∅': 'ɓ', 'm∅': 'm', 'f∅': 'f', 'd∅': 't', 't∅': 'tʰ', 'dd∅': 'ɗ', 'n∅': 'n', 'l∅': 'l', 'sl∅': 'ɬ', 'g∅': 'k', 'k∅': 'kʰ', 'gw∅': 'kʷ', 'kw∅': 'kʷʰ', 'h∅': 'h', 'ng∅': 'ŋ', 'z∅': 'tʃ', 'c∅': 'tʃʰ', 's∅': 'ʃ', 'nj∅': 'ȵ', 'j∅': 'j', 'w∅': 'w', '∅': '', 'aa': 'a', 'ai': 'ai', 'au': 'au', 'an': 'an', 'am': 'am', 'ang': 'aŋ', 'at': 'at', 'ap': 'ap', 'ak': 'ak', 'o': 'ɔ', 'oi': 'ɔi', 'ou': 'ɔu', 'on': 'ɔn', 'om': 'ɔm', 'ong': 'ɔŋ', 'ot': 'ɔt', 'op': 'ɔp', 'ok': 'ɔk', 'oe': 'œ', 'oen': 'œn', 'yng': 'œŋ', 'yet': 'œt', 'yk': 'œk', 'oek': 'œk', 'oep': 'œp', 'e': 'ɛ', 'een': 'ɛn', 'ing': 'eŋ', 'ik': 'ek', 'eo': 'o', 'eou': 'əu', 'eat': 'ət', 'eu': 'ɛu', 'ei': 'ei', 'en': 'ɛn', 'em': 'ɛm', 'eng': 'ɛŋ', 'et': 'ɛt', 'ep': 'ɛp', 'ek': 'ɛk', 'i': 'i', 'iu': 'iu', 'in': 'in', 'im': 'im', 'it': 'it', 'ip': 'ip', 'iik': 'ik', 'u': 'u', 'ui': 'ui', 'un': 'un', 'ung': 'oŋ', 'ut': 'ut', 'uk': 'ok', 'yu': 'y', 'yun': 'yn', 'yut': 'yt', 'm': 'm̩', 'ng': 'ŋ̍', '': ''}
d.clear()
for line in open("鬱林話字表-粵拼版-21年6月3日.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  if len(fs) < 12: continue
  hz = fs[0]
  if len(hz) > 1: continue
  sm,ym,sd,zs = fs[8:12]
  sm = sm.replace("0", "") + "∅"
  yb = (ipas[sm] if sm in ipas else sm.rstrip("∅"))+ipas[ym]
  js = yb + sd + ("`%s`"% zs if zs else "")
  if js not in d[hz]:
    d[hz].append(js)
update("yue_yl", d)
logging.info("處理鬱林話 %.2f" % timeit())

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

#sy
d.clear()
tones = {'51':1,'31':2,'214':3,'22':4,'35':5,'13':6,'5':7,'2':8}
for line in open("松阳方言字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd = fs[:4]
  if not yb or len(hz)!=1: continue
  sd = str(tones[sd])
  js = yb + sd
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and js not in d[jt]:
    d[jt].append(js)
update("wuu_sl_sy", d)
logging.info("處理松陽話 %.2f" % timeit())

#sc
d.clear()
tones = {'55':1,'221':2,'52':3,'13':4,'334':5,'212':6,'5':7,'23':8}
for line in open("吴语遂昌话字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,yb,sd,zs = fs[:4]
  if not yb or len(hz)!=1: continue
  if sd == "0": sd = ""
  else: sd = str(tones[sd])
  js = yb + sd
  if zs: js += "`%s`"%zs
  if js not in d[hz]:
    d[hz].append(js)
update("wuu_sl_sc", d)
logging.info("處理遂昌話 %.2f" % timeit())

#yh
d.clear()
tones = {'324':1,'423':2,'53':3,'21':4,'55':5,'223':6,'5':7,'24':8}
for line in open("云和方言同音字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd,zs = fs[:5]
  if not yb or len(hz)!=1: continue
  if sd == "0": sd = ""
  else: sd = str(tones[sd])
  js = yb + sd
  if zs: js += "`%s`"%zs
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and len(jt) == 1:
    if js not in d[jt]:
      d[jt].append(js)
update("wuu_sl_yh", d)
logging.info("處理雲和話 %.2f" % timeit())

#tsly
d.clear()
tones = {'224':1,'42':2,'51':3,'21':4,'35':5,'11':6,'5':7,'1':8, '33':9}
for line in open("泰顺罗阳同音字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd,zs = fs[:5]
  if not yb or len(hz)!=1: continue
  if sd == "0": sd = ""
  else: sd = str(tones[sd])
  js = yb + sd
  if zs: js += "`%s`"%zs
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and len(jt) == 1:
    if js not in d[jt]:
      d[jt].append(js)
update("wuu_sl_tsly", d)
logging.info("處理泰順羅陽話 %.2f" % timeit())

#ra
d.clear()
for line in open("瑞安東山-方言调查字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,zs = fs[:4]
  if not yb or len(hz) != 1: continue
  yb = yb.rstrip("0")
  js = yb + ("`%s`"% zs if zs else "")
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and len(jt) == 1:
    if js not in d[jt]:
      d[jt].append(js)
update("wuu_oj_rads", d)
logging.info("處理瑞安東山話 %.2f" % timeit())

#yqyc
d.clear()
for line in open("乐清乐成字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb = fs[:3]
  if not yb: continue
  if len(hz) == 1:
    js = yb.rstrip("0")
    if js not in d[hz]:
      d[hz].append(js)
    if jt != hz and len(jt) == 1 and js not in d[jt]:
      d[jt].append(js)
update("wuu_oj_yqyc", d)
logging.info("處理樂清樂成話 %.2f" % timeit())

#wzcd
d.clear()
tones = {'33':1,'31':2,'35':3,'42':5,'22':6,'213':7}
for line in open("温州方言同音字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd,zs = fs[:5]
  if not yb or len(hz) != 1: continue
  sd = str(tones[sd])
  js = yb + sd
  if zs:
    js += "`%s`"%zs
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and len(jt) == 1 and js not in d[jt]:
    d[jt].append(js)
update("wuu_oj_wzcd", d)
logging.info("處理溫州話 %.2f" % timeit())

#cnpm
d.clear()
tones = {'44':1,'31':2,'45':3,'24':4,'42':5,'22':6,'323':7,'212':8}
for line in open("苍南蒲门瓯语方言岛字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd = fs[:4]
  if not yb or len(hz)!=1: continue
  sd = str(tones[sd])
  js = yb + sd
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and len(jt) == 1 and js not in d[jt]:
    d[jt].append(js)
update("wuu_oj_cnpm", d)
logging.info("處理蒼南蒲門話 %.2f" % timeit())

#jy
tones = {'阳入':8,'阴上':3,'阳平':2,'阴入':7,'阳去':6,'阴平':1,'阴去':5,'阳上':4}
d.clear()
for line in open("缙云字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,sd,zs,yb = fs[0],fs[1],fs[4],fs[5],fs[7]
  if not yb or len(hz) != 1: continue
  yb = re.sub("[˩˨˧˦˥]", "", yb.rstrip("0")) + str(tones[sd])
  js = yb + ("`%s`"% zs if zs and not zs.isdigit() else "")
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

#nan_lz
lzs = ["dbls", "ldzz", "lz", "wcls"]
tones = [
  {'33':1,'22':2,'31':3,'42':4,'44':5,'53':6,'453':7,'5':8,'2':9},
  {'45':1,'55':2,'24':3,'22':4,'21':5,'451':6,'443':7,'31':8,'5':9,'2':10},
  {'24':1,'22':2,'41':3,'33':4,'21':5,'453':6,'5':7,'2':8},
  {'33':1,'22':2,'31':3,'42':4,'44':5,'453':6,'5':7,'2':8},
]
for index,lz in enumerate(lzs):
  d.clear()
  for line in open("粤西闽语方言字表2.0.tsv"):
    fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
    if len(fs) < 6: continue
    hz = fs[0]
    ybs = fs[2 + index]
    if not ybs or ybs.startswith("—") or hz == "例字": continue
    zs = "`%s`"%hz[1:] if len(hz)>1 else ""
    hz = hz[0]
    for yb in ybs.split("/"):
      yb = yb.strip()
      sd = re.findall("\d+", yb)[0]
      if sd not in tones[index]: continue
      js = yb.replace(sd, str(tones[index][sd])) + zs
      js = re.sub("[（）]", "`", js)
      if js not in d[hz]:
        d[hz].append(js)
  update("nan_lz_" + lz, d)
logging.info("處理粤西閩語 %.2f" % timeit())

#nan_pn
d.clear()
tones = {'54':8,'53':3,'44':2,'32':7,'311':6,'223':1,'21':5,'213':4}
for line in open("普宁字表初稿2（繁体兼容）.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,yb,zs,sm,ym,sd = fs[:6]
  if not yb or len(hz) != 1: continue
  if sd: sd = str(tones[sd])
  if hz in "?？": hz = "□"
  yb = sm + ym + sd
  js = yb + ("`%s`"% zs if zs else "")
  if js not in d[hz]:
    d[hz].append(js)
update("nan_cs_pn", d)
logging.info("處理普寧話 %.2f" % timeit())

#nan_st
d.clear()
for line in open("方言调查字表2.0 （汕头）(3600字).tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,py,yb,zs = fs[:4]
  yb = yb.replace(' ', '')
  if not yb: continue
  sd = py[-1]
  if not sd.isdigit():
    sd = ""
  if len(hz) == 1:
    yb = re.sub("[˩˨˧˦˥]", "", yb.rstrip("0")) + sd
    js = yb + ("`%s`"% zs if zs else "")
    if js not in d[hz]:
      d[hz].append(js)
update("nan_cs_st", d)
logging.info("處理汕頭話 %.2f" % timeit())

#nan_cc_rp
d.clear()
for line in open("方言调查字表（闽-饶平）1.3.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,py,yb,zs = fs[:4]
  if not yb: continue
  sd = py[-1]
  if not sd.isdigit():
    sd = ""
  if len(hz) == 1:
    yb = re.sub("[˩˨˧˦˥]", "", yb.rstrip("0")) + sd
    js = yb + ("`%s`"% zs if zs else "")
    if js not in d[hz]:
      d[hz].append(js)
update("nan_cs_rp", d)
logging.info("處理饒平話 %.2f" % timeit())

#nan_cdo_nd
tones = {'陽入':8,'上':3,'陽平':2,'陰入':7,'陽去':6,'陰平':1,'陰去':5}
d.clear()
for line in open("闽东宁德方言字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,jt,yb,sd,zs = fs
  if not yb or len(hz) != 1: continue
  yb = yb + str(tones[sd.split("|")[1]])
  js = yb + ("`%s`"% zs if zs else "")
  if js not in d[hz]:
    d[hz].append(js)
  if jt != hz and len(jt) == 1:
    if js not in d[jt]:
      d[jt].append(js)
update("nan_cdo_nd", d)
logging.info("處理寧德話 %.2f" % timeit())

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
  s = s.replace("sl", "ɬ").replace("nj", "ɲ").replace("t", "tʰ").replace("zh", "tʃ").replace("ch", "tʃʰ").replace("sh", "ʃ").replace("p", "pʰ").replace("k", "kʰ").replace("z", "ts").replace("c", "tsʰ").replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ").replace("rh", "ʒ").replace("r", "ʒ").replace("ng", "ŋ").replace("?", "ʔ").replace("b", "p").replace("d", "t").replace("g", "k")
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

#hak_whhb
d.clear()
tones = {'44':1,'13':2,'31':3,'53':5,'1':7,'5':8}
for line in open("五华横陂客家方言字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  hz,yb,sd,zs = fs[:4]
  if not yb or len(hz) != 1: continue
  sd = str(tones.get(sd))
  js = yb + sd + ("`%s`"% zs if zs else "")
  if js not in d[hz]:
    d[hz].append(js)
update("hak_whhb", d)
logging.info("處理五華橫陂話 %.2f" % timeit())

#hak_whsz
d.clear()
tones = {'44':1,'13':2,'31':3,'53':5,'2':7,'4':8}
for line in open("五华水寨客家话字表.tsv"):
  fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
  yb,hz = fs[:2]
  if not yb or len(hz) != 1: continue
  dz = re.findall("\d+$", yb)[0]
  yb = yb[:-len(dz)]
  sd = str(tones.get(dz))
  js = yb + sd
  if js not in d[hz]:
    d[hz].append(js)
update("hak_whsz", d)
logging.info("處理五華水寨話 %.2f" % timeit())

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

#kx
d.clear()
for line in open("kangxizidian-v3f.txt"):
  fs = line.strip().split('\t\t')
  hz = fs[0]
  if len(hz) == 1:
    js = fs[1].replace("", "\n").strip()[6:]
    js = re.sub("頁(\d+)第(\d+)\n", lambda x: "%04d.%d"%(int(x[1]),int(x[2])), js)
    d[hz].append(js)
update("kx", d)
logging.info("處理康熙字典 %.2f" % timeit())

#hd
d.clear()
hd=defaultdict(dict)
numbers="❶❷❸❹❺❻❼❽❾❿⓫⓬⓭⓮⓯⓰⓱⓲⓳⓴㉑㉒㉓㉔㉕㉖㉗㉘㉙㉚㉛㉜㉝㉞㉟㊱㊲㊳㊴㊵㊶㊷㊸㊹㊺㊻㊼㊽㊾㊿"
pages = dict()
for line in open("handa.txt"):
  line = line.strip('\n')
  fs = line.split('\t')
  if len(fs[0]) == 1:
    hz,py,js,page = fs[:4]
    if hz in kCompatibilityVariants and js.startswith("同"): continue
    pages[hz] = page
    if py == "None":
        py = ""
    if py in hd[hz]:
      hd[hz][py].append(js)
    else:
      hd[hz][py] = [js]
for hz in hd:
  for py in hd[hz]:
    if len(hd[hz][py])!=1:
      hd[hz][py] = [numbers[count]+js for count,js in enumerate(hd[hz][py])]
for hz in hd:
  js = "\n\n".join(["%s\n%s" % (py, "\n".join(hd[hz][py])) for py in hd[hz]])
  js = re.sub("=(.)", "“\\1”", js).strip()
  d[hz] = ["%s\n%s"%(pages[hz], js)]
update("hd", d)
logging.info("處理漢語大字典 %.2f" % timeit())

#bh
d.clear()
for line in open("/usr/share/unicode/Unihan_IRGSources.txt"):
    if not line.startswith("U"): continue
    fields = line.strip().split("\t", 2)
    han, typ, val = fields
    if typ == "kTotalStrokes":
      han = hex2chr(han)
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
  vad = unicodes[i]
  if i in variants:
    vad["va"] = " ".join(variants.get(i, i))
logging.info("處理異體字 %.2f" % timeit())

def ishz(c):
  c = c.strip()
  if len(c) != 1: return False
  n = ord(c)
  return 0x3400<=n<0xA000 or n in (0x25A1, 0x3007) or 0xF900<=n<0xFB00 or 0x20000<=n<0x31350

#cj
d.clear()
for line in open("cj3.txt"):
  line = line.strip()
  if not line or " " not in line or "=" in line: continue
  fs = line.split(" ")
  cj = fs[0]
  hz = fs[-1]
  if ishz(hz):
    d[hz].append(cj)
update("cj3", d)

d.clear()
for line in open("Cangjie5.txt"):
  line = line.strip()
  if not line or "\t" not in line: continue
  fs = line.split("\t")
  cj = fs[1]
  hz = fs[0]
  if ishz(hz):
    d[hz].append(cj)
update("cj5", d)

d.clear()
for line in open("cangjie6.dict.yaml"):
  line = line.strip()
  if not line or "\t" not in line: continue
  fs = line.split("\t")
  cj = fs[1]
  hz = fs[0]
  if ishz(hz):
    d[hz].append(cj)
update("cj6", d)
logging.info("處理仓頡 %.2f" % timeit())

#wb
d.clear()
for line in open("wb.csv"):
  line = line.strip()
  fs = line.split(",")
  hz = fs[1]
  wb = fs[2]
  if ishz(hz):
    d[hz].append(wb)
update("wb86", d)

d.clear()
for line in open("wb.csv"):
  line = line.strip()
  fs = line.split(",")
  hz = fs[1]
  wb = fs[3]
  if ishz(hz):
    d[hz].append(wb)
update("wb98", d)

d.clear()
for line in open("wb.csv"):
  line = line.strip()
  fs = line.split(",")
  hz = fs[1]
  wb = fs[4]
  if ishz(hz):
    d[hz].append(wb)
update("wb06", d)

d.clear()
for line in open("wb.csv"):
  line = line.strip()
  fs = line.split(",")
  hz = fs[1]
  wb = fs[5]
  if ishz(hz):
    d[hz].append(wb)
update("wbh", d)
logging.info("處理五筆 %.2f" % timeit())

#lf
d.clear()
for line in open("liangfen.dict.yaml"):
  line = line.strip()
  if not line or "\t" not in line: continue
  fs = line.split("\t")
  lf = fs[1]
  hz = fs[0]
  if ishz(hz):
    d[hz].append(lf)
for line in open("lfzy.tsv"):
  line = line.strip()
  fs = line.split("\t")
  lf = fs[1]
  hz = fs[0]
  if ishz(hz):
    if "(" in lf:
      if " " in lf:
        a,b=lf.split(" ")
        for i in a.strip(")").split("("):
          for j in b.strip(")").split("("):
            d[hz].append(i+j)
      else:
        for j in b.strip(")").split("("):
          d[hz].append(j)
    else:
      lf = lf.replace(" ", "")
      d[hz].append(lf)
update("lf", d)
logging.info("處理兩分 %.2f" % timeit())

#filter
for i in sorted(unicodes.keys()):
  n = ord(i)
  if not(ishz(i)):
    print(i, unicodes[i])
    unicodes.pop(i)

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

NAME='../app/src/main/assets/databases/mcpdict.db'
os.remove(NAME)
conn = sqlite3.connect(NAME)
c = conn.cursor()
c.execute("DROP TABLE IF EXISTS mcpdict")
c.execute("CREATE VIRTUAL TABLE mcpdict USING fts3 (%s)"%FIELDS)
c.executemany(INSERT, ZHEADS[1:])

fpy = open("缺音", "w")
notoext = set(open("NotoSansCJK-Regular.txt").read().strip())
for i in sorted(unicodes.keys(), key=cjkorder):
  d = unicodes[i]
  v = list(map(d.get, KEYS))
  c.execute(INSERT, v)
  if not d.get("cmn"):
    fpy.write(i)
fpy.close()

conn.commit()
conn.close()
logging.info("保存數據庫 %.2f" % timeit())
logging.info("處理總時間 %.2f" % (time() - start0))
logging.info("音典總字數 %d" % len(unicodes))
