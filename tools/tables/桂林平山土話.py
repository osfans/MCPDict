#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._縣志 import 字表 as 表

class 字表(表):
	key = "xxx_glps"
	_file = "桂林平山土话同音字表.tsv"
	#disorder = True

