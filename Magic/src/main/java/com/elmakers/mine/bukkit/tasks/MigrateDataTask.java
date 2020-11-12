package com.elmakers.mine.bukkit.tasks;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.api.data.MageDataStore;
import com.elmakers.mine.bukkit.magic.MagicController;

public class MigrateDataTask implements Runnable {
    private final MagicController controller;
    private final MageDataStore currentStore;
    private final MageDataStore migrateStore;
    private final CommandSender sender;
    private final Collection<String> ids;

    public MigrateDataTask(MagicController controller, MageDataStore currentStore, MageDataStore migrateStore, CommandSender sender) {
        this.controller = controller;
        this.currentStore = currentStore;
        this.migrateStore = migrateStore;
        this.sender = sender;

        ids = migrateStore.getAllIds();
        controller.getLogger().info("Player data migration started by " + sender.getName());
        sender.sendMessage(ChatColor.GREEN + "Beginning migration of " + ids.size() + " player data records");
    }

    public void messageSender(final String message) {
        Bukkit.getScheduler().runTask(controller.getPlugin(), new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(message);
            }
        });
    }

    @Override
    public void run() {
        final int count = ids.size();
        int index = 0;
        int interval = (int)Math.ceil((double)count / 20.0);
        MageDataCallback loadCallback = new MageDataCallback() {
            @Override
            public void run(MageData data) {
                if (data != null) {
                    currentStore.save(data, null, false);
                    migrateStore.migrate(data.getId());
                }
            }
        };
        for (String id : ids) {
            index++;
            if (controller.getRegisteredMage(id) != null) continue;
            migrateStore.load(id, loadCallback);
            if (index % interval == 0) {
                messageSender(ChatColor.AQUA + "Migrated " + index + "/" + count);
            }
        }
        messageSender(ChatColor.GREEN + "Migration complete. Please remove `migrate_data_store` from config.yml");
        controller.finishMigratingPlayerData();
    }
}
