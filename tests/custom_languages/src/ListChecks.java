import android.database.*;
import com.osfans.mcpdict.UI.ListReadings;
public class ListChecks {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  String[] columns={"漢字","語言","讀音","註釋","variants","custom_entry","custom_color","custom_group"};
  MatrixCursor source=new MatrixCursor(columns);
  source.addRow(new Object[]{"心","甲","","","","gap","#000000","A"});
  source.addRow(new Object[]{"心","乙","sin","註","","id1","#123456","A"});
  source.addRow(new Object[]{"心","乙","sin","註","","id2","#654321","B"});
  source.addRow(new Object[]{"新","甲",null,"","","gap2","",""});
  source.addRow(new Object[]{"新","乙","   ","","","gap3","",""});
  source.addRow(new Object[]{"字","","","","","","",""});
  source.moveToPosition(2);MatrixCursor list=(MatrixCursor)ListReadings.copy(source);
  check(source.size()==6&&source.getPosition()==2&&!source.isClosed(),"table source remains intact and positioned");
  check(list.size()==3,"hide missing/null/whitespace readings, preserve character-only row");
  list.moveToFirst();check(list.getString(1).equals("乙")&&list.getString(5).equals("id1")&&list.getString(6).equals("#123456"),"real row metadata and order");
  list.moveToNext();check(list.getString(5).equals("id2"),"duplicate membership retained");
  list.moveToNext();check(list.getString(0).equals("字"),"character heading retained");
  list.close();check(!source.isClosed(),"closing list cannot close table cursor");
  MatrixCursor onlyGap=new MatrixCursor(columns);onlyGap.addRow(new Object[]{"無","甲","","","","","",""});
  check(((MatrixCursor)ListReadings.copy(onlyGap)).size()==0,"all missing means no list matches");
  check(ListReadings.copy(null)==null,"empty search remains empty");
  System.out.println("PASS missing-reading filter, real readings and metadata, duplicate membership, character-only mode and independent cursor ownership.");
 }
}
