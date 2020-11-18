package com.elmakers.mine.bukkit.resourcepack;

import java.io.BufferedInputStream;
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

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.google.common.io.BaseEncoding;

public class ResourcePackUpdateRunnable implements Runnable {
    private final ResourcePackManager manager;
    private final ResourcePackResponse callback;
    private final ResourcePack resourcePack;
    private final boolean force;
    private final boolean filenameChanged;

    public ResourcePackUpdateRunnable(ResourcePackManager manager, ResourcePack resourcePack, ResourcePackResponse callback, boolean force, boolean filenameChanged) {
        this.manager = manager;
        this.resourcePack = resourcePack;
        this.callback = callback;
        this.force = force;
        this.filenameChanged = filenameChanged;
    }

    @Override
    public void run() {
        boolean success = true;
        final MagicController controller = manager.getController();
        final Plugin plugin = controller.getPlugin();
        final Server server = plugin.getServer();
        final boolean initialLoad = !resourcePack.isChecked();
        resourcePack.setChecked(true);
        final String finalResourcePack = resourcePack.getUrl();
        final long modifiedTimestamp = resourcePack.getModified().getTime();
        byte[] resourcePackHash = resourcePack.getHash();
        String currentHashString = resourcePack.getHashString();
        final List<String> responses = new ArrayList<>();
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
                    success = false;
                } else {
                    try {
                        tryParseDate = format.parse(lastModified);
                        hasModifiedTime = true;
                    } catch (ParseException dateFormat) {
                        success = false;
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
                    String newSourcePackHashString = BaseEncoding.base64().encode(resourcePackHash);

                    if (initialLoad) {
                        responses.add(ChatColor.GREEN + "Resource pack hash set to " + ChatColor.GRAY + newSourcePackHashString);
                    } else if (currentHashString != null && currentHashString.equals(newSourcePackHashString))  {
                        responses.add(ChatColor.GREEN + "Resource pack hash has not changed");
                    } else {
                        responses.add(ChatColor.YELLOW + "Resource pack hash changed, use " + ChatColor.AQUA + "/magic rpsend" + ChatColor.YELLOW + " to update connected players");
                    }

                    resourcePack.update(resourcePackHash, modifiedDate);
                    manager.saveResourcePacks();
                } else {
                    if (filenameChanged) {
                        responses.add(ChatColor.YELLOW + "Resource pack changed, use " + ChatColor.AQUA + "/magic rpsend" + ChatColor.YELLOW + " to update connected players");
                    } else {
                        responses.add(ChatColor.GREEN + "Resource pack has not changed, using hash " + currentHashString +  " (" + modifiedDate.getTime() + " <= " + modifiedTimestamp + ")");
                    }
                }
            } else {
                responses.add(ChatColor.RED + "Could not find resource pack at: " + ChatColor.DARK_RED + finalResourcePack);
                success = false;
            }
        }
        catch (Exception ex) {
            success = false;
            responses.add("An unexpected error occurred while checking your resource pack, cancelling checks until restart (see logs): " + ChatColor.DARK_RED + finalResourcePack);
            controller.getLogger().log(Level.WARNING,"Error checking resource pack: " + ex.getMessage());
        }

        if (plugin.isEnabled() && callback != null) {
            boolean finalSuccess = success;
            server.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    callback.finished(finalSuccess, responses, resourcePack);
                }
            });
        }
    }
}
