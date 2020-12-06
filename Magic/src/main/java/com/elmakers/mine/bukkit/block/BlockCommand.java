package com.elmakers.mine.bukkit.block;

public class BlockCommand extends MaterialExtraData {
    protected String command;
    protected String customName = null;

    public BlockCommand(String command) {
        this(command, null);
    }

    public BlockCommand(String command, String customName) {
        this.command = command;
        this.customName = customName;
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockCommand(command, customName);
    }
}
