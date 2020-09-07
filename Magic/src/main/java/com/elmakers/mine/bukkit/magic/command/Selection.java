package com.elmakers.mine.bukkit.magic.command;

import java.util.List;

public class Selection<T> {
    private T selected;
    private List<T> list;

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public boolean hasList() {
        return list != null;
    }

    public List<T> getList() {
        return list;
    }
}
