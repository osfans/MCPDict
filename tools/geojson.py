#!/usr/bin/env python3

import json
from openpyxl import load_workbook

FeatureCollection = {
  "type": "FeatureCollection",
  "features": []
}

sheets = load_workbook("汉音字典字表档案（长期更新）.xlsx")
sheet = sheets["档案"]

for row in sheet.rows:
	fs = [row[i].value if row[i].value else "" for i in range(26)]
	if not fs[0] or not fs[10]:
		continue
	jd, wd = fs[10], fs[11]
	try:
		jd = float(jd)
		wd = float(wd)
	except:
		continue
	color = row[1].fill.fgColor.value[2:]
	place = "".join(fs[12:17])
	size = "small"
	if "市中心" in place: size = "large"
	elif "中心" in place: size = "medium"
	Feature = {
		"type": "Feature",
		"properties": {
			"方言": fs[1],
			"方言片": "".join(fs[5:8]),
			"地点": place,
			"marker-size": size,
			"marker-color": color,
			"marker-symbol": fs[0][0].lower(),
		},
		"geometry": {
			"type": "Point",
			"coordinates": [jd, wd]
		}
	}
	if fs[8]:
		Feature["properties"]["方言岛"] = fs[8]
	if fs[22]:
		Feature["properties"]["录入人"] = fs[22]
	if fs[23]:
		value = fs[23]
		if row[23].hyperlink:
			target = row[23].hyperlink.target
			value = f"<a herf={target}>{value}</a>"
		Feature["properties"]["参考文献"] = value
	if fs[24]:
		Feature["properties"]["繁简"] = fs[24]
	FeatureCollection["features"].append(Feature)

json.dump(FeatureCollection, fp=open("../方言.geojson","w"),ensure_ascii=False,indent=2)
