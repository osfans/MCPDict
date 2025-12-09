#!/usr/bin/env python3
import sys, re

if len(sys.argv) != 3:
	print(f"Usage: {sys.argv[0]} input output")
	sys.exit(1)
input, output = sys.argv[1:3]

from tables._表 import IPA_PATTERN
音典 = dict()

聲母表 = ["b", "p", "pʰ", "m", "f", "v",
	"d", "t", "tʰ", "n", "l", "g", "k", "kʰ", "ŋ", "ɦ", "h", "x", "ɣ", 
	"dʑ", "tɕ", "tɕʰ", "ʑ", "ɕ", "ȵ", "ɲ", "dz", "ts", "tsʰ", "z", "s", "dʐ", "tʂ", "tʂʰ", "ɻ", "ʐ", "ʂ", "ɖ", "ʈ","ȵ", "j", "c", "q", "w", "0", ""]

def split(yb):
	syd = IPA_PATTERN.findall(yb)
	syd = syd[0]
	return syd[1], syd[0], syd[2]

def index(yb):
	syd = IPA_PATTERN.findall(yb)
	syd = syd[0]
	return syd[1], 聲母表.index(syd[0]) if syd[0] in 聲母表 else 100, syd[2]

empty = True
with open(input, "r", encoding="utf-8") as infile, open(output, "w", encoding="utf-8") as outfile:
	for line in infile:
		if line.startswith("#") or not line.strip():
			continue
		字, 音, 註 = line.rstrip("\n").split("\t")
		if 音[-1] in "+-=":
			字 += 音[-1]
			音 = 音[:-1]
		if 音 not in 音典:
			音典[音] = []
		音典[音].append((字, 註))
	韻母0, 聲母0, 聲調0 = "/", "/", "/"
	for 音 in sorted(音典.keys(),key=index):
		韻母, 聲母, 聲調 = split(音)
		if 聲母 == "": 聲母 = "0"
		if 韻母 !=韻母0:
			聲母0, 聲調0 = "/", "/"
			outfile.write(f"#{韻母}" if empty else f"\n\n#{韻母}")
			empty = False
		if 聲母 != 聲母0:
			聲調0 = "/"
			outfile.write(f"\n{聲母}\t")
		if 聲調 != 聲調0:
			outfile.write(f"[{聲調}]")
		for 字, 註 in 音典[音]:
			if 註:
				outfile.write(f"{字}{{{註}}}")
			else:
				outfile.write(f"{字}")
		韻母0, 聲母0, 聲調0 =韻母, 聲母, 聲調