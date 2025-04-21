package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class RectOfEffectAction extends CompoundEntityAction {
    private Vector min;
    private Vector max;
    protected int targetCount;
    protected boolean targetSource;
    protected boolean ignoreModified;
    protected boolean randomChoose;

    private Location point1;
    private Location point2;
    private Location point3;
    private Location point4;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context, context.getTargetEntity(), context.getTargetLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        min = ConfigurationUtils.getVector(parameters, "min");
        max = ConfigurationUtils.getVector(parameters, "max");

        targetCount = parameters.getInt("target_count", -1);
        targetSource = parameters.getBoolean("target_source", true);
        ignoreModified = parameters.getBoolean("ignore_modified", false);
        randomChoose = parameters.getBoolean("random_choose", false);

        super.prepare(context, parameters);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities) {
        // Register Modified entities as ones that should be ignored
        Set<UUID> ignore = null;
        UndoList undoList = context.getUndoList();
        if (ignoreModified && undoList != null) {
            ignore = new HashSet<>();
            for (Entity entity : undoList.getAllEntities()) {
                ignore.add(entity.getUniqueId());
            }
        }

        // Estimate and register the workload
        double deltaX = max.getX() - min.getX();
        double deltaY = max.getY() - min.getY();
        double deltaZ = max.getZ() - min.getZ();
        double volume = deltaX * deltaY * deltaZ;
        context.addWork((int)Math.ceil(volume / 10.0));

        // Log debug
        Mage mage = context.getMage();
        Location sourceLocation = context.getTargetLocation();
        if (mage.getDebugLevel() > 8)
        {
            mage.sendDebugMessage(ChatColor.GREEN + "Rectangle Of Effect Targeting from " + ChatColor.GRAY + sourceLocation.getBlockX()
                    + ChatColor.DARK_GRAY + ","  + ChatColor.GRAY + sourceLocation.getBlockY()
                    + ChatColor.DARK_GRAY + "," + ChatColor.GRAY + sourceLocation.getBlockZ()
                    + ChatColor.DARK_GREEN + " with a volume of " + ChatColor.GREEN + volume + " blocks,"
                    + ChatColor.GRAY + " self? " + ChatColor.DARK_GRAY + context.getTargetsCaster(), 14
            );
        }

        // Calculate the 4 points of the rectangular space.
        double yaw = sourceLocation.getYaw();
        double yawRadians = Math.toRadians(yaw);

        point1 = sourceLocation.clone().add(Math.cos(yawRadians) * -min.getX(), min.getY(), Math.sin(yawRadians) * -min.getX());
        point2 = sourceLocation.clone().add(Math.cos(yawRadians) * -max.getX(), min.getY(), Math.sin(yawRadians) * -max.getX());

        double cos90 = -Math.cos(Math.toRadians(yaw - 90));
        double sin90 = -Math.sin(Math.toRadians(yaw - 90));

        point3 = point1.clone().add(cos90 * max.getZ(), 0, sin90 * max.getZ());
        point4 = point2.clone().add(cos90 * max.getZ(), 0, sin90 * max.getZ());

        point1 = point1.add(cos90 * min.getZ(), 0, sin90 * min.getZ());
        point2 = point2.add(cos90 * min.getZ(), 0, sin90 * min.getZ());

        // Draw the points out in flame particles if were debugging.
        if (mage.getDebugLevel() >= 10) {
            drawLine(context.getWorld(), point1.toVector(), point2.toVector(), 0.2);
            drawLine(context.getWorld(), point3.toVector(), point4.toVector(), 0.2);
            drawLine(context.getWorld(), point1.toVector(), point3.toVector(), 0.2);
            drawLine(context.getWorld(), point2.toVector(), point4.toVector(), 0.2);
        }

        // Get all entities within reasonable range
        double inclusionRadius = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        Collection<Entity> candidates = CompatibilityLib.getCompatibilityUtils().getNearbyEntities(sourceLocation, inclusionRadius, deltaY, inclusionRadius);

        Entity targetEntity = context.getTargetEntity();
        if (targetCount > 0) {
            if (randomChoose) {
                List<Entity> candidatesList = new ArrayList<>();
                for (Entity entity : candidates) {
                    boolean canTarget = entity != targetEntity || targetSource;

                    if (!canTarget || !isTargetWithinRect(sourceLocation, min, max, entity))
                    {
                        continue;
                    }
                    if (ignore != null && ignore.contains(entity.getUniqueId())) {
                        mage.sendDebugMessage(ChatColor.DARK_RED + "Ignoring Modified Target " + ChatColor.GREEN + entity.getType(), 16);
                        continue;
                    }
                    if (canTarget && context.canTarget(entity)) {
                        candidatesList.add(entity);
                        mage.sendDebugMessage(ChatColor.DARK_GREEN + "Target " + ChatColor.GREEN + entity.getType(), 12);
                    } else if (mage.getDebugLevel() > 7) {
                        mage.sendDebugMessage(ChatColor.DARK_RED + "Skipped Target " + ChatColor.GREEN + entity.getType(), 16);
                    }
                }
                Collections.shuffle(candidatesList);
                for (int i = 0; i < targetCount && i < candidatesList.size(); i++) {
                    entities.add(new WeakReference<>(candidatesList.get(i)));
                }
                return;
            }

            List<Target> targets = new ArrayList<>();
            for (Entity entity : candidates) {
                boolean canTarget = true;
                if (entity == targetEntity && !targetSource) continue;

                if (!canTarget || !isTargetWithinRect(sourceLocation, min, max, entity)) {
                    continue;
                }
                if (ignore != null && ignore.contains(entity.getUniqueId())) {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Ignoring Modified Target " + ChatColor.GREEN + entity.getType(), 16);
                    continue;
                }

                if (canTarget && context.canTarget(entity)) {
                    double range = sourceLocation.distance(entity.getLocation());
                    Target target = new Target(sourceLocation, entity, (int)range, 0);
                    targets.add(target);
                    mage.sendDebugMessage(ChatColor.DARK_GREEN + "Target " + ChatColor.GREEN + entity.getType() + ChatColor.DARK_GREEN + ": " + ChatColor.YELLOW + target.getScore(), 12);
                }
                else if (mage.getDebugLevel() > 7) {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Skipped Target " + ChatColor.GREEN + entity.getType(), 16);
                }
            }

            Collections.sort(targets);
            for (int i = 0; i < targetCount && i < targets.size(); i++) {
                Target target = targets.get(i);
                entities.add(new WeakReference<>(target.getEntity()));
            }
        }
        else
        {
            for (Entity entity : candidates) {
                boolean canTarget = true;
                if (entity == targetEntity && !targetSource) continue;

                if (!canTarget || !isTargetWithinRect(sourceLocation, min, max, entity)) {
                    continue;
                }
                if (ignore != null && ignore.contains(entity.getUniqueId())) {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Ignoring Modified Target " + ChatColor.GREEN + entity.getType(), 16);
                    continue;
                }
                if (canTarget && context.canTarget(entity)) {
                    entities.add(new WeakReference<>(entity));
                    mage.sendDebugMessage(ChatColor.DARK_GREEN + "Target " + ChatColor.GREEN + entity.getType(), 12);
                } else if (mage.getDebugLevel() > 7) {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Skipped Target " + ChatColor.GREEN + entity.getType(), 16);
                }
            }
        }
    }

    public boolean isTargetWithinRect(Location sourceLocation, Vector min, Vector max, Entity candidate) {
        Vector location = candidate.getLocation().toVector();

        // Check Y range first.
        if (location.getY() < sourceLocation.getY() + min.getY() || location.getY() > sourceLocation.getY() + max.getY()) {
            return false;
        }

        // If the entity's location is on the left hand side of each of these lines (calculated counter-clockwise),
        // it means the entity is within the rectangle. If one of these fails, they are outside it.
        if (!testPointAgainstLine(location, point3.toVector(), point1.toVector())) {
            return false;
        }
        if (!testPointAgainstLine(location, point1.toVector(), point2.toVector())) {
            return false;
        }
        if (!testPointAgainstLine(location, point2.toVector(), point4.toVector())) {
            return false;
        }
        if (!testPointAgainstLine(location, point4.toVector(), point3.toVector())) {
            return false;
        }

        return true;
    }

    public boolean testPointAgainstLine(Vector point, Vector lineStart, Vector lineEnd) {
        // Check whether the target location is on the Left hand side of a given line.
        double contained = (lineEnd.getX() - lineStart.getX()) * (point.getZ() - lineStart.getZ()) - (point.getX() - lineStart.getX()) * (lineEnd.getZ() - lineStart.getZ());
        return contained < 0;
    }

    public void drawLine(World world, Vector p1, Vector p2, double space) {
        double distance = p1.distance(p2);
        Vector vector = p2.clone().subtract(p1).normalize();
        for (double length = 0; length < distance; p1.add(vector.clone().multiply(space))) {
            world.spawnParticle(Particle.FLAME, p1.getX(), p1.getY(), p1.getZ(), 1, 0, 0, 0, 0);
            length += space;
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("target_count");
        parameters.add("target_source");
        parameters.add("random_choose");
        parameters.add("min");
        parameters.add("max");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_count") || parameterKey.equals("min") || parameterKey.equals("max")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
