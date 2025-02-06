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
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class BossBarConfiguration {
    private String title;
    private BarColor color;
    private BarStyle style;
    private BarFlag[] flags;
    private double radius;
    private long updateInterval;
    private int updateIntervalRandomization;
    private String font;

    public BossBarConfiguration(MageController controller, ConfigurationSection parameters) {
        this(controller, parameters, "$spell");
    }

    public BossBarConfiguration(MageController controller, ConfigurationSection parameters, String defaultTitle) {
        title = parameters.getString("bar_title", parameters.getString("title", defaultTitle));
        font = parameters.getString("font");
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
        String styleString = parameters.getString("bar_style", parameters.getString("style"));
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
        if (flagList == null) {
            flagList = ConfigurationUtils.getStringList(parameters, "flags");
        }
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
        radius = parameters.getDouble("bar_radius", parameters.getDouble("radius", 32));
        updateInterval = parameters.getLong("bar_interval", parameters.getLong("interval", 10000));
        updateIntervalRandomization = parameters.getInt("bar_interval_randomization", parameters.getInt("interval_randomization", 1000));
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

    public BossBar createBossBar(Wand wand) {
        String title = wand.parameterize(this.title);
        String createTitle = title;
        // Update the title in a separate step if it has json in it
        boolean isJSON = ChatUtils.hasJSON(title) || !ChatUtils.isDefaultFont(font);
        if (isJSON) {
            createTitle = "";
        }
        BossBar bossBar = wand.getController().getPlugin().getServer().createBossBar(createTitle, color, style, flags);
        if (isJSON) {
            CompatibilityLib.getCompatibilityUtils().setBossBarTitle(bossBar, title, font);
        }
        bossBar.setVisible(true);
        return bossBar;
    }

    public void updateTitle(BossBar bossBar, CastContext context) {
        String title = context.parameterize(this.title);
        CompatibilityLib.getCompatibilityUtils().setBossBarTitle(bossBar, title, font);
    }

    public void updateTitle(BossBar bossBar, Wand wand) {
        String title = wand.parameterize(this.title);
        CompatibilityLib.getCompatibilityUtils().setBossBarTitle(bossBar, title, font);
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
