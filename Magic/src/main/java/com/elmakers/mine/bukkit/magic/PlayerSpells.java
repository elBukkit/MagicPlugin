package com.elmakers.mine.bukkit.magic;

import java.util.HashMap;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persisted.Persistence;
import com.elmakers.mine.bukkit.utilities.UndoQueue;

public class PlayerSpells
{
    public PlayerSpells(Player player, Magic magic, Persistence persistence)
    {
        this.magic = magic;
        this.persistence = persistence;
        this.player = player;
        
        // TODO: get from player permissions;
        int undoQueueDepth = 256;
        
        undoQueue = new UndoQueue();
        undoQueue.setMaxSize(undoQueueDepth);
    }

    public UndoQueue getUndoQueue()
    {
        return undoQueue;
    }
    
    public SpellVariant get(String spellName)
    {
        SpellVariant variant = persistence.get(spellName, SpellVariant.class);
        
        if (variant != null && variant.hasSpellPermission(player))
        {
            return variant;
        }
        
        return null;
    }
    
    public boolean cast(String spellName)
    {
        SpellVariant variant = get(spellName);
        if (variant == null)
        {
            return false;
        }
        Spell spell = getSpell(variant.getSpell());
        if (spell == null)
        {
            return false;
        }
        return variant.cast(spell);
    }
    
    protected Spell getSpell(String spellName)
    {
        Spell spell = spells.get(spellName);
        if (spell == null)
        {
            
        }
        
        return spell;
    }

    protected HashMap<String, Spell> spells      = new HashMap<String, Spell>();
    protected UndoQueue              undoQueue   = null;
    protected Player                 player      = null;
    protected Persistence            persistence = null;
    protected Magic                  magic       = null;
}
