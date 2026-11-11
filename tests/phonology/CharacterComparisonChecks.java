import java.util.*;
import com.osfans.mcpdict.CharacterSoundComparison;
public class CharacterComparisonChecks {
 static Set<String> s(String... a){return new HashSet<>(Arrays.asList(a));}
 static int count;
 static void check(String expected,Set<String>... sets){String actual=CharacterSoundComparison.classify(Arrays.asList(sets));if(!expected.equals(actual))throw new AssertionError(actual);count++;}
 public static void main(String[] args){
  check("全同",s("tʂu55"),s("tʂu55"));
  check("全異",s("tʂu55"),s("tsu55"));
  check("部分相同",s("a","b"),s("b","c"));
  check("缺資料",s("a"),s());
  check("缺資料",s(),s());
  check("全同",s("a","b"),s("b","a"));
  check("部分相同",s("a"),s("a"),s("b"));
  check("全異",s("a"),s("b"),s("c"));
  check("缺資料",s("a"),s("a"),s());
  boolean refused=false;try{CharacterSoundComparison.classify(Arrays.asList(s("a")));}catch(IllegalArgumentException e){refused=true;}if(!refused)throw new AssertionError("one character must be rejected");
  System.out.println((count+1)+" character comparison checks passed");
 }
}
