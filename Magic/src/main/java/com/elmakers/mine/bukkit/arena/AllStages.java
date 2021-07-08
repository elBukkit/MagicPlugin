package com.elmakers.mine.bukkit.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.entity.EntityData;

public class AllStages implements EditingStage {
    private final Arena arena;

    public AllStages(Arena arena) {
        this.arena = arena;
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    @Override
    public void setName(String name) {
        // No!
    }

    @Override
    public String getName() {
        return "(All Stages)";
    }

    @Override
    public void setRandomizeMobSpawn(Vector vector) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setRandomizeMobSpawn(vector);
        }
    }

    @Override
    public void setWinXP(int xp) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setWinXP(xp);
        }
    }

    @Override
    public void setWinSP(int sp) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setWinSP(sp);
        }
    }

    @Override
    public void setWinMoney(int money) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setWinMoney(money);
        }
    }

    @Override
    public void setStartSpell(String startSpell) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setStartSpell(startSpell);
        }
    }

    @Override
    public void setEndSpell(String endSpell) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setEndSpell(endSpell);
        }
    }

    @Override
    public void addMobSpawn(Location location) {
        for (ArenaStage stage : arena.getStages()) {
            stage.addMobSpawn(location);
        }
    }

    @Override
    public Location removeMobSpawn(Location location) {
        Location removed = null;
        for (ArenaStage stage : arena.getStages()) {
            Location stageRemoved = stage.removeMobSpawn(location);
            if (stageRemoved != null) {
                removed = stageRemoved;
            }
        }
        return removed;
    }

    @Override
    public void addMob(EntityData entityType, int count) {
        for (ArenaStage stage : arena.getStages()) {
            stage.addMob(entityType, count);
        }
    }

    @Override
    public void removeMob(EntityData entityType) {
        for (ArenaStage stage : arena.getStages()) {
            stage.removeMob(entityType);
        }
    }

    @Override
    public void describe(CommandSender sender) {
        for (ArenaStage stage : arena.getStages()) {
            stage.describe(sender);
        }
    }

    @Override
    public Collection<EntityData> getSpawns() {
        List<EntityData> spawns = new ArrayList<>();
        for (ArenaStage stage : arena.getStages()) {
            spawns.addAll(stage.getSpawns());
        }
        return spawns;
    }

    @Override
    public void setDuration(int duration) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setDuration(duration);
        }
    }

    @Override
    public void setRespawnDuration(int duration) {
        for (ArenaStage stage : arena.getStages()) {
            stage.setRespawnDuration(duration);
        }
    }
}
