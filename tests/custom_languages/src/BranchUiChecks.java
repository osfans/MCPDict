import com.osfans.mcpdict.custom.*;import com.osfans.mcpdict.Util.Pref;import java.util.*;
public class BranchUiChecks {
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
 public static void main(String[] args){
  Pref.legacy="[]";CustomSchemes.create("移動");var s=CustomSchemes.current();s.groups.addAll(List.of("A／未分區／C","X"));
  s.colors.put("A／未分區","#AABBCC");s.entries.add(new CustomSchemes.Entry("溫州","A-未分區-C","#123456"));CustomSchemes.replaceCurrent(s);
  String id=CustomSchemes.current().entries.getFirst().id();CustomSchemes.setScope("A／未分區／C");
  check(CustomSchemes.reparentGroup("A／未分區","X"),"ordinary bucket can move");
  var moved=CustomSchemes.current();check(moved.entries.getFirst().id().equals(id)&&moved.entries.getFirst().group().equals("X／未分區／C"),"branch and IDs moved");
  check(moved.colors.get("X／未分區").equals("#AABBCC")&&CustomSchemes.scope().equals("X／未分區／C"),"color and selection moved");
  check(!CustomSchemes.reparentGroup("X／未分區","X／未分區／C"),"reject cycle");
  check(CustomSchemes.languageCount("X")==1&&CustomSchemes.languageCount("")==1,"aggregate and whole-scheme language counts");
  CustomSchemes.createGroup("A／未分區");check(!CustomSchemes.reparentGroup("X／未分區","A"),"reject collision");
  System.out.println("PASS ordinary bucket branch moves, IDs/colors/selection, cycle/collision rejection and language counts.");
 }
}
