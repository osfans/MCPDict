import com.osfans.mcpdict.custom.*;
import com.osfans.mcpdict.Util.Pref;
import android.database.MatrixCursor;
import java.util.*;
public class SchemeChecks {
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 public static void main(String[] args){
  Pref.legacy="[{\"name\":\"原方案\",\"languages\":[\"溫州\",\"普通話\"]}]";
  check(CustomSchemes.currentName().equals("原方案"),"legacy migration name");
  check(!CustomSchemes.deduplicate(),"default keeps duplicates");
  check(new ArrayList<>(CustomSchemes.languages()).equals(List.of("溫州","普通話")),"migration order");
  check(CustomSchemes.create("私人"),"create scheme");
  var s=CustomSchemes.current();s.groups.addAll(List.of("A-B".replace('-','／'),"C"));
  s.entries.add(new CustomSchemes.Entry("溫州","A-B",""));
  s.entries.add(new CustomSchemes.Entry("普通話","A-B","#334455"));
  s.entries.add(new CustomSchemes.Entry("溫州","C","#00AA00"));
  s.entries.add(new CustomSchemes.Entry("未上架","C",""));
  s.entries.add(new CustomSchemes.Entry("重名","C",""));
  s.colors.put("A","#123456");CustomSchemes.replaceCurrent(s);
  check(CustomSchemes.visibleEntries().size()==3,"unavailable and ambiguous languages excluded");
  check(CustomSchemes.visibleEntries().getFirst().color().equals("#123456"),"ancestor color inherited");
  CustomSchemes.setDeduplicate(true);check(CustomSchemes.visibleEntries().size()==2,"optional duplicate display filter");
  check(CustomSchemes.current().entries.size()==5,"dedup never deletes records");CustomSchemes.setDeduplicate(false);
  var input=new MatrixCursor(new String[]{"漢字","語言","讀音","註釋","variants"});
  input.addRow(new Object[]{"心","普","xin","",""});input.addRow(new Object[]{"心","溫","sin","",""});
  var result=(MatrixCursor)CustomResults.arrange(input);check(result.size()==3,"result memberships duplicated");
  result.moveToFirst();check(result.getString(1).equals("溫")&&CustomResults.color(result).equals("#123456"),"result order/colors");
  s=CustomSchemes.current();CustomSchemes.applyOverallColor(s,"A","#AA0000",false);CustomSchemes.replaceCurrent(s);
  check(CustomSchemes.visibleEntries().get(1).color().equals("#334455"),"retain individual override");
  s=CustomSchemes.current();CustomSchemes.applyOverallColor(s,"A","#AA0000",true);CustomSchemes.replaceCurrent(s);
  check(CustomSchemes.visibleEntries().get(1).color().equals("#AA0000"),"bulk reset override");
  check(CustomSchemes.visibleEntries().get(2).color().equals("#00AA00"),"other branch untouched");
  var groups=new ArrayList<>(CustomSchemes.groups());int a=groups.indexOf("A"),c=groups.indexOf("C");
  check(CustomSchemes.moveGroupBranch(groups,a,c),"move branch with descendants");
  check(groups.indexOf("A")<groups.indexOf("A／B"),"parent precedes children");
  check(!CustomSchemes.moveGroupBranch(groups,groups.indexOf("A／B"),groups.indexOf("C")),"reject cross-parent move");
  CustomSchemes.reorderGroups(groups);check(CustomSchemes.visibleEntries().getFirst().group().equals("C"),"language order follows branch");
  CustomSchemes.setScope("A");check(CustomSchemes.visibleEntries().size()==2,"parent scope aggregates");
  check(CustomSchemes.renameGroup("A","D"),"rename parent");check(CustomSchemes.scope().equals("D"),"renamed scope");
  check(CustomSchemes.deleteGroup("D"),"delete branch");check(CustomSchemes.visibleEntries().size()==1,"other branch retained");
  System.out.println("PASS legacy migration, matching, duplicates, colors, branch order, rename/delete and result memberships.");
 }
}
