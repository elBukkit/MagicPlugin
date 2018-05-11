package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import javax.annotation.Nonnull;
import java.util.*;

public class BookAction extends BaseSpellAction {

    @Nonnull
    private String title = "", author = "";
    private List<String> contents = new ArrayList<>();

    private ItemStack createBook(CastContext context, Mage targetMage) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setTitle(title);
        meta.setAuthor(author);
        meta.setPages(replaceContents(context, targetMage));

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

        List<String> copy = new ArrayList<>(contents);
        List<String> newContents = new ArrayList<>();

        for (String str : copy) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                str = str.replace(entry.getKey(), entry.getValue());
            }

            newContents.add(str);
        }

        return newContents;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        Messages messages = context.getController().getMessages();
        String titleParam = parameters.getString("title");
        String authorParam = parameters.getString("author");

        title = messages.get(titleParam, titleParam);
        author = messages.get(authorParam, authorParam);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (context.getTargetEntity() == null) {
            return SpellResult.NO_TARGET;
        }

        if (!(context.getTargetEntity() instanceof InventoryHolder)) {
            return SpellResult.FAIL;
        }

        Mage targetMage = context.getController().getMage(context.getTargetEntity());

        contents = replaceContents(context, targetMage);

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