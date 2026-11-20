# 自選語言回歸檢查

需要 Python 3、JDK 21、org.json 及 kxml2 2.3.0 的 JAR。測試只替代 Android 的游標、偏好設置、顏色及 XML 入口；分類、Excel 和結果排序使用正式源碼。

```sh
python tests/custom_languages/run.py --json-jar /path/to/json.jar --xml-jar /path/to/kxml2-2.3.0.jar
```

JDK 不在 PATH 時可加 `--java-home /path/to/jdk`。介紹與幫助另用標準庫檢查：

```sh
python tests/custom_languages/documentation_checks.py
```

涵蓋父級未分區遷移、記錄 ID 與顏色保留、兄弟分區重複、持久化、整體顏色覆蓋與繼承、樹狀排序、Excel 單表／共享字串／無效資料、列表空讀音與注釋合併順序。測試不會啟動 Android 裝置，也不會修改使用者資料。
