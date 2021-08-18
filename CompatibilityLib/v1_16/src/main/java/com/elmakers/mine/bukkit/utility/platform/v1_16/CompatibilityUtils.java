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

import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class CompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_15.CompatibilityUtils {
    private final Pattern hexColorPattern = Pattern.compile("&(#[A-Fa-f0-9]{6})");

    public CompatibilityUtils(Platform platform) {
        super(platform);
    }

    @Override
    public void sendChatComponents(CommandSender sender, String containsJson) {
        List<BaseComponent> components = new ArrayList<>();
        String[] pieces = getComponents(containsJson);
        for (String component : pieces) {
            List<BaseComponent> addToComponents = components;
            if (!components.isEmpty()) {
                BaseComponent addToComponent = components.get(components.size() - 1);
                addToComponents = addToComponent.getExtra();
                if (addToComponents == null) {
                    addToComponents = new ArrayList<>();
                }
                addToComponent.setExtra(addToComponents);
            }
            try {
                if (component.startsWith("{")) {
                    addToComponents.addAll(Arrays.asList(ComponentSerializer.parse(component)));
                } else {
                    addToComponents.addAll(Arrays.asList(TextComponent.fromLegacyText(component)));
                }
            } catch (Exception ex) {
                platform.getLogger().log(Level.SEVERE, "Error parsing chat components from: " + component, ex);
            }
        }
        sender.spigot().sendMessage(components.toArray(new BaseComponent[0]));
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
}
