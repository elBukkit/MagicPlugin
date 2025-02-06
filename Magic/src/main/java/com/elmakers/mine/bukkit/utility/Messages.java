package com.elmakers.mine.bukkit.utility;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.item.Icon;
import com.elmakers.mine.bukkit.utility.help.Help;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class Messages implements com.elmakers.mine.bukkit.api.magic.Messages {
    private static String PARAMETER_PATTERN_STRING = "\\$([a-zA-Z0-9]+)";
    private static Pattern PARAMETER_PATTERN = Pattern.compile(PARAMETER_PATTERN_STRING);
    private static Random random = new Random();

    public static DecimalFormat RANGE_FORMATTER = new DecimalFormat("0.#");
    public static DecimalFormat HOURS_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat MINUTES_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat SECONDS_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat MOMENT_MILLISECONDS_FORMATTER = new DecimalFormat("0");
    public static DecimalFormat MOMENT_SECONDS_FORMATTER = new DecimalFormat("0.##");

    private Map<String, String> macros = new HashMap<>();
    private Map<String, String> messageMap = new HashMap<>();
    private Map<String, List<String>> listMap = new HashMap<>();
    private Map<String, List<String>> randomized = new HashMap<>();

    private Map<Integer, String> spaceAmounts = new HashMap<>();
    private List<Integer> negativeSpace = new ArrayList<>();
    private List<Integer> positiveSpace = new ArrayList<>();

    private Map<String, Icon> icons;

    private NumberFormat formatter = new DecimalFormat("#0.00");
    private static final Pattern macroSpacesPattern = Pattern.compile("\" ([a-zA-Z0-9])");
    private static final Pattern macroEqualsPattern = Pattern.compile("([a-zA-Z0-9])\\=\"");
    private final Gson gson;
    private final Help help;

    public Messages() {
        gson = new Gson();
        help = new Help(this);
    }

    public void load(ConfigurationSection messages, Map<String, Icon> icons) {
        // Reset help
        help.reset();

        // Set icons so we can use them in macro processing
        this.icons = icons;

        // Special mapping of negative space values
        // These can be used via the <space> macro so we need to read them first
        ConfigurationSection spaceSection = messages.getConfigurationSection("glyphs.space");
        Set<String> spaceKeys = spaceSection.getKeys(false);
        spaceAmounts.clear();
        for (String spaceKey : spaceKeys) {
            try {
                Integer spaceAmount = Integer.parseInt(spaceKey);
                // There shouldn't be a zero
                if (spaceAmount == 0) continue;
                if (spaceAmount < 0) {
                    negativeSpace.add(spaceAmount);
                } else {
                    positiveSpace.add(spaceAmount);
                }
                spaceAmounts.put(spaceAmount, spaceSection.getString(spaceKey));
            } catch (Exception ignore) {
            }
        }

        // These need to go in order from largest magnitude to smallest
        Collections.sort(negativeSpace);
        Collections.sort(positiveSpace);
        Collections.reverse(positiveSpace);

        // Everything else can be added via subsequent calls to load(String)
        load(messages);
    }

    public void load(ConfigurationSection messages) {
        // Preload the macros section so it can be used in the following messages
        ConfigurationSection macros = messages.getConfigurationSection("macros");
        if (macros != null) {
            messages.set("macros", null);
            for (String macroKey : macros.getKeys(true)) {
                this.macros.put(macroKey, macros.getString(macroKey));
            }
        }

        // Process help first, don't store it as regular messages
        help.loadMessages(messages);

        // Leave the help messages in here for editor purposes
        // messages.set("help", null);

        Collection<String> keys = messages.getKeys(true);
        for (String key : keys) {
            if (key.equals("randomized")) {
                ConfigurationSection randomSection = messages.getConfigurationSection(key);
                Set<String> randomKeys = randomSection.getKeys(false);
                for (String randomKey : randomKeys) {
                    randomized.put(randomKey, randomSection.getStringList(randomKey));
                }
            } else if (messages.isString(key)) {
                String value = messages.getString(key);
                value = processMacros(value);
                value = CompatibilityLib.getCompatibilityUtils().translateColors(StringEscapeUtils.unescapeHtml(value));
                messageMap.put(key, value);
            } else if (messages.isList(key)) {
                listMap.put(key, messages.getStringList(key));
            }
        }
    }

    @Nonnull
    public Help getHelp() {
        return help;
    }

    private String processMacros(String message) {
        return expandMacros(message).getText();
    }

    @Nonnull
    public MacroExpansion expandMacros(String message) {
        if (!message.contains("`<")) return new MacroExpansion(message);
        String title = null;
        String tags = null;
        String[] pieces = StringUtils.splitPreserveAllTokens(message, "`");
        boolean leftoverDelimiter = false;
        for (int i = 0; i < pieces.length; i++) {
            String piece = pieces[i];
            String defaultReplace = piece;
            // Put back ` if it wasn't actually part of a `<...>` escape sequence
            if (leftoverDelimiter) {
                defaultReplace = "`" + defaultReplace;
            }
            leftoverDelimiter = true;
            if (!piece.startsWith("<") && !piece.endsWith(">")) {
                pieces[i] = defaultReplace;
                continue;
            }

            // Remove brackets
            piece = piece.substring(1, piece.length() - 1);

            // Auto-replace = with :
            StringBuffer json = new StringBuffer();
            Matcher m = macroEqualsPattern.matcher(piece);
            while (m.find()) {
                m.appendReplacement(json, m.group(1) + ":\"");
            }
            m.appendTail(json);
            piece = json.toString();

            // Check for macro type shortcut
            int firstSpace = piece.indexOf(" ");
            int firstColon = piece.indexOf(":");
            // If there is no colon nor space this a simple replaecment macro, such as <p> or <li>
            if (firstSpace < 0 && firstColon < 0) {
                piece = "macro:\"" + piece + "\"";
            } else if (firstColon < 0 || firstColon > firstSpace) {
                // Otherwise if there is no colon before the first space, expand the tag
                piece = "macro:\"" + piece.substring(0, firstSpace) + "\"" + piece.substring(firstSpace);
            }

            // Auto-insert commas
            json = new StringBuffer();
            m = macroSpacesPattern.matcher(piece);
            while (m.find()) {
                m.appendReplacement(json, "\"," + Matcher.quoteReplacement(m.group(1)));
            }
            m.appendTail(json);
            piece = json.toString();

            // JSON-ify
            piece = "{" + piece + "}";
            try {
                JsonReader reader = new JsonReader(new StringReader(piece));
                reader.setLenient(true);
                Map<String, Object> mapped = gson.fromJson(reader, Map.class);
                Object macroKey = mapped.get("macro");
                if (macroKey == null || !(macroKey instanceof String)) {
                    pieces[i] = defaultReplace;
                    continue;
                }
                String macro = macros.get(macroKey);
                if (macro == null) {
                    pieces[i] = defaultReplace;
                    continue;
                }
                // At this point we are treating this like a macro and replacing it, so we are consuming
                // the delimiter
                leftoverDelimiter = false;
                // Check for special hard-coded macros
                if (macroKey.equals("icon")) {
                    Object iconKey = mapped.get("key");
                    Icon icon = iconKey == null || !(iconKey instanceof String) ? null : icons.get(iconKey);
                    String glyph = icon == null ? null : icon.getGlyph();
                    if (glyph == null) {
                        // Just blank this out, we may have unloaded survival and don't have the icons
                        pieces[i] = "";
                        continue;
                    }
                    macro = macro.replace("$glyph", glyph);
                } else if (macroKey.equals("space")) {
                    Object widthValue = mapped.get("width");
                    try {
                        int width = Integer.parseInt(widthValue.toString());
                        String glyph = getSpace(width);
                        macro = macro.replace("$glyph", glyph);
                    } catch (Exception ex) {
                        pieces[i] = defaultReplace;
                        continue;
                    }
                } else {
                    boolean isTitle = macroKey.equals("title");
                    boolean isTags = macroKey.equals("tags");
                    for (Map.Entry<String, Object> entry : mapped.entrySet()) {
                        String macroParameter = entry.getKey();
                        if (macroParameter.equals("macro")) continue;
                        String value = entry.getValue().toString();
                        if (isTitle && macroParameter.equals("text")) title = value;
                        if (isTags && macroParameter.equals("text")) tags = value;
                        macro = macro.replace("$" + macroParameter, value);
                    }
                }
                pieces[i] = macro;
            } catch (Exception ex) {
                continue;
            }
        }

        return new MacroExpansion(StringUtils.join(pieces), title, tags);
    }

    @Override
    @Nullable
    public List<String> getAll(String path) {
        return listMap.get(path);
    }

    @Override
    @Nonnull
    public Collection<String> getAllKeys() {
        List<String> allKeys = new ArrayList<>();
        allKeys.addAll(listMap.keySet());
        allKeys.addAll(messageMap.keySet());
        return allKeys;
    }

    public void reset() {
        messageMap.clear();
        listMap.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return messageMap.containsKey(key) || listMap.containsKey(key);
    }

    @Override
    @Nonnull
    public String get(String key, String defaultValue) {
        String value = getIfSet(key, defaultValue == null ? "" : defaultValue);
        // This actually should never happen, but I'm not sure how to suppress the @Nonnull warning
        return value == null ? defaultValue : value;
    }

    @Override
    @Nonnull
    public String get(String key) {
        return get(key, key);
    }

    @Nullable
    private String getIfSet(String key, String defaultValue) {
        if (messageMap.containsKey(key)) {
            return messageMap.get(key);
        }
        if (defaultValue == null) {
            return defaultValue;
        }
        return CompatibilityLib.getCompatibilityUtils().translateColors(defaultValue);
    }

    @Override
    @Nullable
    public String getIfSet(String key) {
        return getIfSet(key, null);
    }

    @Override
    public String getParameterized(String key, String paramName, String paramValue) {
        return get(key, key).replace(paramName, paramValue);
    }

    @Override
    public String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2) {
        return get(key, key).replace(paramName1, paramValue1).replace(paramName2, paramValue2);
    }

    @Nullable
    @Override
    public String getRandomized(String key) {
        if (!randomized.containsKey(key)) return null;
        List<String> options = randomized.get(key);
        if (options.size() == 0) return "";
        return options.get(random.nextInt(options.size()));
    }

    @Override
    public String escape(String source) {
        Matcher matcher = PARAMETER_PATTERN.matcher(source);
        String result = source;
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key != null) {
                String randomized = getRandomized(key);
                if (randomized != null)
                {
                    result = result.replace("$" + key, randomized);
                }
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public String describeItem(ItemStack item) {
        if (item == null) return "?";
        String displayName = null;
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            displayName = meta.getDisplayName();
            if ((displayName == null || displayName.isEmpty()) && meta instanceof BookMeta) {
                BookMeta book = (BookMeta)meta;
                displayName = book.getTitle();
            }
        }
        if (displayName == null || displayName.isEmpty()) {
            MaterialAndData material = new MaterialAndData(item);
            displayName = material.getName(this);
        }

        return displayName;
    }

    @Override
    public String describeCurrency(double amount) {
        VaultController vault = VaultController.getInstance();
        if (vault == null) return Integer.toString((int)amount);
        String formatted = vault.format(amount);
        if (!VaultController.hasEconomy()) {
            formatted =  get("currency.currency.amount").replace("$amount", formatted);
        }

        return formatted;
    }

    @Override
    public String getCurrency() {
        VaultController vault = VaultController.getInstance();
        if (VaultController.hasEconomy()) {
            return vault.getCurrency();
        }

        return get("currency.currency.name_singular");
    }

    @Override
    public String getCurrencyPlural() {
        VaultController vault = VaultController.getInstance();
        if (VaultController.hasEconomy()) {
            return vault.getCurrencyPlural();
        }

        return get("currency.currency.name");
    }

    @Override
    public String formatList(String basePath, Collection<String> nodes, String nameKey) {
        StringBuilder buffer = new StringBuilder();
        for (String node : nodes) {
            if (buffer.length() != 0) {
                buffer.append(", ");
            }
            String path = node;
            if (basePath != null) {
                path = basePath + "." + path;
            }
            if (nameKey != null) {
                path = path + "." + nameKey;
            }
            node = get(path, node);
            buffer.append(node);
        }
        return buffer.toString();
    }

    @Override
    public String getLevelString(String templateName, float amount)
    {
        return getLevelString(templateName, amount, 1);
    }

    @Override
    public String getLevelString(String templateName, float amount, float max) {
        String templateString = get(templateName, "");
        return formatLevelString(templateString, amount, max);
    }

    @Override
    public String getPropertyString(String templateName, float amount, float max, String propertyTemplateName) {
        String templateString = get(templateName, "");
        String propertyTemplateString = get(propertyTemplateName, "");
        return formatPropertyString(templateString, amount, max, propertyTemplateString);
    }

    @Override
    public String formatLevelString(String message, float amount)
    {
        return formatLevelString(message, amount, 1);
    }

    @Override
    public String formatLevelString(String message, float amount, float max)
    {
        return formatPropertyString(message, amount, max, null);
    }

    @Override
    public String formatPropertyString(String message, float amount, float max, @Nullable String propertyTemplate)
    {
        if (message.contains("$roman")) {
            if (max != 1 && max != 0) {
                amount = amount / max;
            }
            String property = max == 0 ? getRomanLevelString((int)Math.ceil(amount)) : getRomanString(amount);
            if (propertyTemplate != null) {
                property = propertyTemplate.replace("$property", property);
            }
            message = message.replace("$roman",property);
        }
        String valueString = Math.floor(amount) == amount ? Integer.toString((int)Math.floor(amount)) : formatter.format(amount);
        if (propertyTemplate != null) {
            valueString = propertyTemplate.replace("$property", valueString);
        }
        String amountString = Integer.toString(Math.round(amount));
        if (propertyTemplate != null) {
            amountString = propertyTemplate.replace("$property", amountString);
        }
        String percentString = Integer.toString((int)Math.round(100.0 * amount));
        if (propertyTemplate != null) {
            percentString = propertyTemplate.replace("$property", percentString);
        }
        return message.replace("$amount", amountString)
                .replace("$value", valueString)
                .replace("$percent", percentString);
    }

    @Override
    public String getPercentageString(String templateName, float amount)
    {
        String templateString = get(templateName, "");
        return templateString.replace("$amount", Integer.toString((int)(amount * 100)));
    }

    private String getRomanString(float amount) {
        String roman = "";
        boolean negative = false;
        if (amount < 0) {
            amount = -amount;
            negative = true;
        }

        if (amount > 1) {
            roman = get("wand.enchantment_level_max");
        } else if (amount > 0.8f) {
            roman = get("wand.enchantment_level_5");
        } else if (amount > 0.6f) {
            roman = get("wand.enchantment_level_4");
        } else if (amount > 0.4f) {
            roman = get("wand.enchantment_level_3");
        } else if (amount > 0.2f) {
            roman = get("wand.enchantment_level_2");
        } else {
            roman = get("wand.enchantment_level_1");
        }
        if (negative) {
            roman = "-" + roman;
        }
        return roman;
    }

    private String getRomanLevelString(int level) {
        String roman = "";
        boolean negative = false;
        if (level < 0) {
            level = -level;
            negative = true;
        }

        if (level > 5) {
            roman = get("wand.enchantment_level_max");
        } else {
            if (level == 0) level = 1;
            roman = get("wand.enchantment_level_" + level);
        }
        if (negative) {
            roman = "-" + roman;
        }
        return roman;
    }

    private String getWithFallback(String key, String path, String fallbackPath) {
        if (fallbackPath == null || fallbackPath.isEmpty()) {
            return get(path + "." + key);
        }
        return get(path + "." + key, get(fallbackPath + "." + key));
    }

    @Override
    @Nonnull
    public String getTimeDescription(long time) {
        return getTimeDescription(time, "description", null);
    }

    @Override
    @Nonnull
    public String getTimeDescription(long time, @Nonnull String descriptionType) {
        return getTimeDescription(time, descriptionType, null);
    }

    @Override
    @Nonnull
    public String getTimeDescription(long time, @Nonnull String descriptionType, @Nullable String messagesPath) {
        if (time > 0) {
            double timeInSeconds = (double)time / 1000;
            if (timeInSeconds >= 60 * 60 * 24) {
                double days = timeInSeconds / (60 * 60 * 24);
                if ((long)Math.floor(days) == 1) {
                    return getWithFallback(descriptionType + "_day", "time", messagesPath)
                            .replace("$days", HOURS_FORMATTER.format(days));
                }
                return getWithFallback(descriptionType + "_days", "time", messagesPath)
                        .replace("$days", HOURS_FORMATTER.format(days));
            } else if (timeInSeconds >= 60 * 60) {
                double hours = timeInSeconds / (60 * 60);
                if ((long)Math.floor(hours) == 1) {
                    return getWithFallback(descriptionType + "_hour", "time", messagesPath)
                        .replace("$hours", HOURS_FORMATTER.format(hours));
                }
                return getWithFallback(descriptionType + "_hours", "time", messagesPath)
                    .replace("$hours", HOURS_FORMATTER.format(hours));
            } else if (timeInSeconds >= 60) {
                double minutes = timeInSeconds / 60;
                if ((long)Math.floor(minutes) == 1) {
                    return getWithFallback(descriptionType + "_minute", "time", messagesPath)
                        .replace("$minutes", MINUTES_FORMATTER.format(minutes));
                }
                return getWithFallback(descriptionType + "_minutes", "time", messagesPath)
                    .replace("$minutes", MINUTES_FORMATTER.format(minutes));
            } else if (timeInSeconds >= 2) {
                return getWithFallback(descriptionType + "_seconds", "time", messagesPath)
                    .replace("$seconds", SECONDS_FORMATTER.format(timeInSeconds));
            } else if (timeInSeconds >= 1) {
                return getWithFallback(descriptionType + "_second", "time", messagesPath)
                    .replace("$seconds", SECONDS_FORMATTER.format(timeInSeconds));
            } else {
                String timeDescription = getWithFallback(descriptionType + "_moment", "time", messagesPath);
                timeDescription = timeDescription
                    .replace("$milliseconds", MOMENT_MILLISECONDS_FORMATTER.format(timeInSeconds * 1000))
                    .replace("$seconds", MOMENT_SECONDS_FORMATTER.format(timeInSeconds));
                return timeDescription;
            }
        }
        return "0";
    }

    @Override
    @Nonnull
    public String getRangeDescription(double range, @Nonnull String messagesKey) {
        return get(messagesKey).replace("$range", RANGE_FORMATTER.format(range));
    }

    /**
     * This relies on the negative space font RP:
     * https://github.com/AmberWat/NegativeSpaceFont
     */
    @Nonnull
    @Override
    public String getSpace(int pixels) {
        if (pixels == 0) {
            return "";
        }

        if (spaceAmounts.containsKey(pixels)) {
            return spaceAmounts.get(pixels);
        }

        int totalPixels = pixels;
        int absPixels = Math.abs(pixels);
        List<Integer> spaceValues = pixels > 0 ? positiveSpace : negativeSpace;
        StringBuilder output = new StringBuilder();

        for (Integer spaceValue : spaceValues) {
            int absValue = Math.abs(spaceValue);
            // See if we can fit this space in
            if (absPixels < absValue) continue;

            // Append as many of these as we can
            String entryGlyph = spaceAmounts.get(spaceValue);
            int amount = absPixels / absValue;
            for (int i = 0; i < amount; i++) {
                output.append(entryGlyph);
            }

            // Subtract off the amount of space we just added
            pixels = pixels - (amount * spaceValue);

            // See if we are done
            absPixels = Math.abs(pixels);
            if (absPixels == 0) break;
        }

        // Cache this string so we don't have to recompute it later
        String result = output.toString();
        spaceAmounts.put(totalPixels, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public void loadMeta(InputStream inputStream) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        Map<String, Object> meta = gson.fromJson(reader, Map.class);
        help.loadMetaActions((Map<String, Map<String, Object>>)meta.get("actions"));
        help.loadMetaEffects((Map<String, Map<String, Object>>)meta.get("effectlib_effects"));
    }
}
