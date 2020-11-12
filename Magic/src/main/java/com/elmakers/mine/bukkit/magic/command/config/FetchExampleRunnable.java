package com.elmakers.mine.bukkit.magic.command.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class FetchExampleRunnable extends HttpGet {
    private final String exampleKey;

    public FetchExampleRunnable(MagicController controller, CommandSender sender, String exampleKey, String url) {
        super(controller, sender, url);
        this.exampleKey = exampleKey;
    }

    @Override
    public void processResponse(InputStream response) {
        byte[] buffer = new byte[2048];
        File examplesFolder = new File(controller.getPlugin().getDataFolder(), "examples");
        examplesFolder = new File(examplesFolder, exampleKey);
        List<String> messages = new ArrayList<>();

        if (examplesFolder.exists()) {
            File backupFolder = new File(examplesFolder.getPath() + ".bak");
            if (backupFolder.exists()) {
                messages.add(controller.getMessages().get("commands.mconfig.example.fetch.overwrite_backup").replace("$backup", backupFolder.getName()));
                ConfigurationUtils.deleteDirectory(backupFolder);
                examplesFolder.renameTo(backupFolder);
            } else {
                messages.add(controller.getMessages().get("commands.mconfig.example.fetch.backup").replace("$backup", backupFolder.getName()));
                examplesFolder.renameTo(backupFolder);
            }
        }

        // TODO: If this folder exists we need to rename it to .bak
        // If .bak exists, delete it recursively

        // Tell the user about both of these things

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
                File filePath = new File(examplesFolder, entryName);
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
                    }
                }
            }
            if (empty) {
                throw new IllegalArgumentException("Empty zip file");
            }
        } catch (Exception ex) {
            fail(messages, controller.getMessages().get("commands.mconfig.example.fetch.error"), "Error reading zip file", ex);
            ex.printStackTrace();
            return;
        }
        try {
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = controller.getMessages().get("commands.mconfig.example.fetch.success");
        message = message.replace("$url", url).replace("$example", exampleKey);
        success(messages, message);
    }
}
