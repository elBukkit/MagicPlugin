package com.elmakers.mine.bukkit.utility.platform.v1_21_7.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.utility.StringUtils;

import net.minecraft.world.entity.ai.goal.Goal;

public class MagicGoal extends Goal {
    private final Collection<Goal> goals;
    private final boolean interruptable;
    private Goal currentGoal;

    public MagicGoal(Collection<Goal> goals, boolean interruptable) {
        this(goals, interruptable, null);
    }

    public MagicGoal(Collection<Goal> goals, boolean interruptable, List<String> flagKeys) {
        this.goals = goals;
        this.interruptable = interruptable;

        // You might think, "gee, why not just use the flags of the sub-goals?"
        // Indeed!
        // But unfortunately, as of 1.17.1 the Goal.getFlags method does not properly
        // remapped and so will cause a runtime error.
        // So we'll assume move/look for now
        // TODO: Add parameters to all custom goals to control flags, maybe?
        EnumSet<Flag> flags;
        if (flagKeys == null) {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK);
        } else {
            flags = EnumSet.noneOf(Flag.class);
            for (String flagKey : flagKeys) {
                try {
                    Flag flag = Flag.valueOf(flagKey.toUpperCase());
                    flags.add(flag);
                } catch (Exception ignore) {
                }
            }
        }
        this.setFlags(flags);
    }

    @Override
    public boolean canUse() {
        // Note that goals will perform important logic in their canUse and other
        // methods that look like they would just be queries
        // So we need to call them even if we don't care about the result.
        for (Goal goal : goals) {
            if (goal.canUse() && currentGoal == null) {
                currentGoal = goal;
            }
        }
        return currentGoal != null;
    }

    @Override
    public boolean canContinueToUse() {
        boolean interrupt = false;
        boolean continuing = false;
        for (Goal goal : goals) {
            if (goal == currentGoal) {
                boolean canContinue = goal.canContinueToUse();
                if (canContinue) {
                    continuing = true;
                } else {
                    interrupt = true;
                }
            } else {
                // A higher-priority goal can interrupt
                boolean canUse = goal.canUse();
                if (canUse && !continuing && (currentGoal == null || currentGoal.isInterruptable())) {
                    interrupt = true;
                }
            }
            if (interrupt) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isInterruptable() {
        for (Goal goal : goals) {
            if (!goal.isInterruptable() && goal == currentGoal) {
                return false;
            }
        }
        return interruptable;
    }

    @Override
    public void start() {
        if (currentGoal != null) {
            currentGoal.start();
        }
    }

    @Override
    public void stop() {
        if (currentGoal != null) {
            currentGoal.stop();
            currentGoal = null;
        }
    }

    @Override
    public void tick() {
        if (currentGoal != null) {
            currentGoal.tick();
        }
    }

    protected String getSubDescription() {
        List<String> goalDescriptions = new ArrayList<>();
        for (Goal goal : goals) {
            String goalDescription = goal.toString();
            if (goal == currentGoal) {
                goalDescription = ChatColor.AQUA + goalDescription;
            } else {
                goalDescription = ChatColor.DARK_AQUA + goalDescription;
            }
            goalDescriptions.add(goalDescription);
        }
        return ChatColor.DARK_GRAY + " [" + StringUtils.join(goalDescriptions, " ") + ChatColor.DARK_GRAY + "]";
    }

    protected String getDescription() {
        return "Group";
    }

    @Override
    public String toString() {
        return getDescription() + getSubDescription();
    }
}
