package com.elmakers.mine.bukkit.integration.mobarena;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.waves.MACreature;
import com.garbagemule.MobArena.waves.WaveUtils;

public class MagicCreature extends MACreature {
    private final EntityData entityData;
    private final MageController controller;

    public MagicCreature(MageController controller, String name, EntityData magicMob) {
        super(name, magicMob.getType());
        this.entityData = magicMob;
        this.controller = controller;
    }

    @Nullable
    @Override
    public LivingEntity spawn(Arena arena, World world, Location loc) {
        loc.setWorld(world);
        Entity entity = entityData.spawn(loc);
        if (!(entity instanceof LivingEntity)) return null;

        if (entity instanceof Creature) {
            Creature c = (Creature)entity;
            c.setTarget(WaveUtils.getClosestPlayer(arena, entity));
        }

        return (LivingEntity)entity;
    }
}
