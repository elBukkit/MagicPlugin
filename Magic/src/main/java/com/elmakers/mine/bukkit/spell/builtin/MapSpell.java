package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class MapSpell extends TargetingSpell
{
    @SuppressWarnings("deprecation")
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        World world = getWorld();
        MapView newMap = Bukkit.createMap(world);
        Location location = getLocation();
        newMap.setCenterX(location.getBlockX());
        newMap.setCenterZ(location.getBlockZ());

        MapView.Scale scale = newMap.getScale();
        String scaleType = parameters.getString("scale");
        if (scaleType != null) {
            try {
                scale = MapView.Scale.valueOf(scaleType.toUpperCase());
            } catch (Exception ignored) {

            }
        }

        newMap.setScale(scale);

        ItemStack newMapItem = controller.getMap(newMap.getId());
        world.dropItemNaturally(getLocation(), newMapItem);
        return SpellResult.CAST;
    }
}
