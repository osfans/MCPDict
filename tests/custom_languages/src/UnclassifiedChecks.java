import com.osfans.mcpdict.custom.*;import com.osfans.mcpdict.Util.Pref;import java.util.*;
public class UnclassifiedChecks {
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
 public static void main(String[] args){
  Pref.legacy="[]";CustomSchemes.create("普通分區");check(CustomSchemes.groups().isEmpty(),"no default buckets");
  var s=CustomSchemes.current();s.groups.addAll(List.of("A","A／B","A／未分區","A／未分區／C"));
  s.entries.add(new CustomSchemes.Entry("普通話","A","#111111"));s.entries.add(new CustomSchemes.Entry("普通話","A-B","#222222"));
  s.entries.add(new CustomSchemes.Entry("溫州","A-未分區-C","#334455"));s.entries.add(new CustomSchemes.Entry("歷史音","","#445566"));
  CustomSchemes.replaceCurrent(s);var ids=CustomSchemes.current().entries.stream().map(CustomSchemes.Entry::id).toList();
  for(int i=0;i<30;i++){var groups=new ArrayList<>(CustomSchemes.groups());int a=groups.indexOf("A／B"),b=groups.indexOf("A／未分區");if(a>=0&&b>=0)CustomSchemes.moveGroupBranch(groups,a,b);CustomSchemes.reorderGroups(groups);}
  var after=CustomSchemes.current();check(after.entries.size()==4,"sorting preserves parent/child duplicate records");
  check(new HashSet<>(after.entries.stream().map(CustomSchemes.Entry::id).toList()).equals(new HashSet<>(ids)),"IDs unchanged after repeated sorting");
  check(CustomSchemes.groups().stream().filter(g->g.contains("未分區")).count()==3,"repeated sorting creates no extra buckets");
  check(CustomSchemes.renameGroup("A／未分區","A／常用"),"nested bucket can be renamed");
  check(CustomSchemes.current().entries.stream().anyMatch(e->e.group().equals("A／常用／C")&&e.color().equals("#334455")),"rename carries descendants and color");
  check(CustomSchemes.renameGroup("未分區","根部"),"root bucket can be renamed");
  check(CustomSchemes.current().entries.stream().anyMatch(e->e.group().equals("根部")&&e.color().equals("#445566")),"root record retained");
  check(CustomSchemes.deleteGroup("A／常用"),"bucket branch can be deleted");
  for(int i=0;i<5;i++)CustomSchemes.replaceCurrent(CustomSchemes.current());
  check(CustomSchemes.groups().stream().noneMatch(g->g.contains("未分區")),"renamed/deleted buckets do not regenerate");
  System.out.println("PASS repeated sorting/save/load never create buckets or remove memberships; root/nested buckets support rename/delete with IDs/colors preserved.");
 }
}
