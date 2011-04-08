package com.elmakers.mine.bukkit.wands;

import org.bukkit.Server;

import com.elmakers.mine.bukkit.magic.Magic;

public class Wands
{
    private final Magic  magic;
    private final Server server;

    public Wands(Server server, Magic magic)
    {
        this.server = server;
        this.magic = magic;
    }
}
