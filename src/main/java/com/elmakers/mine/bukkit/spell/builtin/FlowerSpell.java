package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.block.batch.SimpleBlockAction;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

public class FlowerSpell extends BlockSpell
{
	private final static int DEFAULT_RADIUS	= 4;
    private final ArrayList<MaterialAndData> flowers = new ArrayList<MaterialAndData>();
    private final ArrayList<MaterialAndData> tallFlowers = new ArrayList<MaterialAndData>();
    
	public class FlowerAction extends SimpleBlockAction
	{
        private final ArrayList<MaterialAndData> flowers;
        private final ArrayList<MaterialAndData> tallFlowers;
        private final Random random;
        private final Location effectsLocation;
        private final double effectsSpeed;

		public FlowerAction(MageController controller, double itemSpeed, Location effectsLocation, UndoList undoList, ArrayList<MaterialAndData> flowers, ArrayList<MaterialAndData> tallFlowers)
		{
			super(controller, undoList);

            this.flowers = flowers;
            this.tallFlowers = tallFlowers;
            this.effectsLocation = effectsLocation;
            this.effectsSpeed = itemSpeed;
            random = new Random();
		}
		
		public SpellResult perform(Block block)
		{
			if (block.getType() != Material.GRASS)
			{
				return SpellResult.NO_TARGET;
			}
            block = block.getRelative(BlockFace.UP);
            if (block.getType() != Material.AIR) {
                return SpellResult.NO_TARGET;
            }

			MaterialAndData material = null;
            boolean tall = tallFlowers.size() > 0 && random.nextBoolean();
            if (tall) {
                material = tallFlowers.get(random.nextInt(tallFlowers.size()));
            } else {
                if (flowers.size() > 0) {
                    material = flowers.get(random.nextInt(flowers.size()));
                }
            }

            if (material == null) {
                return SpellResult.FAIL;
            }

			super.perform(block);
            material.modify(block);

            if (tall) {
                block = block.getRelative(BlockFace.UP);
                super.perform(block);
                material.setData((byte)8);
                material.modify(block);
            }

            if (effectsSpeed > 0) {
                Vector velocity = block.getLocation().toVector().subtract(effectsLocation.toVector()).normalize().multiply(effectsSpeed);
                ItemStack itemStack = new ItemStack(material.getMaterial(), 1, material.getData());
                NMSUtils.makeTemporary(itemStack, getMessage("removed").replace("$material", material.getName()));
                Item droppedItem = block.getWorld().dropItem(effectsLocation, itemStack);
                droppedItem.setMetadata("temporary", new FixedMetadataValue(controller.getPlugin(), true));
                CompatibilityUtils.ageItem(droppedItem, 4000);
                droppedItem.setVelocity(velocity);
            }
			return SpellResult.CAST;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		if (target == null || !target.isValid()) 
		{
			return SpellResult.NO_TARGET;
		}

		Block targetBlock = target.getBlock();
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = (int)(mage.getRadiusMultiplier() * radius);
        double itemSpeed = parameters.getDouble("item_speed");
        Block faceBlock = getInteractBlock();
        Location effectLocation = faceBlock == null ? mage.getEyeLocation() : faceBlock.getLocation();

        FlowerAction action = new FlowerAction(controller, itemSpeed, effectLocation, getUndoList(), flowers, tallFlowers);

		if (radius <= 1)
		{
			action.perform(targetBlock);
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}

		registerForUndo();
		return SpellResult.CAST;
	}

    @Override
    public void loadTemplate(ConfigurationSection template)
    {
        super.loadTemplate(template);
        flowers.clear();
        tallFlowers.clear();
        Collection<String> flowerKeys = template.getStringList("flowers");
        for (String flowerKey : flowerKeys) {
            flowers.add(new MaterialAndData(flowerKey));
        }
        Collection<String> tallFlowerKeys = template.getStringList("tall_flowers");
        for (String flowerKey : tallFlowerKeys) {
            tallFlowers.add(new MaterialAndData(flowerKey));
        }
    }

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) + (z * z) - (R * R);
	}
}
