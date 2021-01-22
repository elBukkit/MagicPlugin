package com.elmakers.mine.bukkit.action.builtin;

import java.util.List;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class BossBarAction extends BaseSpellAction {
    private String title;
    private BarColor color;
    private BarStyle style;
    private BarFlag[] flags;
    private boolean showTarget;

    private double progress;

    // Transient state
    private BossBar bossBar;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        showTarget = parameters.getBoolean("show_target");
        progress = parameters.getDouble("bar_progress");
        title = parameters.getString("bar_title");
        String colorString = parameters.getString("bar_color");
        if (colorString != null && !colorString.isEmpty()) {
            try {
                color = BarColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Invalid boss bar color: " + colorString);
            }
        } else {
            color = BarColor.BLUE;
        }
        String styleString = parameters.getString("bar_style");
        if (styleString != null && !styleString.isEmpty()) {
            try {
                style = BarStyle.valueOf(styleString.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Invalid boss bar style: " + styleString);
            }
        } else {
            style = BarStyle.SOLID;
        }
        List<String> flagList = ConfigurationUtils.getStringList(parameters, "bar_flags");
        if (flagList != null && !flagList.isEmpty()) {
            this.flags = new BarFlag[flagList.size()];
            int index = 0;
            for (String flagKey : flagList) {
                try {
                    flags[index++] = BarFlag.valueOf(flagKey.toUpperCase());
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid boss bar flag: " + flagKey);
                }
            }
        } else {
            this.flags = new BarFlag[0];
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (bossBar == null) {
            String title = context.parameterizeMessage(this.title);
            bossBar = context.getPlugin().getServer().createBossBar(title, color, style, flags);
            bossBar.setVisible(true);
        }
        bossBar.setProgress(progress);

        Entity targetEntity = showTarget ? context.getTargetEntity() : context.getEntity();
        if (targetEntity == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(targetEntity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        bossBar.addPlayer((Player)targetEntity);
        return SpellResult.NO_ACTION;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        if (bossBar != null) {
            bossBar.setVisible(false);
            bossBar = null;
        }
    }
}
