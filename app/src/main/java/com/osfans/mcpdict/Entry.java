package com.osfans.mcpdict;

public class Entry {
    public String hz, lang, raw, comment;
    public boolean favorite;
    public Entry() {
    }

    public Entry(String hz, String lang, String raw, boolean favorite, String comment) {
        set(hz, lang, raw, favorite, comment);
    }

    public void set(String hz, String lang, String raw, boolean favorite, String comment) {
        this.hz = hz;
        this.lang = lang;
        this.raw = raw;
        this.favorite = favorite;
        this.comment = comment;
    }

    public void set(String hz, boolean favorite, String comment) {
        this.hz = hz;
        this.favorite = favorite;
        this.comment = comment;
    }

    public void set(Entry entry) {
        set(entry.hz, entry.lang,entry.raw, entry.favorite, entry.comment);
    }
}
