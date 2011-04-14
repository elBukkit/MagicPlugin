package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.magic.Magic;
import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.magic.SpellProvider;
import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persistence.Persistence;
import com.elmakers.mine.bukkit.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.spells.Absorb;
import com.elmakers.mine.bukkit.spells.Alter;
import com.elmakers.mine.bukkit.spells.Arrow;
import com.elmakers.mine.bukkit.spells.Blink;
import com.elmakers.mine.bukkit.spells.Bridge;
import com.elmakers.mine.bukkit.spells.Construct;
import com.elmakers.mine.bukkit.spells.Cushion;
import com.elmakers.mine.bukkit.spells.Disintegrate;
import com.elmakers.mine.bukkit.spells.Elevate;
import com.elmakers.mine.bukkit.spells.Familiar;
import com.elmakers.mine.bukkit.spells.Fire;
import com.elmakers.mine.bukkit.spells.Fireball;
import com.elmakers.mine.bukkit.spells.Frost;
import com.elmakers.mine.bukkit.spells.Heal;
import com.elmakers.mine.bukkit.spells.Invincible;
import com.elmakers.mine.bukkit.spells.Lava;
import com.elmakers.mine.bukkit.spells.Manifest;
import com.elmakers.mine.bukkit.spells.Mine;
import com.elmakers.mine.bukkit.spells.Peek;
import com.elmakers.mine.bukkit.spells.Pillar;
import com.elmakers.mine.bukkit.spells.Recall;
import com.elmakers.mine.bukkit.spells.Recurse;
import com.elmakers.mine.bukkit.spells.Time;
import com.elmakers.mine.bukkit.spells.Torch;
import com.elmakers.mine.bukkit.spells.Tower;
import com.elmakers.mine.bukkit.spells.Transmute;
import com.elmakers.mine.bukkit.spells.Tree;
import com.elmakers.mine.bukkit.spells.Tunnel;
import com.elmakers.mine.bukkit.spells.Undo;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.bukkit.wands.WandProvider;
import com.elmakers.mine.bukkit.wands.Wands;
import com.elmakers.mine.bukkit.wands.spells.CycleMaterials;
import com.elmakers.mine.bukkit.wands.spells.CycleSpells;

public class MagicPlugin extends JavaPlugin implements SpellProvider, WandProvider
{
    private final MagicEntityListener entityListener = new MagicEntityListener();
    private final MagicPlayerListener playerListener = new MagicPlayerListener();

    private final Logger              log            = Logger.getLogger("Minecraft");
    private Magic                     magic          = null;
    private Wands                     wands          = null;

    protected Persistence             persistence    = null;
    protected PluginUtilities         utilities      = null;

    protected PluginCommand           castCommand;
    protected PluginCommand           spellsCommand;

    protected void initialize()
    {      
        magic = new Magic(getServer(), persistence, utilities);
        wands = new Wands(getServer(), persistence, utilities, magic);
 
        // Search for Spell providers
        PluginManager pm = this.getServer().getPluginManager();
        Plugin[] plugins = pm.getPlugins();
        for (Plugin plugin : plugins)
        {
            if (plugin instanceof SpellProvider)
            {
                SpellProvider provider = (SpellProvider)plugin;
                List<Spell> provided = provider.getSpells();
                if (provided != null)
                {
                    magic.addSpells(provided);
                }
            }
            if (plugin instanceof WandProvider)
            {
                WandProvider provider = (WandProvider)plugin;
                provider.addDefaultWands(wands);
            }
        }
 
        playerListener.setMagic(magic);
        entityListener.setMagic(magic);

        // setup commands
        castCommand = utilities.getPlayerCommand("cast", "Cast spells by name", "<spellname>");
        spellsCommand = utilities.getPlayerCommand("spells", "List spells you know", null);

        castCommand.bind("onCast");
        spellsCommand.bind("onSpells");
    }

