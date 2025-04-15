package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class DebuggerAction extends BaseSpellAction
{
    private int debugLevel;
    private boolean check;
    private boolean checkBrain;
    private boolean checkBlock;
    private boolean forceMage;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        debugLevel = parameters.getInt("level", 1);
        check = parameters.getBoolean("check", false);
        checkBrain = parameters.getBoolean("check_brain", false);
        checkBlock = parameters.getBoolean("check_block", false);
        forceMage = parameters.getBoolean("force", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        MageController controller = context.getController();
        Mage mage = null;
        Block block = context.getTargetBlock();
        if (entity != null && (controller.isMage(entity) || forceMage)) {
            mage = controller.getMage(entity);
        } else {
            if (DefaultMaterials.isCommand(block.getType())) {
                CommandBlock commandBlock = (CommandBlock)block.getState();
                // This is a bit of hacky ..
                String commandName = commandBlock.getName();
                String mageId = "COMMAND";
                if (commandName != null && commandName.length() > 0) {
                    mageId = "COMMAND-" + commandBlock.getName();
                }
                mage = controller.getRegisteredMage(mageId);
            }
        }
        if (checkBlock) {
            String blockName = block.getType().name().toLowerCase();
            Object data = CompatibilityLib.getCompatibilityUtils().getTileEntityData(block.getLocation());
            if (data != null) {
                blockName += ":" + data;
            } else {
                data = CompatibilityLib.getCompatibilityUtils().getBlockData(block);
                if (data != null) {
                    blockName += ":" + data;
                }
            }
            context.getMage().sendMessage(ChatColor.GOLD + "Targeted: " + ChatColor.YELLOW + blockName);
        }
        if (mage == null) {
            return SpellResult.NO_TARGET;
        }

        if (check) {
            mage.debugPermissions(context.getMage().getCommandSender(), null);
        }
        if (checkBrain) {
            mage.debugBrain(context.getMage().getCommandSender());
        }

        int currentLevel = mage.getDebugLevel();
        if (currentLevel == debugLevel || debugLevel == 0) {
            mage.setDebugLevel(0);
            mage.setDebugger(null);
            return SpellResult.DEACTIVATE;
        }

        mage.setDebugLevel(debugLevel);
        mage.setDebugger(context.getMage().getCommandSender());

        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTarget()
    {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("level");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("level")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
