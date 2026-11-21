import com.osfans.mcpdict.custom.*;import com.osfans.mcpdict.Util.Pref;import java.util.*;
public class BranchReparentChecks {
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
 public static void main(String[] args){
  Pref.legacy="[]";CustomSchemes.create("整枝移動");var s=CustomSchemes.current();
  s.groups.addAll(List.of("A／B／C／D","A／B／F","A／E"));
  s.entries.add(new CustomSchemes.Entry("普通話","A-B-C","#112233"));
  s.entries.add(new CustomSchemes.Entry("溫州","A-B-C-D","#445566"));
  s.entries.add(new CustomSchemes.Entry("歷史音","A-B-F","#778899"));
  s.colors.put("A／B／C","#ABCDEF");s.colors.put("A／B／C／D","#FEDCBA");CustomSchemes.replaceCurrent(s);
  Map<String,String> colors=new HashMap<>();for(var e:CustomSchemes.current().entries)colors.put(e.id(),e.color());
  for(int i=0;i<100;i++){
   CustomSchemes.setScope("A／B／C／D");
   check(CustomSchemes.reparentGroup("A／B／C","A／E"),"A-B-C-D to A-E-C-D");
   var moved=CustomSchemes.current();
   check(moved.entries.stream().anyMatch(e->e.group().equals("A／E／C／D")),"descendant D moved with C");
   check(CustomSchemes.scope().equals("A／E／C／D"),"active descendant follows branch");
   check(moved.colors.get("A／E／C").equals("#ABCDEF")&&moved.colors.get("A／E／C／D").equals("#FEDCBA"),"all group colors follow branch");
   check(CustomSchemes.reparentGroup("A／E／C","A／B"),"move back");
   var order=new ArrayList<>(CustomSchemes.groups());check(CustomSchemes.moveGroupBranch(order,order.indexOf("A／B／C"),order.indexOf("A／B／F")),"reorder sibling with D attached");CustomSchemes.reorderGroups(order);
   var current=CustomSchemes.current();check(current.entries.size()==colors.size(),"no record loss");
   for(var e:current.entries)check(Objects.equals(colors.get(e.id()),e.color()),"stable IDs and per-record colors");
   check(CustomSchemes.groups().stream().noneMatch(g->g.contains("未分區")),"no generated buckets");
  }
  check(!CustomSchemes.reparentGroup("A／B／C","A／B／C／D"),"cycle rejected");
  CustomSchemes.createGroup("A／E／C");check(!CustomSchemes.reparentGroup("A／B／C","A／E"),"same-name destination rejected");
  System.out.println("PASS 100 A-B-C-D <-> A-E-C-D moves and sibling reorders; descendants, IDs, colors and scope preserved; cycles/conflicts rejected.");
 }
}
