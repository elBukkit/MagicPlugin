package com.elmakers.mine.bukkit.plugins.spells;

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
import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persistence.Persistence;
import com.elmakers.mine.bukkit.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;

public class SpellsPlugin extends JavaPlugin
{
    protected PluginCommand            castCommand;

    /*
     * Plugin interface
     */

    private final SpellsEntityListener entityListener = new SpellsEntityListener();

    private final Logger               log            = Logger.getLogger("Minecraft");

    /*
     * Private data
     */
    protected Persistence              persistence    = null;

    private final SpellsPlayerListener playerListener = new SpellsPlayerListener();

    private final Magic                spells         = new Magic();

    /*
     * Help commands
     */

    protected PluginCommand            spellsCommand;

    protected PluginUtilities          utilities      = null;

    protected void bindNetherGatePlugin()
    {
        /*
         * Plugin checkForNether =
         * this.getServer().getPluginManager().getPlugin("NetherGate");
         * 
         * if (checkForNether != null) {
         * log.info("Spells: found NetherGate! Thanks for using my plugins :)");
         * NetherGatePlugin plugin = (NetherGatePlugin)checkForNether;
         * spells.setNether(plugin.getManager()); }
         */
    }

    /*
     * Public API
     */
    public Magic getSpells()
    {
        return spells;
    }

    protected void initialize()
    {
        bindNetherGatePlugin();

        spells.initialize(getServer(), persistence, utilities);

        playerListener.setSpells(spells);
        entityListener.setSpells(spells);

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
        List<SpellVariant> spellVariants = spells.getAllSpells();

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
        List<SpellVariant> spellVariants = spells.getAllSpells();
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
        List<SpellVariant> spellVariants = spells.getAllSpells();
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

        SpellVariant spell = spells.getSpell(spellName, player);
        if (spell == null)
        {
            return false;
        }

        // spell.cast(null, parameters)
        // spells.castSpell(spell, parameters, player);

        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args)
    {
        return utilities.dispatch(this, sender, cmd.getName(), args);
    }

    public void onDisable()
    {
        spells.clear();
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
            log.warning("The Spells plugin depends on Persistence");
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
