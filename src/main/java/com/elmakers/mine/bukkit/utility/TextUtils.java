package com.elmakers.mine.bukkit.utility;

import com.google.common.base.CaseFormat;

import java.util.Set;

public class TextUtils
{
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

    public static String formatTags(Set<String> tags) {
        StringBuilder buffer = new StringBuilder();
        for (String tag : tags) {
            if (buffer.length() != 0) {
                buffer.append(", ");
            }
            buffer.append(formatTagName(tag));
        }
        return buffer.toString();
    }

    public static String formatTagName(String itemName) {
        String[] split = itemName.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : split) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, part));
        }
        return builder.toString();
    }
}
