package com.elmakers.mine.bukkit.magic;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class PhysicsHandler implements Listener {
	private final MagicController controller;
	private long timeout = 0;

	public PhysicsHandler(MagicController controller)
	{
		this.controller = controller;
	}
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (!allowPhysics(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}
	
	protected boolean allowPhysics(Block block)
	{
		if (timeout == 0) {
			controller.unregisterPhysicsHandler(this);
			return true;
		}
		if (System.currentTimeMillis() > timeout) {
			controller.unregisterPhysicsHandler(this);
			timeout = 0;
		}
		return false;
	}

    public void setInterval(long interval)
    {
        this.timeout = System.currentTimeMillis() + interval;
    }
}
