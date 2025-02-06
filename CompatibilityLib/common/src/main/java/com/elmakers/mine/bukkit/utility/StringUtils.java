package com.elmakers.mine.bukkit.utility;

import java.util.Collection;

/*
 * This is here to provide a single entry point into StringUtils.
 * The goal being to one day phase this out and replace with something else,
 * like possibly Guava, Java String. methods or custom code.
 */
public class StringUtils {
    private StringUtils() {
        // This is a static singleton
    }

    public static boolean isEmpty(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

    public static String[] split(String str) {
        return org.apache.commons.lang3.StringUtils.split(str);
    }

    public static String[] split(String str, String separatorChars) {
        return org.apache.commons.lang3.StringUtils.split(str, separatorChars);
    }

    public static String[] split(String str, char separatorChar) {
        return org.apache.commons.lang3.StringUtils.split(str, separatorChar);
    }

    public static String[] split(String str, String separatorChars, int max) {
        return org.apache.commons.lang3.StringUtils.split(str, separatorChars, max);
    }

    public static String[] splitPreserveAllTokens(String str, String separatorChars) {
        return org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(str, separatorChars);
    }

    public static String join(Object[] array) {
        return org.apache.commons.lang3.StringUtils.join(array);
    }

    public static String join(Object[] array, String separator) {
        return org.apache.commons.lang3.StringUtils.join(array, separator);
    }

    public static String join(Object[] array, char separator) {
        return org.apache.commons.lang3.StringUtils.join(array, separator);
    }

    public static String join(Collection collection, char separator) {
        return org.apache.commons.lang3.StringUtils.join(collection, separator);
    }

    public static String join(Collection collection, String separator) {
        return org.apache.commons.lang3.StringUtils.join(collection, separator);
    }

    public static String repeat(String str, int repeat) {
        return org.apache.commons.lang3.StringUtils.repeat(str, repeat);
    }

    public static int getLevenshteinDistance(String str1, String str2) {
        return org.apache.commons.lang3.StringUtils.getLevenshteinDistance(str1, str2);
    }

    public static int countMatches(String str, String sub) {
        return org.apache.commons.lang3.StringUtils.countMatches(str, sub);
    }

    public static boolean isNumeric(String str) {
        return org.apache.commons.lang3.StringUtils.isNumeric(str);
    }

    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        return org.apache.commons.lang3.StringUtils.replaceEach(text, searchList, replacementList);
    }
}
