package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity;

import java.util.Iterator;

import org.bukkit.Keyed;
import org.bukkit.Registry;

public abstract class EntityExtraData extends com.elmakers.mine.bukkit.entity.EntityExtraData {

    protected <T extends Keyed> T cycleRegistryValue(T value, Registry<T> registry) {
        Iterator<T> it = registry.iterator();
        while (it.hasNext()) {
            T testValue = it.next();
            if (value == testValue) {
                if (it.hasNext()) {
                    return it.next();
                } else {
                    return registry.iterator().next();
                }
            }
        }
        return value;
    }
}
