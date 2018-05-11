package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class BookAction extends BaseSpellAction {

    @Nonnull
    private String title = "";
    @Nonnull
    private String author = "";
    @Nullable
    private List<String> contents;

    private ItemStack createBook(CastContext context, Mage targetMage) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        List<String> pages = replaceContents(context, targetMage);
        
        meta.setTitle(title);
        meta.setAuthor(author);
        meta.setPages(pages);

        book.setItemMeta(meta);

        return book;
    }

    private List<String> replaceContents(CastContext context, Mage targetMage) {
        Map<String, String> replacements = new HashMap<>();

        replacements.put("@tn", targetMage.getName());
        replacements.put("@td", targetMage.getDisplayName());

        Set<String> attributes = context.getController().getAttributes();
        Set<String> currencies = context.getController().getCurrencyKeys();

        for (String attr : attributes) {
            replacements.put("$attribute_" + attr, String.valueOf(targetMage.getProperties().getAttribute(attr)));
        }

        for (String currency : currencies) {
            replacements.put("$balance_" + currency, String.valueOf(targetMage.getCurrency(currency)));
        }

        List<String> newContents = new ArrayList<>();
        for (String str : contents) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                str = str.replace(entry.getKey(), entry.getValue());
            }

            newContents.add(ChatColor.translateAlternateColorCodes('&', str));
        }

        return newContents;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        Messages messages = context.getController().getMessages();
        String titleParam = parameters.getString("title");
        String authorParam = parameters.getString("author");

        title = messages.get(titleParam, ChatColor.translateAlternateColorCodes('&', titleParam));
        author = messages.get(authorParam, ChatColor.translateAlternateColorCodes('&', authorParam));
        contents = parameters.getStringList("contents");
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (contents == null) {
            return SpellResult.FAIL;
        }
        Entity target = context.getTargetEntity();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(target instanceof InventoryHolder)) {
            return SpellResult.NO_TARGET;
        }

        Mage targetMage = context.getController().getMage(target);
        ItemStack book = createBook(context, targetMage);

        targetMage.giveItem(book);

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
