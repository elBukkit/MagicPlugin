package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BrushSelectAction extends BaseSpellAction implements GUIAction
{
    private CastContext context;

    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        if (context != null)
        {
            Mage mage = context.getMage();
            mage.deactivateGUI();
            Wand wand = mage.getActiveWand();
            ItemStack item = event.getCurrentItem();
            if (wand != null && com.elmakers.mine.bukkit.wand.Wand.isBrush(item))
            {
                String brushKey = com.elmakers.mine.bukkit.wand.Wand.getBrush(item);
                wand.setActiveBrush(brushKey);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        Wand wand = mage.getActiveWand();
        this.context = context;
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (wand == null) {
            return SpellResult.FAIL;
        }
        List<String> brushKeys = new ArrayList(wand.getBrushes());
        Collections.sort(brushKeys);
        Collection<ItemStack> brushes = new ArrayList<ItemStack>();
        for (String brushKey : brushKeys) {
            if (MaterialBrush.isSchematic(brushKey)) {
                // TODO :: Grouping
                ItemStack brushItem = com.elmakers.mine.bukkit.wand.Wand.createBrushItem(brushKey, controller, null, false);
                if (brushItem != null) {
                    brushes.add(brushItem);
                }
                continue;
            }
            if (MaterialBrush.isSpecialMaterialKey(brushKey)) continue;
            ItemStack brushItem = com.elmakers.mine.bukkit.wand.Wand.createBrushItem(brushKey, controller, null, false);
            if (brushItem != null) {
                brushes.add(brushItem);
            }
        }

        String inventoryTitle = context.getMessage("title", "Brushes");
        int invSize = ((brushes.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack item : brushes)
        {
            displayInventory.addItem(item);
        }
        mage.activateGUI(this);
        mage.getPlayer().openInventory(displayInventory);

        return SpellResult.CAST;
	}
}
