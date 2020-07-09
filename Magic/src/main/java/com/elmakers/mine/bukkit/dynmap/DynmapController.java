package com.elmakers.mine.bukkit.dynmap;

import java.io.InvalidClassException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class DynmapController {
     private final Plugin plugin;
     private DynmapCommonAPI dynmap = null;
     private DateFormat dateFormatter = new SimpleDateFormat("yy-MM-dd HH:mm");

     public DynmapController(Plugin plugin, Plugin dynmapPlugin) throws InvalidClassException {
         this.plugin = plugin;
         if (dynmapPlugin != null && !(dynmapPlugin instanceof DynmapCommonAPI)) {
            throw new InvalidClassException("Dynmap plugin found, but class is not DynmapCommonAPI");
        }
        dynmap = (DynmapCommonAPI)dynmapPlugin;
     }

     public void showCastMarker(Mage mage, Spell spell, SpellResult result) {
        if (isReady()) {
            MarkerAPI markers = dynmap.getMarkerAPI();
            MarkerSet spellSet = markers.getMarkerSet("Spells");
            if (spellSet == null) {
                spellSet = markers.createMarkerSet("Spells", "Spell Casts", null, false);
            }
            final String markerId = "Spell-" + mage.getId();
            final String targetId = "SpellTarget-" + mage.getId();

            int range = 32;
            double radius = 3.0 * mage.getPower() + 3;
            int width = (int)(2.0 * mage.getPower()) + 2;
            radius = Math.min(64, radius);
            width = Math.min(8, width);
            final Location location = spell.getLocation();
            if (location == null) return;
            Color mageColor = mage.getEffectColor();
            mageColor = mageColor == null ? Color.PURPLE : mageColor;
            Color spellColor = spell.getColor();
            spellColor = spellColor == null ? mageColor : spellColor;
            final String worldName = location.getWorld().getName();
            Date now = new Date();
            String label = spell.getName() + " : " + mage.getName() + " @ " + dateFormatter.format(now);

            // Create a circular disc for a spell cast
            CircleMarker marker = spellSet.findCircleMarker(markerId);
            if (marker != null) {
                marker.setCenter(worldName, location.getX(), location.getY(), location.getZ());
                marker.setLabel(label);
            } else {
                marker = spellSet.createCircleMarker(markerId, label, false, worldName, location.getX(), location.getY(), location.getZ(), radius, radius, false);
            }
            marker.setRadius(radius, radius);
            marker.setLineStyle(1, 0.9, spellColor.asRGB());
            marker.setFillStyle(0.5, mageColor.asRGB());

            // Create a targeting indicator line
            Location target = spell.getTargetLocation();

            if (target == null) {
                target = location.clone();
                Vector direction = location.getDirection();
                direction.normalize().multiply(range);
                target.add(direction);
            }

            PolyLineMarker targetMarker = spellSet.findPolyLineMarker(targetId);
            if (targetMarker != null) {
                targetMarker.setCornerLocation(0, location.getX(), location.getY(), location.getZ());
                targetMarker.setCornerLocation(1, target.getX(), target.getY(), target.getZ());
                targetMarker.setLabel(label);
            } else {
                double[] x = {location.getX(), target.getX()};
                double[] y = {location.getY(), target.getY()};
                double[] z = {location.getZ(), target.getZ()};

                targetMarker = spellSet.createPolyLineMarker(targetId, label, false, worldName, x, y, z, false);
            }
            targetMarker.setLineStyle(width, 0.8, spellColor.asRGB());
        }
     }

    public boolean isReady() {
         if (dynmap == null) return false;
         try {
            return dynmap.markerAPIInitialized();
         } catch (Throwable ex) {
             dynmap = null;
             plugin.getLogger().log(Level.WARNING, "Error integrating with Dynmap- is it up to date? Disabling integration until restart.", ex);
         }
         return false;
    }

     public boolean addMarker(String id, String group, String title, String world, int x, int y, int z, String description) {
        boolean created = false;
        if (isReady())
        {
            MarkerAPI markers = dynmap.getMarkerAPI();
            MarkerSet markerSet = markers.getMarkerSet(group);
            if (markerSet == null) {
                markerSet = markers.createMarkerSet(group, group, null, false);
            }
            MarkerIcon wandIcon = markers.getMarkerIcon("wand");
            if (wandIcon == null) {
                wandIcon = markers.createMarkerIcon("wand", "Wand", plugin.getResource("wand_icon32.png"));
            }

            Marker marker = markerSet.findMarker(id);
            if (marker == null) {
                created = true;
                marker = markerSet.createMarker(id, title, world, x, y, z, wandIcon, false);
            } else {
                marker.setLocation(world, x, y, z);
                marker.setLabel(title);
            }
            if (description != null) {
                marker.setDescription(description);
            }
        }

        return created;
     }

    public boolean removeMarker(String id, String group)
    {
        boolean removed = false;
        if (isReady())
        {
            MarkerAPI markers = dynmap.getMarkerAPI();
            MarkerSet markerSet = markers.getMarkerSet(group);
            if (markerSet != null) {
                Marker marker = markerSet.findMarker(id);
                if (marker != null) {
                    removed = true;
                    marker.deleteMarker();
                }
            }
        }

        return removed;
    }

    public int triggerRenderOfVolume(String wid, int minx, int miny, int minz, int maxx, int maxy, int maxz)
    {
        if (!isReady()) return 0;
        return dynmap.triggerRenderOfVolume(wid, minx, miny, minz, maxx, maxy, maxz);
    }

    public int triggerRenderOfBlock(String wid, int x, int y, int z)
    {
        if (!isReady()) return 0;
        return dynmap.triggerRenderOfBlock(wid, x, y, z);
    }
}
