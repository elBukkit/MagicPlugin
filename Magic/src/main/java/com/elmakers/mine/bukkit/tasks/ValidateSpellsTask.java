package com.elmakers.mine.bukkit.tasks;

import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.spell.ActionSpell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.MagicLogger;

public class ValidateSpellsTask implements Runnable {
    private final MagicController controller;
    private final CommandSender sender;

    public ValidateSpellsTask(MagicController controller, CommandSender sender) {
        this.controller = controller;
        this.sender = sender;
    }

    @Override
    public void run() {
        MagicLogger logger = controller.getLogger();
        Mage mage = controller.getRegisteredMage(sender);
        if (mage != null) {
            Collection<SpellTemplate> spells = controller.getSpellTemplates();
            for (SpellTemplate newSpell : spells) {
                String key = newSpell.getKey();
                logger.setContext("spells." + key);
                // For spells to check parameters for errors/warnings
                if (newSpell instanceof BaseSpell) {
                    BaseSpell spell = (BaseSpell)newSpell;
                    try {
                        MageSpell mageSpell = spell.createMageSpell(mage);
                        com.elmakers.mine.bukkit.action.CastContext context = new com.elmakers.mine.bukkit.action.CastContext(mageSpell);
                        context.setWorkingParameters(mageSpell.getSpellParameters());
                        if (mageSpell instanceof ActionSpell) {
                            ActionSpell actionSpell = (ActionSpell)mageSpell;
                            Collection<String> handlers = actionSpell.getHandlers();
                            for (String handler : handlers) {
                                actionSpell.setCurrentHandler(handler, context);
                                actionSpell.reloadParameters(context);
                            }
                        } else {
                            mageSpell.reloadParameters(context);
                        }
                        if (mageSpell instanceof BaseSpell) {
                            ((BaseSpell)mageSpell).validateEffects();
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "There was an error checking spell " + key + " for issues", ex);
                    }
                }
            }
        }

        Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                controller.resetLoading(sender);
            }
        });
    }
}
