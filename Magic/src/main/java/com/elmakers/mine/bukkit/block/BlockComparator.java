package com.elmakers.mine.bukkit.block;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;

import java.util.Comparator;

final class BlockComparator implements Comparator<com.elmakers.mine.bukkit.api.block.BlockData> {
    private MaterialSet attachables;

    public void setAttachables(MaterialSet attachables) {
        this.attachables = attachables;
    }

    @Override
    public int compare(com.elmakers.mine.bukkit.api.block.BlockData block1, com.elmakers.mine.bukkit.api.block.BlockData block2) {
        boolean attachable1 = attachables.testMaterialAndData(block1);
        boolean attachable2 = attachables.testMaterialAndData(block2);
        if (attachable1 && !attachable2) {
            return 1;
        }
        if (attachable2 && !attachable1) {
            return -1;
        }
        return block1.getLocation().getBlockY() - block2.getLocation().getBlockY();
    }
}
