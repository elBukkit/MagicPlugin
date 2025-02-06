package com.elmakers.mine.bukkit.utility;

/*
 * This is here to provide a single entry point into StringEscapeUtils.
 * The goal being to one day phase this out and replace with something else, maybe custom code.
 */
public class StringEscapeUtils {
    private StringEscapeUtils() {
        // This is a static singleton
    }

    public static String unescapeJava(String str) {
        return org.apache.commons.text.StringEscapeUtils.unescapeJava(str);
    }

    public static String unescapeHtml(String str) {
        return org.apache.commons.text.StringEscapeUtils.unescapeHtml4(str);
    }
}
