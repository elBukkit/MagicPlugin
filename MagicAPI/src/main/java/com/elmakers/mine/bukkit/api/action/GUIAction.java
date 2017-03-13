package com.elmakers.mine.bukkit.api.action;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public interface GUIAction {
    void deactivated();
    void clicked(InventoryClickEvent event);
    void dragged(InventoryDragEvent event);
}
