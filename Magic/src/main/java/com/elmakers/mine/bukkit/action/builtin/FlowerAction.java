package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class FlowerAction extends BaseSpellAction {
    private final ArrayList<MaterialAndData> flowers = new ArrayList<>();
    private final ArrayList<MaterialAndData> tallFlowers = new ArrayList<>();
    private MaterialAndData requireBlock;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        flowers.clear();
        tallFlowers.clear();
        Collection<String> flowerKeys = parameters.getStringList("flowers");
        for (String flowerKey : flowerKeys) {
            flowerKey = CompatibilityUtils.migrateMaterial(flowerKey);
            MaterialAndData flower = new MaterialAndData(flowerKey);
            if (flower.isValid()) {
                flowers.add(flower);
            }
        }
        Collection<String> tallFlowerKeys = parameters.getStringList("tall_flowers");
        for (String flowerKey : tallFlowerKeys) {
            flowerKey = CompatibilityUtils.migrateMaterial(flowerKey);
            MaterialAndData flower = new MaterialAndData(flowerKey);
            if (flower.isValid()) {
                tallFlowers.add(flower);
            }
        }
    }

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        requireBlock = new MaterialAndData(parameters.getString("grow_on", "grass_block"));
        if (!requireBlock.isValid()) {
            requireBlock = new MaterialAndData(Material.GRASS);
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Block block = context.getTargetBlock();
        if (block == null)
        {
            return SpellResult.NO_TARGET;
        }
        if (!requireBlock.is(block))
        {
            return SpellResult.NO_TARGET;
        }
        block = block.getRelative(BlockFace.UP);
        if (!DefaultMaterials.isAir(block.getType())) {
            return SpellResult.NO_TARGET;
        }

        MaterialAndData material = null;
        Random random = context.getRandom();
        boolean tall = flowers.size() == 0 || (tallFlowers.size() > 0 && random.nextBoolean());
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

        context.registerForUndo(block);
        material.modify(block);

        if (tall) {
            block = block.getRelative(BlockFace.UP);
            context.registerForUndo(block);
            material.modify(block);
            CompatibilityUtils.setTopHalf(block);
        }
        return SpellResult.CAST;
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
    public boolean isUndoable() {
        return true;
    }

}
