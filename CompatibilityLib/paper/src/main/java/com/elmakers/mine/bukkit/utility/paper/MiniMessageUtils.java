package com.elmakers.mine.bukkit.utility.paper;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MiniMessageUtils implements com.elmakers.mine.bukkit.utility.platform.MiniMessageUtils {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final GsonComponentSerializer gson = GsonComponentSerializer.gson();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

    @Override
    public String toRichText(String text) {
        Component component = miniMessage.deserialize(text);
        return "`" + gson.serialize(component).replace("`", "\\u0060") + "`";
    }

    @Override
    public String toLegacyText(String text) {
        return legacy.serialize(miniMessage.deserialize(text));
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, String text) {
        return Bukkit.createInventory(holder, size, miniMessage.deserialize(text));
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String text) {
        return Bukkit.createInventory(holder, type, miniMessage.deserialize(text));
    }
}
