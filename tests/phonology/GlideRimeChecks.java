import com.osfans.mcpdict.PhoneticParts;
public class GlideRimeChecks {
    private static int checks;
    private static void check(String ipa, String initial, String rime, String tone) {
        PhoneticParts p = PhoneticParts.parse(ipa);
        if (!initial.equals(p.initial) || !rime.equals(p.rime) || !tone.equals(p.tone))
            throw new AssertionError(ipa + " -> " + p.initial + "/" + p.rime + "/" + p.tone);
        checks++;
    }
    public static void main(String[] args) {
        check("kwai55", "k", "wai", "55");
        check("kʰwai55", "kʰ", "wai", "55");
        check("wɐ21", "∅", "wɐ", "21");
        check("tjaŋ31", "t", "jaŋ", "31");
        check("tɥe51", "t", "ɥe", "51");
        check("kɰɯ33", "k", "ɰɯ", "33");
        check("kjwa55", "k", "jwa", "55");
        check("tʰjã˥˩", "tʰ", "jã", "˥˩");
        check("tʂuaŋ³¹", "tʂ", "uaŋ", "³¹");
        check("ɡwɐH①", "ɡ", "wɐ", "H①");
        check("n̩55", "∅", "n̩", "55");
        check("ŋ21", "∅", "ŋ", "21");
        check("tʂu", "tʂ", "u", "∅");
        check("???55", "未識別", "???", "55");
        PhoneticParts p = PhoneticParts.parse("kʰwai55");
        String[] expected = {"kʰ", "wai", "55", "kʰwai", "kʰwai55"};
        for (int i=0;i<expected.length;i++) {
            if (!expected[i].equals(p.dimension(i))) throw new AssertionError("Dimension " + i);
            checks++;
        }
        System.out.println(checks + " glide/rime/dimension checks passed");
    }
}
