package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        String balances = context.getMessage("header");
        for (String key : currencyKeys) {
            Currency currency = controller.getCurrency(key);
            double amount = currency.getBalance(mage, wand);
            if (showTypes != null && !showTypes.contains(key)) {
                continue;
            }
            if (amount > 0 && currency != null && (ignoreTypes == null || !ignoreTypes.contains(key))) {
                balances += "\n" + currency.formatAmount(amount, controller.getMessages());
            }
        }
        book.setPages(balances);
        wand.getItem().setItemMeta(book);
        if (wand == mage.getActiveWand()) {
            mage.getPlayer().getInventory().setItemInMainHand(wand.getItem());
        } else if (wand == mage.getOffhandWand()) {
            mage.getPlayer().getInventory().setItemInOffHand(wand.getItem());
        }

        return SpellResult.CAST;
    }
}
