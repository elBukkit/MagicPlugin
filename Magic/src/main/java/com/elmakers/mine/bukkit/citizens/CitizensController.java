package com.elmakers.mine.bukkit.citizens;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.integration.NPCSupplier;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.npc.MagicNPC;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.trait.text.Text;

public class CitizensController implements NPCSupplier {
    private Citizens citizensPlugin;
    private static Field textTraitText;
    private boolean hasTextTraitField;

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

    @SuppressWarnings({"unchecked"})
    public Collection<MagicNPC> importAll(MagicController controller, Mage creator) {
        List<MagicNPC> npcs = new ArrayList<>();
        Set<Integer> alreadyImported = new HashSet<>();
        for (com.elmakers.mine.bukkit.api.npc.MagicNPC npc : controller.getNPCs()) {
            Integer importedId = npc.getImportedId();
            if (importedId != null) {
                alreadyImported.add(importedId);
            }
        }
        for (NPC npc : citizensPlugin.getNPCRegistry()) {
            if (alreadyImported.contains(npc.getId())) {
                continue;
            }
            MagicNPC magicNPC = new MagicNPC(controller, creator, npc.getStoredLocation(), npc.getName());
            controller.registerNPC(magicNPC);
            ConfigurationSection parameters = magicNPC.getParameters();
            String entityType = "villager";
            Entity entity = npc.getEntity();
            if (entity == null) {
                npc.spawn(npc.getStoredLocation());
                entity = npc.getEntity();
            }
            if (entity == null) {
                creator.sendMessage(ChatColor.YELLOW + "NPC " + npc.getName() + " is missing entity, defaulting to villager");
            } else {
                entityType = entity.getType().name();
                if (entity instanceof Player) {
                    entityType = "villager";
                    ConfigurationSection disguise = new MemoryConfiguration();
                    disguise.set("type", "player");
                    disguise.set("skin", npc.getName());
                    parameters.set("disguise", disguise);
                }
            }

            for (Trait trait : npc.getTraits()) {
                if (trait instanceof Text) {
                    Text text = (Text)trait;
                    if (!hasTextTraitField) {
                        hasTextTraitField = true;
                        try {
                            textTraitText = Text.class.getDeclaredField("text");
                            textTraitText.setAccessible(true);
                        } catch (NoSuchFieldException e) {
                            creator.sendMessage(ChatColor.RED + "Error reading Text trait text, NPC dialog will not be imported");
                        }
                    }
                    if (textTraitText != null) {
                        try {
                            Collection<String> lines = (Collection<String>)textTraitText.get(text);
                            if (lines != null && !lines.isEmpty()) {
                                parameters.set("dialog", lines);
                            }
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                            creator.sendMessage(ChatColor.RED + "Error reading Text trait text, NPC dialog will not be imported");
                        }
                    }
                } else if (trait instanceof CitizensTrait) {
                    CitizensTrait base = (CitizensTrait)trait;
                    if (base.isInvisible()) {
                        List<ConfigurationSection> potionEffects = new ArrayList<>();
                        ConfigurationSection invisibility = new MemoryConfiguration();
                        invisibility.set("type", "invisibility");
                        invisibility.set("duration", "forever");
                        potionEffects.add(invisibility);
                        parameters.set("potion_effects", potionEffects);
                    }
                    ItemStack hat = base.getHat();
                    if (!CompatibilityUtils.isEmpty(hat)) {
                        parameters.set("helmet", controller.getItemKey(hat));
                    }
                    if (base.getCost() > 0) {
                        ConfigurationSection costs = new MemoryConfiguration();
                        costs.set("currency", base.getCost());
                        parameters.set("costs", costs);
                    }
                    String mobKey = base.getMobKey();
                    if (mobKey != null && !mobKey.isEmpty()) {
                        entityType = mobKey;
                    }
                    parameters.set("interact_permission", base.getPermissionNode());

                    if (trait instanceof CommandCitizensTrait) {
                        CommandCitizensTrait command = (CommandCitizensTrait)trait;
                        parameters.set("interact_commands", command.getCommand());
                        if (!command.isConsole()) {
                            if (command.isOP()) {
                                parameters.set("interact_command_source", "OPPED_PLAYER");
                            } else {
                                parameters.set("interact_command_source", "PLAYER");
                            }
                        }
                    }

                    if (trait instanceof MagicCitizensTrait) {
                        MagicCitizensTrait spell = (MagicCitizensTrait)trait;
                        parameters.set("interact_spell", spell.getSpell());
                        parameters.set("interact_spell_parameters", spell.getSpellParameters());
                    }
                }
            }

            // Don't re-import
            magicNPC.setImportedId(npc.getId());

            // This will force-update the NPC
            magicNPC.setType(entityType);
        }
        return npcs;
    }
}
