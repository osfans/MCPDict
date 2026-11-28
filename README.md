# 漢字音典介紹

## 一、項目簡介

[漢字音典](https://github.com/osfans/MCPDict)（MCPDict）是一個面向語言、方言與漢字音研究愛好者的資料整理與查詢項目，收錄各類語言（方言）的漢字讀音及相關釋義，並提供便捷的檢索工具。

目前，漢字音典[已收錄](https://mcpdict.sourceforge.io/info.html)三千多個語言／方言點，內容涵蓋**歷史音、漢語方言、民語漢字音、域外方音、戲曲音**等多種類型。現階段的主要工作，是持續推進全國各地方言及語言的**同音字表及詞表的電子化**。

漢字音典為持續更新中的公開項目，亦歡迎對語言、方言與字詞表整理有興趣的朋友共同參與，或[提出寶貴建議](https://github.com/osfans/MCPDict/issues)。

## 二、項目沿革

漢字音典源自 @王贇 Maigo 開發的「[漢字古今中外讀音查詢（MCPDict）](https://github.com/MaigoAkisame/MCPDict)」程序。原項目始於 V1.0（2014.04.02），至 V3.0（2016.05.03） 後停止更新。

其後，@osfans 發起[漢字音典](https://github.com/osfans/MCPDict)項目，並在 2022.05.16 停更後再次恢復更新。此後，亦有眾多方言愛好者持續參與，為本項目的建設與發展貢獻力量。

## 三、收錄範圍

漢字音典目前的字表收錄範圍如下：

1.收錄內容包括：歷史音、漢語方言、民語漢字音、域外方音、戲曲音等。

2.收錄來源包括：公開發表的學術論文、書籍資料、愛好者田野調查成果，以及母語者自行製作的字詞表。

3.漢字音典所收字詞表為公開整理項目；如涉及版權疑慮，請及時聯係項目組。若個人提交的字表有正式發表規劃，原則上不予收錄。

4.漢字音典僅收錄真實存在的語言及韻書；對於構擬的原始語或人造語言，原則上不予收錄。

## 四、主要功能

- 通過漢字或讀音查詢各方言中的漢字讀音

- 選擇辭典、註釋，通過釋義反查漢字

- 根據字音和字形猜漢字

- 根據字音和相對方位猜方言、地區、分區

- 音典小工具功能藉鑒[古音小鏡·歷史語言學](http://www.kaom.net/)及[不羈](https://github.com/jengzang)開發的[方音圖鑑](https://dialects.yzup.top)，特此鳴謝。內置中古→方言、方言→中古、兩地對應、單字音類、字音比較、便攜Praat等工具。

**具體功能及使用方法可參見程序內「❔幫助」。**

## 五、軟件開發與發佈

- 本漢字音典項目爲**Android版本**

- 目前唯一發佈平臺為[GitHub](https://github.com/osfans/MCPDict)

- 軟件採用[GitHub Actions](https://github.com/osfans/MCPDict/actions)自動編譯生成，並**不定時更新**

- 最新版可於[GitHub Releases](https://github.com/osfans/MCPDict/releases/tag/latest)下載

相關網頁版或延伸工具如下所示：

### 相關項目

- [不羈](https://github.com/jengzang)開發的[方音圖鑑](https://dialects.yzup.top)（[前端](https://github.com/jengzang/dialects-js-frontend)、[後端](https://github.com/jengzang/dialects-backend)）：提供中古音查詢、音位查詢、聲調查詢、單字查詢、分區繪圖、自訂繪圖等功能。

- [唯二](https://github.com/vearvip)開發的[網頁版](https://mcpdict.vear.vip/)（[前端](https://github.com/vearvip/mcpdict-frontend)、[後端](https://github.com/vearvip/mcpdict-backend)）：提供字音查詢、長文注音、語言地圖、設定等功能。

- [綾香](https://github.com/ayaka14732)開發的[音典網頁版](https://nk2028.shn.hk/yindian/)（[前端](https://github.com/nk2028/yindian)、[後端](https://github.com/nk2028/yindian-server)）：提供字音查詢等功能。

- [薪典深耕版](https://c.syndict.com/zi)：查字使用漢字音典數據，實時更新。

- [ghehyoein](https://github.com/ghehyoein)開發的[iOS版](https://github.com/ghehyoein/MCPDict-iOS)

### 參考項目

- [漢字古今中外讀音查詢](https://github.com/MaigoAkisame/MCPDict)：可查多種語言中的漢字讀音，包括中古漢語、普通話、粵語、吳語（上海話）、閩南語、朝鮮語、越南語、日語

- [OpenCC](https://github.com/BYVoid/OpenCC)：提供了漢字繁簡轉換功能

- [方音圖鑑](https://dialects.yzup.top)（[前端](https://github.com/jengzang/dialects-js-frontend)、[後端](https://github.com/jengzang/dialects-backend)）：提供中古音查詢、音位查詢、聲調查詢、單字查詢、分區繪圖、自訂繪圖等功能。

- [古音小鏡·歷史語言學](http://www.kaom.net/)

## 六、主要工作內容

### 同音字表及詞表收錄

- 同音字表及詞表收錄是漢字音典目前最主要的工作內容，也是項目持續擴充資料庫的核心基礎。內容包括公開資料的電子化以及自行製作的字表。

- 字表：[已收錄](https://mcpdict.sourceforge.io/info.html)三千多種語言（方言）的漢字讀音及釋義，可去[QQ羣](https://qm.qq.com/cgi-bin/qm/qr?k=f2hquJ3ZWCDLpe8t63Ufw0NBHvGO3ZRb&jump_from=webapi&authKey=7GHTneh2MEuSa8X6wd8gJfutgwJiKMxR+N2HRKTEdb1TrRSg1r2hPhMnkf2Zakl+)提供同音字表請求收錄。

- 詞表：詞表正在建設中。

- 勘誤：參考[字表解析日誌](https://mcpdict.sourceforge.io/warnings.txt)，可去[QQ羣](https://qm.qq.com/cgi-bin/qm/qr?k=f2hquJ3ZWCDLpe8t63Ufw0NBHvGO3ZRb&jump_from=webapi&authKey=7GHTneh2MEuSa8X6wd8gJfutgwJiKMxR+N2HRKTEdb1TrRSg1r2hPhMnkf2Zakl+)、[GitHub Issues](https://github.com/osfans/MCPDict/issues)提出意見與建議。

## 七、團隊與分工

### 核心分工

- 漢字讀音數據庫及 Android 版軟件開發：@osfans

- 詞表數據庫及 Android 版軟件相關工具開發：ChatGPT、@Resound

- 檔案維護：@Resound 等

- 字表製作：由衆人共同參與；目前貢獻較多者為 @DaiDzao、@Resound

## 八、語言檔案說明

漢字音典的語言檔案由 @Resound 建立，負責項目中的語言資料管理。其主要內容包括：語言名稱及簡稱、提交文件名稱、語言行政區劃及地理坐標、聲調（調類、調名、調值）、參考文獻、錄入人、維護人、説明、多種分區信息等。目前檔案僅對參與字詞表製作的愛好者開放。

## 九、漢字與檢索說明

漢字音典預設使用**繁體字**顯示，但搜尋框可自動識別**簡體與繁體**輸入。

不同字表在繁體、簡體及本字使用上的差異，仍有待進一步整理與優化。

- 字體：可安裝[遍黑體](https://github.com/Fitzgerald-Porthmouth-Koenigsegg/Plangothic_Project)、[文津宋體](https://github.com/takushun-wu/WenJinMincho)等[字體包](https://github.com/osfans/MCPDict/releases/tag/fonts)顯示所有漢字

- 漢字：已收錄□（合音字、本字不明、有音無字）、〇（“星”或“零”）、統一碼18.0的全部漢字（不含部首及兼容區）共101999字

## 十、分區方案

漢字音典提供**地圖集二分區，**以便搜尋、整理與歸類。其基本依據《中國語言地圖集 第2版 漢語方言卷（2012）》的相關文字說明劃分；對於地圖集中未明確提及的方言，則按相近標準歸類。

此外，用戶可通過自選語言功能實現**自主分區**功能，即創建多個自選語言方案，以便依需求進行整理與檢索。

## 十一、免責聲明

1.漢字音典大部分字詞表由 OCR 輔助並經人工校對整理而成，雖已盡力核校，仍難以完全避免錯誤。使用音典資料時，請務必自行查閱原始字詞表或資料出處，不宜直接引用漢字音典作為唯一依據。漢字音典僅為便於快速查閱的工具；凡因未經核實而產生的誤解或錯誤，均與本項目無關。

2.漢字音典中部分字詞表經過修改，涉及部分音值和音類，與字表出處存在一些差異。若有未注明的地方，敬請諒解。引用資料時務必查閱原始字表或資料出處。

3.漢字音典中大部分字詞表由愛好者獨立製作；如某方言點的漢字讀音存在問題，歡迎向漢字音典項目組或相應字詞表製作者反映。

4.**「音典」分區與漢字音典官方無關，凡以漢字音典及團隊名義在愛好者群內肆意兜售分區的都是騙子。**此前因名稱相似且寄于漢字音典發佈，引發較多爭議，現不再默認提供。

5.**漢字音典尊重不同的分區方案。**分區無論優劣，僅為目的考慮。任何人均可以有自己的意見與方案。語音、詞匯、語法；共時形態、親緣關係；調查便利、圈定範圍……每種劃分都有其意義，言之有理即可。**分區不是目的，漢字音典本身也並不在意分區。做好語言的田野調查和資料整理才是最重要的。關注好語言本身。**

**本篇説明主要由@Resound撰寫。解釋權歸撰寫人所有。**
