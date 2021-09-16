package com.elmakers.mine.bukkit.utility;

import javax.annotation.Nonnull;

public class MacroExpansion {
    private final String text;
    private final String title;
    private final String tags;

    public MacroExpansion(String text, String title, String tags) {
        this.text = text;
        this.title = title;
        this.tags = tags;
    }

    public MacroExpansion(String text) {
        this(text, null, null);
    }

    @Nonnull
    public String getText() {
        return text == null ? "" : text;
    }

    @Nonnull
    public String getTitle() {
        return title == null ? "" : title;
    }

    @Nonnull
    public String getTags() {
        return tags == null ? "" : tags;
    }
}
