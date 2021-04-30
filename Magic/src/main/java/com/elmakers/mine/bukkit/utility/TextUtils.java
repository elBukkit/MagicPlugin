package com.elmakers.mine.bukkit.utility;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class TextUtils
{
    private static final NumberFormat[] formatters = {
        new DecimalFormat("#0"),
        new DecimalFormat("#0.0"),
        new DecimalFormat("#0.00"),
        new DecimalFormat("#0.000")
    };
    private static final Pattern replacePattern = Pattern.compile("([$@][\\w]+)([$@]?)");

    enum Numeral {
        I(1), IV(4), V(5), IX(9), X(10), XL(40), L(50), XC(90), C(100), CD(400), D(500), CM(900), M(1000);
        final int weight;

        Numeral(int weight)
        {
            this.weight = weight;
        }
    }

    public static String roman(long n)
    {
        if (n <= 0)
        {
            return "";
        }

        StringBuilder buf = new StringBuilder();

        final Numeral[] values = Numeral.values();
        for (int i = values.length - 1; i >= 0; i--)
        {
            while (n >= values[i].weight)
            {
                buf.append(values[i]);
                n -= values[i].weight;
            }
        }
        return buf.toString();
    }

    public static String printNumber(double number, int digits) {
        NumberFormat formatter = formatters[Math.min(Math.max(0, digits), formatters.length - 1)];
        return formatter.format(number);
    }

    public static String printLocationAndWorld(Location location, int digits) {
        String locationString = printLocation(location, digits);
        World world = location.getWorld();
        if (world != null) {
            locationString = locationString + ChatColor.GRAY + "(" + ChatColor.AQUA + world.getName() + ChatColor.GRAY + ")";
        }
        return locationString;
    }

    public static String printLocation(Location location) {
        return printLocation(location, 2);
    }

    public static String printLocation(Location location, int digits) {
        NumberFormat formatter = formatters[Math.min(Math.max(0, digits), formatters.length - 1)];
        return "" + ChatColor.BLUE + formatter.format(location.getX()) + ChatColor.GRAY + ","
                + ChatColor.BLUE + formatter.format(location.getY()) + ChatColor.GRAY + ","
                + ChatColor.BLUE + formatter.format(location.getZ());
    }

    public static String printBlockLocation(Location location) {
        return "" + ChatColor.BLUE + location.getBlockX() + ChatColor.GRAY + ","
                + ChatColor.BLUE + location.getBlockY() + ChatColor.GRAY + ","
                + ChatColor.BLUE + location.getBlockZ();
    }

    public static String printVector(Vector vector) {
        return printVector(vector, 3);
    }

    public static String printVector(Vector vector, int digits) {
        NumberFormat formatter = formatters[Math.min(Math.max(0, digits), formatters.length - 1)];
        return "" + ChatColor.BLUE + formatter.format(vector.getX()) + ChatColor.GRAY + ","
                + ChatColor.BLUE + formatter.format(vector.getY()) + ChatColor.GRAY + ","
                + ChatColor.BLUE + formatter.format(vector.getZ());
    }

    public static String printBlock(Block block) {
        return printLocation(block.getLocation(), 0) + ChatColor.GRAY + "(" + ChatColor.GOLD + block.getType() + ChatColor.GRAY + ")";
    }

    public static String parameterize(String command, Location location, Player player) {
        return parameterize(command, new BasicReplacer(location, player));
    }

    public static String parameterize(String text, Replacer replacer) {
        StringBuffer parameterized = new StringBuffer();
        Matcher m = replacePattern.matcher(text);
        while (m.find()) {
            String symbol = m.group(1);
            if (symbol.length() < 2) continue;
            char prefix = symbol.charAt(0);
            boolean integerValue = prefix == '@';
            symbol = symbol.substring(1);
            String repString = replacer.getReplacement(symbol, integerValue);
            if (repString != null)
                m.appendReplacement(parameterized, repString);
        }
        m.appendTail(parameterized);
        return parameterized.toString();
    }

    public static void sendMessage(CommandSender target, String message) {
        sendMessage(target, "", message);
    }

    public static void sendMessage(CommandSender target, String prefix, String message) {
        sendMessage(target, target instanceof Player ? (Player)target : null, prefix, message);
    }

    public static void sendMessage(CommandSender sender, Player player, String prefix, String message) {
        if (message == null || message.length() == 0 || sender == null) return;
        boolean isTitle = false;
        boolean isActionBar = false;
        if (message.startsWith("a:")) {
            prefix = "";
            isActionBar = true;
            message = message.substring(2);
        } else if (message.startsWith("t:")) {
            isTitle = true;
            prefix = "";
            message = message.substring(2);
        } else if (prefix.startsWith("a:")) {
            isActionBar = true;
            prefix = prefix.substring(2);
        } else if (prefix.startsWith("t:")) {
            isTitle = true;
            prefix = prefix.substring(2);
        }

        String[] messages = StringUtils.split(message, "\n");
        if (messages.length == 0) {
            return;
        }

        if (isTitle && player != null) {
            String fullMessage = prefix + messages[0];
            String subtitle = messages.length > 1 ? prefix + messages[1] : null;
            CompatibilityUtils.sendTitle(player, fullMessage, subtitle, -1, -1, -1);
            if (messages.length > 2) {
                messages = Arrays.copyOfRange(messages, 2, messages.length);
            } else {
                return;
            }
        }

        for (String line : messages) {
            if (line.trim().isEmpty()) continue;
            boolean lineIsActionBar = isActionBar;
            if (line.startsWith("a:")) {
                lineIsActionBar = true;
                line = line.substring(2);
            }

            isActionBar = false;
            String fullMessage = prefix + line;
            if (lineIsActionBar && player != null) {
                CompatibilityUtils.sendActionBar(player, fullMessage);
            } else {
                sender.sendMessage(fullMessage);
            }
        }
    }

    public static String nameItem(ItemStack itemStack) {
        if (itemStack == null) {
            return "Nothing";
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        return itemStack.getType().name();
    }
}
