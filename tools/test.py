import sqlite3, sys
hz = sys.argv[1]
dic = "漢大" if len(sys.argv) <= 2 else sys.argv[2]
dbname = '../app/src/main/assets/databases/mcpdict.db'
conn = sqlite3.connect(dbname)
conn.row_factory = sqlite3.Row
c = conn.cursor()
c.execute(f'SELECT * FROM mcpdict where `漢字` match "{hz}"')
result = c.fetchall()
for i in result:
 print(dict(i)[dic])
c.close()