package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
        UNLOCK
    };

    private LockActionType actionType;
    private MaterialAndData iconType;
    private String keyName;
    private String keyDescription;
    private boolean override;

    @Override
    public SpellResult perform(CastContext context)
    {
		Block targetBlock = context.getTargetBlock();
        if (!context.hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        boolean result = false;
        if (actionType == LockActionType.LOCK) {
            if (!override && CompatibilityUtils.isLocked(targetBlock.getLocation()))
            {
                return SpellResult.FAIL;
            }
            String keyName = this.keyName;
            if (keyName.isEmpty())
            {
                keyName = context.getMessage("key_name");
            }
            keyName = keyName.replace("$name", context.getMage().getName());
            String keyDescription = this.keyDescription;
            if (keyDescription.isEmpty())
            {
                keyDescription = context.getMessage("key_description");
            }
            keyDescription = keyDescription.replace("$name", context.getMage().getName());
            result = CompatibilityUtils.setLock(targetBlock.getLocation(), keyName);

            Player player = context.getMage().getPlayer();
            if (player != null)
            {
                boolean hasKey = false;
                ItemStack[] items = player.getInventory().getContents();
                for (ItemStack item : items)
                {
                    if (item != null && item.hasItemMeta())
                    {
                        String displayName = item.getItemMeta().getDisplayName();
                        if (displayName != null && displayName.equals(keyName))
                        {
                            hasKey = true;
                            break;
                        }
                    }
                }

                if (!hasKey)
                {
                    ItemStack keyItem = null;
                    keyItem = iconType.getItemStack(1);
                    ItemMeta meta = keyItem.getItemMeta();
                    meta.setDisplayName(keyName);
                    if (!keyDescription.isEmpty())
                    {
                        List<String> lore = new ArrayList<String>();
                        lore.add(keyDescription);
                        meta.setLore(lore);
                    }
                    keyItem.setItemMeta(meta);
                    context.getController().giveItemToPlayer(player, keyItem);
                }
            }
        } else {
            if (!CompatibilityUtils.isLocked(targetBlock.getLocation()))
            {
                return SpellResult.FAIL;
            }
            result = CompatibilityUtils.clearLock(targetBlock.getLocation());
        }
		
		return result ? SpellResult.CAST : SpellResult.FAIL;
	}

    @Override
    public boolean isUndoable() {
        return false;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        actionType = LockActionType.LOCK;
        String type = parameters.getString("type", "lock");
        if (type.equalsIgnoreCase("unlock")) {
            actionType = LockActionType.UNLOCK;
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
        } else if (parameterKey.equals("override")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
