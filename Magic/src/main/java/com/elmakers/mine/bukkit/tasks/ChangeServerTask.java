package com.elmakers.mine.bukkit.tasks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ChangeServerTask implements Runnable {
    private final Player player;
    private final String server;
    private final Plugin plugin;

    public ChangeServerTask(Plugin plugin, Player player, String server) {
        this.player = player;
        this.server = server;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException ex) {
            // Impossible
        }
        player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
    }
}
