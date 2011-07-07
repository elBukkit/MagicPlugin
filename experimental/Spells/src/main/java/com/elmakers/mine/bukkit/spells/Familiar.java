package com.elmakers.mine.bukkit.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.player.PlayerEvent;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.magic.SpellEventType;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Familiar extends Spell
{
    private final String                        DEFAULT_FAMILIARS = "chicken,sheep,cow,pig";
    private final String                        DEFAULT_MONSTERS  = "creeper,pigzombie,skeleton,spider,squid,zombie,ghast,giant";
    private final String                        ALL_FAMILIARS  =    "chicken,sheep,cow,pig,creeper,pigzombie,skeleton,spider,squid,zombie,ghast,giant";
    private List<String>                        defaultFamiliars  = new ArrayList<String>();
    private List<String>                        defaultMonsters   = new ArrayList<String>();
    private List<String>                        allFamiliars      = new ArrayList<String>();

    private final HashMap<String, Creature>     familiars         = new HashMap<String, Creature>();
    private int                                 maxFamiliars      = 1;

    private final Random                        rand              = new Random();

    protected void checkListener()
    {
        boolean anyFamiliars = false;
        for (Creature familiar : familiars.values())
        {
            if (familiar != null)
            {
                anyFamiliars = true;
                break;
            }
        }
        if (anyFamiliars)
        {
            magic.registerEvent(SpellEventType.PLAYER_QUIT, this);
        }
        else
        {
            magic.unregisterEvent(SpellEventType.PLAYER_QUIT, this);
        }
    }

    @Override
    public String getDescription()
    {
        return "Create an animal familiar";
    }

    @Override
    public String getName()
    {
        return "familiar";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        if (familiars.size() == 1 && maxFamiliars == 1)
        {
            Creature familiar = familiars.values().iterator().next();
            if (familiar != null)
            {
                familiar.setHealth(0);
            }
            familiars.clear();
            
            castMessage(player, "You release your familiar");
            checkListener();
            return true;
        }
        
        targeting.noTargetThrough(Material.STATIONARY_WATER);
        targeting.noTargetThrough(Material.WATER);

        Block target = targeting.getTargetBlock();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        target = target.getFace(BlockFace.UP);

        CreatureType famType = CreatureType.CHICKEN;
        if (parameters.hasFlag("any"))
        {
            int randomFamiliar = rand.nextInt(allFamiliars.size());
            famType = CreatureType.fromName(allFamiliars.get(randomFamiliar));
        }
        else if (parameters.hasFlag("monster"))
        {
            int randomFamiliar = rand.nextInt(defaultMonsters.size());
            famType = CreatureType.fromName(defaultMonsters.get(randomFamiliar));
        }
        else 
        {
            ParameterData typeParameter = parameters.get("type");
            if (typeParameter != null)
            {
                famType = CreatureType.fromName(typeParameter.getValue());
            }
        }
    
        if (famType == CreatureType.CHICKEN)
        {
            int randomFamiliar = rand.nextInt(defaultFamiliars.size());
            famType = CreatureType.fromName(defaultFamiliars.get(randomFamiliar));
        }

        if (target.getType() == Material.WATER || target.getType() == Material.STATIONARY_WATER)
        {
            famType = CreatureType.SQUID;
        }

        Creature entity = (Creature)target.getLocation().getWorld().spawnCreature(target.getLocation(), famType);
        if (entity == null)
        {
            sendMessage(player, "Your familiar is DOA");
            return false;
        }
        castMessage(player, "You create a " + famType.name().toLowerCase() + " familiar!");
        familiars.put("default", entity);
        checkListener();
        return true;
    
    }

    @Override
    public void onLoad()
    {
        defaultFamiliars = csvParser.parseStrings(DEFAULT_FAMILIARS);
        allFamiliars = csvParser.parseStrings(ALL_FAMILIARS);
        defaultMonsters = csvParser.parseStrings(DEFAULT_MONSTERS);
    }

    @Override
    public void onPlayerQuit(PlayerEvent event)
    {
        for (Creature familiar : familiars.values())
        {
            familiar.setHealth(0);
        }
        familiars.clear();
        checkListener();
    }

}
