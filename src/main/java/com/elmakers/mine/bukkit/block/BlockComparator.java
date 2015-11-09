package com.elmakers.mine.bukkit.block;

import com.elmakers.mine.bukkit.api.block.*;
import org.bukkit.Material;

import java.util.Comparator;
import java.util.Set;

class BlockComparator implements Comparator<com.elmakers.mine.bukkit.api.block.BlockData> {
    private Set<Material> attachables;

    public void setAttachables(Set<Material> attachables) {
        this.attachables = attachables;
    }

    @Override
    public int compare(com.elmakers.mine.bukkit.api.block.BlockData block1, com.elmakers.mine.bukkit.api.block.BlockData block2) {
        Material material1 = block1.getMaterial();
        Material material2 = block2.getMaterial();
        boolean attachable1 = attachables.contains(material1);
        boolean attachable2 = attachables.contains(material2);
        if (attachable1 && !attachable2) {
            return 1;
        }
        if (attachable2 && !attachable1) {
            return -1;
        }
        return block1.getLocation().getBlockY() - block2.getLocation().getBlockY();
    }
}
