import com.osfans.mcpdict.custom.*;import java.util.*;
public class UnclassifiedChecks {
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 static List<String> names(CustomSchemes.Scheme s,String group){return s.entries.stream().filter(e->e.group().equals(group)).map(CustomSchemes.Entry::language).toList();}
 public static void main(String[] args){
  var s=new CustomSchemes.Scheme("分類");s.groups.add("A／B");s.colors.put("A／B","#112233");
  for(int i=1;i<=5;i++)s.entries.add(new CustomSchemes.Entry(""+i,"A-B",i==1?"#AABBCC":""));
  String id4=s.entries.get(3).id();CustomSchemes.normalizeSubgroups(s);check(names(s,"A／B").size()==5,"leaf keeps its direct languages");
  s.groups.addAll(List.of("A／B／C","A／B／D"));for(int i=1;i<=3;i++)s.entries.add(new CustomSchemes.Entry(""+i,"A-B-C",""));for(int i=6;i<=7;i++)s.entries.add(new CustomSchemes.Entry(""+i,"A-B-D",""));
  CustomSchemes.normalizeSubgroups(s);
  check(names(s,"A／B／未分區").equals(List.of("4","5"))&&names(s,"A／B").isEmpty(),"parent leftovers routed to unclassified");
  check(names(s,"A／B／C").equals(List.of("1","2","3"))&&names(s,"A／B／D").equals(List.of("6","7")),"named child memberships retained");
  check(s.entries.stream().anyMatch(e->e.id().equals(id4)&&e.group().equals("A／B／未分區")),"moved record ID retained");
  check(s.entries.stream().filter(e->e.language().equals("1")).findFirst().get().color().equals("#AABBCC")&&s.colors.get("A／B").equals("#112233"),"individual and parent colors retained");
  var snapshot=s.entries.stream().map(Object::toString).toList();CustomSchemes.normalizeSubgroups(s);check(s.entries.stream().map(Object::toString).toList().equals(snapshot),"normalization idempotent");
  s.entries.add(new CustomSchemes.Entry("8","A-B",""));CustomSchemes.normalizeSubgroups(s);check(names(s,"A／B／未分區").equals(List.of("4","5","8")),"adding at parent goes to its bucket");
  s.entries.add(new CustomSchemes.Entry("4","A-B-C",""));s.entries.add(new CustomSchemes.Entry("2","A-B-D",""));CustomSchemes.normalizeSubgroups(s);
  check(names(s,"A／B／未分區").equals(List.of("5","8"))&&s.entries.stream().filter(e->e.language().equals("2")).count()==2,"classified points leave bucket; sibling duplication allowed");
  com.osfans.mcpdict.Util.Pref.legacy="[]";CustomSchemes.create("新方案");check(CustomSchemes.groups().contains("未分區")&&CustomSchemes.current().entries.isEmpty(),"new empty scheme has unclassified bucket");
  CustomSchemes.replaceCurrent(s);check(CustomSchemes.current().entries.size()==s.entries.size(),"save/load retains normalized data");
  check(!CustomSchemes.renameGroup("A／B／未分區","其他")&&!CustomSchemes.deleteGroup("A／B／未分區"),"system bucket cannot be renamed/deleted");
  System.out.println("PASS parent leftovers, automatic classification, duplicate rules, IDs/colors/order, idempotence, new defaults and save/load.");
 }
}
