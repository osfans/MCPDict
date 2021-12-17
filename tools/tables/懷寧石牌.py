#!/usr/bin/env python3

from tables._空島 import 字表 as 表

class 字表(表):
	key = "gan_hy_hnsp"
	note = "版本：2021-12-30<br>方言點：安徽省安慶市懷寧縣石牌鎮<br>來源：《懷寧石牌方言語音系統》，“空島。”整理修改"
	tones = "31 1 1a 陰平 ꜀,44 2 1b 陽平 ꜁,42 3 2 上 ꜂,,35 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅"
	_file = "安徽省石牌方言同音字表*.tsv"
	_color = "#20B2AA,#800080"
	toneValues = {"①":1,"②":2,"③":3,"④":5,"⑤":6}
