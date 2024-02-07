package com.elmakers.mine.bukkit.item;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class Icon implements com.elmakers.mine.bukkit.api.item.Icon {
    private final MageController controller;
    private final String itemKey;
    private final String itemDisabledKey;
    private final String legacyItemKey;
    private final String legacyItemDisabledKey;
    private final String vanillaItemKey;
    private final String vanillaItemDisabledKey;
    private final String url;
    private final String urlDisabled;
    private final String glyph;
    private final String type;
    private final boolean forceUrl;

    public Icon(MageController controller) {
        this.controller = controller;
        itemKey = BaseSpell.DEFAULT_SPELL_ICON.name().toLowerCase();
        itemDisabledKey = null;
        legacyItemKey = null;
        legacyItemDisabledKey = null;
        vanillaItemKey = null;
        vanillaItemDisabledKey = null;
        url = null;
        urlDisabled = BaseSpell.DEFAULT_DISABLED_ICON_URL;
        glyph = null;
        forceUrl = false;
        type = null;
    }

    public Icon(MageController controller, ConfigurationSection configuration) {
        this.controller = controller;
        itemKey = configuration.getString("item");
        itemDisabledKey = configuration.getString("item_disabled");
        legacyItemKey = configuration.getString("legacy_item");
        legacyItemDisabledKey = configuration.getString("legacy_item_disabled");
        vanillaItemKey = configuration.getString("vanilla_item");
        vanillaItemDisabledKey = configuration.getString("vanilla_item_disabled");
        url = configuration.getString("url");
        urlDisabled = configuration.getString("url_disabled");
        glyph = configuration.getString("glyph");
        forceUrl = configuration.getBoolean("force_url", false);
        type = configuration.getString("type");
    }

    public Icon(com.elmakers.mine.bukkit.api.item.Icon defaultAPI, com.elmakers.mine.bukkit.api.item.Icon baseAPI) {
        if (!(baseAPI instanceof Icon) || !(defaultAPI instanceof Icon)) {
            throw new IllegalStateException("Icon is not the correct implementation type");
        }
        Icon defaultIcon = (Icon)defaultAPI;
        Icon baseIcon = (Icon)baseAPI;
        this.controller = defaultIcon.controller;
        itemKey = baseIcon.itemKey != null ? baseIcon.itemKey : defaultIcon.itemKey;
        itemDisabledKey = baseIcon.itemDisabledKey != null ? baseIcon.itemDisabledKey : defaultIcon.itemDisabledKey;
        legacyItemKey = baseIcon.legacyItemKey != null ? baseIcon.legacyItemKey : defaultIcon.legacyItemKey;
        legacyItemDisabledKey = baseIcon.legacyItemDisabledKey != null ? baseIcon.legacyItemDisabledKey : defaultIcon.legacyItemDisabledKey;
        vanillaItemKey = baseIcon.vanillaItemKey != null ? baseIcon.vanillaItemKey : defaultIcon.vanillaItemKey;
        vanillaItemDisabledKey = baseIcon.vanillaItemDisabledKey != null ? baseIcon.vanillaItemDisabledKey : defaultIcon.vanillaItemDisabledKey;
        url = baseIcon.url != null ? baseIcon.url : defaultIcon.url;
        urlDisabled = baseIcon.urlDisabled != null ? baseIcon.urlDisabled : defaultIcon.urlDisabled;
        glyph = baseIcon.glyph != null ? baseIcon.glyph : defaultIcon.glyph;
        forceUrl = baseIcon.forceUrl;
        type = baseIcon.type;
    }

    public Icon(com.elmakers.mine.bukkit.api.item.Icon baseIcon, ConfigurationSection configuration, String itemIcon) {
        if (!(baseIcon instanceof Icon)) {
            throw new IllegalStateException("Icon is not the correct implementation type");
        }
        Icon other = (Icon)baseIcon;
        this.controller = other.controller;
        itemKey = configuration.getString("icon_item", itemIcon != null ? itemIcon : other.itemKey);
        itemDisabledKey = configuration.getString("icon_disabled", other.itemDisabledKey);
        legacyItemKey = configuration.getString("legacy_icon", other.legacyItemKey);
        legacyItemDisabledKey = configuration.getString("legacy_icon_disabled", other.legacyItemDisabledKey);
        vanillaItemKey = configuration.getString("vanilla_item", other.vanillaItemKey);
        vanillaItemDisabledKey = configuration.getString("vanilla_item_disabled", other.vanillaItemDisabledKey);
        url = configuration.getString("icon_url", other.url);
        urlDisabled = configuration.getString("icon_disabled_url", other.urlDisabled);
        glyph = configuration.getString("glyph", other.glyph);
        boolean onlyHasUrl = configuration.contains("icon_url") && itemIcon == null;
        forceUrl = configuration.getBoolean("force_url", onlyHasUrl);
        type = configuration.getString("type", other.type);
    }

    @Override
    @Nullable
    public String getUrl() {
        return url;
    }

    @Override
    @Nullable
    public String getUrlDisabled() {
        return urlDisabled;
    }

    @Override
    @Nullable
    public String getGlyph() {
        return glyph;
    }

    @Override
    public boolean forceUrl() {
        return forceUrl;
    }

    private MaterialAndData getItem(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        ItemData itemData = controller.getOrCreateItem(key);
        return itemData == null ? null : itemData.getMaterialAndData();
    }

    @Override
    @Nullable
    public MaterialAndData getItemMaterial(MageController controller) {
        if (forceUrl || controller.isUrlIconsEnabled()) {
            if (url != null) {
                return getItem("skull:" + url);
            }
        }
        return getItemMaterial(controller.isLegacyIconsEnabled(), controller.isVanillaIconsEnabled());
    }

    @Override
    @Nullable
    @Deprecated
    public MaterialAndData getItemMaterial(boolean isLegacy) {
        return getItemMaterial(isLegacy, false);
    }

    @Nullable
    public MaterialAndData getItemMaterial(boolean isLegacy, boolean isVanilla) {
        String useKey = null;
        if (isLegacy) {
            useKey = legacyItemKey;
        }
        if (isVanilla && (useKey == null || useKey.isEmpty())) {
            useKey = vanillaItemKey;
        }
        if (useKey == null || useKey.isEmpty()) {
            useKey = itemKey;
        }
        return getItem(useKey);
    }

    @Override
    @Nullable
    public MaterialAndData getItemDisabledMaterial(MageController controller) {
        if (forceUrl || controller.isUrlIconsEnabled()) {
            if (urlDisabled != null) {
                return getItem("skull:" + urlDisabled);
            }
            if (url != null) {
                return getItem("skull:" + url);
            }
        }
        return getItemDisabledMaterial(controller.isLegacyIconsEnabled(), controller.isVanillaIconsEnabled());
    }

    @Override
    @Nullable
    @Deprecated
    public MaterialAndData getItemDisabledMaterial(boolean isLegacy) {
        return getItemDisabledMaterial(isLegacy, false);
    }

    @Nullable
    public MaterialAndData getItemDisabledMaterial(boolean isLegacy, boolean isVanilla) {
        String useKey = null;
        if (isLegacy) {
            useKey = legacyItemDisabledKey != null && !legacyItemDisabledKey.isEmpty()
                    ? legacyItemDisabledKey : legacyItemKey;
        }
        if (isVanilla && (useKey == null || useKey.isEmpty())) {
            useKey = vanillaItemDisabledKey != null && !vanillaItemDisabledKey.isEmpty()
                    ? vanillaItemDisabledKey : vanillaItemKey;
        }
        if (useKey == null || useKey.isEmpty()) {
            useKey = itemDisabledKey != null && !itemDisabledKey.isEmpty()
                    ? itemDisabledKey : itemKey;
        }
        return getItem(useKey);
    }

    @Override
    @Nullable
    public String getType() {
        return type;
    }
}
