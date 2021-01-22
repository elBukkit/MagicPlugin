package com.elmakers.mine.bukkit.action.builtin;

import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class ModifyNPCAction extends BaseSpellAction {
    private UUID npcId;
    private String name;
    private String npcTemplate;
    private ConfigurationSection npcConfiguration;
    private boolean nameFromWand;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        name = parameters.getString("name");
        npcTemplate = parameters.getString("npc_template");
        npcConfiguration = parameters.getConfigurationSection("npc_parameters");
        nameFromWand = parameters.getBoolean("name_from_wand", false);
        String idString = parameters.getString("npc_id");
        if (idString != null && !idString.isEmpty()) {
            try {
                npcId = UUID.fromString(idString);
            } catch (Exception ex) {
                context.getLogger().warning("Invalid npc_id: " + idString);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (npcId == null) {
            return SpellResult.FAIL;
        }
        MageController controller = context.getController();
        MagicNPC npc = controller.getNPC(npcId);
        if (npc == null) {
            return SpellResult.NO_TARGET;
        }
        if (name != null && !name.isEmpty()) {
            npc.setName(name);
        }
        if (npcTemplate != null) {
            npc.setTemplate(npcTemplate);
        }
        if (npcConfiguration != null) {
            Set<String> keys = npcConfiguration.getKeys(true);
            for (String key : keys) {
                if (npcConfiguration.isConfigurationSection(key)) continue;
                if (npcConfiguration.isString(key)) {
                    String value = npcConfiguration.getString(key);
                    value = parameterize(context, value, npc.getId(), npc.getName());
                    npc.configure(key, value);
                } else {
                    npc.configure(key, npcConfiguration.get(key));
                }
            }
        }
        if (nameFromWand) {
            Wand wand = context.getWand();
            if (wand != null) {
                npc.setName(wand.getName());
            }
        }
        return SpellResult.CAST;
    }

    private String parameterize(CastContext context, String message, UUID id, String name) {
        message = message.replace("$npc_name", name);
        message = message.replace("$npc", id.toString());
        message = context.parameterizeMessage(message);
        return message;
    }
}
