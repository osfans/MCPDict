import java.util.*;
import com.osfans.mcpdict.PhonologyOrders;
public class DivisionChecks {
 public static void main(String[] args){
  if(!PhonologyOrders.division("三","A").equals("三A"))throw new AssertionError();
  if(!PhonologyOrders.division("三","B").equals("三B"))throw new AssertionError();
  if(!PhonologyOrders.division("三","C").equals("三C"))throw new AssertionError();
  if(!PhonologyOrders.division("三","").equals("三"))throw new AssertionError("untagged third must stay untagged");
  if(!PhonologyOrders.ordered(4,Arrays.asList("四","三C","二","三A","三B","一")).equals(Arrays.asList("一","二","三","三A","三B","三C","四")))throw new AssertionError("order");
  for(String v:new String[]{"三","三A","三B","三C"})if(!PhonologyOrders.matches(4,Collections.singleton("三"),v))throw new AssertionError("parent "+v);
  if(PhonologyOrders.matches(4,Collections.singleton("三A"),"三B"))throw new AssertionError("subclass");
  if(PhonologyOrders.matches(4,Collections.singleton("三"),"四"))throw new AssertionError("fourth");
  if(!PhonologyOrders.matches(4,new HashSet<>(Arrays.asList("三A","三C")),"三C"))throw new AssertionError("multi");
  System.out.println("12 division parsing/filter/order checks passed");
 }
}
