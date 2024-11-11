import sqlite3, sys
hz = sys.argv[1]
dic = "漢大" if len(sys.argv) <= 2 else sys.argv[2]
dbname = '../app/src/main/assets/databases/mcpdict.db'
conn = sqlite3.connect(dbname)
conn.row_factory = sqlite3.Row
c = conn.cursor()
if len(hz) > 1 and " " not in hz:
	hz = f'"{" ".join(hz)}"'
elif len(hz) > 1 and " " in hz:
	hzs = hz.split(" ")
	hz = " ".join([f'"{" ".join(i)}"' for i in hzs])
	print(hz)
c.execute(f'SELECT * FROM mcpdict where `{dic}` match \'{hz}\'')
result = c.fetchall()
count = 0
for i in result:
	count += 1
	print("-----")
	print(count, dict(i)["漢字"])
	print(dict(i)[dic])
c.close()