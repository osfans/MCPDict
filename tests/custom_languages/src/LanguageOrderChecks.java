import android.database.*;import com.osfans.mcpdict.UI.ListReadings;import java.util.*;
public class LanguageOrderChecks {
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 static String values(MatrixCursor c,int index){StringBuilder text=new StringBuilder();for(c.moveToFirst();!c.isAfterLast();c.moveToNext())text.append(c.getString(index)).append('|');return text.toString();}
 public static void main(String[] args){
  MatrixCursor nativeRows=new MatrixCursor(new String[]{"漢字","語言","讀音","註釋","variants"});
  nativeRows.addRow(new Object[]{"甲","B","b1","n1",""});nativeRows.addRow(new Object[]{"乙","A","a1","n2",""});nativeRows.addRow(new Object[]{"丙","B","b2","n3",""});
  nativeRows.moveToPosition(1);MatrixCursor ordered=(MatrixCursor)ListReadings.copy(nativeRows,true,List.of("A","B"));
  check(values(ordered,1).equals("A|B|B|"),"native language order independent of first character");
  check(values(ordered,0).equals("乙|甲|丙|"),"within-language character order preserved");
  check(nativeRows.getPosition()==1&&!nativeRows.isClosed(),"original cursor preserved");
  MatrixCursor custom=new MatrixCursor(new String[]{"漢字","語言","讀音","註釋","variants","custom_entry","custom_color","custom_group"});
  custom.addRow(new Object[]{"甲","B","b1","n1","","b","#112233","乙"});
  custom.addRow(new Object[]{"乙","A","a1","n2","","a","#445566","甲"});
  custom.addRow(new Object[]{"丙","B","b2","n3","","b","#112233","乙"});
  custom.addRow(new Object[]{"丁","A","a2","n4","","a2","#778899","丙"});
  MatrixCursor merged=(MatrixCursor)ListReadings.copy(custom,true,List.of("a2","a","b"));
  check(values(merged,5).equals("a2|a|b|b|"),"custom membership order includes duplicate-language memberships");
  check(values(merged,6).equals("#778899|#445566|#112233|#112233|"),"colors remain attached to their memberships");
  MatrixCursor disabled=(MatrixCursor)ListReadings.copy(custom,false,List.of("a2","a","b"));
  check(values(disabled,0).equals("甲|乙|丙|丁|"),"disabled mode keeps original character order");
  System.out.println("PASS configured native/custom language order, within-language result order, duplicate memberships and preserved metadata.");
 }
}
