package com.elmakers.mine.bukkit.api.action;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public interface GUIAction {
    public void deactivated();
    public void clicked(InventoryClickEvent event);
    public void dragged(InventoryDragEvent event);
}
