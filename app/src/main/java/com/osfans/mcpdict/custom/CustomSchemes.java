package com.osfans.mcpdict.custom;

import android.graphics.Color;
import android.database.Cursor;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.Pref;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

/** Ordered local classifications, independent of the bundled language database. */
public final class CustomSchemes {
    public static final String ALL_SCOPE = "\u0001ALL";
    private static final String STORE = "custom_classifications_v2";
    private static final String CURRENT = "custom_classification_current";
    private static final String SCOPE = "custom_classification_group";
    private static final String DEDUPLICATE = "custom_classification_deduplicate";

    public record Entry(String id, String language, String group, String color) {
        public Entry(String language, String group, String color) {
            this(UUID.randomUUID().toString(), language, normalizeGroup(group), color);
        }
    }
    public static final class Scheme {
        public String name;
        public final List<Entry> entries = new ArrayList<>();
        public final LinkedHashSet<String> groups = new LinkedHashSet<>();
        public final List<String> groupOrder = new ArrayList<>();
        public final LinkedHashMap<String, String> colors = new LinkedHashMap<>();
        public Scheme(String name) { this.name = name; }
    }
    public record DisplayEntry(String key, String label, String language, String group, String color) {}
    private record Language(String label, boolean island, boolean historical) {}
    private static volatile Map<String, Language> languageCatalog;

    private CustomSchemes() {}

