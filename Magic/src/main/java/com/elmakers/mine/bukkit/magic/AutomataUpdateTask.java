package com.elmakers.mine.bukkit.magic;

public class AutomataUpdateTask implements Runnable {
    private final MagicController controller;

    public AutomataUpdateTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.tickAutomata();
    }
}
