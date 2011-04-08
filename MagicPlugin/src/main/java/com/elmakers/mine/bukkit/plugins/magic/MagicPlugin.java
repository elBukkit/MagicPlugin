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
import com.elmakers.mine.bukkit.spells.AbsorbSpell;
import com.elmakers.mine.bukkit.spells.AlterSpell;
import com.elmakers.mine.bukkit.spells.ArrowSpell;
import com.elmakers.mine.bukkit.spells.BlinkSpell;
import com.elmakers.mine.bukkit.spells.BridgeSpell;
import com.elmakers.mine.bukkit.spells.ConstructSpell;
import com.elmakers.mine.bukkit.spells.CushionSpell;
import com.elmakers.mine.bukkit.spells.DisintegrateSpell;
import com.elmakers.mine.bukkit.spells.ElevateSpell;
import com.elmakers.mine.bukkit.spells.FamiliarSpell;
import com.elmakers.mine.bukkit.spells.FireSpell;
import com.elmakers.mine.bukkit.spells.FireballSpell;
import com.elmakers.mine.bukkit.spells.FrostSpell;
import com.elmakers.mine.bukkit.spells.HealSpell;
import com.elmakers.mine.bukkit.spells.InvincibleSpell;
import com.elmakers.mine.bukkit.spells.LavaSpell;
import com.elmakers.mine.bukkit.spells.ManifestSpell;
import com.elmakers.mine.bukkit.spells.MineSpell;
import com.elmakers.mine.bukkit.spells.PeekSpell;
import com.elmakers.mine.bukkit.spells.PillarSpell;
import com.elmakers.mine.bukkit.spells.RecallSpell;
import com.elmakers.mine.bukkit.spells.RecurseSpell;
import com.elmakers.mine.bukkit.spells.TimeSpell;
import com.elmakers.mine.bukkit.spells.TorchSpell;
import com.elmakers.mine.bukkit.spells.TowerSpell;
import com.elmakers.mine.bukkit.spells.TransmuteSpell;
import com.elmakers.mine.bukkit.spells.TreeSpell;
import com.elmakers.mine.bukkit.spells.TunnelSpell;
import com.elmakers.mine.bukkit.spells.UndoSpell;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;

public class MagicPlugin extends JavaPlugin implements SpellProvider
{
    protected PluginCommand           castCommand;
    private final MagicEntityListener entityListener = new MagicEntityListener();
    private final Logger              log            = Logger.getLogger("Minecraft");
    protected Persistence             persistence    = null;
    private final MagicPlayerListener playerListener = new MagicPlayerListener();
    private final Magic               magic          = new Magic();
    protected PluginCommand           spellsCommand;
    protected PluginUtilities         utilities      = null;

    /*
     * Public API
     */
    
    public List<Spell> getSpells()
    {
        List<Spell> spells = new ArrayList<Spell>();

        spells.add(new AbsorbSpell());
        spells.add(new AlterSpell());
        spells.add(new ArrowSpell());
        spells.add(new BlinkSpell());
        spells.add(new BridgeSpell());
        spells.add(new ConstructSpell());
        spells.add(new CushionSpell());
        spells.add(new DisintegrateSpell());
        spells.add(new ElevateSpell());
        spells.add(new FamiliarSpell());
        spells.add(new FireballSpell());
        spells.add(new FireSpell());
        spells.add(new FrostSpell());
        spells.add(new HealSpell());
        spells.add(new InvincibleSpell());
        spells.add(new LavaSpell());
        spells.add(new ManifestSpell());
        spells.add(new MineSpell());
        spells.add(new PeekSpell());
        spells.add(new PillarSpell());
        spells.add(new RecallSpell());
        spells.add(new RecurseSpell());
        spells.add(new TimeSpell());
        spells.add(new TorchSpell());
        spells.add(new TowerSpell());
        spells.add(new TransmuteSpell());
        spells.add(new TreeSpell());
        spells.add(new TunnelSpell());
        spells.add(new UndoSpell());
        
        // WIP!
        // spells.add(new FlingSpell());
        // spells.add(new StairsSpell());
        
        
        return spells;
    }

    protected void initialize()
    {      
        magic.initialize(getServer(), persistence, utilities);
 
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
 
        return magic.cast(player, spellName);
        
        // TODO: Parameters!
        /*
    
        String[] parameters = new String[castParameters.length - 1];
        for (int i = 1; i < castParameters.length; i++)
        {
            parameters[i - 1] = castParameters[i];
        }

        SpellVariant spell = magic.getSpell(spellName, player);
        if (spell == null)
        {
            return false;
        }

        spell.cast(null, parameters)
        spells.castSpell(spell, parameters, player);
        */
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

}
