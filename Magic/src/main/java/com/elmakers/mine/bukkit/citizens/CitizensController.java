package com.elmakers.mine.bukkit.citizens;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;

public class CitizensController {
    private Citizens citizensPlugin;

    public CitizensController(Plugin plugin) {
        citizensPlugin = (Citizens)plugin;

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MagicCitizensTrait.class).withName("magic"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CommandCitizensTrait.class).withName("command"));
    }

    public Citizens getCitizensPlugin() {
        return citizensPlugin;
    }

    @Nullable
    private NPC getNPC(Entity entity) {
        if (entity == null) {
            return null;
        }

        return CitizensAPI.getNPCRegistry().getNPC(entity);
    }

    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    public boolean isStaticNPC(Entity entity) {
        NPC npc = getNPC(entity);
        if (npc == null) {
            return false;
        }
        return npc.data().has(NPC.DEFAULT_PROTECTED_METADATA);
    }
}
