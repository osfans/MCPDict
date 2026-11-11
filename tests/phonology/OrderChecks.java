import com.osfans.mcpdict.PhonologyOrders;
import java.util.*;
public class OrderChecks {
    static int passed;
    static void order(int index,String input,String expected){String actual=String.join(",",PhonologyOrders.ordered(index,Arrays.asList(input.split(","))));if(!actual.equals(expected))throw new AssertionError(actual);passed++;}
    public static void main(String[] args){order(0,"見,並,幫,明,滂","幫,滂,並,明,見");order(1,"東,歌,眞,麻,魚","歌,麻,魚,眞,東");order(2,"入,去,上,平","平,上,去,入");order(7,"次濁,次清,全濁,全清","全清,次清,全濁,次濁");if(!PhonologyOrders.canonical("孃羣真鐘").equals("娘群眞鍾"))throw new AssertionError();System.out.println((passed+1)+" ordering checks passed");}
}
