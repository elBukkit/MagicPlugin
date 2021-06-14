package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;

public class CycleEntityAction extends BaseSpellAction {

    @Override
    public SpellResult perform(CastContext context) {
        Entity entity = context.getTargetEntity();
        EntityType entityType = entity.getType();
        Mage mage = context.getMage();
        switch (entityType) {
            case PAINTING:
                context.registerModified(entity);
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
                int mapId = InventoryUtils.getMapId(frameItem);
                mapId++;
                MapView mapView = DeprecatedUtils.getMap(mapId);
                if (mapView == null) {
                    mapId = 0;
                    mapView = DeprecatedUtils.getMap(mapId);
                    if (mapView == null) {
                        return SpellResult.NO_TARGET;
                    }
                }
                context.registerModified(entity);
                InventoryUtils.setMapId(frameItem, mapId);
                itemFrame.setItem(frameItem);
                break;
            case HORSE:
                context.registerModified(entity);
                Horse horse = (Horse)entity;

                Horse.Color color = horse.getColor();
                Horse.Color[] colorValues = Horse.Color.values();
                color = colorValues[(color.ordinal() + 1) % colorValues.length];

                Horse.Style horseStyle = horse.getStyle();
                Horse.Style[] styleValues = Horse.Style.values();
                horseStyle = styleValues[(horseStyle.ordinal() + 1) % styleValues.length];

                horse.setStyle(horseStyle);
                horse.setColor(color);
                break;
            case OCELOT:
                context.registerModified(entity);
                Ocelot ocelot = (Ocelot)entity;
                Ocelot.Type catType = ocelot.getCatType();
                Ocelot.Type[] typeValues = Ocelot.Type.values();
                catType = typeValues[(catType.ordinal() + 1) % typeValues.length];
                ocelot.setCatType(catType);
                break;
            case VILLAGER:
                context.registerModified(entity);
                Villager villager = (Villager)entity;
                Villager.Profession profession = villager.getProfession();
                Villager.Profession[] professionValues = Villager.Profession.values();
                int villagerOrdinal = (profession.ordinal() + 1) % professionValues.length;
                // Special-cases for zombie-reserved professions
                if (villagerOrdinal == 0 || villagerOrdinal == 7) {
                    villagerOrdinal = 1;
                }
                profession = professionValues[villagerOrdinal];
                villager.setProfession(profession);
                break;
            case WOLF:
                context.registerModified(entity);
                Wolf wolf = (Wolf)entity;
                DyeColor wolfColor = wolf.getCollarColor();
                DyeColor[] wolfColorValues = DyeColor.values();
                wolfColor = wolfColorValues[(wolfColor.ordinal() + 1) % wolfColorValues.length];
                wolf.setCollarColor(wolfColor);
                break;
            case SHEEP:
                context.registerModified(entity);
                Sheep sheep = (Sheep)entity;
                DyeColor dyeColor = sheep.getColor();
                DyeColor[] dyeColorValues = DyeColor.values();
                dyeColor = dyeColorValues[(dyeColor.ordinal() + 1) % dyeColorValues.length];
                sheep.setColor(dyeColor);
                break;
            default:
                return SpellResult.NO_TARGET;
        };

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}
