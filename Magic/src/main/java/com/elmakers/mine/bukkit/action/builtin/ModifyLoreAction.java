package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.NumberConversions;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

public class ModifyLoreAction extends BaseSpellAction
{
    private int digits = 0;

    private static class ModifyLoreLine {
        public Pattern pattern;
        public Object value;
        public boolean numeric;
        public Double min;
        public Double max;
        public String defaultValue;

        public ModifyLoreLine(String pattern, Object value) {
            this.pattern = Pattern.compile(ChatColor.translateAlternateColorCodes('&', pattern));
            this.value = value;
        }

        public ModifyLoreLine(ConfigurationSection configuration) {
            this(configuration.getString("pattern"), configuration.get("value"));
            defaultValue = configuration.getString("default");
            if (defaultValue != null)
                defaultValue = ChatColor.translateAlternateColorCodes('&', defaultValue);
            if (configuration.contains("min"))
                min = configuration.getDouble("min");
            if (configuration.contains("max"))
                max = configuration.getDouble("max");
            numeric = configuration.getBoolean("numeric", true);
        }

        @Nullable
        public String match(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return null;
        }

        @Nonnull
        public String replace(String line, String value) {
            Matcher matcher = pattern.matcher(line);
            matcher.find();
            return new StringBuilder(line).replace(matcher.start(1), matcher.end(1), value).toString();
        }
    }

    private List<ModifyLoreLine> modify;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        digits = parameters.getInt("digits");
        modify = new ArrayList<>();
        Object modifyObject = parameters.get("modify");
        if (modifyObject instanceof ConfigurationSection) {
            ConfigurationSection simple = (ConfigurationSection)modifyObject;
            Set<String> keys = simple.getKeys(true);
            for (String key : keys) {
                ModifyLoreLine property = new ModifyLoreLine(key, simple.get(key));
                modify.add(property);
            }
        } else {
            Collection<ConfigurationSection> complex = ConfigurationUtils.getNodeList(parameters, "modify");
            for (ConfigurationSection section : complex) {
                ModifyLoreLine property = new ModifyLoreLine(section);
                modify.add(property);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (modify == null) {
            return SpellResult.FAIL;
        }
        Entity entity = context.getTargetEntity();
        if (!(entity instanceof Player))
            return SpellResult.PLAYER_REQUIRED;
        Player player = (Player)entity;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null)
            return SpellResult.NO_TARGET;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null)
            lore = new ArrayList<>();

        int modified = 0;
        for (ModifyLoreLine property : modify) {
            boolean found = false;
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                String originalValue = property.match(line);
                if (originalValue != null) {
                    if (property.value == null) continue;
                    String value = "";
                    if (property.value instanceof String) {
                        if (property.numeric) {
                            EquationTransform transform = EquationStore.getInstance().getTransform((String)property.value);
                            double currentValue = NumberConversions.toDouble(originalValue);
                            if (transform.isValid()) {
                                transform.setVariable("x", currentValue);
                                double transformedValue = transform.get();
                                if (Double.isNaN(transformedValue)) continue;

                                if (property.max != null) {
                                    if (currentValue >= property.max && transformedValue >= property.max) continue;
                                    transformedValue = Math.min(transformedValue, property.max);
                                }
                                if (property.min != null) {
                                    if (currentValue <= property.min && transformedValue <= property.min) continue;
                                    transformedValue = Math.max(transformedValue, property.min);
                                }
                                value = TextUtils.printNumber(transformedValue, digits);
                            }
                        } else {
                            value = ChatColor.translateAlternateColorCodes('&', (String)property.value);
                        }
                    } else if (property.value instanceof Number) {
                        value = TextUtils.printNumber(NumberConversions.toDouble(originalValue), digits);
                    }

                    modified++;
                    lore.set(i, property.replace(line, value));
                    break;
                }
            }
            if (!found && property.defaultValue != null) {
                lore.add(property.defaultValue);
                modified++;
                continue;
            }
        }

        if (modified == 0)
            return SpellResult.NO_TARGET;

        meta.setLore(lore);
        item.setItemMeta(meta);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("modify");
        parameters.add("digits");
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }
}
