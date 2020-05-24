package com.elmakers.mine.bukkit.magic;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class MageClass extends BaseMageModifier implements com.elmakers.mine.bukkit.api.magic.MageClass  {

    @SuppressWarnings("null") // template initialised via setter
    public MageClass(@Nonnull Mage mage, @Nonnull MageClassTemplate template) {
        super(mage, template.hasParent() ? MagicPropertyType.SUBCLASS : MagicPropertyType.CLASS, template);
        this.setTemplate(template);
    }

    @Override
    protected String getMessageKey(String key) {
        TemplateProperties template = getTemplate();
        if (template != null) {
            String mageKey = "classes." + template.getKey() + "." + key;
            if (controller.getMessages().containsKey(mageKey)) {
                return mageKey;
            }
        }
        return "mage." + key;
    }

    public void onRemoved() {
        onLocked();
        trigger("removed");
    }

    @Override
    public void onLocked() {
        deactivateAttributes();
        if (getBoolean("clean_on_lock", false)) {
            Player player = mage.getPlayer();
            if (player != null) {
                Inventory inventory = player.getInventory();
                String key = getKey();
                for (int i = 0; i < inventory.getSize(); i++) {
                    ItemStack item = inventory.getItem(i);
                    if (controller.isSkill(item)) {
                        String skillClass = Wand.getSpellClass(item);
                        if (skillClass != null && skillClass.equals(key)) {
                            inventory.setItem(i, null);
                        }
                    }
                }
            }
        }

        List<String> classItems = getStringList("class_items");
        if (classItems != null) {
            for (String classItemKey : classItems) {
                ItemStack item = controller.createItem(classItemKey);
                if (item == null) {
                    // We already nagged about this on load...
                    continue;
                }

                mage.removeItem(item);
            }
        }
        cancelTrigger("unlock");
        cancelTrigger("join");
        trigger("lock");
    }

    @Override
    public void onUnlocked() {
        activateAttributes();
        List<String> classItems = getStringList("class_items");
        if (classItems != null) {
            for (String classItemKey : classItems) {
                ItemStack item = controller.createItem(classItemKey);
                if (item == null) {
                    controller.getLogger().warning("Invalid class item in " + getKey() + ": " + classItemKey);
                    continue;
                }

                if (!mage.hasItem(item)) {
                    String wandKey = controller.getWandKey(item);
                    if (wandKey != null) {
                        Wand wand = mage.getBoundWand(wandKey);
                        if (wand != null) {
                            mage.giveItem(wand.getItem());
                            continue;
                        }
                    }

                    mage.giveItem(item);
                }
            }
        }
        trigger("unlock");
    }

    public void setTemplate(@Nonnull MageClassTemplate template) {
        // TODO: This won't update the "type" field of the base base base class here if the
        // template hierarchy has drastically changed.
        super.setTemplate(template.getMageTemplate(getMage()));
    }

    @Override
    public void load(@Nullable ConfigurationSection configuration) {
        this.configuration = new MageParameters(getMage(), "Mage class " + getKey());
        if (configuration != null) {
            ConfigurationUtils.addConfigurations(this.configuration, configuration);
        }
    }

    @Override
    @Deprecated
    @Nullable
    public SpellTemplate getBaseSpell(String spellKey) {
        return getSpellTemplate(spellKey);
    }
}
