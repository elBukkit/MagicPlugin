package com.elmakers.mine.bukkit.magic.command.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class FetchExampleRunnable extends HttpGet {
    private final String exampleKey;
    private final boolean quiet;
    private final ExampleUpdatedCallback callback;
    private final long startTime;
    private NumberFormat fileSizeFormatter = new DecimalFormat("#0.00");

    public FetchExampleRunnable(MagicController controller, CommandSender sender, String exampleKey, String url) {
        this(controller, sender, exampleKey, url, null, false);
    }

    public FetchExampleRunnable(MagicController controller, CommandSender sender, String exampleKey, String url, ExampleUpdatedCallback callback, boolean quiet) {
        super(controller, sender, url);
        this.exampleKey = exampleKey;
        this.quiet = quiet;
        this.callback = callback;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void processResponse(InputStream response) {
        final Plugin plugin = controller.getPlugin();
        if (response == null) {
            call(plugin, callback);
            return;
        }
        byte[] buffer = new byte[2048];
        File examplesFolder = new File(plugin.getDataFolder(), "examples");
        examplesFolder = new File(examplesFolder, exampleKey);
        List<String> messages = new ArrayList<>();
        File buildFolder = new File(examplesFolder.getPath() + ".download");
        if (buildFolder.exists()) {
             ConfigurationUtils.deleteDirectory(buildFolder);
        }

        boolean success = true;
        String minRequiredVersion = "";
        int size = 0;
        try (ZipInputStream zip = new ZipInputStream(response)) {
            ZipEntry entry;
            boolean empty = true;
            boolean testedForRootFolder = false;
            String rootFolder = null;
            while ((entry = zip.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (!testedForRootFolder) {
                    testedForRootFolder = true;
                    if (entryName.contains("/")) {
                        rootFolder = StringUtils.split(entryName, "/")[0] + "/";
                    }
                }
                if (entry.isDirectory()) continue;
                if (rootFolder != null) {
                    entryName = entryName.replace(rootFolder, "");
                }
                File filePath = new File(buildFolder, entryName);
                File folder = filePath.getParentFile();
                if (folder != null) {
                    folder.mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(filePath);
                    BufferedOutputStream outputBuffer = new BufferedOutputStream(fos, buffer.length)) {
                    int len;
                    while ((len = zip.read(buffer)) > 0) {
                        outputBuffer.write(buffer, 0, len);
                        empty = false;
                        size += len;
                    }
                }

                if (entryName.equals("example.yml")) {
                    YamlConfiguration exampleConfiguration = new YamlConfiguration();
                    exampleConfiguration.load(filePath);
                    minRequiredVersion = exampleConfiguration.getString("min_version");
                    String[] minVersion = StringUtils.split(minRequiredVersion, ".");
                    try {
                        String[] pluginVersion = StringUtils.split(plugin.getDescription().getVersion(), "-");
                        pluginVersion = StringUtils.split(pluginVersion[0], ".");
                        for (int i = 0; i < minVersion.length; i++) {
                            if (i >= pluginVersion.length) {
                                success = false;
                                break;
                            }
                            int pluginVersionNumber = Integer.parseInt(pluginVersion[i]);
                            int minVersionNumber = Integer.parseInt(minVersion[i]);
                            if (minVersionNumber > pluginVersionNumber) {
                                success = false;
                                break;
                            }
                            if (pluginVersionNumber > minVersionNumber) {
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        controller.getLogger().log(Level.WARNING, "Failed to parse plugin version requirements in example " + exampleKey, ex);
                    }
                    if (!success) {
                        break;
                    }
                }
            }
            if (empty) {
                throw new IllegalArgumentException("Empty zip file");
            }
        } catch (Exception ex) {
            fail(messages, controller.getMessages().get("commands.mconfig.example.fetch.error"), "Error reading zip file", ex);
            ex.printStackTrace();
            call(plugin, callback);
            return;
        }

        if (success) {
            if (examplesFolder.exists()) {
                File backupFolder = new File(examplesFolder.getPath() + ".bak");
                if (backupFolder.exists()) {
                    if (!quiet) {
                        messages.add(controller.getMessages().get("commands.mconfig.example.fetch.overwrite_backup").replace("$backup", backupFolder.getName()));
                    }
                    ConfigurationUtils.deleteDirectory(backupFolder);
                    examplesFolder.renameTo(backupFolder);
                } else {
                    if (!quiet) {
                        messages.add(controller.getMessages().get("commands.mconfig.example.fetch.backup").replace("$backup", backupFolder.getName()));
                    }
                    examplesFolder.renameTo(backupFolder);
                }
            }
            buildFolder.renameTo(examplesFolder);
        } else {
            ConfigurationUtils.deleteDirectory(buildFolder);
            call(plugin, callback);
            String message = controller.getMessages().get("commands.mconfig.example.fetch.outdated")
                .replace("$example", exampleKey)
                .replace("$version", minRequiredVersion);
            message(messages, message);
            return;
        }

        try {
            response.close();
        } catch (IOException ex) {
            controller.getLogger().log(Level.WARNING, "Error closing http connection", ex);
        }

        File urlFile = new File(examplesFolder, "url.txt");
        if (urlFile.exists()) {
            messages.add(controller.getMessages().get("commands.mconfig.example.fetch.url_exists").replace("$example", exampleKey));
        } else {
            try (OutputStreamWriter writer =
             new OutputStreamWriter(new FileOutputStream(urlFile), StandardCharsets.UTF_8)) {
                writer.write(url);
                if (!quiet) {
                    messages.add(controller.getMessages().get("commands.mconfig.example.fetch.url_write").replace("$example", exampleKey));
                }
            } catch (IOException ex) {
                messages.add(controller.getMessages().get("commands.mconfig.example.fetch.url_write_failed").replace("$example", exampleKey));
                controller.getLogger().log(Level.WARNING, "Error writing url file to example " + urlFile.getAbsolutePath(), ex);
            }
        }

        String message = quiet ? controller.getMessages().get("commands.mconfig.example.fetch.success_quiet") : controller.getMessages().get("commands.mconfig.example.fetch.success");
        message = message.replace("$url", url).replace("$example", exampleKey);
        long timeSince = System.currentTimeMillis() - startTime;
        String sinceMessage = controller.getMessages().getTimeDescription(timeSince, "description", "cooldown");
        message = message.replace("$time", sinceMessage);
        String fileSize = "?";
        if (size > 0) {
            fileSize = fileSizeFormatter.format((double)size / 1024 / 1024);
        }
        message = message.replace("$size", fileSize);
        success(messages, message);
        call(plugin, callback);
    }

    private void call(Plugin plugin, ExampleUpdatedCallback callback) {
        if (callback != null) {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    callback.updated(true, exampleKey, url);
                }
            });
        }
    }
}
