
package android.graphics;
public class Color { public static int parseColor(String s) { return (int)(Long.parseLong(s.substring(1),16)|0xff000000L); } }
