package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LockAction extends BaseSpellAction
{
    private enum LockActionType {
        LOCK,
        UNLOCK,
        KEY
    };

    private LockActionType actionType;
    private MaterialAndData iconType;
    private String keyName;
    private String keyDescription;
    private boolean override;

    @Override
    public SpellResult perform(CastContext context)
    {
        String keyName = this.keyName;
        if (keyName.isEmpty())
        {
            keyName = context.getMessage("key_name");
        }
        keyName = keyName
                    .replace("$name", context.getMage().getName())
                    .replace("$uuid", context.getMage().getId());
        String keyDescription = this.keyDescription;
        if (keyDescription.isEmpty())
        {
            keyDescription = context.getMessage("key_description");
        }
        keyDescription = keyDescription.replace("$name", context.getMage().getName());
        Mage mage = context.getMage();
        boolean result = false;
        if (actionType == LockActionType.KEY) {
            giveKey(mage, keyName, keyDescription);
            return SpellResult.CAST;
        }

        Block targetBlock = context.getTargetBlock();
        if (targetBlock == null)
        {
            return SpellResult.NO_TARGET;
        }
        if (!context.hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        if (actionType == LockActionType.LOCK) {
            String lock = CompatibilityUtils.getLock(targetBlock);
            if (lock != null)
            {
                if (lock.equals(keyName))
                {
                    context.sendMessageKey("already");
                    return SpellResult.NO_TARGET;
                }
                if (!override && !InventoryUtils.hasItem(mage, lock))
                {
                    return SpellResult.FAIL;
                }
                context.sendMessageKey("acquire");
            }
            result = CompatibilityUtils.setLock(targetBlock, keyName);
            giveKey(mage, keyName, keyDescription);
        } else {
            if (!CompatibilityUtils.isLocked(targetBlock))
            {
                return SpellResult.FAIL;
            }
            result = CompatibilityUtils.clearLock(targetBlock);
        }
		
		return result ? SpellResult.CAST : SpellResult.FAIL;
	}

    protected void giveKey(Mage mage, String keyName, String keyDescription) {
        if (!InventoryUtils.hasItem(mage, keyName)) {
            ItemStack keyItem = null;
            keyItem = iconType.getItemStack(1);
            ItemMeta meta = keyItem.getItemMeta();
            meta.setDisplayName(keyName);
            if (!keyDescription.isEmpty()) {
                List<String> lore = new ArrayList<String>();
                String[] lines = StringUtils.split(keyDescription, "\n");
                for (String line : lines) {
                    lore.add(line);
                }
                meta.setLore(lore);
            }
            keyItem.setItemMeta(meta);
            keyItem = CompatibilityUtils.makeReal(keyItem);
            CompatibilityUtils.makeUnplaceable(keyItem);
            mage.giveItem(keyItem);
        }
    }

    @Override
    public boolean isUndoable() {
        return false;
    }

    @Override
    public boolean requiresTarget() {
        return actionType != LockActionType.KEY;
    }

    @Override
    public boolean requiresBuildPermission() {
        return actionType != LockActionType.KEY;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        actionType = LockActionType.LOCK;
        String type = parameters.getString("type", "lock");
        if (type.equalsIgnoreCase("unlock")) {
            actionType = LockActionType.UNLOCK;
        } else if (type.equalsIgnoreCase("key")) {
            actionType = LockActionType.KEY;
        }
        keyName = parameters.getString("key_name", "");
        keyDescription = parameters.getString("key_description", "");
        iconType = ConfigurationUtils.getMaterialAndData(parameters, "key_icon", new MaterialAndData(Material.TRIPWIRE_HOOK));
        override = parameters.getBoolean("override", false);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("type");
        parameters.add("key_icon");
        parameters.add("key_name");
        parameters.add("key_description");
        parameters.add("override");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("type")) {
            examples.add("lock");
            examples.add("unlock");
            examples.add("key");
        } else if (parameterKey.equals("override")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
