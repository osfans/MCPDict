#!/usr/bin/env python3
from openpyxl import Workbook
from openpyxl.cell.text import InlineFont 
from openpyxl.cell.rich_text import TextBlock, CellRichText
import sys, os

fname = sys.argv[1]
rich = True if len(sys.argv) > 2 else False
tname = os.path.basename(fname.replace(".tsv", ".xlsx"))

doc = Workbook()
for line in open(fname, encoding="utf-8"):
    line = line.strip()
    cells = []
    for j in line.split("\t"):
        if not rich:
            cells.append(j)
            continue
        cell = CellRichText()
        last = ""
        sub = False
        for i in j:
            if i == "{":
                sub = True
                last = ""
            elif i == "}":
                font = InlineFont(vertAlign='subscript')
                block = TextBlock(font, last)
                cell.append(block)
                sub = False
            elif i == "-":
                cell[-1].font.underline = "single"
            elif i == "=":
                cell[-1].font.underline = "double"
            elif sub:
                last += i
            else:
                cell.append(TextBlock(InlineFont(), i))
        cells.append(cell)
    doc.active.append(cells)
doc.save(tname)