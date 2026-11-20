
package android.database;
import java.util.*;
public class MatrixCursor implements Cursor {
    private final String[] columns;
    private final List<Object[]> rows=new ArrayList<>();
    private int position=-1;
    public MatrixCursor(String[] c) {columns=c;}
    public void addRow(Object[] row) {rows.add(row);}
    public boolean isAfterLast(){return position>=rows.size();}
    public boolean moveToFirst(){position=0; return !rows.isEmpty();}
    public boolean moveToNext(){return ++position<rows.size();}
    public int getColumnIndex(String name){return Arrays.asList(columns).indexOf(name);}
    public String getString(int index){Object s=rows.get(position)[index]; return s==null?null:s.toString();}
    public int size(){return rows.size();}
    private boolean closed;
    public String[] getColumnNames(){return columns.clone();}
    public int getPosition(){return position;}
    public boolean moveToPosition(int index){position=index;return index>=0&&index<rows.size();}
    public boolean isClosed(){return closed;}
    public void close(){closed=true;}
}
