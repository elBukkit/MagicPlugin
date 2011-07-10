package com.elmakers.mine.bukkit.plugins.magic;

import java.util.HashMap;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageMask
{
    protected float overall = 0;
    protected HashMap<DamageCause, Float> specific = null;
    
    public DamageMask()
    {
        
    }
    
    public float getMask(DamageCause cause)
    {
        if (cause == null) return overall;
        
        if (specific == null) return 0;
        
        Float specificMask = specific.get(cause);
        return specificMask == null ? 0 : specificMask;
    }
    
    public void setMask(DamageCause cause, float amount)
    {
        if (cause == null) overall = amount;
        
        if (specific == null) 
        {
            if (amount == 0) return;
            
            specific = new HashMap<DamageCause, Float>();
        }
        
        if (amount == 0)
        {
            specific.remove(cause);
            if (specific.size() == 0) specific = null;
        }
    }
}
