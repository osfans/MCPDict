#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_tt_yz"
	_file = "扬中.tsv"
	
	def format(self, line):
		if '""	"#' in line: line = line.split("\t", 1)[1]
		return line

