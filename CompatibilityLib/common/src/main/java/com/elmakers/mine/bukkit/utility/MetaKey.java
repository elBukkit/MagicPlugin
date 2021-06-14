package com.elmakers.mine.bukkit.utility;

import com.google.common.base.Preconditions;

public class MetaKey<T> {
    private final Class<T> type;
    private final String name;

    public MetaKey(Class<T> type, String name) {
        this.type = Preconditions.checkNotNull(type, "type");
        this.name = Preconditions.checkNotNull(name, name);
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
