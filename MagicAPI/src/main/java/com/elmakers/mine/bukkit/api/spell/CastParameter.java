package com.elmakers.mine.bukkit.api.spell;

public class CastParameter {
    private final String parameter;
    private final String value;
    private final Object convertedValue;

    public CastParameter(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;

        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        Object converted = value;
        if (isTrue || isFalse) {
            converted = isTrue;
        } else {
            try {
                converted = Integer.parseInt(value);
            } catch (Exception ex) {
                try {
                    converted = Double.parseDouble(value);
                } catch (Exception ignore) {
                }
            }
        }
        convertedValue = converted;
    }

    public String getParameter() {
        return parameter;
    }

    public String getValue() {
        return value;
    }

    public Object getConvertedValue() {
        return convertedValue;
    }
}
