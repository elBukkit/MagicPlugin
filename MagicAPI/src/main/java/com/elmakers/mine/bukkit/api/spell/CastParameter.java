package com.elmakers.mine.bukkit.api.spell;

public class CastParameter {
    private final String parameter;
    private final String value;

    public CastParameter(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter() {
        return parameter;
    }

    public String getValue() {
        return value;
    }
}
