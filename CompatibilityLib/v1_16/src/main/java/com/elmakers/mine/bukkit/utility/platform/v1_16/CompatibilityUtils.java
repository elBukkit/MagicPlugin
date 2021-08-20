package com.elmakers.mine.bukkit.utility.platform.v1_16;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Pattern hexColorPattern = Pattern.compile("&(#[A-Fa-f0-9]{6})");

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
                if (component.startsWith("{")) {
                    addComponents = Arrays.asList(ComponentSerializer.parse(component));
                } else {
                    addComponents = Arrays.asList(TextComponent.fromLegacyText(component));
                }
                if (!addComponents.isEmpty()) {
                    addToComponents.addAll(addComponents);
                    if (addToComponent != null) {
                        addToComponent.setExtra(addToComponents);
                    }

                    addToComponent = addToComponents.get(addToComponents.size() - 1);
                    addToComponents = addToComponent.getExtra();
                    if (addToComponents == null) {
                        addToComponents = new ArrayList<>();
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
        sender.spigot().sendMessage(parseChatComponents(containsJson));
    }

    @Override
    public String translateColors(String message) {
        message = super.translateColors(message);
        Matcher matcher = hexColorPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String match = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of(match).toString());
        }
        return matcher.appendTail(buffer).toString();
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
        if (message.contains("`{")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, parseChatComponents(message));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
        return true;
    }

    @Override
    public boolean sendActionBar(Player player, String message, String font) {
        if (font == null || font.isEmpty()) {
            return sendActionBar(player, message);
        }
        BaseComponent[] components;
        if (message.contains("`{")) {
            components = parseChatComponents(message);
        } else {
            components = new BaseComponent[]{new TextComponent(message)};
        }
        BaseComponent[] fontComponent = new ComponentBuilder("").font(font).append(components).create();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, fontComponent);
        return true;
    }
}
