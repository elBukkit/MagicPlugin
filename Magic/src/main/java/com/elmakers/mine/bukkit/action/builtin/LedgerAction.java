package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class LedgerAction extends BaseSpellAction {

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
            double amount = mage.getCurrency(key);
            if (amount > 0 && currency != null) {
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
