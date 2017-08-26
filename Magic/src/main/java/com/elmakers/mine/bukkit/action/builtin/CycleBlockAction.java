package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CycleBlockAction extends BaseSpellAction {

    private Map<MaterialAndData, MaterialAndData> materials = new HashMap<>();

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
        @SuppressWarnings("unchecked")
        List<List<String>> allMaterials = (List<List<String>>)parameters.getList("materials");
        this.materials.clear();
        for (List<String> list : allMaterials) {
            List<MaterialAndData> materialList = new ArrayList<>();
            for (String material : list) {
                MaterialAndData entry = new MaterialAndData(material);
                if (entry.isValid()) {
                    materialList.add(entry);
                }
            }
            for (int i = 0; i < materialList.size(); i++) {
                materials.put(materialList.get(i), materialList.get((i + 1) % materialList.size()));
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();
        if (!context.hasBuildPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        MaterialAndData targetMaterial = new MaterialAndData(block);
        MaterialAndData newMaterial = materials.get(targetMaterial);
        if (newMaterial == null) {
            return SpellResult.NO_TARGET;
        }

        context.registerForUndo(block);
        newMaterial.modify(block);
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}