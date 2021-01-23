package com.elmakers.mine.bukkit.action.builtin;

import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class CreateNPCAction extends BaseSpellAction {
    private String name;
    private String keyItem;
    private String keyBookContents;
    private String npcTemplate;
    private ConfigurationSection keyItemOverrides;
    private ConfigurationSection npcConfiguration;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        name = parameters.getString("name", "NPC");
        keyItem = parameters.getString("key_item");
        keyBookContents = parameters.getString("key_book_contents");
        keyItemOverrides = parameters.getConfigurationSection("key_overrides");
        npcTemplate = parameters.getString("npc_template");
        npcConfiguration = parameters.getConfigurationSection("npc_parameters");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage owner = context.getMage();
        MageController controller = context.getController();
        String escapedName = context.parameterizeMessage(name);
        MagicNPC npc = controller.addNPC(owner, escapedName);
        if (npcTemplate != null) {
            npc.setTemplate(npcTemplate);
        }
        if (npcConfiguration != null) {
            Set<String> keys = npcConfiguration.getKeys(true);
            for (String key : keys) {
                if (npcConfiguration.isConfigurationSection(key)) continue;
                if (npcConfiguration.isString(key)) {
                    String value = npcConfiguration.getString(key);
                    value = parameterize(context, value, npc.getId(), escapedName);
                    npc.configure(key, value);
                } else {
                    npc.configure(key, npcConfiguration.get(key));
                }
            }
        }
        if (npc != null && keyItem != null) {
            ItemStack item = controller.createItem(keyItem);
            if (keyItemOverrides != null && controller.isWand(item)) {
                Wand wand = controller.getWand(item);
                Set<String> overrideKeys = keyItemOverrides.getKeys(false);
                for (String overrideKey : overrideKeys) {
                    String value = keyItemOverrides.getString(overrideKey);
                    value = parameterize(context, value, npc.getId(), escapedName);
                    wand.setOverride(overrideKey, value);
                }
                String name = wand.getName();
                name = parameterize(context, name, npc.getId(), escapedName);
                wand.setName(name);
                wand.saveState();
            }
            owner.giveItem(item);
        }
        return npc == null ? SpellResult.FAIL : SpellResult.CAST;
    }

    private String parameterize(CastContext context, String message, UUID id, String name) {
        message = message.replace("$npc_name", name);
        message = message.replace("$npc", id.toString());
        message = context.parameterizeMessage(message);
        return message;
    }
}
