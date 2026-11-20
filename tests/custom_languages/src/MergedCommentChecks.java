import android.database.*;import com.osfans.mcpdict.UI.ListReadings;import java.util.*;
public class MergedCommentChecks {
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 static String sequence(MatrixCursor c){StringBuilder out=new StringBuilder();for(c.moveToFirst();!c.isAfterLast();c.moveToNext())out.append(c.getString(0)).append(c.getString(1)).append('|');return out.toString();}
 public static void main(String[] args){
  MatrixCursor source=new MatrixCursor(new String[]{"漢字","語言","讀音","註釋","variants","custom_entry","custom_color","custom_group"});
  for(String hz:List.of("甲","乙"))for(String lang:List.of("A","B"))source.addRow(new Object[]{hz,lang,"ipa-"+hz+lang,"note-"+hz+lang,"",lang,"#123456","分區"});
  source.moveToPosition(2);MatrixCursor normal=(MatrixCursor)ListReadings.copy(source,false),merged=(MatrixCursor)ListReadings.copy(source,true);
  check(source.getPosition()==2&&!source.isClosed(),"table/source cursor preserved");
  check(sequence(normal).equals("甲A|甲B|乙A|乙B|"),"disabled preserves per-character order");
  check(sequence(merged).equals("甲A|乙A|甲B|乙B|"),"enabled combines by language without losing characters");
  merged.moveToFirst();normal.moveToFirst();check(ListReadings.isMerged(merged)&&!ListReadings.isMerged(normal),"explicit presentation marker");
  check(merged.getString(3).equals("note-甲A")&&merged.getString(6).equals("#123456"),"annotation and color metadata retained");
  merged.moveToNext();check(merged.getString(0).equals("乙")&&merged.getString(2).equals("ipa-乙A"),"detail operations retain actual character and pronunciation");
  check(merged.size()==normal.size(),"all real results retained");
  merged.close();check(!source.isClosed(),"merged list does not own table source");
  System.out.println("PASS optional language grouping, default list order, real-character detail metadata, annotations and independent source cursor.");
 }
}
