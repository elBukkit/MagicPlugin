package com.elmakers.mine.bukkit.spell;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class BrushSpell extends BlockSpell {

    protected MaterialBrush brush;
    protected String brushKey;
    protected boolean hasBrush = false;
    protected boolean usesBrush = true;
    protected boolean brushIsErase = false;
    protected boolean usesBrushEffects = true;

    public static final String[] BRUSH_PARAMETERS = {
        "brushmod", "brush", "obx", "oby", "obz", "obworld", "btarget", "brushcolor", "preserve_data"
    };

    @Override
    public void processParameters(ConfigurationSection parameters)
    {
        super.processParameters(parameters);

        brushKey = parameters.getString("brush", null);
        brushIsErase = brushKey != null && (brushKey.equalsIgnoreCase("erase") || brushKey.equalsIgnoreCase("air"));

        hasBrush = brushKey != null && !brushKey.isEmpty();
        if (hasBrush) {
            brush = new MaterialBrush(mage, getLocation(), brushKey);

            if (parameters.getBoolean("preserve_data", false)) {
                brush.setData(null);
            }

            if (parameters.contains("brushmod")) {
                brush.update(parameters.getString("brushmod"));
                brush.update(brushKey);
            }

            Double dmxValue = ConfigurationUtils.getDouble(parameters, "obx", null);
            Double dmyValue = ConfigurationUtils.getDouble(parameters, "oby", null);
            Double dmzValue = ConfigurationUtils.getDouble(parameters, "obz", null);
            String targetWorldName = parameters.getString("obworld", null);
            if (dmxValue != null || dmyValue != null || dmzValue != null || targetWorldName != null) {
                Vector offset = new Vector(
                        dmxValue == null ? 0 : dmxValue,
                        dmyValue == null ? 0 : dmyValue,
                        dmzValue == null ? 0 : dmzValue);

                brush.clearCloneTarget();
                brush.setTargetOffset(offset, targetWorldName);
            }

            if (parameters.getBoolean("brushtarget", false)) {
                brush.clearCloneLocation();
            }

            if (parameters.getBoolean("brushcolor", false)) {
                Color color = mage.getEffectColor();
                if (color != null) {
                    DyeColor bestDyeColor = null;
                    Double bestDistance = null;
                    for (DyeColor testDyeColor : DyeColor.values()) {
                        Color testColor = testDyeColor.getColor();
                        double testDistance = ColorHD.getDistance(testColor, color);
                        if (bestDistance == null || testDistance < bestDistance) {
                            bestDistance = testDistance;
                            bestDyeColor = testDyeColor;
                            if (testDistance == 0) break;
                        }
                    }

                    if (bestDyeColor != null) {
                        brush.colorize(bestDyeColor);
                    }
                }
            }
        } else {
            brush = null;
        }
    }

    @Override
    protected void loadTemplate(ConfigurationSection node)
    {
        super.loadTemplate(node);
        usesBrush = node.getBoolean("uses_brush", usesBrush);
        usesBrushEffects = node.getBoolean("brush_effects", true);

        ConfigurationSection parameters = node.getConfigurationSection("parameters");
        if (parameters != null)
        {
            brushKey = parameters.getString("brush", null);
            hasBrush = brushKey != null && !brushKey.isEmpty();
        }
        else
        {
            brushKey = null;
            hasBrush = false;
        }
        brushIsErase = brushKey != null && (brushKey.equalsIgnoreCase("erase") || brushKey.equalsIgnoreCase("air"));
    }

    @Override
    public boolean brushIsErase() {
        if (mage != null && !hasBrush) {
            return getBrush().isErase();
        }
        return brushIsErase;
    }

    @Override
    public boolean requiresBuildPermission() {
        return !brushIsErase();
    }

    @Override
    public boolean requiresBreakPermission() {
        return brushIsErase();
    }

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialBrush getBrush()
    {
        if (brush == null && hasBrush)
        {
            brush = new MaterialBrush(controller, brushKey);
        }
        if (brush != null)
        {
            return brush;
        }

        return super.getBrush();
    }

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        if (!usesBrush || !usesBrushEffects) return null;
        return getBrush();
    }

    @Override
    public boolean hasBrushOverride()
    {
        return hasBrush;
    }

    @Override
    public boolean usesBrush()
    {
        return usesBrush && !hasBrushOverride();
    }

    @Override
    protected String getDisplayMaterialName()
    {
        if (usesBrush) {
            com.elmakers.mine.bukkit.api.block.MaterialBrush useBrush = getBrush();
            if (useBrush != null) {
                return useBrush.getName();
            }
        }

        return super.getDisplayMaterialName();
    }

    @Override
    public void getParameters(Collection<String> parameters)
    {
        super.getParameters(parameters);
        parameters.addAll(Arrays.asList(BRUSH_PARAMETERS));
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("bmod") || parameterKey.equals("brush")) {
            examples.addAll(controller.getBrushKeys());
        } else if (parameterKey.equals("btarget")) {
            examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("obx") || parameterKey.equals("oby") || parameterKey.equals("obz")) {
            examples.addAll(Arrays.asList(EXAMPLE_VECTOR_COMPONENTS));
        } else if (parameterKey.equals("obworld")) {
            List<World> worlds = Bukkit.getWorlds();
            for (World world : worlds) {
                examples.add(world.getName());
            }
        }
    }
}
