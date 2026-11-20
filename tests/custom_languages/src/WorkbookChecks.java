import com.osfans.mcpdict.custom.*;
import java.util.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.zip.*;
public class WorkbookChecks {
static void require(boolean yes,String msg){if(!yes)throw new AssertionError(msg);}
public static void main(String[] args)throws Exception{
        List<List<String>> rows=List.of(
            List.of("語言","自選分區","自選顏色","地圖集二分區","音典分區"),
            List.of("溫州","甲／一","#123456","參考一","參考二"),
            List.of("溫州","乙","#00AA00","參考三","參考四"),
            List.of("未上架 & 名稱","未分區","","",""));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        SchemeWorkbook.write(bytes,rows);
        List<CustomSchemes.Entry> imported=SchemeWorkbook.read(new ByteArrayInputStream(bytes.toByteArray()));
        require(imported.size()==3&&imported.get(1).group().equals("乙"),"xlsx repeated rows");
        require(imported.get(2).language().equals("未上架 & 名稱"),"xml escaping");
        require(imported.get(0).color().equals("#123456"),"xlsx color");

        ByteArrayOutputStream ss=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(ss)){
            zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
            zip.write(("<sst xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'>"
                +"<si><t>语言</t></si><si><t>自选分区</t></si><si><t>自选颜色</t></si>"
                +"<si><r><t>温</t></r><r><t>州</t></r></si><si><t>私人/甲</t></si><si><t>#AABBCC</t></si></sst>").getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            zip.write(("<worksheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'><sheetData>"
                +"<row r='1'><c r='A1' t='s'><v>0</v></c><c r='B1' t='s'><v>1</v></c><c r='C1' t='s'><v>2</v></c></row>"
                +"<row r='2'><c r='A2' t='s'><v>3</v></c><c r='B2' t='s'><v>4</v></c><c r='C2' t='s'><v>5</v></c></row>"
                +"</sheetData></worksheet>").getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        List<CustomSchemes.Entry> shared=SchemeWorkbook.read(new ByteArrayInputStream(ss.toByteArray()));
        require(shared.size()==1&&shared.get(0).language().equals("温州")&&shared.get(0).group().equals("私人／甲"),"shared strings and simplified headers");
        byte[] malformed="not an xlsx".getBytes(StandardCharsets.UTF_8);
        try {SchemeWorkbook.read(new ByteArrayInputStream(malformed));throw new AssertionError("invalid file");}
        catch(IOException expected){}
        bytes.reset();
        SchemeWorkbook.write(bytes,List.of(rows.get(0),List.of("溫州","甲","red","","")));
        try{SchemeWorkbook.read(new ByteArrayInputStream(bytes.toByteArray()));throw new AssertionError("invalid color");}
        catch(IOException expected){}
System.out.println("PASS XLSX shared strings, simplified headers, repeated rows, escaping and rejection of invalid files/colors.");
}}
