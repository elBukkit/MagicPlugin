package com.elmakers.mine.bukkit.utility.platform.v1_16;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class CompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_15.CompatibilityUtils {
    public CompatibilityUtils(Platform platform) {
        super(platform);
    }

    protected BaseComponent[] parseChatComponents(String containsJson) {
        List<BaseComponent> components = new ArrayList<>();
        List<BaseComponent> addToComponents = components;
        String[] pieces = ChatUtils.getComponents(containsJson);
        BaseComponent addToComponent = null;
        for (String component : pieces) {
            try {
                List<BaseComponent> addComponents;
                boolean isJson = component.startsWith("{");
                if (isJson) {
                    addComponents = Arrays.asList(ComponentSerializer.parse(component));
                } else {
                    addComponents = Arrays.asList(TextComponent.fromLegacyText(component));
                }
                if (!addComponents.isEmpty()) {
                    addToComponents.addAll(addComponents);
                    if (addToComponent != null) {
                        addToComponent.setExtra(addToComponents);
                    }

                    // If this is a legacy text string, append to it to keep the legacy
                    // behavior of formatting affecting everything after it.
                    // if this is a json block, formatting only affects what is in that block.
                    if (!isJson) {
                        addToComponent = addToComponents.get(addToComponents.size() - 1);
                        addToComponents = addToComponent.getExtra();
                        if (addToComponents == null) {
                            addToComponents = new ArrayList<>();
                        }
                    }
                }
            } catch (Exception ex) {
                platform.getLogger().log(Level.SEVERE, "Error parsing chat components from: " + component, ex);
            }
        }
        return components.toArray(new BaseComponent[0]);
    }

    @Override
    public void sendChatComponents(CommandSender sender, String containsJson) {
        if (sender instanceof Player) {
            sender.spigot().sendMessage(parseChatComponents(containsJson));
        } else {
            sender.sendMessage(ChatUtils.getSimpleMessage(containsJson));
        }
    }

    @Override
    protected String getHexColor(String hexCode) {
        return ChatColor.of(hexCode).toString();
    }

    @Override
    public boolean tame(Entity entity, Player tamer) {
        if (entity instanceof Fox) {
            if (tamer == null) return false;
            Fox fox = (Fox)entity;
            AnimalTamer current = fox.getFirstTrustedPlayer();
            if (current != null && current.getUniqueId().equals(tamer.getUniqueId())) {
                return false;
            }
            fox.setFirstTrustedPlayer(tamer);
        }
        return super.tame(entity, tamer);
    }

    @Override
    public Recipe createSmithingRecipe(String key, ItemStack item, ItemStack source, ItemStack addition) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = new RecipeChoice.ExactChoice(source);
            RecipeChoice additionChoice = new RecipeChoice.ExactChoice(addition);
            return new SmithingRecipe(namespacedKey, item, choice, additionChoice);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating smithing recipe", ex);
        }
        return null;
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        if (ChatUtils.hasJSON(message)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, parseChatComponents(message));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
        return true;
    }

    @Override
    public boolean sendActionBar(Player player, String message, String font) {
        if (ChatUtils.isDefaultFont(font)) {
            return sendActionBar(player, message);
        }
        BaseComponent[] components;
        if (ChatUtils.hasJSON(message)) {
            components = parseChatComponents(message);
        } else {
            components = new BaseComponent[]{new TextComponent(message)};
        }
        BaseComponent[] fontComponent = new ComponentBuilder("").font(font).append(components).create();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, fontComponent);
        return true;
    }

    @Override
    public String translateAlternateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
