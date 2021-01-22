package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class BookAction extends BaseSpellAction {

    @Nonnull
    private String title = "";
    @Nonnull
    private String author = "";
    @Nullable
    private ConfigurationSection pages;
    private boolean giveBook;

    private ItemStack updateBook(CastContext context, Mage targetMage, ItemStack book) {
        BookMeta meta = (BookMeta) book.getItemMeta();

        List<String> pages = replaceContents(context, targetMage);

        meta.setTitle(title);
        meta.setAuthor(author);
        meta.setPages(pages);

        book.setItemMeta(meta);

        return book;
    }

    private static class Replacement {
        public String replace;
        public String with;

        public Replacement(String replace, String with) {
            this.replace = replace;
            this.with = with;
        }
    }

    private List<String> replaceContents(CastContext context, Mage targetMage) {
        List<Replacement> replacements = new ArrayList<>();

        Set<String> attributes = context.getController().getAttributes();
        Set<String> currencies = context.getController().getCurrencyKeys();

        for (String attr : attributes) {
            Double value = targetMage.getAttribute(attr);
            if (value != null) {
                replacements.add(new Replacement("$" + attr, Integer.toString((int)Math.ceil(value))));
            }
        }

        for (String currency : currencies) {
            replacements.add(new Replacement("$" + currency, Integer.toString((int)Math.ceil(targetMage.getCurrency(currency)))));
        }

        replacements.add(new Replacement("@tn", targetMage.getName()));
        replacements.add(new Replacement("@td", targetMage.getDisplayName()));
        replacements.add(new Replacement("@p", context.getMage().getName()));
        replacements.add(new Replacement("@pd", context.getMage().getDisplayName()));

        MageClass mageClass = targetMage.getActiveClass();
        String className = mageClass != null ? mageClass.getName() : "";
        ProgressionPath magePath = targetMage.getActiveProperties().getPath();
        String pathName = magePath != null ? magePath.getName() : "";

        replacements.add(new Replacement("$class", className));
        replacements.add(new Replacement("$path", pathName));

        List<String> newContents = new ArrayList<>();
        Set<String> pageKeys = pages.getKeys(false);
        for (String pageKey : pageKeys) {
            int pageNumber = 0;
            try {
                pageNumber = Integer.parseInt(pageKey) - 1;
            } catch (NumberFormatException ex) {
                context.getController().getLogger().warning("Invalid page number: " + pageKey);
                continue;
            }
            String pageText = "";
            List<String> lines = ConfigurationUtils.getStringList(pages, pageKey);
            if (lines == null) {
                pageText = pages.getString(pageKey);
            } else {
                pageText = StringUtils.join(lines, "\n");
            }

            for (Replacement replacement : replacements) {
                pageText = pageText.replace(replacement.replace, replacement.with);
            }

            while (newContents.size() <= pageNumber) newContents.add("");
            newContents.set(pageNumber, ChatColor.translateAlternateColorCodes('&', pageText));
        }

        return newContents;
    }

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);

        Messages messages = context.getController().getMessages();
        String titleParam = parameters.getString("title", "");
        String authorParam = parameters.getString("author", context.getMage().getName());

        title = messages.get(titleParam, ChatColor.translateAlternateColorCodes('&', titleParam));
        author = messages.get(authorParam, ChatColor.translateAlternateColorCodes('&', authorParam));
        pages = parameters.getConfigurationSection("pages");
        giveBook = parameters.getBoolean("give_book", false);
    }

    public SpellResult performGive(CastContext context) {
        Entity target = context.getTargetEntity();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(target instanceof InventoryHolder)) {
            return SpellResult.NO_TARGET;
        }

        Mage targetMage = context.getController().getMage(target);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        updateBook(context, targetMage, book);

        targetMage.giveItem(book);

        return SpellResult.CAST;
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (pages == null) {
            return SpellResult.FAIL;
        }
        if (giveBook) {
            return performGive(context);
        }
        Mage mage = context.getMage();
        Wand wand = context.getWand();
        if (wand == null || wand.getItem().getType() != Material.WRITTEN_BOOK) {
            return SpellResult.NO_TARGET;
        }

        ItemStack book = wand.getItem();
        updateBook(context, mage, book);
        if (wand == mage.getActiveWand()) {
            mage.getPlayer().getInventory().setItemInMainHand(wand.getItem());
        } else if (wand == mage.getOffhandWand()) {
            mage.getPlayer().getInventory().setItemInOffHand(wand.getItem());
        }

        return SpellResult.CAST;
    }
}
