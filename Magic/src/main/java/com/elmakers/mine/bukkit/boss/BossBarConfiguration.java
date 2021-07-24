package com.elmakers.mine.bukkit.boss;

import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class BossBarConfiguration {
    private String title;
    private BarColor color;
    private BarStyle style;
    private BarFlag[] flags;
    private double radius;
    private long updateInterval;
    private int updateIntervalRandomization;

    public BossBarConfiguration(MageController controller, ConfigurationSection parameters) {
        this(controller, parameters, "$spell");
    }

    public BossBarConfiguration(MageController controller, ConfigurationSection parameters, String defaultTitle) {
        title = parameters.getString("bar_title", defaultTitle);
        String colorString = parameters.getString("bar_color");
        if (colorString != null && !colorString.isEmpty()) {
            try {
                color = BarColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid boss bar color: " + colorString);
            }
        }
        if (color == null) {
            color = BarColor.BLUE;
        }
        String styleString = parameters.getString("bar_style");
        if (styleString != null && !styleString.isEmpty()) {
            try {
                style = BarStyle.valueOf(styleString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid boss bar style: " + styleString);
            }
        }
        if (style == null) {
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
                    controller.getLogger().warning("Invalid boss bar flag: " + flagKey);
                }
            }
        } else {
            this.flags = new BarFlag[0];
        }
        radius = parameters.getDouble("bar_radius", 32);
        updateInterval = parameters.getLong("bar_interval", 10000);
        updateIntervalRandomization = parameters.getInt("bar_interval_randomization", 1000);
    }

    @Nullable
    public static BossBarConfiguration parse(MageController controller, ConfigurationSection config, String defaultTitle) {
        return parse(controller, config, defaultTitle, "boss_bar");
    }

    @Nullable
    public static BossBarConfiguration parse(MageController controller, ConfigurationSection config, String defaultTitle, String key) {
        BossBarConfiguration bossBarConfiguration = null;
        if (config.getBoolean(key)) {
            bossBarConfiguration = new BossBarConfiguration(controller, config, defaultTitle);
        } else {
            ConfigurationSection bossBarConfig = config.getConfigurationSection(key);
            if (bossBarConfig != null) {
                bossBarConfiguration = new BossBarConfiguration(controller, bossBarConfig, defaultTitle);
            }
        }
        return bossBarConfiguration;
    }

    public BossBar createBossBar(CastContext context) {
        String title = context.parameterize(this.title);
        BossBar bossBar = context.getPlugin().getServer().createBossBar(title, color, style, flags);
        bossBar.setVisible(true);
        return bossBar;
    }

    public BossBar createBossBar(Mage mage) {
        String title = mage.parameterize(this.title);
        BossBar bossBar = mage.getController().getPlugin().getServer().createBossBar(title, color, style, flags);
        bossBar.setVisible(true);
        return bossBar;
    }

    public void updateTitle(BossBar bossBar, CastContext context) {
        String title = context.parameterize(this.title);
        bossBar.setTitle(title);
    }

    public BossBarTracker createTracker(Mage mage) {
        return new BossBarTracker(mage, this);
    }

    public double getRadius() {
        return radius;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public int getUpdateIntervalRandomization() {
        return updateIntervalRandomization;
    }
}
