#!/usr/bin/env python3
"從Unihan提取異體字"

from collections import defaultdict,OrderedDict

def hex2chr(uni):
    "把unicode轉換成漢字"
    return chr(int(uni[2:], 16))

def get():
    "主程序"
    fname = "orthography_hz_variants.txt"
    org = OrderedDict()
    for line in open(fname):
        line = line.strip()
        org[line[0]]=line[1:]
        
    jtd = defaultdict(set)
    for line in open("../../ytenx/ytenx/sync/jihthex/JihThex.csv"):
        line = line.strip()
        if line.startswith("#"): continue
        han = line[0]
        jts = line[1:].replace(",","")
        if han in org:
            for i in jts:
                if i not in org[han] and i != han:
                    org[han] += i
        else:
            org[han] = jts
    for line in open("../../ytenx/ytenx/sync/jihthex/ThaJihThex.csv"):
        line = line.strip()
        if line.startswith("#"): continue
        han = line[0]
        jts = line[1:].replace(",","")
        if han in org:
            for i in jts:
                if i not in org[han] and i != han:
                    org[han] += i
        else:
            org[han] = jts

    dic = defaultdict(set)
    for line in open("/usr/share/unicode-data/Unihan_Variants.txt"):
        line = line.strip()
        if not line:
            continue
        fields = line.split("\t", 2)
        if len(fields) == 3:
            han, var_type, var = fields
            if var_type == "kSpoofingVariant": continue
            if var_type == "kSpecializedSemanticVariant": continue
            for uni in var.split(" "):
                uni = uni.split("<")[0]
                if hex2chr(han) != hex2chr(uni):
                    dic[hex2chr(han)].add(hex2chr(uni))
    for line in open("異體字字典"):
        line = line.strip()
        dic[line[0]].update(set(line[1:]))
    for han in sorted(dic.keys()):
        if han in org:
            for i in dic[han]:
                if i not in org[han]:
                    org[han]+=i
        if han not in org:
            org[han] = "".join(dic[han]).strip()

    return {han:org[han] for han in org if org[han]}
