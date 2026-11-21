import com.osfans.mcpdict.custom.*;import com.osfans.mcpdict.Util.Pref;import java.util.*;
public class FreeParentChecks {
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 public static void main(String[] args){
  Pref.legacy="[]";CustomSchemes.create("自由層級");var s=CustomSchemes.current();s.groups.addAll(List.of("A／B／C／D","X／Y／Z"));
  s.entries.add(new CustomSchemes.Entry("普通話","A-B-C-D","#123456"));s.entries.add(new CustomSchemes.Entry("溫州","A-B-C","#ABCDEF"));
  s.colors.put("A／B／C","#112233");CustomSchemes.replaceCurrent(s);var ids=CustomSchemes.current().entries.stream().map(CustomSchemes.Entry::id).toList();
  check(CustomSchemes.reparentGroup("A／B／C","X／Y／Z"),"move under arbitrarily deeper parent");
  check(CustomSchemes.current().entries.stream().anyMatch(e->e.group().equals("X／Y／Z／C／D")),"descendant follows");
  check(CustomSchemes.reparentGroup("X／Y／Z／C",""),"become top-level");check(CustomSchemes.groups().contains("C／D"),"subtree retained at top level");
  check(CustomSchemes.reparentGroup("C／D","X／Y／Z"),"move leaf arbitrarily");
  check(CustomSchemes.reparentGroup("X／Y／Z／D",CustomSchemes.parentGroup("X／Y／Z")),"outdent one level");check(CustomSchemes.groups().contains("X／Y／D"),"outdent result");
  var after=CustomSchemes.current();check(new HashSet<>(after.entries.stream().map(CustomSchemes.Entry::id).toList()).equals(new HashSet<>(ids)),"stable membership IDs");check(after.colors.get("C").equals("#112233"),"group color follows");
  s=CustomSchemes.current();s.entries.add(new CustomSchemes.Entry("歷史音","","#AABBCC"));CustomSchemes.replaceCurrent(s);
  check(CustomSchemes.current().entries.stream().anyMatch(e->e.group().equals("未分區")&&e.color().equals("#AABBCC")),"legacy empty root canonically remains ordinary unclassified");
  check(CustomSchemes.reparentGroup("未分區","X"),"ordinary unclassified can nest");check(CustomSchemes.reparentGroup("X／未分區",""),"ordinary unclassified can become top-level again");
  check(CustomSchemes.renameGroup("未分區","一般"),"root can rename after moving");
  System.out.println("PASS arbitrary parents, top-level promotion, outdent, subtrees, colors/IDs and legacy unclassified-root migration.");
 }
}
