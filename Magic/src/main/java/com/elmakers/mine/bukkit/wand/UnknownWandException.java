package com.elmakers.mine.bukkit.wand;

public class UnknownWandException extends Exception {
    private final String templateName;

    public UnknownWandException(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
