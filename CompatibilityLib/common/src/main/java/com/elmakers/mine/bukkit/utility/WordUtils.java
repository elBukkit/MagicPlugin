package com.elmakers.mine.bukkit.utility;

/*
 * This is here to provide a single entry point into WordUtils.
 * The goal being to one day phase this out and replace with something else,
 * like possibly Guava, Java String. methods or custom code.
 */
public class WordUtils {
    private WordUtils() {
        // This is a static singleton
    }

    public static String capitalize(String str) {
        return org.apache.commons.text.WordUtils.capitalize(str);
    }

    public static String capitalizeFully(String str) {
        return org.apache.commons.text.WordUtils.capitalizeFully(str);
    }
}
