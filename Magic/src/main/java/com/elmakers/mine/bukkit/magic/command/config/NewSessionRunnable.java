package com.elmakers.mine.bukkit.magic.command.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.google.gson.Gson;

public class NewSessionRunnable implements Runnable {
    private final MagicController controller;
    private final CommandSender sender;
    private final NewSessionRequest request;
    private final Gson gson;

    public NewSessionRunnable(MagicController controller, Gson gson, CommandSender sender, NewSessionRequest request) {
        this.controller = controller;
        this.gson = gson;
        this.sender = sender;
        this.request = request;
    }

    @Override
    public void run() {
        String newSessionRequest = gson.toJson(request);
        HttpsURLConnection connection;
        try {
            URL url = new URL(controller.getEditorURL() + "/session/new");
            connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(newSessionRequest.getBytes("UTF-8"));
            outputStream.close();

            BufferedReader responseInput = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            String response = "";
            while ((line = responseInput.readLine()) != null) {
                response += line;
            }

            NewSessionResponse newSessionResponse = gson.fromJson(response, NewSessionResponse.class);
            if (!newSessionResponse.isSuccess()) {
                success(controller.getMessages().get("commands.mconfig.editor.server_error")
                        .replace("$message", newSessionResponse.getMessage()));
            } else {
                String message = controller.getMessages().get("commands.mconfig.editor.new_session");
                message = message.replace("$url", controller.getEditorURL() + "/" + newSessionResponse.getSession());
                success(message);
            }
        } catch (Exception ex) {
            fail(controller.getMessages().get("commands.mconfig.editor.error"), "Error sending HTTP request", ex);
            return;
        }
    }

    protected void success(String message) {
        final Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(message);
            }
        });
    }

    protected void fail(String message, String errorMessage, Exception ex) {
        final Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(message);
                controller.getLogger().log(Level.WARNING, errorMessage, ex);
            }
        });
    }
}
