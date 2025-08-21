package com.elmakers.mine.bukkit.block;

public class GenericExtraData implements Cloneable {
    protected Object equippable;

    @Override
    public GenericExtraData clone() {
        GenericExtraData cloned = new GenericExtraData();
        cloned.equippable = equippable;
        return cloned;
    }

    public Object getEquippable() {
        return equippable;
    }

    public void setEquippable(Object equippable) {
        this.equippable = equippable;;
    }
}
