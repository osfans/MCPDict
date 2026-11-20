package com.osfans.mcpdict.custom;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.osfans.mcpdict.DB;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Lifecycle-owned file actions, without a separate management screen. */
public final class CustomSchemeFiles extends Fragment {
    public static final String CHANGED="custom-files-changed";
    private static final String TAG="custom-scheme-files";
    private static final String XLSX="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private CustomSchemes.Scheme scheme;
    private Context appContext;
    private boolean busy;
    private File pendingExportFile;
    private final ActivityResultLauncher<String[]> importFile=registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),uri->{if(uri!=null)readFile(uri);});
    private final ActivityResultLauncher<String> exportFile=registerForActivityResult(
            new ActivityResultContracts.CreateDocument(XLSX),uri->{
                File file=pendingExportFile;pendingExportFile=null;if(file==null)return;
                if(uri!=null)writeFile(uri,file);else file.delete();
            });
    public static CustomSchemeFiles get(Fragment parent) {
        FragmentManager manager=parent.getChildFragmentManager();
        CustomSchemeFiles actions=(CustomSchemeFiles)manager.findFragmentByTag(TAG);
        if(actions==null){actions=new CustomSchemeFiles();manager.beginTransaction().add(actions,TAG).commitNow();}
        return actions;
    }
    @Override public void onAttach(Context context){super.onAttach(context);appContext=context.getApplicationContext();}
    @Override public void onCreate(Bundle state){super.onCreate(state);if(state!=null&&state.getString("pendingExport")!=null)pendingExportFile=new File(appContext.getCacheDir(),new File(state.getString("pendingExport")).getName());}
    public void importExcel(){if(!busy)importFile.launch(new String[]{XLSX,"application/octet-stream"});}
    public void exportExcel(){if(!busy)exportOptions();}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private LinearLayout column(){LinearLayout view=new LinearLayout(requireContext());view.setOrientation(LinearLayout.VERTICAL);return view;}
    private void message(String text){new AlertDialog.Builder(requireContext()).setMessage(text).setPositiveButton("確定",null).show();}
    private void ui(Runnable action){android.app.Activity activity=getActivity();if(activity!=null)activity.runOnUiThread(()->{if(isAdded())action.run();});}
    private EditText field(LinearLayout body, String label, String value) {
        TextView caption = new TextView(requireContext());
        caption.setText(label);
        body.addView(caption);
        EditText input = new EditText(requireContext());
        input.setSingleLine();
        input.setText(value);
        body.addView(input);
        return input;
    }
    private String fileName(Uri uri) {
        try (Cursor cursor = appContext.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0).replaceFirst("(?i)\\.xlsx$", "");
        }
        return "導入方案";
    }
    private void readFile(Uri uri) {
        busy = true;
        worker.execute(() -> {
            try (InputStream input = appContext.getContentResolver().openInputStream(uri)) {
                if (input == null) throw new IOException("無法開啟檔案");
                SchemeWorkbook.Data data = SchemeWorkbook.readData(input);
                List<CustomSchemes.Entry> entries = data.entries;
                Set<String> known = CustomSchemes.matchableLanguages();
                List<String> unresolved = entries.stream().map(CustomSchemes.Entry::language)
                        .filter(s -> !known.contains(s)).distinct().collect(java.util.stream.Collectors.toList());
                String filename = fileName(uri);
                ui(() -> {
                    if (!isAdded()) return;
                    busy = false;
                    confirmImport(filename, data, unresolved);
                });
            } catch (IOException | RuntimeException error) { failed(error); }
        });
    }
    private void confirmImport(String filename, SchemeWorkbook.Data data, List<String> unresolved) {
        List<CustomSchemes.Entry> entries = data.entries;
        LinearLayout body = column();
        body.setPadding(dp(20), dp(8), dp(20), 0);
        EditText name = field(body, "方案名稱", filename);
        TextView preview = new TextView(requireContext());
        long matched = entries.stream().filter(e -> !unresolved.contains(e.language())).map(CustomSchemes.Entry::language).distinct().count();
        preview.setText(entries.size() + " 條記錄 · 可匹配 " + matched + " 個語言\n"
                + (unresolved.isEmpty() ? "" : "未匹配的名稱會保留，暫不顯示\n" + String.join("\n", unresolved)));
        ScrollView scroll = new ScrollView(requireContext());
        scroll.addView(preview);
        body.addView(scroll, new LinearLayout.LayoutParams(-1, dp(180)));
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("導入預覽").setView(body)
                .setPositiveButton("導入", null).setNegativeButton("取消", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String target = name.getText().toString().trim();
            if (target.isEmpty()) { name.setError("請填方案名稱"); return; }
            if (Arrays.asList(CustomSchemes.names()).contains(target)) {
                new AlertDialog.Builder(requireContext()).setMessage("同名方案已存在，要更新該方案嗎？")
                        .setPositiveButton("更新", (x, w) -> { install(target, data); dialog.dismiss(); })
                        .setNegativeButton("返回改名", null).show();
            } else { install(target, data); dialog.dismiss(); }
        }));
        dialog.show();
    }
    private void install(String name, SchemeWorkbook.Data data) {
        List<CustomSchemes.Scheme> schemes = CustomSchemes.load();
        schemes.removeIf(s -> s.name.equals(name));
        CustomSchemes.Scheme imported = data.scheme(name);
        schemes.add(imported);
        CustomSchemes.save(schemes, name);
        CustomSchemes.setScope("");
        com.osfans.mcpdict.Util.Pref.putBool(com.osfans.mcpdict.R.string.pref_key_custom_language_color_by_scheme, true);
        getParentFragmentManager().setFragmentResult(CHANGED,new Bundle());
        Toast.makeText(requireContext(),"已導入方案",Toast.LENGTH_SHORT).show();
    }
    private void exportOptions() {
        List<CustomSchemes.Scheme> schemes=CustomSchemes.load();
        List<String> choices=new ArrayList<>(); for(CustomSchemes.Scheme item:schemes)choices.add("方案："+item.name);
        choices.add("全部語言模板");
        new AlertDialog.Builder(requireContext()).setTitle("選擇導出內容").setItems(choices.toArray(new String[0]),(dialog,index)->{
            List<CustomSchemes.Entry> entries=new ArrayList<>();
            if(index<schemes.size()){scheme=schemes.get(index);entries.addAll(scheme.entries);}
            else {scheme=new CustomSchemes.Scheme("全部語言模板");for(String language:DB.getLanguages())entries.add(new CustomSchemes.Entry(language,"",""));}
            if(entries.isEmpty()){message("目前沒有可導出的語言記錄。");return;}
            chooseExportRows(entries);
        }).setNegativeButton("取消",null).show();
    }
    private void chooseExportRows(List<CustomSchemes.Entry> entries) {
        String[] names = entries.stream().map(e -> e.language() + " · " + CustomSchemes.displayGroup(e.group())).toArray(String[]::new);
        boolean[] selected = new boolean[names.length];
        Arrays.fill(selected, true);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("選擇導出記錄")
                .setMultiChoiceItems(names, selected, (d, i, checked) -> selected[i] = checked)
                .setPositiveButton("下一步", (d, w) -> {
                    List<CustomSchemes.Entry> chosen = new ArrayList<>();
                    for (int i = 0; i < entries.size(); i++) if (selected[i]) chosen.add(entries.get(i));
                    if (chosen.isEmpty()) { message("請至少選擇一條記錄"); return; }
                    chooseAuxiliary(chosen);
                }).setNeutralButton("全選／清空", null).setNegativeButton("取消", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            boolean all = true;
            for (boolean check : selected) all &= check;
            Arrays.fill(selected, !all);
            for (int i = 0; i < selected.length; i++) dialog.getListView().setItemChecked(i, !all);
        }));
        dialog.show();
    }
    private void chooseAuxiliary(List<CustomSchemes.Entry> chosen) {
        String[] fields = {"簡稱", "地圖集二分區", "地圖集二顏色", "音典分區", "音典顏色", "地點", "經緯度"};
        boolean[] selected = new boolean[fields.length];
        new AlertDialog.Builder(requireContext()).setTitle("選擇輔助資訊")
                .setMultiChoiceItems(fields, selected, (d, i, check) -> selected[i] = check)
                .setPositiveButton("選擇儲存位置", (d, w) -> prepareExport(chosen, fields, selected))
                .setNegativeButton("取消", null).show();
    }
    private void prepareExport(List<CustomSchemes.Entry> entries, String[] fields, boolean[] selected) {
        CustomSchemes.Scheme exportScheme=scheme;
        busy = true;
        worker.execute(() -> {
            try {
                List<List<String>> rows = new ArrayList<>();
                List<String> headers = new ArrayList<>(Arrays.asList("語言", "自選分區", "自選顏色"));
                for (int i = 0; i < fields.length; i++) if (selected[i]) headers.add(fields[i]);
                rows.add(headers);
                for (CustomSchemes.Entry entry : entries) {
                    String label = DB.getLabelByLanguage(entry.language());
                    String color = entry.color();
                    String group = entry.group();
                    while(color.isEmpty()) {
                        color=exportScheme.colors.getOrDefault(group,"");
                        if(!color.isEmpty()||group.isEmpty())break;
                        int split=group.lastIndexOf('／');group=split<0?"":group.substring(0,split);
                    }
                    List<String> row = new ArrayList<>(Arrays.asList(entry.language(), entry.group().replace('／', '-'), color));
                    for (int i = 0; i < fields.length; i++) if (selected[i]) {
                        String value = fields[i].equals("簡稱") ? label : DB.getFieldByLabel(label, fields[i]);
                        row.add(value == null ? "" : value);
                    }
                    rows.add(row);
                }
                File file = File.createTempFile("classification-", ".xlsx", appContext.getCacheDir());
                try (OutputStream out = new FileOutputStream(file)) { SchemeWorkbook.write(out, rows); }
                ui(() -> {
                    if (!isAdded()) { file.delete(); return; }
                    busy = false;
                    pendingExportFile = file;
                    exportFile.launch(exportScheme.name + ".xlsx");
                });
            } catch (IOException | RuntimeException error) { failed(error); }
        });
    }
    private void writeFile(Uri uri, File file) {
        busy = true;
        worker.execute(() -> {
            try (InputStream input = new FileInputStream(file);
                 OutputStream out = appContext.getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new IOException("無法開啟輸出檔案");
                byte[] buffer = new byte[8192];
                int bytes;
                while ((bytes = input.read(buffer)) != -1) out.write(buffer, 0, bytes);
                ui(() -> {
                    if (!isAdded()) return;
                    busy = false;
                    Toast.makeText(requireContext(), "已導出 Excel", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException | RuntimeException error) { failed(error); }
            finally { file.delete(); }
        });
    }
    private void failed(Exception error) {
        ui(() -> {
            if (!isAdded()) return;
            busy = false;
            message(error.getMessage() == null ? "無法處理檔案" : error.getMessage());
        });
    }
    @Override public void onSaveInstanceState(Bundle state) {
        if (pendingExportFile != null) state.putString("pendingExport", pendingExportFile.getName());
        super.onSaveInstanceState(state);
    }
    @Override public void onDestroy() { worker.shutdownNow(); super.onDestroy(); }
}
