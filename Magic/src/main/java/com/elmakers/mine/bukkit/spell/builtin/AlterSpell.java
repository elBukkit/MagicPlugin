package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class AlterSpell extends BlockSpell
{
    // TODO: Fix and future-proof all this mess!
    // Note: LOG_2 changed to only cycle from acacia to dark oak, there's a "hole" in the bit values
    // That will throw an error.
    // Changing this all to lists of materials and states would of course fix this problem.
    static final String DEFAULT_ADJUSTABLES = "3 ,5, 6, 8, 9, 10,11,12,17,162,18,161,23,24,27,28,29,31,33,35,37,38,43,44,52,53,54,55,58,59,60,61,62,63,65,66,67,68,69,77,78,81,83,85,86,93,94,95,97,98,99,100,104,105,108,109,114,115,125,126,128,134,135,136,140,141,142,144,155,156,159,160,171,172,175";
    static final String DEFAULT_ADJUST_MAX =  "2 ,5, 5 ,15,15,15,15,1 ,15,1 ,3 ,1  ,5 ,2 ,9 ,9 ,5 ,2 ,5 ,15,8 ,8 ,15,15,15,3 ,5 ,15,5 ,7 ,8 ,5 ,5 ,15,3 ,9 ,3 ,2 ,14,15,7 ,15,15,5 ,0 ,5 ,5 ,15, 5,3 ,15,15 ,7  ,7  ,3  ,3  ,3  ,7  ,15 ,15 ,3  ,3  ,3  ,3  ,15 ,7  ,7  ,4  ,4  ,3  ,15 ,15 ,15 ,15 ,6";
    static final String DEFAULT_ADJUST_MIN =  "0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0  ,0 ,0  ,2 ,0 ,0, 0, 0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,2 ,0 ,2 ,0 ,0 ,2 ,2 ,0 ,0 ,0 ,0 ,5 ,6 ,0 ,0 ,0 ,0 ,0 ,3 ,2 ,2 ,0 , 0,0 ,0 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0";
    static final int DEFAULT_RECURSE_DISTANCE = 0;

    public static List<Integer> parseIntegers(String csvList) {
        List<Integer> ints = new ArrayList<>();

        String[] intStrings = StringUtils.split(csvList, ',');
        for (String s : intStrings) {
            try {
                int thisInt = Integer.parseInt(s.trim());
                ints.add(thisInt);
            } catch (NumberFormatException ignored) {

            }
        }
        return ints;
    }


    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        if (target.hasEntity()) {
            SpellResult result = alterEntity(target.getEntity());
            if (result != SpellResult.NO_TARGET) {
                return result;
            }
        }
        Block targetBlock = target.getBlock();
        if (targetBlock == null)
        {
            return SpellResult.NO_TARGET;
        }

        int recurseDistance = parameters.getInt("depth", DEFAULT_RECURSE_DISTANCE);
        recurseDistance = (int)(mage.getRadiusMultiplier() * recurseDistance);

        List<Integer> adjustableMaterials = parseIntegers(DEFAULT_ADJUSTABLES);
        List<Integer> maxData = parseIntegers(DEFAULT_ADJUST_MAX);
        List<Integer> minData = parseIntegers(DEFAULT_ADJUST_MIN);

        if (adjustableMaterials.size() != maxData.size() || maxData.size() != minData.size())
        {
            controller.getLogger().warning("Spells:Alter: Mis-match in adjustable material lists!");
        }

        if (!adjustableMaterials.contains(targetBlock.getType().getId()))
        {
            return SpellResult.FAIL;
        }
        if (!hasBuildPermission(targetBlock)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (mage.isIndestructible(targetBlock)) {
            return SpellResult.NO_TARGET;
        }
        if (!isDestructible(targetBlock)) {
            return SpellResult.NO_TARGET;
        }

        int originalData = targetBlock.getData();

        int materialIndex = adjustableMaterials.indexOf(targetBlock.getType().getId());
        int minValue = minData.get(materialIndex);
        int maxValue = maxData.get(materialIndex);
        int dataSize = maxValue - minValue + 1;

        byte data = (byte)((((originalData - minValue) + 1) % dataSize) + minValue);

        boolean recursive = recurseDistance > 0;
        adjust(targetBlock, data, recursive, recurseDistance, 0);
        registerForUndo();
        return SpellResult.CAST;
    }

    protected void adjust(Block block, byte dataValue, boolean recursive, int recurseDistance, int rDepth)
    {
        registerForUndo(block);
        try {
            block.setData(dataValue);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Failed to alter block state: " + ex.getMessage());
        }

        if (recursive && rDepth < recurseDistance)
        {
            Material targetMaterial = block.getType();
            tryAdjust(block.getRelative(BlockFace.NORTH), dataValue,targetMaterial, recurseDistance, rDepth + 1);
            tryAdjust(block.getRelative(BlockFace.WEST), dataValue,targetMaterial, recurseDistance, rDepth + 1);
            tryAdjust(block.getRelative(BlockFace.SOUTH), dataValue,targetMaterial, recurseDistance, rDepth + 1);
            tryAdjust(block.getRelative(BlockFace.EAST), dataValue,targetMaterial, recurseDistance, rDepth + 1);
            tryAdjust(block.getRelative(BlockFace.UP), dataValue,targetMaterial, recurseDistance, rDepth + 1);
            tryAdjust(block.getRelative(BlockFace.DOWN), dataValue,targetMaterial, recurseDistance, rDepth + 1);
        }
    }

    protected SpellResult alterEntity(Entity entity) {
        EntityType entityType = entity.getType();
        switch (entityType) {
            case PAINTING:
                registerModified(entity);
                Painting painting = (Painting)entity;
                Art[] artValues = Art.values();
                Art oldArt = painting.getArt();
                Art newArt = oldArt;
                int ordinal = (oldArt.ordinal() + 1);
                for (int i = 0; i < artValues.length; i++) {
                    newArt = artValues[ordinal++ % artValues.length];
                    painting.setArt(newArt);
                    newArt = painting.getArt();
                    if (oldArt != newArt) {
                        break;
                    }
                }
                if (oldArt == newArt) {
                    return SpellResult.FAIL;
                }
                mage.sendDebugMessage("Altering art from " + oldArt + " to " + newArt);
                break;
            case ITEM_FRAME:
                ItemFrame itemFrame = (ItemFrame)entity;
                ItemStack frameItem = itemFrame.getItem();
                if (frameItem == null || !DefaultMaterials.isFilledMap(frameItem.getType())) {
                    return SpellResult.NO_TARGET;
                }
                int mapId = CompatibilityLib.getInventoryUtils().getMapId(frameItem);
                mapId++;
                MapView mapView = CompatibilityLib.getDeprecatedUtils().getMap(mapId);
                if (mapView == null) {
                    mapId = 0;
                    mapView = CompatibilityLib.getDeprecatedUtils().getMap(mapId);
                    if (mapView == null) {
                        return SpellResult.NO_TARGET;
                    }
                }
                registerModified(entity);
                CompatibilityLib.getInventoryUtils().setMapId(frameItem, mapId);
                itemFrame.setItem(frameItem);
                break;
            case HORSE:
                registerModified(entity);
                Horse horse = (Horse)entity;

                Color color = horse.getColor();
                Color[] colorValues = Color.values();
                color = colorValues[(color.ordinal() + 1) % colorValues.length];

                Style horseStyle = horse.getStyle();
                Style[] styleValues = Style.values();
                horseStyle = styleValues[(horseStyle.ordinal() + 1) % styleValues.length];

                horse.setStyle(horseStyle);
                horse.setColor(color);
                break;
            case OCELOT:
                registerModified(entity);
                Ocelot ocelot = (Ocelot)entity;
                Type catType = ocelot.getCatType();
                Type[] typeValues = Type.values();
                catType = typeValues[(catType.ordinal() + 1) % typeValues.length];
                ocelot.setCatType(catType);
                break;
            case VILLAGER:
                registerModified(entity);
                Villager villager = (Villager)entity;
                Profession profession = villager.getProfession();
                Profession[] professionValues = Profession.values();
                profession = professionValues[(profession.ordinal() + 1) % professionValues.length];
                villager.setProfession(profession);
                break;
            case WOLF:
                registerModified(entity);
                Wolf wolf = (Wolf)entity;
                DyeColor wolfColor = wolf.getCollarColor();
                DyeColor[] wolfColorValues = DyeColor.values();
                wolfColor = wolfColorValues[(wolfColor.ordinal() + 1) % wolfColorValues.length];
                wolf.setCollarColor(wolfColor);
                break;
            case SHEEP:
                registerModified(entity);
                Sheep sheep = (Sheep)entity;
                DyeColor dyeColor = sheep.getColor();
                DyeColor[] dyeColorValues = DyeColor.values();
                dyeColor = dyeColorValues[(dyeColor.ordinal() + 1) % dyeColorValues.length];
                sheep.setColor(dyeColor);
                break;
            default:
                return SpellResult.NO_TARGET;
        };
        registerForUndo();
        return SpellResult.CAST;
    }

    protected void tryAdjust(Block target, byte dataValue, Material targetMaterial, int recurseDistance, int rDepth)
    {
        if (target.getType() != targetMaterial || contains(target))
        {
            return;
        }

        adjust(target, dataValue, true, recurseDistance, rDepth);
    }
}
