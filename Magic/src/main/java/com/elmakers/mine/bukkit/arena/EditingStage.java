package com.elmakers.mine.bukkit.arena;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.entity.EntityData;

public interface EditingStage {

    default String getFullName() {
        return ChatColor.AQUA + getArena().getName() + ChatColor.DARK_AQUA + " " + getName();
    }

    Arena getArena();
    void setName(String name);
    String getName();
    void setRandomizeMobSpawn(Vector vector);
    void setWinXP(int xp);
    void setWinSP(int sp);
    void setWinMoney(int money);
    void setStartSpell(String startSpell);
    void setEndSpell(String endSpell);
    void addMobSpawn(Location location);
    Location removeMobSpawn(Location location);
    void addMob(EntityData entityType, int count);
    void removeMob(EntityData entityType);
    void describe(CommandSender sender);
    Collection<EntityData> getSpawns();
    void setDuration(int duration);
    void setRespawnDuration(int duration);
}
