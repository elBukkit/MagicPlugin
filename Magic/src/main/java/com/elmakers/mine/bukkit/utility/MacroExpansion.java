package com.elmakers.mine.bukkit.utility;

import javax.annotation.Nonnull;

public class MacroExpansion {
    private final String text;
    private final String title;

    public MacroExpansion(String text, String title) {
        this.text = text;
        this.title = title;
    }

    public MacroExpansion(String text) {
        this(text, null);
    }

    @Nonnull
    public String getText() {
        return text == null ? "" : text;
    }

    @Nonnull
    public String getTitle() {
        return title == null ? "" : title;
    }
}
