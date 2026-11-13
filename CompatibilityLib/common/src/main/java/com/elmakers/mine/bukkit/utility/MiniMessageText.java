package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MiniMessageText {
    private static final Set<String> TAGS = new HashSet<>(Arrays.asList(
        "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "grey",
        "dark_gray", "dark_grey", "blue", "green", "aqua", "red", "light_purple", "yellow", "white",
        "color", "colour", "c", "bold", "b", "italic", "i", "em", "underlined", "u", "strikethrough", "st",
        "obfuscated", "obf", "reset", "newline", "br", "gradient", "rainbow", "transition", "font", "click",
        "hover", "insertion", "key", "lang", "tr", "translate", "selector", "sel", "score", "nbt", "data",
        "shadow", "pre"
    ));

    private static final Set<String> INLINE_TAGS = new HashSet<>(Arrays.asList(
        "reset", "newline", "br", "key", "lang", "tr", "translate", "selector", "sel", "score", "nbt", "data"
    ));

    private static final String[] LEGACY_COLOR_NAMES = {
        "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
        "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"
    };

    private static class Token {
        final String text;
        final boolean tag;
        final boolean closing;
        final boolean negated;
        final boolean selfClosing;
        final String name;

        Token(String text) {
            this.text = text;
            this.tag = false;
            this.closing = false;
            this.negated = false;
            this.selfClosing = false;
            this.name = null;
        }

        Token(String text, String name, boolean closing, boolean negated, boolean selfClosing) {
            this.text = text;
            this.tag = true;
            this.closing = closing;
            this.negated = negated;
            this.selfClosing = selfClosing;
            this.name = name;
        }
    }

    private static boolean isHexColor(String name) {
        if (name.length() != 7 || name.charAt(0) != '#') return false;
        for (int i = 1; i < name.length(); i++) {
            if (Character.digit(name.charAt(i), 16) < 0) return false;
        }
        return true;
    }

    private static int findTagEnd(String text, int start) {
        char quote = 0;
        for (int i = start + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quote != 0) {
                if (c == '\\') {
                    i++;
                } else if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            if (c == '\'' || c == '"') {
                quote = c;
            } else if (c == '>') {
                return i;
            } else if (c == '<') {
                return -1;
            }
        }
        return -1;
    }

    private static List<Token> tokenize(String text) {
        List<Token> tokens = new ArrayList<>();
        int length = text.length();
        int i = 0;
        while (i < length) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < length && (text.charAt(i + 1) == '<' || text.charAt(i + 1) == '\\')) {
                tokens.add(new Token(text.substring(i, i + 2)));
                i += 2;
                continue;
            }
            if (c == '<') {
                int end = findTagEnd(text, i);
                if (end > 0) {
                    String inner = text.substring(i + 1, end);
                    boolean closing = inner.startsWith("/");
                    String core = closing ? inner.substring(1) : inner;
                    boolean negated = core.startsWith("!");
                    if (negated) {
                        core = core.substring(1);
                    }
                    boolean selfClosing = !closing && core.endsWith("/");
                    if (selfClosing) {
                        core = core.substring(0, core.length() - 1);
                    }
                    int colon = core.indexOf(':');
                    String name = (colon >= 0 ? core.substring(0, colon) : core).toLowerCase(Locale.ROOT);
                    if (TAGS.contains(name) || isHexColor(name)) {
                        tokens.add(new Token(text.substring(i, end + 1), name, closing, negated, selfClosing));
                        i = end + 1;
                        continue;
                    }
                }
            }
            tokens.add(new Token(String.valueOf(c)));
            i++;
        }
        return tokens;
    }

    public static boolean containsTags(String text) {
        if (text == null || text.indexOf('<') < 0) return false;
        for (Token token : tokenize(text)) {
            if (token.tag) return true;
        }
        return false;
    }

    public static String legacyToMiniMessage(String text) {
        if (text == null || text.indexOf('\u00a7') < 0) return text;
        StringBuilder result = new StringBuilder();
        List<String> decorations = new ArrayList<>();
        int length = text.length();
        int i = 0;
        while (i < length) {
            char c = text.charAt(i);
            if (c != '\u00a7' || i + 1 >= length) {
                result.append(c);
                i++;
                continue;
            }
            char code = Character.toLowerCase(text.charAt(i + 1));
            if (code == 'x' && i + 13 < length && isHexSequence(text, i + 2)) {
                closeDecorations(result, decorations);
                result.append("<#");
                for (int j = 0; j < 6; j++) {
                    result.append(text.charAt(i + 3 + j * 2));
                }
                result.append('>');
                i += 14;
                continue;
            }
            int colorIndex = Character.digit(code, 16);
            if (colorIndex >= 0) {
                closeDecorations(result, decorations);
                result.append('<').append(LEGACY_COLOR_NAMES[colorIndex]).append('>');
            } else {
                String decoration = null;
                switch (code) {
                    case 'k':
                        decoration = "obfuscated";
                        break;
                    case 'l':
                        decoration = "bold";
                        break;
                    case 'm':
                        decoration = "strikethrough";
                        break;
                    case 'n':
                        decoration = "underlined";
                        break;
                    case 'o':
                        decoration = "italic";
                        break;
                    case 'r':
                        decorations.clear();
                        result.append("<reset>");
                        break;
                    default:
                        break;
                }
                if (decoration != null) {
                    decorations.add(decoration);
                    result.append('<').append(decoration).append('>');
                }
            }
            i += 2;
        }
        return result.toString();
    }

    private static boolean isHexSequence(String text, int start) {
        if (start + 12 > text.length()) return false;
        for (int j = 0; j < 6; j++) {
            if (text.charAt(start + j * 2) != '\u00a7') return false;
            if (Character.digit(text.charAt(start + j * 2 + 1), 16) < 0) return false;
        }
        return true;
    }

    private static void closeDecorations(StringBuilder result, List<String> decorations) {
        for (int i = decorations.size() - 1; i >= 0; i--) {
            result.append("</").append(decorations.get(i)).append('>');
        }
        decorations.clear();
    }

    public static void wrap(String text, int maxLength, String wrapPrefix, Collection<String> list) {
        String source = legacyToMiniMessage(text);
        String continuation = legacyToMiniMessage(wrapPrefix == null ? "" : wrapPrefix);
        int continuationLength = visibleLength(continuation);
        List<String> openTags = new ArrayList<>();
        List<String> openNames = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        int currentLength = 0;
        boolean hasWord = false;

        List<Token> word = new ArrayList<>();
        List<Token> tokens = tokenize(source);
        tokens.add(new Token(" "));
        for (Token token : tokens) {
            if (!token.tag && token.text.equals(" ")) {
                if (word.isEmpty()) continue;
                int wordLength = 0;
                for (Token wordToken : word) {
                    if (!wordToken.tag) wordLength++;
                }

                if (currentLength != 0 && currentLength + wordLength > maxLength) {
                    closeOpenTags(currentLine, openNames);
                    list.add(currentLine.toString());
                    currentLine.setLength(0);
                    currentLine.append(continuation);
                    for (String openTag : openTags) {
                        currentLine.append(openTag);
                    }
                    currentLength = continuationLength;
                    hasWord = false;
                } else if (hasWord) {
                    currentLine.append(' ');
                }

                for (Token wordToken : word) {
                    currentLine.append(wordToken.text);
                    if (wordToken.tag) {
                        trackTag(wordToken, openTags, openNames);
                    }
                }
                currentLength += wordLength;
                hasWord = true;
                word.clear();
            } else {
                word.add(token);
            }
        }

        if (currentLength > 0) {
            list.add(currentLine.toString());
        }
    }

    private static int visibleLength(String text) {
        int length = 0;
        for (Token token : tokenize(text)) {
            if (!token.tag) length++;
        }
        return length;
    }

    private static void trackTag(Token tag, List<String> openTags, List<String> openNames) {
        if (tag.selfClosing || INLINE_TAGS.contains(tag.name)) {
            if (tag.name.equals("reset")) {
                openTags.clear();
                openNames.clear();
            }
            return;
        }
        if (tag.closing) {
            for (int i = openNames.size() - 1; i >= 0; i--) {
                if (openNames.get(i).equals(tag.name)) {
                    openNames.remove(i);
                    openTags.remove(i);
                    break;
                }
            }
            return;
        }
        if (tag.negated) {
            return;
        }
        openTags.add(tag.text);
        openNames.add(tag.name);
    }

    private static void closeOpenTags(StringBuilder line, List<String> openNames) {
        for (int i = openNames.size() - 1; i >= 0; i--) {
            line.append("</").append(openNames.get(i)).append('>');
        }
    }
}
