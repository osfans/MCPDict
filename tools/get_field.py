# get field from mcpdict
import os, sqlite3, sys

field = sys.argv[1]
NAME = os.path.join('..', 'app/src/main/assets/databases/mcpdict.db')
conn = sqlite3.connect(NAME)
c = conn.cursor()
c.execute(f"SELECT 漢字,{field} FROM mcpdict")
rows = c.fetchall()
t = open(f"{field}.txt", "w", encoding="U8", newline="\n")
for row in rows:
	if not all(row):
		continue
	for j in row[1].split("\t"):
		t.write(row[0] + "\t" + j + "\n")
t.close()
