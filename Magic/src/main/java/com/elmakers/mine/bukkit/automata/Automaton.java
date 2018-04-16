package com.elmakers.mine.bukkit.automata;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.magic.MagicController;

public class Automaton extends BlockData {
    private boolean valid = false;
    @Nonnull
    private MagicController controller;
    @Nullable
    private AutomatonTemplate template;
    private long createdAt;

    private long nextTick;
    private List<WeakReference<Entity>> spawned;

    public Automaton(@Nonnull MagicController controller, @Nonnull ConfigurationSection node) {
        super(node);
        this.controller = controller;
        String templateKey = node.getString("template");
        if (templateKey != null) {
            setTemplate(controller.getAutomatonTemplate(templateKey));
            valid = true;
        }
        createdAt = node.getLong("created", 0);
    }

    public Automaton(@Nonnull Block block, @Nonnull String templateKey) {
        super(block);

        setTemplate(controller.getAutomatonTemplate(templateKey));
        valid = true;
        createdAt = System.currentTimeMillis();
    }

    private void setTemplate(AutomatonTemplate template) {
        this.template = template;
        if (template != null) {
            nextTick = System.currentTimeMillis() + template.getInterval();
        }
    }

    @Override
    public void save(ConfigurationSection node) {
        super.save(node);
        node.set("created", createdAt);
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

    public void tick() {
        if (template == null) return;

        long now = System.currentTimeMillis();
        if (now < nextTick) return;

        Entity entity = template.spawn(controller, getBlock().getLocation());
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

    public boolean isValid() {
        return valid;
    }
}