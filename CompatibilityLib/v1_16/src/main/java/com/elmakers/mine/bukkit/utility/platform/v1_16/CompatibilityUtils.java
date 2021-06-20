package com.elmakers.mine.bukkit.utility.platform.v1_16;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.md_5.bungee.api.ChatColor;

public class CompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_15.CompatibilityUtils {
    private final Pattern hexColorPattern = Pattern.compile("&(#[A-Fa-f0-9]{6})");

    public CompatibilityUtils(Platform platform) {
        super(platform);
    }

    @Override
    public String translateColors(String message) {
        message = super.translateColors(message);
        Matcher matcher = hexColorPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String match = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of(match).toString());
        }
        return matcher.appendTail(buffer).toString();
    }
}
