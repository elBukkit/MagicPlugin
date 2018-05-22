package com.elmakers.mine.bukkit.citizens;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.integration.NPCSupplier;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;

public class CitizensController implements NPCSupplier {
    private Citizens citizensPlugin;

    public CitizensController(Plugin plugin, MageController controller, boolean enableTraits) {
        citizensPlugin = (Citizens)plugin;

        if (enableTraits) {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MagicCitizensTrait.class).withName("magic"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CommandCitizensTrait.class).withName("command"));
        } else {
            controller.getLogger().info("Citizens traits disabled.");
        }
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

    @Override
    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    @Override
    public boolean isStaticNPC(Entity entity) {
        NPC npc = getNPC(entity);
        if (npc == null) {
            return false;
        }
        return npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
    }
}
