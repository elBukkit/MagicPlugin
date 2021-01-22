package com.elmakers.mine.bukkit.utility;

import java.util.List;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class BossBarConfiguration {
    private String title;
    private BarColor color;
    private BarStyle style;
    private BarFlag[] flags;

    public BossBarConfiguration(MageController controller, ConfigurationSection parameters) {
        title = parameters.getString("bar_title", "$spell");
        String colorString = parameters.getString("bar_color");
        if (colorString != null && !colorString.isEmpty()) {
            try {
                color = BarColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid boss bar color: " + colorString);
            }
        } else {
            color = BarColor.BLUE;
        }
        String styleString = parameters.getString("bar_style");
        if (styleString != null && !styleString.isEmpty()) {
            try {
                style = BarStyle.valueOf(styleString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid boss bar style: " + styleString);
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
                    controller.getLogger().warning("Invalid boss bar flag: " + flagKey);
                }
            }
        } else {
            this.flags = new BarFlag[0];
        }
    }

    public BossBar createBossBar(CastContext context) {
        String title = context.parameterizeMessage(this.title);
        BossBar bossBar = context.getPlugin().getServer().createBossBar(title, color, style, flags);
        bossBar.setVisible(true);
        return bossBar;
    }
}
