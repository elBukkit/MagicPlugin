package com.elmakers.mine.bukkit.magic.command.config;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;

public abstract class HttpGet extends AsyncProcessor implements Runnable {
    protected final String url;
    protected final MagicController controller;
    protected final CommandSender sender;

    public HttpGet(MagicController controller, CommandSender sender, String url) {
        this.controller = controller;
        this.sender = sender;
        this.url = url;
    }

    @Override
    public void run() {
        HttpsURLConnection connection;
        try {
            URL url = new URL(this.url);
            connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/zip");
            connection.setDoInput(true);
            InputStream inputStream = connection.getInputStream();
            if (inputStream == null) {
                throw new IllegalArgumentException("Empty response");
            }
            processResponse(inputStream);
        } catch (Exception ex) {
            fail(controller.getMessages().get("commands.mconfig.example.fetch.error"), "Error processing HTTP request", ex);
            processResponse(null);
            return;
        }
    }

    protected abstract void processResponse(InputStream response);

    protected void success(List<String> messages, String message) {
        messages.add(message);
        success(controller, sender, StringUtils.join(messages, "\n"));
    }

    protected void success(String message) {
        success(controller, sender, message);
    }

    protected void fail(String message) {
        fail(controller, sender, message);
    }

    protected void fail(List<String> messages, String message, String errorMessage, Exception ex) {
        messages.add(message);
        fail(controller, sender, StringUtils.join(messages, "\n"), errorMessage, ex);
    }

    protected void fail(String message, String errorMessage, Exception ex) {
        fail(controller, sender, message, errorMessage, ex);
    }
}
