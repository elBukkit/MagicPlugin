package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.entity.SpawnedEntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public class EntityPaintingData extends EntityExtraData {
    protected Art art;
    protected BlockFace facing;

    public EntityPaintingData(Art art, BlockFace facing) {
        this.art = art;
        this.facing = facing;
    }

    public EntityPaintingData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String artString = parameters.getString("art");
        if (artString != null && !artString.isEmpty()) {
            try {
                art = Art.valueOf(artString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid art: " + artString, ex);
            }
        }
        String facingString = parameters.getString("facing");
        if (facingString != null && !facingString.isEmpty()) {
            try {
                facing = BlockFace.valueOf(facingString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid facing: " + facingString, ex);
            }
        }
    }

    public EntityPaintingData(Entity entity) {
        if (entity instanceof Painting) {
            Painting painting = (Painting)entity;
            art = painting.getArt();
            facing = painting.getFacing();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Painting) {
            Painting painting = (Painting)entity;
            if (art != null) {
                painting.setArt(art);
            }
            if (facing != null) {
                painting.setFacingDirection(facing, true);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnedEntityExtraData spawn(Location location) {
        Entity newEntity = PlatformInterpreter.getPlatform().getCompatibilityUtils().createPainting(location, facing, art);
        return new SpawnedEntityExtraData(newEntity, false);
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!(entity instanceof Painting)) {
            return false;
        }
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
        return oldArt != newArt;
    }

    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Painting;
    }
}
