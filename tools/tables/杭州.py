#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "wuu_th_hz_hz"
	toneNames = {"陰平33": 1, "陽平213": 2, "陰上53": 3, "陰去445": 5, "陽去13": 6, "陰入5": 7, "陽入2": 8}
