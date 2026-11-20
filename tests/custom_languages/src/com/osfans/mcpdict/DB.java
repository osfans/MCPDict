
package com.osfans.mcpdict;
import android.database.*;
public class DB {
    public enum FILTER { CUSTOM,ALL }
    public static final int COL_HZ=0,COL_LANG=1,COL_IPA=2;
    public static String[] getLanguages(){return new String[]{"普通話","溫州","歷史音","重名"};}
    public static int getColor(String label){return 0xff112233;}
    public static Cursor getCursor(String sql) {
        MatrixCursor c=new MatrixCursor(new String[]{"語言","簡稱","方言島","歷史音"});
        c.addRow(new Object[]{"普通話","普","0","0"});
        c.addRow(new Object[]{"溫州","溫","1","0"});
        c.addRow(new Object[]{"歷史音","古","0","red"});
        c.addRow(new Object[]{"重名","重名甲","0","0"});
        c.addRow(new Object[]{"重名","重名乙","0","0"});
        c.moveToFirst();
        return c;
    }
}
