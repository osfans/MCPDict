import com.osfans.mcpdict.custom.*;import java.util.*;import java.io.*;
public class ModalColorChecks {
 static void require(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  List<List<String>> rows=List.of(List.of("語言","自選分區","自選顏色","音典顏色"),List.of("甲","A-B","#AA0000","#000000"),List.of("乙","A-B","#AA0000","#000000"),List.of("丙","A-B","#0000BB","#000000"),List.of("丁","A-B","","#000000"),List.of("戊","A-C","#00CC00","#000000"),List.of("己","A-C","#CC00CC","#000000"));
  var out=new ByteArrayOutputStream();SchemeWorkbook.write(out,rows);var s=SchemeWorkbook.readData(new ByteArrayInputStream(out.toByteArray())).scheme("推斷");
  require(s.colors.get("A／B").equals("#AA0000"),"group majority");
  require(s.entries.get(0).color().isEmpty()&&s.entries.get(1).color().isEmpty(),"majority languages inherit");
  require(s.entries.get(2).color().equals("#0000BB")&&s.entries.get(3).color().isEmpty(),"minority and blank preserved");
  require(s.colors.get("A／C").equals("#00CC00"),"tie uses first color");
  require(s.colors.get("A").equals("#AA0000"),"parent includes descendants");
  require(s.entries.get(5).color().equals("#CC00CC"),"tie minority override retained");
  require(!s.colors.containsValue("#000000"),"auxiliary colors ignored");
  require(s.entries.get(0).language().equals("甲")&&s.entries.get(5).language().equals("己"),"record order unchanged");
  var zip=new java.util.zip.ZipInputStream(new ByteArrayInputStream(out.toByteArray()));int sheets=0;java.util.zip.ZipEntry part;while((part=zip.getNextEntry())!=null)if(part.getName().startsWith("xl/worksheets/"))sheets++;
  require(sheets==1,"single-sheet format");
  System.out.println("PASS majority color inference, inheritance reset, minority overrides, ties, blanks, parent grouping and single-sheet export.");
 }
}
