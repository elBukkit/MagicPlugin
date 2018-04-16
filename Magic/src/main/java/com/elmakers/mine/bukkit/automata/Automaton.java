package com.elmakers.mine.bukkit.automata;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class Automaton {
    @Nonnull
    private final MagicController controller;
    @Nullable
    private AutomatonTemplate template;
    @Nullable
    private ConfigurationSection parameters;
    private String templateKey;
    @Nonnull
    private final Location location;
    private long createdAt;

    private long nextTick;
    private List<WeakReference<Entity>> spawned;

    public Automaton(@Nonnull MagicController controller, @Nonnull ConfigurationSection node) {
        this.controller = controller;
        templateKey = node.getString("template");
        parameters = ConfigurationUtils.getConfigurationSection(node, "parameters");
        if (templateKey != null) {
            setTemplate(controller.getAutomatonTemplate(templateKey));
        }
        if (template == null) {
            controller.getLogger().warning("Automaton missing template: " + templateKey);
        }
        createdAt = node.getLong("created", 0);

        int x = node.getInt("x");
        int y = node.getInt("y");
        int z = node.getInt("z");
        String worldName = node.getString("world");
        if (worldName == null || worldName.isEmpty()) {
            worldName = "world";
            controller.getLogger().warning("Automaton missing world name, defaulting to 'world'");
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            controller.getLogger().warning("Automaton has unknown world: " + worldName + ", will be removed!");
        }
        location = new Location(world, x, y, z);
    }

    public Automaton(@Nonnull MagicController controller, @Nonnull Block block, @Nonnull String templateKey) {
        this.controller = controller;
        this.templateKey = templateKey;
        setTemplate(controller.getAutomatonTemplate(templateKey));
        createdAt = System.currentTimeMillis();
        location = block.getLocation();
    }

    private void setTemplate(AutomatonTemplate template) {
        this.template = template;
        if (template != null) {
            if (parameters != null) {
                this.template = template.getVariant(parameters);
            }
            nextTick = System.currentTimeMillis() + template.getInterval();
        }
    }

    public void reload() {
        if (template != null) {
            setTemplate(controller.getAutomatonTemplate(template.getKey()));
        }
    }

    public void save(ConfigurationSection node) {
        node.set("created", createdAt);
        node.set("template", templateKey);
        World world = location.getWorld();
        node.set("world", location.getWorld().getName());
        node.set("x", location.getBlockX());
        node.set("y", location.getBlockY());
        node.set("z", location.getBlockZ());
    }

    public long getCreatedTime() {
        return createdAt;
    }

    public void pause() {
        if (spawned != null) {
            for (WeakReference<Entity> mobReference : spawned) {
                Entity mob = mobReference.get();
                if (mob != null && mob.isValid()) {
                    mob.remove();
                }
            }
        }
    }

    public void resume() {

    }

    public Location getLocation() {
        return location;
    }

    public void tick() {
        if (template == null) return;

        long now = System.currentTimeMillis();
        if (now < nextTick) return;

        Entity entity = template.spawn(getLocation());
        if (entity != null) {
            if (spawned == null) {
                spawned = new ArrayList<>();
            }
            spawned.add(new WeakReference<>(entity));
        }
        if (spawned != null) {
            Iterator<WeakReference<Entity>> iterator = spawned.iterator();
            while (iterator.hasNext()) {
                WeakReference<Entity> mobReference = iterator.next();
                Entity mob = mobReference.get();
                if (mob == null || !mob.isValid()) {
                    iterator.remove();
                }
            }
        }

        nextTick = now + template.getInterval();
    }

    public long getId() {
        return BlockData.getBlockId(getLocation());
    }

    public boolean isValid() {
        return location.getWorld() != null;
    }
}