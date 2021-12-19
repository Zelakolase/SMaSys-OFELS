package lib;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class TextToHashmap {
    public static HashMap<String, String> Convert(String text, String EDelimiter, String KVDelimiter) {
        /**
         * This function converts String/Text to HashMap
         * 
         * @param text        The text to be converted
         * @param EDelimiter  The delimiter between every element
         * @param KVDelimiter The delimiter between every Key and Value in one element
         */
        HashMap<String, String> out = new HashMap<String, String>();
        String[] AText = text.split(EDelimiter);
        for (int x = 0; x < AText.length; x++) {
            String Element = AText[x];
            String[] pair = Element.split(KVDelimiter);
            out.put(pair[0], URIDecode(pair[1]));
        }
        return out;
    }
	public static String URIDecode(String in) {
		in = in.replaceAll("\\+", " ");
		in = in.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
		return java.net.URLDecoder.decode(in, StandardCharsets.UTF_8);
	}
}