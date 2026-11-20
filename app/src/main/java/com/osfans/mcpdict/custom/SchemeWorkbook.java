package com.osfans.mcpdict.custom;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

/** Small OOXML reader/writer for editable classification tables. */
public final class SchemeWorkbook {
    private static final int MAX_EXPANDED_BYTES = 64 * 1024 * 1024;
    private SchemeWorkbook() {}

    public static final class Data {
        public final List<CustomSchemes.Entry> entries = new ArrayList<>();
        public final LinkedHashMap<String,String> groupColors = new LinkedHashMap<>();
        public String schemeColor = "";
        public boolean structured;
        public CustomSchemes.Scheme scheme(String name) {
            CustomSchemes.Scheme result = new CustomSchemes.Scheme(name); result.entries.addAll(entries);
            for (Map.Entry<String,String> group : groupColors.entrySet()) {
                result.groups.add(group.getKey()); result.groupOrder.add(CustomSchemes.groupTitle(group.getKey()));
                if (!group.getValue().isEmpty()) result.colors.put(group.getKey(),group.getValue());
            }
            if (!schemeColor.isEmpty()) result.colors.put("",schemeColor);
            CustomSchemes.normalizeSubgroups(result);
            if (!structured) inferGroupColors(result);
            return result;
        }
    }
    private static void inferGroupColors(CustomSchemes.Scheme scheme) {
        Map<String,LinkedHashMap<String,Integer>> votes=new LinkedHashMap<>();
        for(CustomSchemes.Entry entry:scheme.entries) {
            if(entry.color().isEmpty())continue;
            String group=entry.group();
            while(true) {
                LinkedHashMap<String,Integer> counts=votes.computeIfAbsent(group,key->new LinkedHashMap<>());
                counts.put(entry.color(),counts.getOrDefault(entry.color(),0)+1);
                if(group.isEmpty())break;
                int split=group.lastIndexOf('／');
                if(split<0)break;
                group=group.substring(0,split);
            }
        }
        for(Map.Entry<String,LinkedHashMap<String,Integer>> group:votes.entrySet()) {
            String winner="";int most=0;
            for(Map.Entry<String,Integer> color:group.getValue().entrySet())if(color.getValue()>most){winner=color.getKey();most=color.getValue();}
            if(!winner.isEmpty())scheme.colors.put(group.getKey(),winner);
        }
        for(int i=0;i<scheme.entries.size();i++) {
            CustomSchemes.Entry entry=scheme.entries.get(i);
            if(!entry.color().isEmpty()&&entry.color().equals(scheme.colors.get(entry.group())))
                scheme.entries.set(i,new CustomSchemes.Entry(entry.id(),entry.language(),entry.group(),""));
        }
    }
    public static List<CustomSchemes.Entry> read(InputStream input) throws IOException { return readData(input).entries; }
    private static String checkedColor(String value) throws IOException {
        String color=value.trim().toUpperCase(Locale.ROOT);
        if(!color.isEmpty()&&!color.matches("#[0-9A-F]{6}")) throw new IOException("顏色須為 #RRGGBB，例如 #B71C1C。");
        return color;
    }
    public static Data readData(InputStream input) throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        int size = 0;
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (!(name.equals("xl/sharedStrings.xml") || name.startsWith("xl/worksheets/") && name.endsWith(".xml"))) continue;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int count;
                while ((count = zip.read(buffer)) != -1) {
                    size += count;
                    if (size > MAX_EXPANDED_BYTES) throw new IOException("Excel 資料過大，請刪除無關工作表後重試。");
                    out.write(buffer, 0, count);
                }
                files.put(name, out.toByteArray());
            }
        }
        Data result=new Data(); boolean languageTable=false;
        try {
            List<String> shared=sharedStrings(files.get("xl/sharedStrings.xml"));
            for(Map.Entry<String,byte[]> file:files.entrySet()) {
                if(!file.getKey().startsWith("xl/worksheets/"))continue;
                List<List<String>> rows=rows(file.getValue(),shared);
                for(int i=0;i<rows.size();i++) {
                    List<String> headers=rows.get(i).stream().map(SchemeWorkbook::header).collect(java.util.stream.Collectors.toList());
                    int language=column(headers,"語言","語言名稱","名稱"),group=column(headers,"自選分區","分區"),color=column(headers,"自選顏色","顏色");
                    int groupColor=column(headers,"分區顏色","自選分區顏色"),schemeColor=column(headers,"方案顏色");
                    if(language>=0&&group>=0&&color>=0) {
                        if(languageTable) throw new IOException("找到多份語言表，請每個檔案只保留一個方案的語言表。");
                        languageTable=true;
                        for(int j=i+1;j<rows.size();j++) {
                            List<String> row=rows.get(j);String name=cell(row,language).trim();if(name.isEmpty())continue;
                            result.entries.add(new CustomSchemes.Entry(name,cell(row,group),checkedColor(cell(row,color))));
                        }
                        break;
                    } else if(group>=0&&groupColor>=0&&language<0) {
                        result.structured=true;
                        for(int j=i+1;j<rows.size();j++) {
                            List<String> row=rows.get(j);String title=cell(row,group).trim();if(title.isEmpty())continue;
                            String path=CustomSchemes.normalizeGroup(title),value=checkedColor(cell(row,groupColor));
                            if(result.groupColors.containsKey(path)&&!result.groupColors.get(path).equals(value)) throw new IOException("同一分區有不同顏色："+title);
                            result.groupColors.put(path,value);
                        }
                        break;
                    } else if(schemeColor>=0&&group<0&&language<0) {
                        result.structured=true;
                        if(i+1<rows.size())result.schemeColor=checkedColor(cell(rows.get(i+1),schemeColor));
                        break;
                    }
                }
            }
        } catch(XmlPullParserException|NumberFormatException|IndexOutOfBoundsException error) {
            throw new IOException("無法解析 Excel，請使用 .xlsx 格式及匯出的欄位名稱。",error);
        }
        if(!languageTable)throw new IOException("找不到「語言」「自選分區」「自選顏色」欄位。");
        if(result.entries.isEmpty()&&!result.structured)throw new IOException("沒有可導入的語言記錄。");
        return result;
    }

    private static String header(String s) {
        return s.trim().replace("语言", "語言").replace("名称", "名稱").replace("自选", "自選")
                .replace("分区", "分區").replace("颜色", "顏色");
    }
    private static int column(List<String> headers, String... names) {
        for (String name : names) if (headers.contains(name)) return headers.indexOf(name);
        return -1;
    }
    private static String cell(List<String> row, int i) { return i < row.size() ? row.get(i) : ""; }
    private static XmlPullParser parser(byte[] data) throws XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(data), "UTF-8");
        return parser;
    }
    private static List<String> sharedStrings(byte[] data) throws IOException, XmlPullParserException {
        if (data == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        XmlPullParser p = parser(data);
        StringBuilder value = null;
        while (p.next() != XmlPullParser.END_DOCUMENT) {
            if (p.getEventType() == XmlPullParser.START_TAG) {
                if (p.getName().equals("si")) value = new StringBuilder();
                if (p.getName().equals("t") && value != null) value.append(p.nextText());
            } else if (p.getEventType() == XmlPullParser.END_TAG && p.getName().equals("si")) {
                result.add(value.toString());
                value = null;
            }
        }
        return result;
    }
    private static List<List<String>> rows(byte[] data, List<String> shared) throws IOException, XmlPullParserException {
        List<List<String>> result = new ArrayList<>();
        XmlPullParser p = parser(data);
        List<String> row = null;
        String type = "";
        int col = 0;
        StringBuilder value = new StringBuilder();
        while (p.next() != XmlPullParser.END_DOCUMENT) {
            if (p.getEventType() == XmlPullParser.START_TAG) {
                switch (p.getName()) {
                    case "row" -> row = new ArrayList<>();
                    case "c" -> {
                        String reference = p.getAttributeValue(null, "r");
                        col = reference == null ? row.size() : columnIndex(reference);
                        type = p.getAttributeValue(null, "t");
                        value.setLength(0);
                    }
                    case "v", "t" -> value.append(p.nextText());
                }
            } else if (p.getEventType() == XmlPullParser.END_TAG) {
                if (p.getName().equals("c") && row != null) {
                    while (row.size() <= col) row.add("");
                    String text = value.toString();
                    row.set(col, "s".equals(type) ? shared.get(Integer.parseInt(text)) : text);
                } else if (p.getName().equals("row") && row != null) {
                    result.add(row);
                    row = null;
                }
            }
        }
        return result;
    }
    private static int columnIndex(String reference) throws IOException {
        int result = 0;
        for (int i = 0; i < reference.length() && Character.isLetter(reference.charAt(i)); i++) {
            result = result * 26 + reference.charAt(i) - 'A' + 1;
            if (result > 16384) throw new IOException("Excel 欄位超出範圍。");
        }
        if (result == 0) throw new IOException("Excel 儲存格座標無效。");
        return result - 1;
    }
    private static String reference(int i) {
        StringBuilder s = new StringBuilder();
        for (i++; i > 0; i = (i - 1) / 26) s.insert(0, (char) ('A' + (i - 1) % 26));
        return s.toString();
    }
    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
    private static void part(ZipOutputStream zip, String name, String text) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(text.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
    public static void write(OutputStream output,List<List<String>> rows) throws IOException {
        writeSheets(output,Collections.singletonList("自選分區"),Collections.singletonList(rows));
    }
    private static void writeSheets(OutputStream output,List<String> names,List<List<List<String>>> sheets) throws IOException {
        try(ZipOutputStream zip=new ZipOutputStream(output,StandardCharsets.UTF_8)) {
            StringBuilder types=new StringBuilder("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
            StringBuilder workbook=new StringBuilder("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
            StringBuilder rels=new StringBuilder("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
            for(int i=1;i<=sheets.size();i++) {
                types.append("<Override PartName=\"/xl/worksheets/sheet"+i+".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
                workbook.append("<sheet name=\""+escape(names.get(i-1))+"\" sheetId=\""+i+"\" r:id=\"rId"+i+"\"/>");
                rels.append("<Relationship Id=\"rId"+i+"\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet"+i+".xml\"/>");
            }
            part(zip,"[Content_Types].xml",types.append("</Types>").toString());
            part(zip,"_rels/.rels","<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            part(zip,"xl/workbook.xml",workbook.append("</sheets></workbook>").toString());part(zip,"xl/_rels/workbook.xml.rels",rels.append("</Relationships>").toString());
            for(int n=0;n<sheets.size();n++) {
                zip.putNextEntry(new ZipEntry("xl/worksheets/sheet"+(n+1)+".xml"));Writer writer=new OutputStreamWriter(zip,StandardCharsets.UTF_8);
                writer.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" state=\"frozen\"/></sheetView></sheetViews><cols><col min=\"1\" max=\"20\" width=\"28\" customWidth=\"1\"/></cols><sheetData>");
                List<List<String>> data=sheets.get(n);
                for(int i=0;i<data.size();i++){writer.write("<row r=\""+(i+1)+"\">");for(int j=0;j<data.get(i).size();j++)writer.write("<c r=\""+reference(j)+(i+1)+"\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"+escape(data.get(i).get(j))+"</t></is></c>");writer.write("</row>");}
                writer.write("</sheetData></worksheet>");writer.flush();zip.closeEntry();
            }
        }
    }
}
