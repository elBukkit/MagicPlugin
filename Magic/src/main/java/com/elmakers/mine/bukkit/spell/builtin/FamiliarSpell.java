package com.elmakers.mine.bukkit.spell.builtin;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class FamiliarSpell extends UndoableSpell implements Listener
{
    private final Deque<WeightedPair<String>> entityTypeProbability = new ArrayDeque<>();

    private final Random rand = new Random();
    private int spawnCount = 0;
    private CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.EGG;

    private List<LivingEntity> familiars = null;

    public boolean hasFamiliar()
    {
        return familiars != null;
    }

    public void setFamiliars(List<LivingEntity> f)
    {
        familiars = f;
    }

    public void releaseFamiliars()
    {
        if (familiars != null)
        {
            for (LivingEntity familiar : familiars)
            {
                familiar.remove();
            }
            familiars = null;
        }
    }

    public void releaseFamiliar(Entity entity)
    {
        if (familiars != null)
        {
            List<LivingEntity> iterate = new ArrayList<>(familiars);
            for (LivingEntity familiar : iterate)
            {
                if (Objects.equals(familiar.getUniqueId(), entity.getUniqueId())) {
                    familiar.remove();
                    familiars.remove(familiar);
                }
            }
            familiars = null;
        }
}

    public boolean isFamiliar(Entity e)
    {
        if (familiars == null) return false;

        for (LivingEntity c : familiars)
        {
            if (c.getEntityId() == e.getEntityId()) return true;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        spawnCount = 0;

        Target target = getTarget();
        if (!target.hasTarget())
        {
            return SpellResult.NO_TARGET;
        }
        Block originalTarget = target.getBlock();
        Block targetBlock = originalTarget;
        LivingEntity targetEntity = null;

        boolean track = parameters.getBoolean("track", true);
        boolean loot = parameters.getBoolean("loot", false);
        boolean setTarget = parameters.getBoolean("set_target", true);
        double spawnRange = parameters.getInt("spawn_range", 0);
        String entityName = parameters.getString("name", "");

        if (hasFamiliar() && track)
        {   // Dispel familiars if you target them and cast
            boolean isFamiliar = target.hasEntity() && isFamiliar(target.getEntity());
            if (isFamiliar)
            {
                checkListener();
                releaseFamiliar(target.getEntity());
                return SpellResult.DEACTIVATE;
            }

            releaseFamiliars();
        }

        if (target.hasEntity())
        {
            targetBlock = targetBlock.getRelative(BlockFace.SOUTH);
            Entity e = target.getEntity();
            if (e instanceof LivingEntity)
            {
                targetEntity = (LivingEntity)e;
            }
        }

        targetBlock = targetBlock.getRelative(BlockFace.UP);
        Location centerLoc = targetBlock.getLocation();

        Location caster = getLocation();
        if (spawnRange > 0) {
            double distanceSquared = targetBlock.getLocation().distanceSquared(caster);
            if (spawnRange * spawnRange < distanceSquared) {
                Vector direction = caster.getDirection().normalize().multiply(spawnRange);
                centerLoc = caster.clone().add(direction);
                for (int i = 0; i < spawnRange; i++) {
                    Material blockType = centerLoc.getBlock().getType();
                    if (blockType == Material.AIR || blockType == Material.WATER)
                    {
                        break;
                    }
                    centerLoc = centerLoc.add(0, 1, 0);
                }
            }
        }

        EntityType famType = null;
        int famCount = parameters.getInt("count", 1);

        String famTypeName = parameters.getString("type", null);
        if (famTypeName != null && !famTypeName.isEmpty()) {
            try {
                famType = EntityType.valueOf(famTypeName.toUpperCase());
            } catch (Throwable ex) {
                sendMessage("Unknown entity type: " + famTypeName);
                return SpellResult.FAIL;
            }
        }

        if (originalTarget.getType() == Material.WATER)
        {
            famType = EntityType.SQUID;
        }

        boolean spawnBaby = parameters.getBoolean("baby", false);

        List<LivingEntity> newFamiliars = new ArrayList<>();
        for (int i = 0; i < famCount; i++)
        {
            EntityType entityType = famType;
            if (entityType == null)
            {
                String randomType = RandomUtils.weightedRandom(entityTypeProbability);
                try {
                    entityType = EntityType.fromName(randomType);
                } catch (Throwable ex) {
                    sendMessage("Unknown entity type: " + randomType);
                    return SpellResult.FAIL;
                }
            }

            if (parameters.contains("reason")) {
                String reasonText = parameters.getString("reason").toUpperCase();
                try {
                    spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
                } catch (Exception ex) {
                    sendMessage("Unknown spawn reason: " + reasonText);
                    return SpellResult.FAIL;
                }
            }

            final Location targetLoc = centerLoc.clone();
            if (famCount > 1)
            {
                targetLoc.setX(targetLoc.getX() + rand.nextInt(2 * famCount) - famCount);
                targetLoc.setZ(targetLoc.getZ() + rand.nextInt(2 * famCount) - famCount);
            }

            targetLoc.setPitch(caster.getPitch());
            targetLoc.setYaw(caster.getYaw());
            if (entityType != null) {
                final LivingEntity entity = spawnFamiliar(targetLoc, entityType, targetBlock.getLocation(), targetEntity, setTarget);
                if (entity != null)
                {
                    if (entityName != null && !entityName.isEmpty())
                    {
                        entity.setCustomName(entityName);
                    }
                    if (!loot)
                    {
                        entity.setMetadata("nodrops", new FixedMetadataValue(mage.getController().getPlugin(), true));
                    }
                    if (spawnBaby && entity instanceof Ageable) {
                        Ageable ageable = (Ageable)entity;
                        ageable.setBaby();
                    }
                    entity.teleport(targetLoc);
                    newFamiliars.add(entity);
                    spawnCount++;
                    registerForUndo(entity);
                }
            }
        }

        registerForUndo();

        if (track) {
            setFamiliars(newFamiliars);
            checkListener();
        }
        return SpellResult.CAST;

    }

    @Nullable
    protected LivingEntity spawnFamiliar(Location target, EntityType famType, Location targetLocation, LivingEntity targetEntity, boolean setTarget) {
        LivingEntity familiar = null;
        try {
            World world = getWorld();
            Entity famEntity;
            try {
                Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, CreatureSpawnEvent.SpawnReason.class);
                famEntity = (Entity)spawnMethod.invoke(world, target, famType.getEntityClass(), spawnReason);
            } catch (Exception ex) {
                famEntity = getWorld().spawnEntity(target, famType);
            }

            if (famEntity == null || !(famEntity instanceof LivingEntity)) return null;

            familiar = (LivingEntity)famEntity;
            if (familiar instanceof Skeleton) {
                Skeleton skellie = (Skeleton)familiar;
                skellie.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
            }
            if (targetLocation != null && setTarget)
            {
                //CompatibilityUtils.setTarget(familiar, targetLocation);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return familiar;
    }

    protected void checkListener()
    {
        if (hasFamiliar())
        {
            mage.registerEvent(SpellEventType.PLAYER_QUIT, this);
        }
        else
        {
            mage.unregisterEvent(SpellEventType.PLAYER_QUIT, this);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerEvent event)
    {
        if (hasFamiliar())
        {
            releaseFamiliars();
            checkListener();
        }
    }

    @Override
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);
        return message.replace("$count", Integer.toString(spawnCount));
    }

    @Override
    protected void loadTemplate(ConfigurationSection template)
    {
        super.loadTemplate(template);

        if (template.contains("entity_types")) {
            RandomUtils.populateStringProbabilityMap(entityTypeProbability, template.getConfigurationSection("entity_types"), 0, 0, 0);
        } else {
            entityTypeProbability.add(new WeightedPair<>(100.0f, "pig"));
        }
    }
}