    public static synchronized List<Scheme> load() {
        String stored = Pref.get().getString(STORE, null);
        if (stored == null) return migrate();
        try {
            JSONArray data = new JSONArray(stored);
            List<Scheme> result = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject source = data.getJSONObject(i);
                Scheme scheme = new Scheme(source.getString("name"));
                JSONArray order = source.optJSONArray("groupOrder");
                if (order != null) for (int g = 0; g < order.length(); g++) scheme.groupOrder.add(normalizeGroup(order.getString(g)).isEmpty() ? "未分區" : normalizeGroup(order.getString(g)));
                JSONArray savedGroups = source.optJSONArray("groups");
                if (savedGroups != null) for (int g = 0; g < savedGroups.length(); g++) scheme.groups.add(groupTitle(normalizeGroup(savedGroups.getString(g))));
                JSONArray rows = source.getJSONArray("entries");
                for (int j = 0; j < rows.length(); j++) {
                    JSONObject row = rows.getJSONObject(j);
                    scheme.entries.add(new Entry(row.getString("id"), row.getString("language"),
                            groupTitle(normalizeGroup(row.optString("group"))), row.optString("color")));
                }
                JSONObject colors = source.optJSONObject("colors");
                if (colors != null) for (Iterator<String> it = colors.keys(); it.hasNext();) {
                    String group = it.next();
                    scheme.colors.put(normalizeGroup(group), colors.getString(group));
                }
                normalizeSubgroups(scheme);
                result.add(scheme);
            }
            if (result.isEmpty()) throw new JSONException("No schemes");
            return result;
        } catch (JSONException error) {
            // Preserve the stored data so that recovery never silently overwrites it.
            throw new IllegalStateException("無法讀取自選方案，請保留資料後重新啟動。", error);
        }
    }

    private static List<Scheme> migrate() {
        List<Scheme> result = new ArrayList<>();
        try {
            JSONArray legacy = new JSONArray(Pref.getStr(R.string.pref_key_custom_language_schemes, "[]"));
            for (int i = 0; i < legacy.length(); i++) {
                JSONObject row = legacy.getJSONObject(i);
                Scheme scheme = new Scheme(row.getString("name"));
                if (Pref.getBool(R.string.pref_key_custom_language_color_by_scheme, false)) {
                    scheme.colors.put("", String.format(Locale.ROOT, "#%06X", Pref.schemeIndexToColor(i) & 0xffffff));
                }
                JSONArray languages = row.optJSONArray("languages");
                if (languages != null) for (int j = 0; j < languages.length(); j++) {
                    scheme.entries.add(new Entry(languages.getString(j), "", ""));
                }
                result.add(scheme);
            }
        } catch (JSONException error) {
            throw new IllegalStateException("無法轉換舊自選方案，原資料已保留。", error);
        }
        if (result.isEmpty()) {
            Scheme scheme = new Scheme(Pref.getString(R.string.custom_scheme_default_name));
            for (String language : Pref.getStrSet(R.string.pref_key_custom_languages)) {
                scheme.entries.add(new Entry(language, "", ""));
            }
            result.add(scheme);
        }
        String legacyCurrent = Pref.getStr(R.string.pref_key_custom_language_scheme_current, "");
        String current = result.stream().anyMatch(s -> s.name.equals(legacyCurrent)) ? legacyCurrent : result.get(0).name;
        save(result, current);
        return result;
    }

    public static synchronized void save(List<Scheme> schemes, String current) {
        if (schemes.isEmpty()) throw new IllegalArgumentException("至少保留一個方案");
        JSONArray data = new JSONArray();
        try {
            Set<String> names = new HashSet<>();
            for (Scheme scheme : schemes) {
                normalizeSubgroups(scheme);
                if (scheme.name.trim().isEmpty() || !names.add(scheme.name)) throw new IllegalArgumentException("方案名稱重複或為空");
                JSONArray rows = new JSONArray();
                for (Entry row : scheme.entries) rows.put(new JSONObject()
                        .put("id", row.id).put("language", row.language).put("group", row.group).put("color", row.color));
                data.put(new JSONObject().put("name", scheme.name).put("entries", rows)
                        .put("colors", new JSONObject(scheme.colors)).put("groups", new JSONArray(scheme.groups)).put("groupOrder", new JSONArray(scheme.groupOrder)));
            }
        } catch (JSONException error) { throw new IllegalStateException(error); }
        if (!Pref.get().edit().putString(STORE, data.toString()).putString(CURRENT, current).commit()) {
            throw new IllegalStateException("方案儲存失敗");
        }
    }

    public static String currentName() {
        List<Scheme> schemes = load();
        String name = Pref.get().getString(CURRENT, schemes.get(0).name);
        return schemes.stream().anyMatch(s -> s.name.equals(name)) ? name : schemes.get(0).name;
    }
    public static Scheme current() {
        String name = currentName();
        return load().stream().filter(s -> s.name.equals(name)).findFirst().get();
    }
    public static String[] names() { return load().stream().map(s -> s.name).toArray(String[]::new); }
    public static void select(String name) {
        List<Scheme> schemes = load();
        if (schemes.stream().noneMatch(s -> s.name.equals(name))) return;
        save(schemes, name);
        setScope("");
    }
    public static boolean create(String name) {
        name = name.trim();
        if (name.isEmpty() || Arrays.asList(names()).contains(name)) return false;
        List<Scheme> schemes = load();
        schemes.add(new Scheme(name));
        save(schemes, name);
        setScope("");
        return true;
    }
    public static void replaceCurrent(Scheme updated) {
        String name = currentName();
        List<Scheme> schemes = load();
        for (int i = 0; i < schemes.size(); i++) if (schemes.get(i).name.equals(name)) schemes.set(i, updated);
        save(schemes, updated.name);
    }
    public static boolean rename(String name) {
        name = name.trim();
        if (name.equals(currentName())) return true;
        if (name.isEmpty() || Arrays.asList(names()).contains(name)) return false;
        Scheme scheme = current();
        scheme.name = name;
        replaceCurrent(scheme);
        return true;
    }
    public static boolean delete() {
        List<Scheme> schemes = load();
        if (schemes.size() == 1) return false;
        String old = currentName();
        schemes.removeIf(s -> s.name.equals(old));
        save(schemes, schemes.get(0).name);
        setScope("");
        return true;
    }
    public static String scope() {
        String value = Pref.get().getString(SCOPE, "");
        return value.equals(ALL_SCOPE) || value.equals("未分區") ? value : normalizeGroup(value);
    }
    public static void setScope(String value) { Pref.get().edit().putString(SCOPE, value).apply(); }
    public static boolean deduplicate() { return Pref.get().getBoolean(DEDUPLICATE, false); }
    public static void setDeduplicate(boolean on) { Pref.get().edit().putBoolean(DEDUPLICATE, on).apply(); }
    public static String normalizeGroup(String name) {
        String group = name.trim().replaceAll("[-/／－]+", "／").replaceAll("^／+|／+$", "");
        return group;
    }
    public static String groupTitle(String name) { return name.isEmpty() ? "未分區" : name; }
    public static String displayGroup(String name) { return groupTitle(name).replace('／', '-'); }

    public static List<Entry> scopedEntries() {
        List<Entry> result = new ArrayList<>();
        String scope = scope();
        List<Scheme> schemes = scope.equals(ALL_SCOPE) ? load() : Collections.singletonList(current());
        for (Scheme scheme : schemes) for (Entry row : scheme.entries) {
            if (scope.isEmpty() || scope.equals(ALL_SCOPE) || groupTitle(row.group).equals(scope) || row.group.startsWith(scope + "／")) {
                String color = row.color;
                String group = row.group;
                while (color.isEmpty()) {
                    color = scheme.colors.getOrDefault(group, "");
                    int end = group.lastIndexOf('／');
                    if (!color.isEmpty() || group.isEmpty()) break;
                    group = end < 0 ? "" : group.substring(0, end);
                }
                result.add(new Entry(row.id, row.language, row.group, color));
            }
        }
        return result;
    }

    public static List<DisplayEntry> visibleEntries() {
        Map<String, Language> known = catalog();
        Set<String> seen = new HashSet<>();
        List<DisplayEntry> result = new ArrayList<>();
        for (Entry row : scopedEntries()) {
            Language language = known.get(row.language);
            if (language == null || deduplicate() && !seen.add(row.language)) continue;
            String label = language.label;
            boolean island = language.island;
            boolean historic = language.historical;
            boolean include = switch (Pref.getIslandHistory()) {
                case ISLAND_ONLY -> island;
                case ISLAND_HIDE -> !island;
                case HISTORY_ONLY -> historic;
                case HISTORY_HIDE -> !historic;
                default -> true;
            };
            if (include) result.add(new DisplayEntry(row.id, label, row.language, row.group, row.color));
        }
        return result;
    }
    private static Map<String, Language> catalog() {
        Map<String, Language> cached = languageCatalog;
        if (cached != null) return cached;
        Map<String, Language> result = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();
        try (Cursor cursor = DB.getCursor("SELECT 語言,簡稱,方言島,歷史音 FROM info WHERE 音節數 IS NOT NULL")) {
            if (cursor == null) return Collections.emptyMap();
            do {
                String name = cursor.getString(0);
                Language row = new Language(cursor.getString(1), "1".equals(cursor.getString(2)), !"0".equals(cursor.getString(3)));
                Language previous = result.put(name, row);
                if (previous != null && !previous.label.equals(row.label)) ambiguous.add(name);
            } while (cursor.moveToNext());
        }
        for (String name : ambiguous) result.remove(name);
        languageCatalog = Collections.unmodifiableMap(result);
        return languageCatalog;
    }
    public static Set<String> matchableLanguages() { return catalog().keySet(); }
    public static Set<String> languages() {
        Set<String> result = new LinkedHashSet<>();
        for (DisplayEntry row : visibleEntries()) result.add(row.language);
        return result;
    }
    public static String summary() {
        List<DisplayEntry> rows = visibleEntries();
        return languages().size() + " 個語言" + (deduplicate() ? "" : " · " + rows.size() + " 條記錄");
    }
    public static List<String> groups() {
        Set<String> available = catalog().keySet();
        Scheme scheme = current();
        Set<String> paths = new LinkedHashSet<>(scheme.groups);
        for (Entry row : scheme.entries) if (available.contains(row.language)) paths.add(groupTitle(row.group));
        Set<String> result = new LinkedHashSet<>();
        for (String name : paths) {
            if(name.isEmpty()){result.add("未分區");continue;}
            String path = "";
            for (String part : name.split("／")) {
                if (part.isEmpty()) continue;
                path = path.isEmpty() ? part : path + "／" + part;
                result.add(path);
            }
        }
        return hierarchicalGroups(result, scheme.groupOrder);
    }
    public static String parentGroup(String path) {
        int split=path.lastIndexOf('／');return split<0?"":path.substring(0,split);
    }
    public static List<String> hierarchicalGroups(Collection<String> paths, List<String> order) {
        Set<String> all=new LinkedHashSet<>();
        for(String full:paths){String path="";for(String part:full.split("／")){if(part.isEmpty())continue;path=path.isEmpty()?part:path+"／"+part;all.add(path);}}
        Map<String,Integer> rank=new HashMap<>();for(int i=0;i<order.size();i++)rank.put(order.get(i),i);
        Map<String,List<String>> children=new LinkedHashMap<>();
        for(String path:all)children.computeIfAbsent(parentGroup(path),key->new ArrayList<>()).add(path);
        for(List<String> siblings:children.values())siblings.sort(Comparator.comparingInt(path->rank.getOrDefault(path,Integer.MAX_VALUE)));
        List<String> result=new ArrayList<>();appendGroups("",children,result);return result;
    }
    private static void appendGroups(String parent,Map<String,List<String>> children,List<String> result) {
        for(String child:children.getOrDefault(parent,Collections.emptyList())){result.add(child);appendGroups(child,children,result);}
    }
    public static boolean moveGroupBranch(List<String> paths,int from,int to) {
        if(from<0||to<0||from>=paths.size()||to>=paths.size()||from==to)return false;
        String moving=paths.get(from),target=paths.get(to);
        if(!parentGroup(moving).equals(parentGroup(target)))return false;
        int end=from+1;while(end<paths.size()&&paths.get(end).startsWith(moving+"／"))end++;
        int targetEnd=to+1;while(targetEnd<paths.size()&&paths.get(targetEnd).startsWith(target+"／"))targetEnd++;
        List<String> block=new ArrayList<>(paths.subList(from,end));paths.subList(from,end).clear();
        int insertion=from<to?targetEnd-block.size():to;paths.addAll(insertion,block);return true;
    }
    public static void reorderGroups(List<String> names) {
        if (names.size() != new HashSet<>(names).size() || !new HashSet<>(names).equals(new HashSet<>(groups())))
            throw new IllegalArgumentException("分區清單已改變，請重新開啟");
        if(!names.equals(hierarchicalGroups(names,names)))throw new IllegalArgumentException("只能在同一父分區內調整順序");
        Scheme scheme = current(); scheme.groupOrder.clear(); scheme.groupOrder.addAll(names);
        Map<String,Integer> rank = new HashMap<>();
        for (int i = 0; i < names.size(); i++) rank.put(names.get(i), i);
        // Stable sort: whole membership blocks move; within-group order and record IDs remain intact.
        scheme.entries.sort(Comparator.comparingInt(entry -> rank.getOrDefault(groupTitle(entry.group), Integer.MAX_VALUE)));
        replaceCurrent(scheme);
    }
    public static void reorderSchemes(List<String> names) {
        List<Scheme> live = load();
        if (names.size() != live.size() || names.size() != new HashSet<>(names).size()
                || !new HashSet<>(names).equals(new HashSet<>(Arrays.asList(names()))))
            throw new IllegalArgumentException("方案清單已改變，請重新開啟");
        Map<String, Scheme> byName = new HashMap<>();
        for (Scheme scheme : live) byName.put(scheme.name, scheme);
        List<Scheme> ordered = new ArrayList<>();
        for (String name : names) ordered.add(byName.get(name));
        save(ordered, currentName());
    }
    private static boolean groupBranch(String path, String root) {
        return path.equals(root) || !root.isEmpty() && path.startsWith(root + "／");
    }
    private static String renamedPath(String path, String old, String replacement) {
        if (old.isEmpty() && path.equals("未分區")) return replacement;
        return groupBranch(path, old) ? replacement + path.substring(old.length()) : path;
    }
    public static boolean renameGroup(String oldName, String newName) {
        String old = normalizeGroup(oldName), replacement = normalizeGroup(newName);
        List<String> existing = groups();
        if (!existing.contains(oldName) || replacement.isEmpty()) return false;
        if (old.equals(replacement)) return true;
        if (existing.contains(replacement) || !old.isEmpty() && replacement.startsWith(old + "／")) return false;
        for (String name : existing) {
            String path = normalizeGroup(name);
            if (groupBranch(path, old) && existing.contains(renamedPath(path, old, replacement))) return false;
        }
        Scheme scheme = current();
        for (int i = 0; i < scheme.entries.size(); i++) {
            Entry entry = scheme.entries.get(i);
            if (groupBranch(entry.group, old)) scheme.entries.set(i, new Entry(entry.id, entry.language, renamedPath(entry.group, old, replacement), entry.color));
        }
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        for (String path : scheme.groups) paths.add(renamedPath(path, old, replacement));
        paths.add(replacement); scheme.groups.clear(); scheme.groups.addAll(paths);
        for (int i = 0; i < scheme.groupOrder.size(); i++) scheme.groupOrder.set(i, renamedPath(scheme.groupOrder.get(i), old, replacement));
        if (!old.isEmpty()) {
            LinkedHashMap<String,String> palette = new LinkedHashMap<>();
            for (Map.Entry<String,String> color : scheme.colors.entrySet()) palette.put(renamedPath(color.getKey(), old, replacement), color.getValue());
            scheme.colors.clear(); scheme.colors.putAll(palette);
        }
        String selected = scope();
        replaceCurrent(scheme);
        if (selected.equals(oldName) || !old.isEmpty() && selected.startsWith(old + "／")) setScope(renamedPath(selected, old, replacement));
        return true;
    }
    public static boolean deleteGroup(String name) {
        if (!groups().contains(name)) return false;
        String root = normalizeGroup(name);
        Scheme scheme = current();
        scheme.entries.removeIf(entry -> groupBranch(entry.group, root));
        scheme.groups.removeIf(path -> groupBranch(path, root));
        scheme.groupOrder.removeIf(path -> groupBranch(normalizeGroup(path), root));
        if (!root.isEmpty()) scheme.colors.keySet().removeIf(path -> groupBranch(path, root));
        String selected = scope(); replaceCurrent(scheme);
        if (selected.equals(name) || !root.isEmpty() && selected.startsWith(root + "／")) setScope("");
        return true;
    }
    public static boolean isUnclassifiedGroup(String path) {
        return path.isEmpty() || path.equals("未分區") || path.endsWith("／未分區");
    }
    public static boolean requiredUnclassifiedGroup(String name) { return false; }
    /** Move a complete branch; renameGroup preserves IDs, colours and selection. */
    public static boolean reparentGroup(String name,String parent) {
        String path=normalizeGroup(name),target=normalizeGroup(parent);
        if(target.equals(path) || !path.isEmpty() && target.startsWith(path+"／"))return false;
        if(!target.isEmpty()&&!groups().contains(target))return false;
        String leaf=path.isEmpty()?"未分區":path.substring(path.lastIndexOf('／')+1);
        String destination=target.isEmpty()?leaf:target+"／"+leaf;
        if(destination.equals(path))return true;
        if(!renameGroup(name,destination))return false;
        reorderGroups(groups());return true;
    }
    /** Reading, saving and sorting never create groups or move/delete memberships. */
    public static void normalizeSubgroups(Scheme scheme) {
        // Migrate legacy empty membership paths to an ordinary root node. The
        // empty path remains reserved for the whole-scheme scope/palette only.
        for(int i=0;i<scheme.entries.size();i++) {
            Entry row=scheme.entries.get(i);
            if(row.group.isEmpty())scheme.entries.set(i,new Entry(row.id,row.language,"未分區",row.color));
        }
        if(scheme.groups.remove(""))scheme.groups.add("未分區");
        // Existing buckets are ordinary user-owned groups, including empty groups.
        Set<String> seen=new HashSet<>();scheme.groupOrder.removeIf(path -> !seen.add(path));
    }
    public static int languageCount(String scope) {
        Map<String,Language> known=catalog();Set<String> names=new HashSet<>();
        for(Entry entry:current().entries) {
            Language language=known.get(entry.language);if(language==null)continue;
            if(!scope.isEmpty()&&!groupTitle(entry.group).equals(scope)&&!entry.group.startsWith(scope+"／"))continue;
            boolean include=switch(Pref.getIslandHistory()) {
                case ISLAND_ONLY -> language.island;case ISLAND_HIDE -> !language.island;
                case HISTORY_ONLY -> language.historical;case HISTORY_HIDE -> !language.historical;default -> true;
            };
            if(include)names.add(entry.language);
        }
        return names.size();
    }
    public static boolean createGroup(String name) {
        String path = normalizeGroup(name);
        if (path.isEmpty() || groups().contains(path)) return false;
        Scheme scheme = current();
        scheme.groups.add(path);
        replaceCurrent(scheme);
        setScope(path);
        return true;
    }
    public static void toggle(String language) {
        Scheme scheme = current();
        String group = scope().equals(ALL_SCOPE) ? "" : scope();
        boolean exists = scheme.entries.stream().anyMatch(e -> e.language.equals(language) && inGroup(e, group));
        if (exists) scheme.entries.removeIf(e -> e.language.equals(language) && inGroup(e, group));
        else {
            prepareManualGroup(scheme, normalizeGroup(group));
            scheme.entries.add(new Entry(language, group, ""));
        }
        replaceCurrent(scheme);
    }
    public static void prepareManualGroup(Scheme scheme, String group) {
        if (group.isEmpty() || scheme.colors.containsKey(group)
                || scheme.entries.stream().anyMatch(e -> e.group.equals(group))) return;
        int index = (int) scheme.entries.stream().map(Entry::group).filter(s -> !s.isEmpty()).distinct().count();
        scheme.colors.put(group, String.format(Locale.ROOT, "#%06X", Pref.schemeIndexToColor(index) & 0xffffff));
    }
    private static boolean inGroup(Entry entry, String group) {
        return group.isEmpty() || groupTitle(entry.group).equals(group) || entry.group.startsWith(group + "／");
    }
    public static boolean checked(String language) {
        String group = scope().equals(ALL_SCOPE) ? "" : scope();
        return current().entries.stream().anyMatch(e -> e.language.equals(language) && inGroup(e, group));
    }
    private static boolean colorMember(Entry entry, String group) {
        return group == null || entry.group.equals(group)
                || !group.isEmpty() && entry.group.startsWith(group + "／");
    }
    private static boolean childColor(String path, String group) {
        return group == null ? !path.isEmpty() : !group.isEmpty() && path.startsWith(group + "／");
    }
    public static boolean hasColorOverrides(Scheme scheme, String group) {
        return scheme.entries.stream().anyMatch(entry -> colorMember(entry, group) && !entry.color.isEmpty())
                || scheme.colors.keySet().stream().anyMatch(path -> childColor(path, group));
    }
    public static void applyOverallColor(Scheme scheme, String group, String value, boolean resetMembers) {
        if (resetMembers) {
            for (int i = 0; i < scheme.entries.size(); i++) {
                Entry entry = scheme.entries.get(i);
                if (colorMember(entry, group)) scheme.entries.set(i, new Entry(entry.id, entry.language, entry.group, ""));
            }
            scheme.colors.keySet().removeIf(path -> childColor(path, group));
        }
        String path = group == null ? "" : group;
        if (value.isEmpty()) scheme.colors.remove(path); else scheme.colors.put(path, value);
    }
    public static int color(String value, String label) {
        return value.isEmpty() ? DB.getColor(label) : Color.parseColor(value);
    }
    public static int firstColor(String label) {
        for (DisplayEntry row : visibleEntries()) if (row.label.equals(label)) return color(row.color, label);
        return DB.getColor(label);
    }
}
