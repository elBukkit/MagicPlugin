package com.elmakers.mine.bukkit.magic;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.block.UndoList;

public class PhysicsHandler implements Listener {
	private final MagicController controller;
	private long timeout = 0;
	private long timeoutBuffer = 2000;

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
		long now = System.currentTimeMillis();
		if (now > timeout) {
			controller.unregisterPhysicsHandler(this);
			timeout = 0;
			return true;
		}

		BlockData registered = UndoList.getBlockData(block.getLocation());
		if (registered == null) {
			return true;
		}
		com.elmakers.mine.bukkit.api.block.UndoList registeredList = registered.getUndoList();
		if (registeredList != null && !registeredList.getApplyPhysics()) {
			timeout = Math.min(now + timeoutBuffer, timeout);
			return false;
		}

		return true;
	}

	public long getTimeout() {
		return timeout;
	}

    public void setInterval(long interval)
    {
        this.timeout = Math.max(this.timeout, System.currentTimeMillis() + interval);
    }
}
