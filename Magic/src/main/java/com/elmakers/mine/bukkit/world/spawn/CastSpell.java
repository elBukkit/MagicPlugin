package com.elmakers.mine.bukkit.world.spawn;

import org.apache.commons.lang.StringUtils;

public class CastSpell {
    private final String            name;
    private final String[]         parameters;

    public CastSpell(String commandLine) {
        if (commandLine == null || commandLine.isEmpty() || commandLine.equalsIgnoreCase("none")) {
            this.name = null;
            this.parameters = null;
        } else if (commandLine.contains(" ")) {
            String[] pieces = StringUtils.split(commandLine, " ");
            name = pieces[0];
            parameters = new String[pieces.length - 1];
            for (int i = 1; i < pieces.length; i++) {
                parameters[i - 1] = pieces[i];
            }
        } else {
            name = commandLine;
            parameters = new String[0];
        }
    }

    public boolean isEmpty() {
        return this.name == null;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String[] getParameters() {
        return parameters;
    }
}
