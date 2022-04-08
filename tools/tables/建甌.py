#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "mnp_jo"
	toneNames = {"平聲54": 1, "上聲21": 3, "陰去33": 5, "陽去44": 6, "陰入24": 7, "陽入42": 8}
