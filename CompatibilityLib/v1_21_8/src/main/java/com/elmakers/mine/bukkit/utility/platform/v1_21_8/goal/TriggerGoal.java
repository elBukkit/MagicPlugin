package com.elmakers.mine.bukkit.utility.platform.v1_21_8.goal;

import java.util.Collection;

import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.api.magic.Mage;

import net.minecraft.world.entity.ai.goal.Goal;

public class TriggerGoal extends MagicGoal {
    private final Mage mage;
    private final String trigger;
    private final int interval;
    private long lastTrigger;

    public TriggerGoal(Mage mage, Collection<Goal> goals, boolean interruptable, String trigger, int interval) {
        super(goals, interruptable);
        this.mage = mage;
        this.interval = interval * 50;
        this.lastTrigger = System.currentTimeMillis();
        this.trigger = trigger;
    }

    @Override
    public void tick() {
        super.tick();
        long now = System.currentTimeMillis();
        if (now > lastTrigger + interval) {
            lastTrigger = now;
            mage.trigger(trigger);
        }
    }

    @Override
    protected String getDescription() {
        return "Trigger" + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + trigger + ChatColor.GRAY + ")";
    }
}
