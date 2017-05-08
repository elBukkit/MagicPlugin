package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.plugin.Plugin;

public class ProtocolLibManager {
    private final Plugin owningPlugin;
    private final Plugin protocolLibPlugin;
    private final MageController controller;

    public ProtocolLibManager(MageController controller, Plugin protocolLibPlugin) {
        this.protocolLibPlugin = protocolLibPlugin;
        this.controller = controller;
        this.owningPlugin = controller.getPlugin();
    }

    public boolean initialize() {
        if (protocolLibPlugin == null) {
            return false;
        }
        /*
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(new PacketAdapter(owningPlugin, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    final Player player = event.getPlayer();
                    if (player.getVehicle() != null && event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
                        Mage mage = controller.getMage(player);
                        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                            final PacketContainer packet = event.getPacket();
                            ((com.elmakers.mine.bukkit.magic.Mage)mage).setVehicleMovementDirection(packet.getFloat().readSafely(1));
                        }
                    }
                }
            });

        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "Error registering ProtocolLib hooks", ex);
            return false;
        }
        */
        return true;
    }
}
