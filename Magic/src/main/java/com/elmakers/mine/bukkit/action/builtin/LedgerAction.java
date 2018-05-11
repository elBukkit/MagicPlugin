package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.BookMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class LedgerAction extends BaseSpellAction {
    private Set<String> ignoreTypes;
    private Set<String> showTypes;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        List<String> ignoreList = ConfigurationUtils.getStringList(parameters, "ignore_currencies");
        if (ignoreList != null) {
            ignoreTypes = new HashSet<>(ignoreList);
        }
        List<String> showList = ConfigurationUtils.getStringList(parameters, "show_currencies");
        if (showList != null) {
            showTypes = new HashSet<>(showList);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Wand wand = context.getWand();
        if (wand == null || wand.getItem().getType() != Material.WRITTEN_BOOK) {
            return SpellResult.NO_TARGET;
        }

        Mage mage = context.getMage();
        BookMeta book = (BookMeta)wand.getItem().getItemMeta();
        book.setTitle(context.getMessage("title"));
        book.setAuthor(context.getMage().getName());

        MageController controller = context.getController();
        Collection<String> currencyKeys = controller.getCurrencyKeys();
        String header = context.getMessage("header");
        String currencyPrefix = context.getMessage("currency_prefix");
        String valuePrefix = context.getMessage("value_prefix");
        List<String> pages = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        for (String key : currencyKeys) {
            Currency currency = controller.getCurrency(key);
            if (currency == null) {
                continue;
            }
            double amount = currency.getBalance(mage, wand);
            if (showTypes != null && !showTypes.contains(key)) {
                continue;
            }
            if (amount > 0 && (ignoreTypes == null || !ignoreTypes.contains(key))) {
                if (lines.size() >= 12) {
                    pages.add(header + "\n" + StringUtils.join(lines, "\n"));
                    lines.clear();
                }
                lines.add(currencyPrefix + currency.getName(controller.getMessages()));
                lines.add(valuePrefix + currency.formatAmount(amount, controller.getMessages()));
            }
        }
        if (!lines.isEmpty()) {
            pages.add(header + "\n" + StringUtils.join(lines, "\n"));
        }
        book.setPages(pages);
        wand.getItem().setItemMeta(book);
        if (wand == mage.getActiveWand()) {
            mage.getPlayer().getInventory().setItemInMainHand(wand.getItem());
        } else if (wand == mage.getOffhandWand()) {
            mage.getPlayer().getInventory().setItemInOffHand(wand.getItem());
        }

        return SpellResult.CAST;
    }
}
