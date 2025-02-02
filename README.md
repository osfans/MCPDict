# MCPDict/漢字音典

本程序源自[漢字古今中外讀音查詢](https://github.com/MaigoAkisame/MCPDict)，現收錄了[數百種語言（方言）](https://geojson.io/#id=github:osfans/MCPDict/blob/master/%E6%96%B9%E8%A8%80.geojson&map=3.02/37.65/108.48)的漢字讀音，使用國際音標注音，可查詢漢字在古今中外多種語言中的讀音及釋義，也能給語言學習者提供有限的幫助。可去[QQ群](mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D-hNzAQCgZQL-uIlhFrxWJ56umCexsmBi)、[GitHub](https://github.com/osfans/MCPDict)提出意見与建議，提供同音字表請求收錄。

本程序最開始僅有安卓離綫版，後來又開發了在綫網頁版，另外[nk2028](https://github.com/nk2028)和[唯二](https://github.com/vearvip)也各自開發了網頁版：
- [最新安卓版](https://github.com/osfans/MCPDict/releases/tag/latest)（[代碼](https://github.com/osfans/MCPDict)）：使用[Github Actions](https://github.com/osfans/MCPDict/actions)自動編譯生成的安卓版、[錯誤日志](https://mcpdict.sourceforge.io/warnings.txt)。  
- [最新網頁版](https://mcpdict.sourceforge.io/)（[代碼](https://github.com/osfans/MCPDict/tree/master/cgi/cgi-bin)）：提供了與安卓版界面類似的一部分功能。  
- [唯二網頁版](https://mcpdict.vear.vip/)（[前端](https://github.com/vearvip/mcpdict-frontend)、[後端](https://github.com/vearvip/mcpdict-backend)）：提供了字音查詢、長文注音、語言地圖、設置等功能。  
- [nk2028網頁版](https://nk2028.shn.hk/hdqt/)（[前端](https://github.com/nk2028/hdqt)、[後端](https://github.com/nk2028/hdqt-server)）：提供了字音查詢等功能。  

本程序收錄了[統一碼16.0](https://www.unicode.org/reports/tr38/tr38-37.html)全部漢字（不包含部首及兼容區漢字）、〇（同“星”或“零”）、□（有音無字、本字不明），可使用[文津宋體](https://github.com/takushun-wu/WenJinMincho)顯示所有漢字。支持形音義等多種查詢方式：可輸入𰻞（漢字）、30EDE（統一碼）、biang2（普通話拼音，音節末尾的“?”可匹配任何聲調）、43（總筆畫數）、辵39（部首餘筆），查到“𰻞”字。也可使用兩分、五筆、倉頡等形碼進行查詢，亦可選擇說文解字、康熙字典、漢語大字典等通過釋義搜索到相應的漢字。


