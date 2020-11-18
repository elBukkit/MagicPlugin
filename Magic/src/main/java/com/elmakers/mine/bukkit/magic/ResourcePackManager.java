package com.elmakers.mine.bukkit.magic;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.tasks.RPCheckTask;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.google.common.io.BaseEncoding;

public class ResourcePackManager {
    private static final String RP_FILE = "resourcepack";

    private final MagicController controller;
    private boolean enableResourcePackCheck = true;
    private int resourcePackPromptDelay = 0;
    private boolean resourcePackPrompt = false;
    private int resourcePackCheckInterval = 0;
    private int resourcePackCheckTimer = 0;
    private String defaultResourcePack = null;
    private boolean checkedResourcePack = false;
    private String resourcePack = null;
    private byte[] resourcePackHash = null;
    private long resourcePackDelay = 0;
    private boolean isResourcePackEnabledByDefault = true;

    public ResourcePackManager(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection properties, CommandSender sender, boolean firstLoad) {
        String currentResourcePack = defaultResourcePack;
        isResourcePackEnabledByDefault = properties.getBoolean("resource_pack_default_auto", true);

        resourcePackPrompt = properties.getBoolean("resource_pack_prompt", false);
        enableResourcePackCheck = properties.getBoolean("enable_resource_pack_check", true);
        resourcePackCheckInterval = properties.getInt("resource_pack_check_interval", 0);
        resourcePackPromptDelay = properties.getInt("resource_pack_prompt_delay", 0);
        defaultResourcePack = properties.getString("resource_pack", null);
        // For legacy configs
        defaultResourcePack = properties.getString("default_resource_pack", defaultResourcePack);
        // For combined configs
        if (controller.hasAddedExamples() && !defaultResourcePack.isEmpty()) {
            defaultResourcePack = properties.getString("add_resource_pack", defaultResourcePack);
        }

        if (!properties.getBoolean("enable_resource_pack")) {
            defaultResourcePack = null;
        }

        // For reloading after disabling the RP
        if (defaultResourcePack == null || defaultResourcePack.isEmpty()) {
            resourcePack = null;
            resourcePackHash = null;
        }

        resourcePackDelay = properties.getLong("resource_pack_delay", 0);

        if (!firstLoad && resourcePack != null && !defaultResourcePack.equals(currentResourcePack)) {
            checkResourcePack(sender, false, false, true);
        }
    }

