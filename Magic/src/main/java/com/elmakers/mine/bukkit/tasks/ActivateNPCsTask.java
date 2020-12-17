package com.elmakers.mine.bukkit.tasks;

import java.util.List;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.npc.MagicNPC;

public class ActivateNPCsTask implements Runnable {
    final List<MagicNPC> npcs;
    final MagicController controller;

    public ActivateNPCsTask(MagicController controller, List<MagicNPC> npcs) {
        this.controller = controller;
        this.npcs = npcs;
    }

    @Override
    public void run() {
        for (MagicNPC npc : npcs) {
            npc.restore();
            controller.activateNPC(npc);
        }
    }
}
