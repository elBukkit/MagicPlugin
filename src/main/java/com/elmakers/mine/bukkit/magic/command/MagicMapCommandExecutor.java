package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.maps.MapController;
import com.elmakers.mine.bukkit.api.maps.URLMap;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.InflaterInputStream;

public class MagicMapCommandExecutor extends MagicMapExecutor {
	public MagicMapCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mmap"))
        {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0)
		{
            sender.sendMessage("Usage: mmap [list|give|load|import]");
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
            org.bukkit.Bukkit.getLogger().info("Args: " + args.length + ": " + keyword);
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
                recipient = Bukkit.getPlayer(args[1]);
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
                } catch (Exception ex) {

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
                } catch (Exception ex) {

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
            if (args.length == 1) {
                sender.sendMessage("Usage: mmap player <name>");
                return true;
            }

            String playerName = args[1];
            onMapPlayer(sender, world, playerName);
        }
        else if (subCommand.equalsIgnoreCase("load"))
        {
            if (args.length == 1)
            {
                sender.sendMessage("Usage: mmap load <file/url> [width] [height]");
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
            onMapLoad(sender, world, args[1], mapName, width, height, x, y, priority);
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
        short mapId = item.getDurability();
        sender.sendMessage("Loaded map id " + mapId);
        if (sender instanceof Player)
        {
            ItemStack mapItem = maps.getMapItem(mapId);
            api.giveItemToPlayer((Player)sender, mapItem);
        }
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
        short mapId = item.getDurability();
        sender.sendMessage("Loaded map id " + mapId);
        if (sender instanceof Player)
        {
            ItemStack mapItem = maps.getMapItem(mapId);
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
            if (cacheData == null || cacheData.length() == 0) {
                sender.sendMessage("There were no images founds in the render cache file");
                return;
            }

            String[] renderConfigs = cacheData.split("#");
            for (int i = 0; i < renderConfigs.length; i++) {
                String renderConfig = renderConfigs[i];
                String[] renderPieces = renderConfig.split("@");
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
		List<String> options = new ArrayList<String>();
        if (!sender.hasPermission("Magic.commands.mmap")) return options;

		if (args.length == 1) {
            options.add("give");
            options.add("import");
            options.add("list");
            options.add("load");
            options.add("remove");
            options.add("player");
		}
		return options;
	}
}
