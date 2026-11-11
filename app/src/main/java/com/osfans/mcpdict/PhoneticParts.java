package com.osfans.mcpdict;

import java.text.Normalizer;
import java.util.regex.*;

/** Explicit heuristic: preserve unknown symbols rather than silently deleting them. */
public final class PhoneticParts {
    private static final String VOWELS="aeiouyɑɐɒæɓəɘɚɛɜɝɞɤɨɪɯɵœøɶɔɷʊʉʌʏɿʅɩɶᴀᴇɞ".replace("ɓ","");
    public final String initial, rime, tone, syllable, toneless;
    private PhoneticParts(String initial,String rime,String tone,String syllable,String toneless){this.initial=initial;this.rime=rime;this.tone=tone;this.syllable=syllable;this.toneless=toneless;}
    public static PhoneticParts parse(String value) {
        String text=Normalizer.normalize(value.trim(),Normalizer.Form.NFD);
        Matcher tone=Pattern.compile("[0-9⁰¹²³⁴⁵⁶⁷⁸⁹˥˦˧˨˩꜀꜁꜂꜃꜄꜅꜆꜇ᴴᴹᴸHMLRFS⓪①②③④⑤⑥⑦⑧⑨⑩]+$").matcher(text);
        String t="∅";if(tone.find()){t=tone.group();text=text.substring(0,tone.start());}
        int nucleus=-1;
        for(int i=0;i<text.length();i++) {
            char ch=text.charAt(i);
            if(VOWELS.indexOf(ch)>=0){nucleus=i;break;}
            if(ch=='\u0329'||ch=='\u030d'){nucleus=Math.max(0,i-1);break;}
        }
        if(nucleus<0) {
            if(text.matches("[mŋnɲɳɴlrɹɻʋvβzʐʑɣʁ]+[̩̍̃ːˑ]*")) nucleus=0;
            else return new PhoneticParts("未識別",Normalizer.normalize(text,Normalizer.Form.NFC),t,value,Normalizer.normalize(text.trim(),Normalizer.Form.NFC));
        }
        int onset=nucleus;
        for(int i=0;i<nucleus;i++) if("jwɥɰ".indexOf(text.charAt(i))>=0) {
            // All pre-nuclear glides belong to the rime, including w after k/kʰ.
            onset=i;break;
        }
        return new PhoneticParts(onset==0?"∅":Normalizer.normalize(text.substring(0,onset),Normalizer.Form.NFC),Normalizer.normalize(text.substring(onset),Normalizer.Form.NFC),t,value,Normalizer.normalize(text.trim(),Normalizer.Form.NFC));
    }
    public String dimension(int index){return index==0?initial:index==1?rime:index==2?tone:index==3?toneless:syllable;}
}
