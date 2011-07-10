package com.elmakers.mine.bukkit.plugins.magic.wand;

public class WandPermissions 
{
	private boolean use = false;
	private boolean admin = false;
	private boolean modify = false;
	
	public boolean canUse() 
	{
		return use || admin || modify;
	}
	
	public void setCanUse(boolean use) 
	{
		this.use = use;
		if (!use)
		{
			admin = false;
			modify = false;
		}
	}
	
	public boolean canAdminister() 
	{
		return admin;
	}
	
	public void setCanAdminister(boolean admin) 
	{
		this.admin = admin;
	}
	
	public boolean canModify() 
	{
		return modify || admin;
	}
	
	public void setCanModify(boolean modify) 
	{
		this.modify = modify;
		if (!modify)
		{
			admin = false;
		}
	}
}
