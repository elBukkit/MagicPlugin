package com.elmakers.mine.bukkit.magic.listener;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import com.elmakers.mine.bukkit.api.event.SaveEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.arena.Arena;
import com.elmakers.mine.bukkit.arena.ArenaController;
import com.elmakers.mine.bukkit.arena.ArenaPlayer;
import com.elmakers.mine.bukkit.block.DefaultMaterials;

public class ArenaListener implements Listener {
    private final ArenaController controller;
    private static final String SIGN_KEY = ChatColor.GOLD + "[" + ChatColor.BLUE + "Arena" + ChatColor.GOLD + "]";

    public ArenaListener(ArenaController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onSpellPreCast(PreCastEvent event) {
        Mage mage = event.getMage();
        Player player = mage.getPlayer();
        if (player == null) {
            return;
        }
        ArenaPlayer arenaPlayer = controller.getArenaPlayer(player);
        if (arenaPlayer == null || !arenaPlayer.isBattling()) {
            return;
        }
        Arena arena = arenaPlayer.getArena();
        if (arena != null && arena.hasOpCheck()) {
            Wand wand = arenaPlayer.getMage().getActiveWand();
            boolean op = wand != null && (
                       wand.isSuperPowered() || wand.isSuperProtected()
                    || wand.getPower() > 1 || wand.getHealthRegeneration() > 0
                    || wand.getCooldownReduction() > 1);
            if (op)
            {
                event.setCancelled(true);
                mage.sendMessage("You're too OP!!");
                controller.leave(player);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("respawnLocation")) {
            Collection<MetadataValue> metadata = player.getMetadata("respawnLocation");
            for (MetadataValue value : metadata) {
                e.setRespawnLocation((Location)value.value());
            }
            player.removeMetadata("respawnLocation", controller.getPlugin());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) return;
        Arena arena = controller.getMobArena(entity);
        if (arena != null) {
            arena.mobDied(entity);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Arena arena = controller.getArena(player);
        if (arena != null && !arena.isAllowConsuming()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        damager = controller.getMagic().getDamageSource(damager);
        if (!(damager instanceof Player)) return;
        Arena arena = controller.getArena((Player)damager);
        if (arena != null && !controller.getMagic().isDamaging()) {
            boolean isProjectile = event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE;
            boolean isMelee = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK;
            if (isProjectile && !arena.isAllowProjectiles()) {
                event.setCancelled(true);
            } else if (isMelee && !arena.isAllowMelee()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        Arena arena = controller.getArena(player);
        if (arena != null && !arena.isItemWear()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Arena arena = controller.getArena(player);
        if (arena != null) {
            arena.died(player);

            if (arena.isKeepInventory()) {
                e.setKeepInventory(true);
                e.getDrops().clear();
            }

            if (arena.isKeepLevel()) {
                e.setKeepLevel(true);
                e.setDroppedExp(0);
            }
        }
        if (player.hasMetadata("death_message")) {
            Collection<MetadataValue> metadata = player.getMetadata("death_message");
            for (MetadataValue value : metadata) {
                e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', value.asString()).replace("@p", player.getDisplayName()));
            }
            player.removeMetadata("death_message", controller.getPlugin());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        ArenaPlayer leftPlayer = controller.leave(player);
        if (leftPlayer != null) {
            leftPlayer.quit();
            Arena arena = leftPlayer.getArena();
            arena.announce(ChatColor.RED + leftPlayer.getDisplayName() + ChatColor.DARK_AQUA + " has left " + ChatColor.AQUA + arena.getName());
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        Player player = e.getPlayer();
        if (!player.hasPermission("MagicArenas.signs.create")) {
            return;
        }
        String firstLine = e.getLine(0);
        if (firstLine.equalsIgnoreCase("[Arena]")) {
            String secondLine = e.getLine(1);
            if (secondLine.equalsIgnoreCase("Join")) {
                String arenaName = e.getLine(2);
                if (!arenaName.isEmpty()) {
                    Arena arena = controller.getArena(arenaName);
                    if (arena != null) {
                        e.setLine(0, SIGN_KEY);
                        e.setLine(1, ChatColor.DARK_AQUA + "Join");
                    } else {
                        e.getBlock().breakNaturally();
                        e.getPlayer().sendMessage(ChatColor.RED + "Unknown arena: " + arenaName);
                    }
                } else {
                    e.getBlock().breakNaturally();
                    e.getPlayer().sendMessage(ChatColor.RED + "You must specify an arena!");
                }
            } else if (secondLine.equalsIgnoreCase("Leave")) {
                e.setLine(0, SIGN_KEY);
                e.setLine(1, ChatColor.AQUA + "Leave");
            } else if (secondLine.equalsIgnoreCase("Leaderboard")) {
                String arenaName = e.getLine(2);
                if (!arenaName.isEmpty()) {
                    Arena arena = controller.getArena(arenaName);
                    if (arena != null) {
                        e.setLine(0, SIGN_KEY);
                        e.setLine(1, ChatColor.DARK_PURPLE + "Leaderboard");
                        if (!arena.placeLeaderboard(e.getBlock())) {
                            e.getBlock().breakNaturally();
                            e.getPlayer().sendMessage(ChatColor.RED + "Leaderboard must be a wall sign with " + ChatColor.YELLOW + arena.getLeaderboardSize() + ChatColor.RED + " empty blocks above it above it to the right");
                        }
                    } else {
                        e.getBlock().breakNaturally();
                        e.getPlayer().sendMessage(ChatColor.RED + "Unknown arena: " + arenaName);
                    }
                } else {
                    e.getBlock().breakNaturally();
                    e.getPlayer().sendMessage(ChatColor.RED + "You must specify an arena!");
                }
            } else {
                e.getBlock().breakNaturally();
                e.getPlayer().sendMessage(ChatColor.RED + "You must specify Join, Leave or Leaderboard");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!player.hasPermission("MagicArenas.signs.use")) {
            return;
        }

        Block clickedBlock = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (DefaultMaterials.isSign(clickedBlock.getType()))) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            String firstLine = sign.getLine(0);
            firstLine = firstLine.replace("" + ChatColor.RESET, "");
            if (firstLine.equals(SIGN_KEY)) {
                String secondLine = sign.getLine(1);
                if (secondLine.contains("Join")) {
                    String arenaName = sign.getLine(2);
                    Arena arena = controller.getArena(arenaName);
                    if (arena != null) {
                        arena.join(e.getPlayer());
                    } else {
                        player.sendMessage(ChatColor.RED + "Sorry, that arena isn't available.");
                    }
                } else if (secondLine.contains("Leave")) {
                    controller.leave(e.getPlayer());
                } else if (secondLine.contains("Leaderboard")) {
                    String arenaName = sign.getLine(2);
                    Arena arena = controller.getArena(arenaName);
                    if (arena != null) {
                        arena.showLeaderboard(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Sorry, that arena isn't available.");
                    }
                }
            }
        }
    }

    protected boolean onEnterPortal(Entity entity) {
        // Mob arenas eventually!
        if (!(entity instanceof Player)) {
            return false;
        }
        Player player = ((Player)entity).getPlayer();
        Arena arena = controller.getArena(player);
        if (arena != null && arena.getPortalEnterDamage() > 0) {
            String portalDeathMessage = arena.getPortalDeathMessage();
            if (portalDeathMessage != null && !portalDeathMessage.isEmpty()) {
                player.setMetadata("death_message", new FixedMetadataValue(controller.getPlugin(), portalDeathMessage));
            }
            player.damage(arena.getPortalEnterDamage());
            if (portalDeathMessage != null && !portalDeathMessage.isEmpty()) {
                player.removeMetadata("death_message", controller.getPlugin());
            }
            return true;
        }

        return false;
    }

    protected boolean onPortal(Entity entity) {
        // Mob arenas eventually!
        if (!(entity instanceof Player)) {
            return false;
        }
        Player player = ((Player)entity).getPlayer();
        Arena arena = controller.getArena(player);
        if (arena != null && arena.getPortalDamage() > 0) {
            String portalDeathMessage = arena.getPortalDeathMessage();
            if (portalDeathMessage != null && !portalDeathMessage.isEmpty()) {
                player.setMetadata("death_message", new FixedMetadataValue(controller.getPlugin(), portalDeathMessage));
            }
            player.damage(arena.getPortalDamage());
            if (portalDeathMessage != null && !portalDeathMessage.isEmpty()) {
                player.removeMetadata("death_message", controller.getPlugin());
            }

            return true;
        }

        return false;
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEnterEvent event) {
        onEnterPortal(event.getEntity());
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (onPortal(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("allow_teleport")) {
            player.removeMetadata("allow_teleport", controller.getPlugin());
            return;
        }
        Arena arena = controller.getArena(player);
        if (arena != null && arena.getMaxTeleportDistance() >= 0) {
            Location center = arena.getCenter();
            if (!center.getWorld().equals(event.getTo().getWorld())
             || !event.getFrom().getWorld().equals(event.getTo().getWorld())
             || event.getFrom().distanceSquared(event.getTo()) > arena.getMaxTeleportDistance() * arena.getMaxTeleportDistance()) {
                controller.leave(player);
                player.sendMessage(ChatColor.DARK_RED + "You have given up and left the arena");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Leaderboard")) {
            event.getWhoClicked().closeInventory();
        }
    }

    @EventHandler
    public void onMagicSave(SaveEvent event) {
        controller.saveData(event.isAsynchronousSave());
    }
}
