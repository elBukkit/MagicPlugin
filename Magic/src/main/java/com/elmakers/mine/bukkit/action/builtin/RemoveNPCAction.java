package com.elmakers.mine.bukkit.action.builtin;

import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class RemoveNPCAction extends BaseSpellAction {
    private UUID npcId;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
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
        controller.removeNPC(npc);
        return SpellResult.CAST;
    }
}
