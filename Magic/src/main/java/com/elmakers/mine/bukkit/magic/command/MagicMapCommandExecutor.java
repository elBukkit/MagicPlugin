package com.elmakers.mine.bukkit.magic.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.InflaterInputStream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.maps.MapController;
import com.elmakers.mine.bukkit.api.maps.URLMap;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicMapCommandExecutor extends MagicMapExecutor {
    public MagicMapCommandExecutor(MagicAPI api) {
        super(api, "mmap");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode()))
        {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage("Usage: mmap [list|give|load|slice|import|player|fix|restore|unnamed|name]");
            return true;
        }

        String subCommand = args[0];
        if (!api.hasPermission(sender, "Magic.commands.mmap." + subCommand))
        {
            sendNoPermission(sender);
            return true;
        }
        World world = null;
        if (sender instanceof Player)
        {
            world = ((Player)sender).getWorld();
        }
        else
        {
            world = Bukkit.getWorlds().get(0);
        }
        if (subCommand.equalsIgnoreCase("list"))
        {
            String keyword = "";
            for (int i = 1; i < args.length; i++)
            {
                if (i != 1) keyword = keyword + " ";
                keyword = keyword + args[i];
            }
            onMapList(sender, keyword);
        }
        else if (subCommand.equalsIgnoreCase("give"))
        {
            if (args.length == 1)
            {
                sender.sendMessage("Usage: mmap give <player> [id]");
                return true;
            }
            Player recipient = null;
            String mapId = null;
            if (args.length == 2)
            {
                mapId = args[1];
            }
            else if (args.length > 2)
            {
                mapId = args[2];
            }
            if (args.length == 2 && sender instanceof Player)
            {
                recipient = (Player)sender;
            }
            else if (args.length > 2)
            {
                recipient = CompatibilityLib.getDeprecatedUtils().getPlayer(args[1]);
            }

            if (recipient == null)
            {
                if (args.length > 2)
                {
                    sender.sendMessage("Unknown player: " + args[1]);
                    return true;
                }
                else
                {
                    sender.sendMessage("Console usage: mmap give [player] [id]");
                    return true;
                }
            }

            Short parsedId = null;
            if (mapId != null)
            {
                try {
                    parsedId = Short.parseShort(mapId);
                } catch (Exception ignored) {

                }
            }
            if (parsedId == null)
            {
                sender.sendMessage("Invalid map id, expecting integer: " + mapId);
                return true;
            }
            onMapGive(sender, recipient, parsedId);
        }
        else if (subCommand.equalsIgnoreCase("remove"))
        {
            if (args.length == 1)
            {
                sender.sendMessage("Usage: mmap delete [id]");
                return true;
            }
            String mapId = args[1];
            Short parsedId = null;
            if (mapId != null)
            {
                try {
                    parsedId = Short.parseShort(mapId);
                } catch (Exception ignored) {

                }
            }
            if (parsedId == null)
            {
                sender.sendMessage("Invalid map id, expecting integer: " + mapId);
                return true;
            }
            MapController maps = api.getController().getMaps();
            if (maps.remove(parsedId))
            {
                sender.sendMessage("Unregistered map id: " + parsedId);
            }
            else
            {
                sender.sendMessage("Map id " + parsedId + " is not registered");
            }
        }
        else if (subCommand.equalsIgnoreCase("player"))
        {
            if (args.length <= 1) {
                sender.sendMessage("Usage: mmap player <name>");
                return true;
            }

            String playerName = args[1];
            onMapPlayer(sender, world, playerName);
        }
        else if (subCommand.equalsIgnoreCase("fix"))
        {
            int limit = 100;
            if (args.length > 1)
            {
                try {
                    limit = Integer.parseInt(args[1]);
                } catch (Exception ignored) {

                }
            }
            onMapFix(sender, world, limit);
        }
        else if (subCommand.equalsIgnoreCase("name"))
        {
            if (args.length <= 1) {
                sender.sendMessage("Usage: mmap name <name>");
            }
            String mapName = args[1];
            for (int i = 2; i < args.length; i++) {
                mapName = mapName + " " + args[i];
            }
            onMapName(sender, mapName);
        }
        else if (subCommand.equalsIgnoreCase("unnamed"))
        {
            onMapUnnamed(sender);
        }
        else if (subCommand.equalsIgnoreCase("restore"))
        {
            int startingId = 1;
            if (args.length > 1)
            {
                try {
                    startingId = Integer.parseInt(args[1]);
                } catch (Exception ignored) {
                }
            }
            onMapRestore(sender, world, startingId);
        }
        else if (subCommand.equalsIgnoreCase("load"))
        {
            if (args.length == 1)
            {
                sender.sendMessage("Usage: mmap load <file/url> [name] [width] [height] [x] [y]");
                return true;
            }
            int width = 0;
            int height = 0;
            int x = 0;
            int y = 0;
            Integer priority = null;
            String mapName = null;

            if (args.length > 2)
            {
                mapName = args[2];
            }
            if (args.length > 3)
            {
                try {
                    width = Integer.parseInt(args[3]);
                } catch (Exception ex) {
                    sender.sendMessage("Invalid width: " + args[3]);
                }
            }
            if (args.length > 4)
            {
                try {
                    height = Integer.parseInt(args[4]);
                } catch (Exception ex) {
                    sender.sendMessage("Invalid height: " + args[4]);
                }
            }

            if (args.length > 5)
            {
                try {
                    x = Integer.parseInt(args[5]);
                } catch (Exception ex) {
                    sender.sendMessage("Invalid x: " + args[5]);
                }
            }

            if (args.length > 6)
            {
                try {
                    y = Integer.parseInt(args[6]);
                } catch (Exception ex) {
                    sender.sendMessage("Invalid y: " + args[6]);
                }
            }
            onMapLoad(sender, world, args[1], mapName, width, height, x, y, priority);
        }
        else if (subCommand.equalsIgnoreCase("slice"))
        {
            if (args.length == 1) {
                sender.sendMessage("Usage: mmap slice <file/url> [name] [horizontal slices] [vertical slices]");
                return true;
            }
            int xSlices = 0;
            int ySlices = 0;
            Integer priority = null;
            String mapName = null;

            if (args.length > 2) {
                mapName = args[2];
            }
            if (args.length > 3) {
                try {
                    xSlices = Integer.parseInt(args[3]);
                } catch (Exception ex) {
                    sender.sendMessage("Invalid horizontal slices: " + args[3]);
                }
            }
            if (args.length > 4) {
                try {
                    ySlices = Integer.parseInt(args[4]);
                } catch (Exception ex) {
                    sender.sendMessage("Invalid vertical slices: " + args[4]);
                }
            }
            onMapSlice(sender, world, args[1], mapName, xSlices, ySlices, priority);
        }
        else if (subCommand.equalsIgnoreCase("import"))
        {
            String path = "plugins/Pixelator/renderers.cache";
            if (args.length > 1)
            {
                path = args[1];
            }
            onMapImport(sender, world, path);
        }
        else
        {
            sender.sendMessage("Usage: mmap [list|remove|give|load|import]");
        }
        return true;
    }

    protected void onMapLoad(CommandSender sender, World world, String url, String mapName, int width, int height, int x, int y, Integer priority)
    {
        MapController maps = api.getController().getMaps();
        ItemStack item = maps.getURLItem(world.getName(), url, mapName, x, y, width, height, priority);
        if (item == null)
        {
            sender.sendMessage("Failed to load map: " + url);
            return;
        }
        int mapId = CompatibilityLib.getInventoryUtils().getMapId(item);
        sender.sendMessage("Loaded map id " + mapId);
        if (sender instanceof Player)
        {
            ItemStack mapItem = maps.getMapItem((short)mapId);
            api.giveItemToPlayer((Player)sender, mapItem);
        }
    }

    protected void onMapSlice(CommandSender sender, World world, String url, String mapName, int xSlices, int ySlices, Integer priority)
    {
        if (xSlices * ySlices > 100) {
            sender.sendMessage(ChatColor.RED + "Can't make more than 100 maps at a time, seems like a bad idea");
            return;
        }
        if (xSlices < 1 || ySlices < 1) {
            sender.sendMessage(ChatColor.RED + "Invalid x or y slices");
            return;
        }
        MapController maps = api.getController().getMaps();
        List<ItemStack> items = maps.getURLSlices(world.getName(), url, mapName, xSlices, ySlices, priority);
        if (items.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Failed to slice map: " + url);
            return;
        }
        List<String> ids = new ArrayList<>();
        for (ItemStack item : items) {
            int mapId = CompatibilityLib.getInventoryUtils().getMapId(item);
            ids.add(Integer.toString(mapId));
            if (sender instanceof Player) {
                ItemStack mapItem = maps.getMapItem((short)mapId, false);
                api.giveItemToPlayer((Player)sender, mapItem);
            }
        }
        sender.sendMessage("Loaded map ids " + StringUtils.join(ids, ","));
    }

    protected void onMapPlayer(CommandSender sender, World world, String playerName)
    {
        MapController maps = api.getController().getMaps();
        ItemStack item = maps.getPlayerPortrait(world.getName(), playerName, null, "Photo of " + playerName);
        if (item == null)
        {
            sender.sendMessage("Failed to load player skin: " + playerName);
            return;
        }
        int mapId = CompatibilityLib.getInventoryUtils().getMapId(item);
        sender.sendMessage("Loaded map id " + mapId + " as player " + playerName);
        if (sender instanceof Player)
        {
            ItemStack mapItem = maps.getMapItem((short)mapId);
            api.giveItemToPlayer((Player)sender, mapItem);
        }
    }

    protected void onMapImport(CommandSender sender, World world, String rendererFile)
    {
        File baseFolder = api.getPlugin().getDataFolder().getParentFile().getParentFile();
        File pixelFile = new File(baseFolder, rendererFile);

        int imported = 0;
        int skipped = 0;
        MapController maps = api.getController().getMaps();
        try {
            InflaterInputStream in = new InflaterInputStream(new FileInputStream(pixelFile));
            ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
            byte[] readBuffer = new byte[16 * 1024];

            int len;
            while ((len = in.read(readBuffer)) > 0)
            {
                tempStream.write(readBuffer, 0, len);
            }

            in.close();
            String cacheData = new String(tempStream.toByteArray(), "UTF-8");
            if (cacheData.length() == 0) {
                sender.sendMessage("There were no images founds in the render cache file");
                return;
            }

            String[] renderConfigs = StringUtils.split(cacheData, '#');
            for (int i = 0; i < renderConfigs.length; i++) {
                String renderConfig = renderConfigs[i];
                String[] renderPieces = StringUtils.split(renderConfig, '@');
                short id = Short.parseShort(renderPieces[0]);
                String filename = renderPieces[1];
                if (maps.hasMap(id)) {
                    skipped++;
                } else {
                    imported++;
                    sender.sendMessage("Importing: " + filename);
                    String mapName = filename;
                    int lastIndex = mapName.lastIndexOf('/');
                    if (lastIndex > 0 && lastIndex < mapName.length() - 1)
                    {
                        mapName = mapName.substring(lastIndex + 1);
                    }
                    maps.loadMap(world.getName(), id, filename, mapName, 0, 0, 0, 0, null);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage("There was an error reading the render cache file");
            return;
        }
        sender.sendMessage("Imported " + imported + " images, skipped " + skipped);
    }

    protected void onMapFix(CommandSender sender, World world, int retries)
    {
        sender.sendMessage(ChatColor.AQUA + "Fixing maps, using up to " + ChatColor.DARK_AQUA + retries
                + ChatColor.AQUA + " ids at a time.");

        MapController mapController = api.getController().getMaps();
        List<URLMap> maps = mapController.getAll();
        int fixed = 0;
        int notFixed = 0;
        int skipped = 0;
        for (URLMap map : maps)
        {
            if (map.isEnabled()) {
                skipped++;
            } else {
                if (map.fix(world, retries)) {
                    fixed++;
                } else {
                    notFixed++;
                }
            }
        }

        mapController.save();
        sender.sendMessage(ChatColor.AQUA + "Fixed " + ChatColor.DARK_AQUA + fixed
                + ChatColor.AQUA + " and skipped " + ChatColor.DARK_AQUA + skipped + ChatColor.AQUA + " maps");
        if (notFixed > 0) {
            sender.sendMessage(ChatColor.RED + "There are still " + ChatColor.DARK_RED + notFixed + ChatColor.RED + " maps disabled, you may want to try running this command again.");
        }
    }

    protected void onMapUnnamed(CommandSender sender)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command only works in-game");
            return;
        }
        Player player = (Player)sender;
        MapController mapController = api.getController().getMaps();
        List<URLMap> maps = mapController.getAll();
        for (URLMap map : maps) {
            if (map.getName() == null) {
                ItemStack newMap = controller.getMap(map.getId());
                api.giveItemToPlayer(player, newMap);
                sender.sendMessage("Found unnamed map id " + map.getId() + " with url " + ChatColor.AQUA + map.getURL());
                return;
            }
        }
        sender.sendMessage("There are no unnamed maps!");
    }

    protected void onMapName(CommandSender sender, String mapName)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command only works in-game");
            return;
        }
        Player player = (Player)sender;
        ItemStack currentMap = player.getInventory().getItemInMainHand();
        if (currentMap == null || !DefaultMaterials.isFilledMap(currentMap.getType())) {
            sender.sendMessage("You must be holding a map");
            return;
        }
        int mapId = CompatibilityLib.getInventoryUtils().getMapId(currentMap);
        MapController mapController = api.getController().getMaps();
        URLMap map = mapController.getMap((short)mapId);
        if (map == null) {
            sender.sendMessage("Map id " + mapId + " is not registered");
            return;
        }
        map.setName(mapName);
        mapController.save();
        sender.sendMessage("Renamed map id " + map.getId() + " to " + map.getName());
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
    }

    protected void onMapRestore(CommandSender sender, World world, int mapId)
    {
        final boolean backwards = mapId > 1;
        String direction = ChatColor.YELLOW + " " + (backwards ? "moving backward" : "moving forward");
        sender.sendMessage(ChatColor.AQUA + "Restoring maps, starting at id " + ChatColor.DARK_AQUA + mapId + direction);

        // Getting dirty now!
        MapController apiController = api.getController().getMaps();
        com.elmakers.mine.bukkit.maps.MapController mapController = (com.elmakers.mine.bukkit.maps.MapController)apiController;
        List<URLMap> maps = mapController.getAll();
        Set<String> urls = new HashSet<>();
        Set<Integer> ids = new HashSet<>();
        int maxId = 0;
        final String skinURL = "http://skins.minecraft.net/MinecraftSkins/";
        final String alternateSkinURL = "http://s3.amazonaws.com/MinecraftSkins/";
        for (URLMap map : maps) {
            maxId = Math.max(maxId, map.getId());
            if (map.getURL().startsWith(skinURL) && map.getName() == null) {
                map.setName("Photo of " + map.getURL().replace(skinURL, "").replace(".png", ""));
                sender.sendMessage("Added name to " + map.getName());
            } else if (map.getURL().startsWith(alternateSkinURL) && map.getName() == null) {
                map.setName("Photo of " + map.getURL().replace(alternateSkinURL, "").replace(".png", ""));
                sender.sendMessage("Added name to " + map.getName());
            }
            urls.add(map.getURL());
            ids.add(map.getId());
        }
        if (!backwards) {
            mapId = maxId;
        }
        int addedFiles = 0;
        File[] cacheFiles = mapController.getCacheFolder().listFiles();
        Arrays.sort(cacheFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2)
            {
                if (backwards) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
                return Long.compare(f1.lastModified(), f2.lastModified());
            } });
        for (File cacheFile : cacheFiles) {
            try {
                while (mapId > 0 && ids.contains(mapId)) mapId--;
                if (mapId <= 0) break;
                if (cacheFile.getName().startsWith(".") || cacheFile.isDirectory()) continue;
                String url = URLDecoder.decode(cacheFile.getName(), "UTF-8");
                if (urls.contains(url)) {
                    sender.sendMessage("Skipping " + url);
                    continue;
                }

                String name = null;
                int x = 0;
                int y = 0;
                int width = 0;
                int height = 0;
                Integer xOverlay = null;
                Integer yOverlay = null;

                if (url.startsWith(skinURL) || url.startsWith(alternateSkinURL)) {
                    name = "Photo of " + url.replace(skinURL, "").replace(alternateSkinURL, "").replace(".png", "");
                    x = 8;
                    y = 8;
                    width = 8;
                    height = 8;
                    xOverlay = 40;
                    yOverlay = 8;

                    sender.sendMessage("Added " + mapId + " as player skin: " + name);
                } else {
                    sender.sendMessage("Added " + mapId + " as: " + url);
                }
                mapController.get(world.getName(), (short)mapId, url, name, x, y, xOverlay, yOverlay, width, height, null);

                addedFiles++;
                if (backwards) {
                    mapId--;
                } else {
                    mapId++;
                }
            } catch (UnsupportedEncodingException ex) {
                sender.sendMessage("Error decoding: " + cacheFile.getName());
            }
        }

        mapController.save();
        sender.sendMessage(ChatColor.AQUA + "Restored " + ChatColor.DARK_AQUA + addedFiles);
    }

    protected void onMapGive(CommandSender sender, Player recipient, short mapId)
    {
        MapController maps = api.getController().getMaps();
        ItemStack mapItem = maps.getMapItem(mapId);
        if (mapItem == null)
        {
            sender.sendMessage("Failed to load map id " + mapId);
            return;
        }

        if (sender != recipient)
        {
            String mapLabel = "(No Name)";
            ItemMeta meta = mapItem.getItemMeta();
            if (meta != null)
            {
                mapLabel = meta.getDisplayName();
            }
            sender.sendMessage("Gave map " + mapLabel + " (" + mapId + ") to " + recipient.getName());
        }

        api.giveItemToPlayer(recipient, mapItem);
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.mmap")) return options;

        if (args.length == 1) {
            options.add("give");
            options.add("import");
            options.add("list");
            options.add("load");
            options.add("slice");
            options.add("remove");
            options.add("player");
            options.add("fix");
            options.add("restore");
            options.add("name");
            options.add("unnamed");
        } else if (args.length == 2) {
            if (args[0].equals("give")) {
                options.addAll(api.getPlayerNames());
            } else if (args[0].equals("slice") || args[1].equals("load")) {
                options.add("http");
            }
        } else if (args.length == 3) {
            if (args[0].equals("slice") || args[1].equals("load")) {
                options.add("Map_Name");
            }
        } else if (args.length == 4) {
            if (args[0].equals("slice")) {
                options.add("1");
                options.add("2");
                options.add("3");
                options.add("4");
                options.add("5");
            } else if (args[0].equals("load")) {
                options.add("128");
                options.add("256");
                options.add("512");
                options.add("1024");
            }
        } else if (args.length == 5) {
            if (args[0].equals("slice")) {
                options.add("1");
                options.add("2");
                options.add("3");
                options.add("4");
                options.add("5");
            } else if (args[0].equals("load")) {
                options.add("128");
                options.add("256");
                options.add("512");
                options.add("1024");
            }
        }
        return options;
    }
}
