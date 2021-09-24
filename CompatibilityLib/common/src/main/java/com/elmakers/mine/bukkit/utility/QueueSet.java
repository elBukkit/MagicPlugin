package com.elmakers.mine.bukkit.utility;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class QueueSet<E> extends LinkedHashSet<E> {
    public E remove() {
        Iterator<E> i = iterator();
        E next = i.next();
        i.remove();
        return next;
    }
}
