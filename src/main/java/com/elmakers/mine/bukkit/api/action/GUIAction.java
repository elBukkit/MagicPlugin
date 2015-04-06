package com.elmakers.mine.bukkit.api.action;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public interface GUIAction extends SpellAction {
    public void deactivated();
    public void clicked(InventoryClickEvent event);
    public void dragged(InventoryDragEvent event);
}
