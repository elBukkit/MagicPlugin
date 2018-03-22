package com.elmakers.mine.bukkit.utility;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TextUtils
{
    private static final NumberFormat[] formatters = {
        new DecimalFormat("#0"),
        new DecimalFormat("#0.0"),
        new DecimalFormat("#0.00"),
        new DecimalFormat("#0.000")
    };

    enum Numeral 
    {
        I(1), IV(4), V(5), IX(9), X(10), XL(40), L(50), XC(90), C(100), CD(400), D(500), CM(900), M(1000);
        int weight;

        Numeral(int weight) 
        {
            this.weight = weight;
        }
    };

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

    public static String printLocation(Location location) {
        return printLocation(location, 2);
    }

    public static String printLocation(Location location, int digits) {
        NumberFormat formatter = formatters[Math.min(Math.max(0, digits), formatters.length - 1)];
        return "" + ChatColor.BLUE + formatter.format(location.getX()) + ChatColor.GRAY + "," +
            ChatColor.BLUE + formatter.format(location.getY()) + ChatColor.GRAY + "," +
            ChatColor.BLUE + formatter.format(location.getZ());
    }

    public static String printVector(Vector vector) {
        return printVector(vector, 3);
    }

    public static String printVector(Vector vector, int digits) {
        NumberFormat formatter = formatters[Math.min(Math.max(0, digits), formatters.length - 1)];
        return "" + ChatColor.BLUE + formatter.format(vector.getX()) + ChatColor.GRAY + "," +
            ChatColor.BLUE + formatter.format(vector.getY()) + ChatColor.GRAY + "," +
            ChatColor.BLUE + formatter.format(vector.getZ());
    }

    public static String printBlock(Block block) {
        return printLocation(block.getLocation(), 0) + ChatColor.GRAY + "(" + ChatColor.GOLD + block.getType() + ChatColor.GRAY + ")";
    }
}
