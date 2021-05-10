#!/usr/bin/env python3
"從Unihan提取兼容字"

from collections import defaultdict,OrderedDict

def hex2chr(uni):
    "把unicode轉換成漢字"
    return chr(int(uni[2:], 16))

def main():
    "主程序"
    fname = "../app/src/main/res/raw/orthography_hz_compatibility.txt"
    target = open(fname, "w", encoding="U8")
    for line in open("/usr/share/unicode/Unihan_IRGSources.txt"):
        if not line.startswith("U"): continue
        fields = line.strip().split("\t", 2)
        han, typ, val = fields
        if typ == "kCompatibilityVariant":
          print("%s%s" % (hex2chr(han), hex2chr(val)), file=target)
    target.close()

if __name__ == "__main__":
    main()
