package com.elmakers.mine.bukkit.integration;

import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.elmakers.mine.bukkit.api.magic.MageController;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VillagerWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class LibsDisguiseManager {
    private final Plugin disguisePlugin;
    private final MageController controller;

    public LibsDisguiseManager(MageController controller, Plugin disguisePlugin) {
        this.disguisePlugin = disguisePlugin;
        this.controller = controller;
    }

    public boolean initialize() {
        return (disguisePlugin != null && disguisePlugin instanceof LibsDisguises);
    }

    public boolean isDisguised(Entity entity) {
        return DisguiseAPI.isDisguised(entity);
    }

    public boolean disguise(Entity entity, ConfigurationSection configuration) {
        if (configuration == null) {
            DisguiseAPI.undisguiseToAll(entity);
            return true;
        }
        String disguiseName = configuration.getString("type");
        if (disguiseName == null || disguiseName.isEmpty()) {
            return false;
        }

        Disguise disguise = DisguiseAPI.getCustomDisguise(disguiseName);
        if (disguise == null) {
            try {
                DisguiseType disguiseType = DisguiseType.valueOf(disguiseName.toUpperCase());
                switch (disguiseType) {
                    case PLAYER:
                        String name = configuration.getString("name", entity.getCustomName());
                        if (name == null || name.isEmpty()) {
                            controller.getLogger().warning("Missing disguise name in player disguise");
                            return false;
                        }
                        PlayerDisguise playerDisguise = new PlayerDisguise(name);
                        String skin = configuration.getString("skin");
                        if (skin != null) {
                            playerDisguise.setSkin(skin);
                        }
                        disguise = playerDisguise;
                        break;
                    case ARMOR_STAND:
                        disguise = new MobDisguise(DisguiseType.ARMOR_STAND);
                        ArmorStandWatcher watcher = (ArmorStandWatcher)disguise.getWatcher();
                        watcher.setMarker(configuration.getBoolean("marker", false));
                        watcher.setNoBasePlate(!configuration.getBoolean("baseplate", true));
                        watcher.setSmall(configuration.getBoolean("small", false));
                        watcher.setShowArms(configuration.getBoolean("arms", false));
                        break;
                    case FALLING_BLOCK:
                    case DROPPED_ITEM:
                        Material material = Material.valueOf(configuration.getString("material").toUpperCase());
                        MiscDisguise itemDisguise = new MiscDisguise(disguiseType, material, configuration.getInt("data"));
                        disguise = itemDisguise;
                        break;
                    case SPLASH_POTION:
                    case PAINTING:
                        MiscDisguise paintingDisguise = new MiscDisguise(disguiseType, configuration.getInt("data"));
                        disguise = paintingDisguise;
                        break;
                    case ARROW:
                    case SPECTRAL_ARROW:
                    case FIREBALL:
                    case SMALL_FIREBALL:
                    case DRAGON_FIREBALL:
                    case WITHER_SKULL:
                    case FISHING_HOOK:
                        MiscDisguise miscDisguise = new MiscDisguise(disguiseType);
                        disguise = miscDisguise;
                        break;
                    case VILLAGER:
                        disguise = new MobDisguise(DisguiseType.VILLAGER);
                        String professionName = configuration.getString("profession");
                        if (professionName != null && !professionName.isEmpty()) {
                            try {
                                Villager.Profession profession = Villager.Profession.valueOf(professionName.toUpperCase());
                                VillagerWatcher villager = (VillagerWatcher)disguise.getWatcher();
                                villager.setProfession(profession);
                            } catch (Exception ex) {
                                controller.getLogger().warning("Invalid villager profession in disguise config: " + professionName);
                            }
                        }
                        break;
                    default:
                        boolean isBaby = configuration.getBoolean("baby", false);
                        disguise = new MobDisguise(disguiseType, !isBaby);
                }

                FlagWatcher watcher = disguise.getWatcher();
                String customName = configuration.getString("custom_name");
                if (customName != null) {
                    watcher.setCustomName(customName);
                    watcher.setCustomNameVisible(configuration.getBoolean("custom_name_visible", true));
                }
                ItemStack helmet = controller.createItem(configuration.getString("helmet"));
                if (helmet != null) {
                    watcher.setHelmet(helmet);
                }
                ItemStack boots = controller.createItem(configuration.getString("boots"));
                if (boots != null) {
                    watcher.setBoots(boots);
                }
                ItemStack leggings = controller.createItem(configuration.getString("leggings"));
                if (leggings != null) {
                    watcher.setLeggings(leggings);
                }
                ItemStack chestplate = controller.createItem(configuration.getString("chestplate"));
                if (chestplate != null) {
                    watcher.setChestplate(chestplate);
                }
                ItemStack mainhand = controller.createItem(configuration.getString("mainhand"));
                if (mainhand != null) {
                    watcher.setItemInMainHand(mainhand);
                }
                ItemStack offhand = controller.createItem(configuration.getString("offhand"));
                if (offhand != null) {
                    watcher.setItemInOffHand(offhand);
                }
                if (configuration.getBoolean("invisible", false)) watcher.setInvisible(true);
                if (configuration.getBoolean("burning", false)) watcher.setBurning(true);
                if (configuration.getBoolean("glowing", false)) watcher.setGlowing(true);
                if (configuration.getBoolean("flying", false)) watcher.setFlyingWithElytra(true);
                if (configuration.getBoolean("sneaking", false)) watcher.setSneaking(true);
                if (configuration.getBoolean("animations", false)) watcher.setAddEntityAnimations(true);
                if (configuration.getBoolean("sprinting", false)) watcher.setSprinting(true);
                if (configuration.getBoolean("swimming", false)) watcher.setSwimming(true);
                if (configuration.getBoolean("right_clicking", false)) watcher.setRightClicking(true);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Error creating disguise", ex);
                return false;
            }
        }
        if (disguise != null) {
            try {
                DisguiseAPI.disguiseEntity(entity, disguise);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Error applying disguise", ex);
                return false;
            }
        }
        return true;
    }

    @Nullable
    public String getSkin(Player player) {
        WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);
        if (profile == null) {
            return null;
        }
        return DisguiseUtilities.getGson().toJson(profile);
    }
}
