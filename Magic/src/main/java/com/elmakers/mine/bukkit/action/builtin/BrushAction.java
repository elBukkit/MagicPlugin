package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;

public class BrushAction extends CompoundAction {
    private List<String> brushes = new ArrayList<>();
    private boolean sample = false;
    private String brushMod;
    private Map<Material, MaterialAndData> materialMap;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        brushes.clear();

        brushMod = parameters.getString("brushmod");
        String materialKey = parameters.getString("brush", null);
        if (materialKey != null) {
            addBrush(materialKey);
        }
        List<String> brushList = parameters.getStringList("brushes");
        if (brushList != null) {
            for (String brushKey : brushList) {
                addBrush(brushKey);
            }
        }
        sample = parameters.getBoolean("sample", false);
        ConfigurationSection replaceConfiguration = parameters.getConfigurationSection("replacements");
        if (replaceConfiguration != null) {
            materialMap = new HashMap<>();
            Set<String> fromKeys = replaceConfiguration.getKeys(false);
            for (String fromKey : fromKeys) {
                Material fromMaterial;
                try {
                    fromMaterial = Material.valueOf(fromKey.toUpperCase());
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid material replacement (from): " + fromKey);
                    continue;
                }
                String toKey = replaceConfiguration.getString(fromKey);
                MaterialAndData toMaterial = new MaterialAndData(toKey);
                if (!toMaterial.isValid()) {
                    context.getLogger().warning("Invalid material replacement (to): " + toKey);
                    continue;
                }
                materialMap.put(fromMaterial, toMaterial);
            }
        }
    }

    protected void addBrush(String brushKey) {
        brushes.add(brushKey);
    }

    @Override
    public SpellResult step(CastContext context) {
        if (brushes.size() == 0 && !sample) {
            return startActions();
        }

        if (sample)
        {
            Block targetBlock = context.getTargetBlock();
            if (targetBlock != null)
            {
                Mage mage = context.getMage();
                MaterialBrush brush = new MaterialBrush(mage, targetBlock);
                actionContext.setBrush(brush);
            }
        }
        else if (brushes != null)
        {
            String brushKey = brushes.get(context.getRandom().nextInt(brushes.size()));
            MaterialBrush brush = null;
            if (brushMod != null) {
                brush = new MaterialBrush(context.getMage(), context.getLocation(), brushMod);
                brush.update(brushKey);
            } else {
                brush = new MaterialBrush(context.getMage(), context.getLocation(), brushKey);
            }
            actionContext.setBrush(brush);
        }
        if (materialMap != null) {
            com.elmakers.mine.bukkit.api.block.MaterialBrush activeBrush = actionContext.getBrush();
            MaterialAndData replacement = materialMap.get(activeBrush.getMaterial());
            if (replacement != null) {
                activeBrush.setMaterial(replacement.getMaterial());
                activeBrush.setData(replacement.getData());
            }
        }
        return startActions();
    }

    @Override
    public boolean usesBrush() {
        return false;
    }
}