    public boolean sendResourcePackToAllPlayers(CommandSender sender) {
        if (resourcePack == null || resourcePackHash == null) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "No RP set or RP already set in server.properties, not sending.");
            }
            return false;
        }
        int sent = 0;
        int skipped = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Mage mage = controller.getRegisteredMage(player);
            if (mage != null && !mage.isResourcePackEnabled()) {
                skipped++;
                continue;
            }
            sendResourcePack(player);
            sent++;
        }
        if (sender != null) {
            sender.sendMessage(ChatColor.AQUA + "Sent current RP to " + sent + " players, skipped " + skipped + " players");
        }

        return true;
    }

    protected boolean promptResourcePack(final Player player, String message) {
        if (message != null && !message.isEmpty()) {
            if (resourcePackPromptDelay <= 0) {
                com.elmakers.mine.bukkit.magic.Mage.sendMessage(player, player, controller.getMessagePrefix(), message);
            } else {
                Plugin plugin = controller.getPlugin();
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        com.elmakers.mine.bukkit.magic.Mage.sendMessage(player, player, controller.getMessagePrefix(), message);
                    }
                }, resourcePackPromptDelay / 50);
            }
        }
        return true;
    }

    public boolean promptResourcePack(final Player player) {
        if (resourcePack == null || resourcePackHash == null) {
            return false;
        }

        if (resourcePackPrompt) {
            String message = controller.getMessages().get("resource_pack.prompt");
            promptResourcePack(player, message);
            return false;
        }

        return sendResourcePack(player);
    }

    public boolean promptNoResourcePack(final Player player) {
        if (resourcePack == null || resourcePackHash == null) {
            return false;
        }

        String message = controller.getMessages().get("resource_pack.off_prompt");
        return promptResourcePack(player, message);
    }

    public boolean sendResourcePack(final Player player) {
        if (resourcePack == null || resourcePackHash == null) {
            return false;
        }
        String message = controller.getMessages().get("resource_pack.sending");
        if (message != null && !message.isEmpty()) {
            com.elmakers.mine.bukkit.magic.Mage.sendMessage(player, player, controller.getMessagePrefix(), message);
        }

        // Give them some time to read the message
        Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
            @Override
            public void run() {
                CompatibilityUtils.setResourcePack(player, resourcePack, resourcePackHash);
            }
        }, resourcePackDelay * 20 / 1000);

        return true;
    }

    public boolean checkResourcePack(final CommandSender sender, final boolean quiet, final boolean force, final boolean filenameChanged) {
        final Plugin plugin = controller.getPlugin();
        if (!plugin.isEnabled()) return false;
        final Server server = plugin.getServer();
        resourcePack = null;
        resourcePackHash = null;
        final boolean initialLoad = !checkedResourcePack;

        if (defaultResourcePack == null || defaultResourcePack.isEmpty()) {
            if (!quiet) sender.sendMessage("Resource pack in config.yml has been disabled, Magic skipping RP check");
            return false;
        }

        String serverResourcePack = CompatibilityUtils.getResourcePack(server);
        if (serverResourcePack != null) serverResourcePack = serverResourcePack.trim();

        if (serverResourcePack != null && !serverResourcePack.isEmpty()) {
            if (!quiet) sender.sendMessage("Resource pack configured in server.properties, Magic not using RP from config.yml");
            return false;
        }
        resourcePack = defaultResourcePack;

        checkedResourcePack = true;
        if (!quiet) sender.sendMessage("Magic checking resource pack for updates: " + ChatColor.GRAY + resourcePack);

        long modifiedTime = 0;
        String currentSHA = null;
        final YamlConfiguration rpConfig = new YamlConfiguration();
        final File rpFile = new File(plugin.getDataFolder(), "data/" + RP_FILE + ".yml");
        final String rpKey = resourcePack.replace(".", "_");
        if (rpFile.exists()) {
            try {
                rpConfig.load(rpFile);
                ConfigurationSection rpSection = rpConfig.getConfigurationSection(rpKey);
                if (rpSection != null) {
                    currentSHA = rpSection.getString("sha1");
                    modifiedTime = rpSection.getLong("modified");

                    // Ignore old encoding, we will need to update
                    if (currentSHA != null && currentSHA.length() < 40) {
                        resourcePackHash = BaseEncoding.base64().decode(currentSHA);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        final String finalResourcePack = resourcePack;
        final long modifiedTimestamp = modifiedTime;
        final String currentHash = currentSHA;
        server.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                final List<String> responses = new ArrayList<>();
                String newResourcePackHash = currentHash;
                try {
                    URL rpURL = new URL(finalResourcePack);
                    HttpURLConnection connection = (HttpURLConnection)rpURL.openConnection();
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("HEAD");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                        Date tryParseDate = new Date(1L);
                        boolean hasModifiedTime = false;
                        final String lastModified = connection.getHeaderField("Last-Modified");
                        if (lastModified == null || lastModified.isEmpty()) {
                            responses.add(ChatColor.YELLOW + "Server did not return a Last-Modified field, cancelling checks until restart");
                            cancelResourcePackChecks();
                        } else {
                            try {
                                tryParseDate = format.parse(lastModified);
                                hasModifiedTime = true;
                            } catch (ParseException dateFormat) {
                                cancelResourcePackChecks();
                                responses.add("Error parsing resource pack modified time, cancelling checks until restart: " + lastModified);
                            }
                        }
                        final Date modifiedDate = tryParseDate;
                        if (modifiedDate.getTime() > modifiedTimestamp || resourcePackHash == null || (force && !hasModifiedTime)) {
                            final boolean isUnset = (resourcePackHash == null);
                            if (filenameChanged) {
                               responses.add(ChatColor.YELLOW + "Resource pack changed, checking for updated hash");
                            } else if (modifiedTimestamp <= 0) {
                                responses.add(ChatColor.YELLOW + "Checking resource pack for the first time");
                            } else if (isUnset) {
                                responses.add(ChatColor.YELLOW + "Resource pack hash format changed, downloading for one-time update");
                            } else if (!hasModifiedTime && force) {
                                responses.add(ChatColor.YELLOW + "Forcing resource pack check with missing modified time, redownloading");
                            } else {
                                responses.add(ChatColor.YELLOW + "Resource pack modified, redownloading (" + modifiedDate.getTime() + " > " + modifiedTimestamp + ")");
                            }

                            MessageDigest digest = MessageDigest.getInstance("SHA1");
                            try (BufferedInputStream in = new BufferedInputStream(rpURL.openStream())) {
                                final byte[] data = new byte[1024];
                                int count;
                                while ((count = in.read(data, 0, 1024)) != -1) {
                                    digest.update(data, 0, count);
                                }
                            }
                            resourcePackHash = digest.digest();
                            newResourcePackHash = BaseEncoding.base64().encode(resourcePackHash);

                            if (initialLoad) {
                                responses.add(ChatColor.GREEN + "Resource pack hash set to " + ChatColor.GRAY + newResourcePackHash);
                            } else if (currentHash != null && currentHash.equals(newResourcePackHash))  {
                                responses.add(ChatColor.GREEN + "Resource pack hash has not changed");
                            } else {
                                responses.add(ChatColor.YELLOW + "Resource pack hash changed, use " + ChatColor.AQUA + "/magic rpsend" + ChatColor.YELLOW + " to update connected players");
                            }

                            ConfigurationSection rpSection = rpConfig.createSection(rpKey);

                            rpSection.set("sha1", newResourcePackHash);
                            rpSection.set("modified", modifiedDate.getTime());
                            rpSection.set("filename", resourcePack);
                            rpConfig.save(rpFile);
                        } else {
                            if (filenameChanged) {
                                responses.add(ChatColor.YELLOW + "Resource pack changed, use " + ChatColor.AQUA + "/magic rpsend" + ChatColor.YELLOW + " to update connected players");
                            } else {
                                responses.add(ChatColor.GREEN + "Resource pack has not changed, using hash " + newResourcePackHash +  " (" + modifiedDate.getTime() + " <= " + modifiedTimestamp + ")");
                            }
                        }
                    }
                    else
                    {
                        responses.add(ChatColor.RED + "Could not find resource pack at: " + ChatColor.DARK_RED + finalResourcePack);
                        cancelResourcePackChecks();
                    }
                }
                catch (Exception ex) {
                    cancelResourcePackChecks();
                    responses.add("An unexpected error occurred while checking your resource pack, cancelling checks until restart (see logs): " + ChatColor.DARK_RED + finalResourcePack);
                    controller.getLogger().log(Level.WARNING,"Error checking resource pack: " + ex.getMessage());
                }

                if (!quiet && plugin.isEnabled()) {
                    server.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            for (String response : responses) {
                                sender.sendMessage(response);
                            }
                        }
                    });
                }
            }
        });
        return true;
    }

    protected void cancelResourcePackChecks() {
        if (resourcePackCheckTimer != 0) {
            Bukkit.getScheduler().cancelTask(resourcePackCheckTimer);
            resourcePackCheckTimer = 0;
        }
    }

    public void startResourcePackChecks() {
        if (checkResourcePack(Bukkit.getConsoleSender(), false, false, false) && resourcePackCheckInterval > 0 && enableResourcePackCheck) {
            int intervalTicks = resourcePackCheckInterval * 60 * 20;
            resourcePackCheckTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(controller.getPlugin(), new RPCheckTask(controller), intervalTicks, intervalTicks);
        }
    }

    public String getDefaultResourcePackURL() {
        return resourcePack;
    }

    public boolean isResourcePackEnabledByDefault() {
        return isResourcePackEnabledByDefault;
    }
}