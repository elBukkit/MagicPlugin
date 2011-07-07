package com.elmakers.mine.bukkit.plugins.spells.utilities;

import org.bukkit.Server;
import org.bukkit.World;

public class BukkitServer
{
    protected static Server instance;
    
    public static Server getInstance()
    {
        return instance;
    }
    
    public static void setInstance(Server server)
    {
        BukkitServer.instance = server;
    }
    
    public static World getWorld(String name)
    {
        Server server = getInstance();
        if (server == null) return null;
        
        return server.getWorld(name);
    }
}
