package com.elmakers.mine.bukkit.block;

import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;

import java.io.StringReader;
import java.util.logging.Level;

public class BlockSign extends BlockExtraData {
    protected String[] lines = new String[4];
    protected static Gson gson;

    public BlockSign(String[] lines) {
        for (int i = 0; i < lines.length && i < this.lines.length; i++) {
            this.lines[i] = lines[i];
        }
    }
    
    public BlockSign(Object tag) {
        for (int i = 0; i < 4; ++i) {
            String line = InventoryUtils.getMetaString(tag, "Text" + (i + 1));
            if (line != null && !line.startsWith("{")) {
                try {
                    if (gson == null) {
                        gson = new Gson();
                    }
                    JsonReader reader = new JsonReader(new StringReader(line));
                    reader.setLenient(true);
                    line = gson.fromJson(reader, String.class);
                    if (line == null) line = "";
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.WARNING, "Error loading sign data", ex);
                    line = "";
                }
            }
            this.lines[i] = line;
        }
    }

    @Override
    public BlockExtraData clone() {
        return new BlockSign(lines);
    }
}
