package com.osfans.mcpdict;
import java.util.*;
/** Traditional orders shared with the original tools filters. */
public final class PhonologyOrders {
 public static final String[][] VALUES={
  {"幫","滂","並","明","端","透","定","泥","來","精","清","從","心","邪","知","徹","澄","娘","莊","初","崇","生","俟","章","昌","船","書","常","日","見","溪","群","疑","曉","匣","影","云","以"},
  {"歌","戈","麻","模","魚","虞","咍","灰","泰","皆","佳","夬","祭","廢","齊","支","脂","之","微","豪","肴","宵","蕭","侯","尤","幽","覃","談","咸","銜","鹽","嚴","添","凡","侵","寒","桓","山","刪","仙","元","先","痕","魂","眞","臻","諄","殷","文","唐","陽","江","登","蒸","庚","耕","清","青","東","冬","鍾"},
  {"平","上","去","入"},
  {"果","假","遇","蟹","止","效","流","咸","深","山","臻","宕","江","曾","梗","通"},
  {"一","二","三","三A","三B","三C","四"},
  {"開","合","未標"},
  {},
  {"全清","次清","全濁","次濁","未分類"}
 };
 public static List<String> ordered(int dimension,Collection<String> source){List<String> result=new ArrayList<>(new LinkedHashSet<>(source));if(dimension==4&&result.stream().anyMatch(v->v.startsWith("三"))&&!result.contains("三"))result.add("三");List<String> order=Arrays.asList(VALUES[dimension]);result.sort((a,b)->{int x=order.indexOf(a),y=order.indexOf(b);if(x<0)x=999;if(y<0)y=999;return x==y?a.compareTo(b):Integer.compare(x,y);});return result;}
 public static String division(String number,String subtype){return "三".equals(number)?number+subtype:number;}
 public static boolean matches(int dimension,Set<String> selected,String value){return selected==null||selected.isEmpty()||selected.contains(value)||(dimension==4&&selected.contains("三")&&value.startsWith("三"));}
 public static String canonical(String s){return s.replace("孃","娘").replace("羣","群").replace("真","眞").replace("鐘","鍾");}
}
