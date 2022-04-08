#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "xxx_lgh"
	note = "說明：<br>1.本帖整理自《臨高漢詞典》<br>2.本帖只收錄臨高話的漢字讀書音，不收老藉詞音，也不收海南話、儋州話、粵語、普通話借音<br>3.陽平和上聲少數字互混，可能是受到海口話影響（海口話陽平31接近臨高話漢字音上聲21，海口話上聲213接近臨高話漢字音陽平13）<br>4.個別字聲韻屬老藉詞音，聲調屬讀書音，標<><br>特點：<br>1.見系戈韻低化回ua，但端系歌戈不能分，說明端系戈韻死得異常早<br>2.影母完全獨立，喻母讀j-、v-<br>3.疑母開口一三四等都不掛，但二等有一部分讀j-。然而這一部分字也不混喻母：雅ja!=惹jia，眼jan!=衍jian。疑母合口呼也不掛<br>4.覃韻、合韻有殘留<br>5.山攝見二開全無介音，但疑母讀j-暴露了其介音失落是後起的。可以參考粵語的ɐ類韻。<br>"
	_file = "临高话汉字音同音字汇.txt"

	def format(self, line):
		if " " not in line: return "#"
		line = re.sub("<(.*?)>","\\1{讀書音}",line)
		line = re.sub("\[(.*?)\]","\\1{特殊音}",line)
		line = re.sub("(.)\*","\\1{海口話影響}",line)
		line = re.sub("([1-5])", "[\\1]", line)
		line = re.sub("([ptk]) ", "\\1 [5]", line)
		line = re.sub("(.*?)\[", "\\1	[", line)
		return line
