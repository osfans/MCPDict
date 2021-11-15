#!/usr/bin/env python3

def hex2chr(uni):
	"把unicode轉換成漢字"
	if uni.startswith("U+"): uni = uni[2:]
	return chr(int(uni, 16))

def ishz(c):
	c = c.strip()
	if len(c) != 1: return False
	n = ord(c)
	return 0x3400<=n<0xA000 or n in (0x25A1, 0x3007) or 0xF900<=n<0xFB00 or 0x20000<=n<0x31350

def cjkorder(s):
	n = ord(s)
	return n + 0x10000 if n < 0x4E00 else n

