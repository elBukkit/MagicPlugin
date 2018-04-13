package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

/* package */ final class SimpleMaterialSetManager
        implements MaterialSetManager {
    private Map<String, MaterialSet> materialSets = new HashMap<>();
    private Set<String> unmodifiableMaterialSetKeys = Collections
            .unmodifiableSet(materialSets.keySet());

    @Override
    public Collection<String> getMaterialSets() {
        return unmodifiableMaterialSetKeys;
    }

    @Override
    public MaterialSet getMaterialSet(String name) {
        checkNotNull(name, "name");
        return materialSets.get(name);
    }

    @Override
    public MaterialSet getMaterialSet(String name, MaterialSet fallback) {
        checkNotNull(name, "name");
        checkNotNull(fallback, "fallback");
        MaterialSet set = getMaterialSet(name);
        return set != null ? set : fallback;
    }

    @Override
    public MaterialSet getMaterialSetEmpty(String name) {
        return getMaterialSet(name, MaterialSets.empty());
    }

    @Nullable
    @Override
    public MaterialSet fromConfig(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        MaterialSet materials = materialSets.get(name);
        if (materials == null) {
            materials = createMaterialSetFromString(name);
            materialSets.put(name, materials);
        }

        return materials;
    }

    @Override
    public MaterialSet fromConfig(String name, MaterialSet fallback) {
        // Where is my MoreObjects#firstNonNull when you need it
        MaterialSet v = fromConfig(name);
        return v != null ? v : fallback;
    }

    @Override
    public MaterialSet fromConfigEmpty(String name) {
        return fromConfig(name, MaterialSets.empty());
    }

    /* package */ void loadMaterials(ConfigurationSection materialNode) {
        // Create Material sets
        Set<String> keys = materialNode.getKeys(false);
        for (String key : keys) {
            MaterialSet set = createMaterialSet(materialNode, key);
            if (set != null) {
                materialSets.put(key, set);
            }
        }

    }

    private MaterialSet createMaterialSetFromString(String materialSet) {
        if (materialSet.equals("*")) {
            return MaterialSets.wildcard();
        }

        boolean negate;
        String materialString;
        if (materialSet.startsWith("!")) {
            materialString = materialSet.substring(1);
            negate = true;
        } else {
            materialString = materialSet;
            negate = false;
        }

        String[] names = StringUtils.split(materialString, ',');
        MaterialSet created = createMaterialSetFromStringList(Arrays.asList(names));
        return negate ? created.not() : created;
    }

    @Nullable
    private MaterialSet parseMaterialSet(String materialSet) {
        boolean negate;
        String materialString;
        if (materialSet.startsWith("!")) {
            materialString = materialSet.substring(1);
            negate = true;
        } else {
            materialString = materialSet;
            negate = false;
        }

        MaterialSet existing = getMaterialSet(materialString);
        if (existing == null) {
            return null;
        }

        return negate ? existing.not() : existing;
    }

    @Nullable
    private MaterialSet createMaterialSet(ConfigurationSection node, String key) {
        if (node.isString(key)) {
            return createMaterialSetFromString(node.getString(key));
        }

        List<String> materialData = node.getStringList(key);
        if (materialData == null) {
            // Ignore malformed data
            // TODO: Support for explicitly specifying block state and tile meta
            return null;
        }

        return createMaterialSetFromStringList(materialData);
    }

    private MaterialSet createMaterialSetFromStringList(List<String> names) {
        MaterialSets.Union union = MaterialSets.unionBuilder();
        for (String matName : names) {
            MaterialSet resolved = parseMaterialSet(matName);
            if (resolved != null) {
                union.add(resolved);
            } else if (matName.contains("|") || matName.contains(":")) {
                // TODO: Warn on invalid data
                MaterialAndData material = ConfigurationUtils
                        .toMaterialAndData(matName);
                if (material != null && material.isValid()) {
                    union.add(material);
                }
            } else {
                // No data specified => Match all materials.
                Material material = ConfigurationUtils.toMaterial(matName);
                if (material != null) {
                    union.add(material);
                }
            }
        }

        return union.build();
    }

}