    public void listCategories(Player player)
    {
        HashMap<String, Integer> spellCounts = new HashMap<String, Integer>();
        List<String> spellGroups = new ArrayList<String>();
        List<SpellVariant> spellVariants = persistence.getAll(SpellVariant.class);

        for (SpellVariant spell : spellVariants)
        {
            if (!spell.hasSpellPermission(player))
            {
                continue;
            }

            for (String category : spell.getTags())
            {
                Integer spellCount = spellCounts.get(category);
                if (spellCount == null || spellCount == 0)
                {
                    spellCounts.put(category, 1);
                    spellGroups.add(category);
                }
                else
                {
                    spellCounts.put(category, spellCount + 1);
                }
            }
        }
        if (spellGroups.size() == 0)
        {
            player.sendMessage("You don't know any spells");
            return;
        }

        Collections.sort(spellGroups);
        for (String group : spellGroups)
        {
            player.sendMessage(group + " [" + spellCounts.get(group) + "]");
        }
    }

    public void listSpells(Player player)
    {
        List<SpellVariant> spellVariants = persistence.getAll(SpellVariant.class);
        Collections.sort(spellVariants);

        for (SpellVariant spell : spellVariants)
        {
            if (spell.hasSpellPermission(player))
            {
                player.sendMessage(" " + spell.getName() + " : " + spell.getDescription());
            }
        }
    }

    public void listSpellsByCategory(Player player, String category)
    {
        List<SpellVariant> categorySpells = new ArrayList<SpellVariant>();
        List<SpellVariant> spellVariants = persistence.getAll(SpellVariant.class);
        for (SpellVariant spell : spellVariants)
        {
            if (spell.hasTag(category) && spell.hasSpellPermission(player))
            {
                categorySpells.add(spell);
            }
        }

        if (categorySpells.size() == 0)
        {
            player.sendMessage("You don't know any spells");
            return;
        }

        Collections.sort(categorySpells);
        for (SpellVariant spell : categorySpells)
        {
            player.sendMessage(spell.getName() + " : " + spell.getDescription());
        }
    }

    public boolean onCast(Player player, String[] castParameters)
    {
        if (castParameters.length < 1)
        {
            return false;
        }

        String spellName = castParameters[0];
        String[] parameters = new String[castParameters.length - 1];
        for (int i = 1; i < castParameters.length; i++)
        {
            parameters[i - 1] = castParameters[i];
        }
        
        return magic.cast(player, spellName, parameters);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        return utilities.dispatch(this, sender, cmd.getName(), args);
    }

    public void onDisable()
    {
        magic.clear();
    }

    public void onEnable()
    {
        Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
        if (checkForPersistence != null)
        {
            PersistencePlugin plugin = (PersistencePlugin) checkForPersistence;
            persistence = plugin.getPersistence();
            utilities = plugin.createUtilities(this);
        }
        else
        {
            log.warning("The Magic plugin depends on Persistence");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initialize();

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);

        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);

        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
    }

    public boolean onSpells(Player player, String[] parameters)
    {
        listSpells(player);

        return true;
    }

    public List<Spell> getSpells()
    {
        List<Spell> spells = new ArrayList<Spell>();

        spells.add(new Absorb());
        spells.add(new Alter());
        spells.add(new Arrow());
        spells.add(new Blink());
        spells.add(new Bridge());
        spells.add(new Construct());
        spells.add(new Cushion());
        spells.add(new Disintegrate());
        spells.add(new Elevate());
        spells.add(new Familiar());
        spells.add(new Fireball());
        spells.add(new Fire());
        spells.add(new Frost());
        spells.add(new Heal());
        spells.add(new Invincible());
        spells.add(new Lava());
        spells.add(new Manifest());
        spells.add(new Mine());
        spells.add(new Peek());
        spells.add(new Pillar());
        spells.add(new Recall());
        spells.add(new Recurse());
        spells.add(new Time());
        spells.add(new Torch());
        spells.add(new Tower());
        spells.add(new Transmute());
        spells.add(new Tree());
        spells.add(new Tunnel());
        spells.add(new Undo());
        
        // Wand
        spells.add(new CycleSpells(wands));
        spells.add(new CycleMaterials(wands));
        
        // WIP!
        // spells.add(new FlingSpell());
        // spells.add(new StairsSpell());
           
        return spells;
    }

    public void addDefaultWands(Wands wands)
    {
        
    }
}
