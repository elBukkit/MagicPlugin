package com.elmakers.mine.bukkit.world;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.world.spawn.SpawnResult;

public class CastSpell {
    private final String            name;
    private final String[]         parameters;
    private final SpawnResult       spawnResult;
    private final BlockResult       blockResult;

    public CastSpell(String commandLine) {
        if (commandLine == null || commandLine.isEmpty() || commandLine.equalsIgnoreCase("none")) {
            this.name = null;
            this.parameters = null;
            this.spawnResult = SpawnResult.REMOVE;
            this.blockResult = BlockResult.REMOVE_DROPS;
        } else if (commandLine.contains(" ")) {
            String[] pieces = StringUtils.split(commandLine, " ");
            name = pieces[0];
            parameters = new String[pieces.length - 1];
            for (int i = 1; i < pieces.length; i++) {
                parameters[i - 1] = pieces[i];
            }
            this.spawnResult = SpawnResult.REMOVE;
            this.blockResult = BlockResult.REMOVE_DROPS;
        } else {
            boolean noSpell = false;
            SpawnResult spawnResult = SpawnResult.REMOVE;
            try {
                // If using a specific return type, don't cast a spell
                spawnResult = SpawnResult.valueOf(commandLine.toUpperCase());
                noSpell = true;
            } catch (Exception ignore) {
            }
            BlockResult blockResult = BlockResult.REMOVE_DROPS;
            try {
                // If using a specific return type, don't cast a spell
                blockResult = BlockResult.valueOf(commandLine.toUpperCase());
                noSpell = true;
            } catch (Exception ignore) {
            }
            name = noSpell ? null : commandLine;
            parameters = noSpell ? null : new String[0];
            this.spawnResult = spawnResult;
            this.blockResult = blockResult;
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

    public SpawnResult getSpawnResult() {
        return spawnResult;
    }

    public BlockResult getBlockResult() {
        return blockResult;
    }
}
