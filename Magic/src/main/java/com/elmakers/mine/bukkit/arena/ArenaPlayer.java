package com.elmakers.mine.bukkit.arena;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class ArenaPlayer implements Comparable<ArenaPlayer> {
    private Mage mage;
    private ProgressionPath path;
    private final Arena arena;
    private final UUID uuid;
    private String name;
    private String displayName;

    private int wins;
    private int losses;
    private int quits;
    private int joins;
    private int draws;

    public ArenaPlayer(Arena arena, ConfigurationSection config) {
        mage = null;
        this.arena = arena;

        uuid = UUID.fromString(config.getString("uuid"));
        name = config.getString("name");
        displayName = config.getString("display_name");
        wins = config.getInt("wins");
        losses = config.getInt("losses");
        quits = config.getInt("quits");
        joins = config.getInt("joins");
        draws = config.getInt("draws");
    }

    public ArenaPlayer(Arena arena, Player player) {
        uuid = player.getUniqueId();
        this.arena = arena;
        update(player);
    }

    public void update(Player player) {
        if (player == null) {
            return;
        }

        ArenaController controller = arena.getController();
        name = player.getName();
        mage = controller.getMagic().getMage(player);
        displayName = player.getDisplayName();

        path = null;
        CasterProperties mageClass = mage.getActiveProperties();
        if (mageClass != null) {
            path = mageClass.getPath();
        }

        wins = get("won");
        losses = get("lost");
        quits = get("quit");
        joins = get("joined");
        draws = get("draw");
    }

    public void save(ConfigurationSection config) {
        config.set("uuid", uuid.toString());
        config.set("name", name);
        config.set("display_name", displayName);
        config.set("wins", wins);
        config.set("losses", losses);
        config.set("quits", quits);
        config.set("joins", joins);
        config.set("draws", draws);
    }

    public boolean isValid() {
        return mage == null ? false : mage.isValid();
    }

    public boolean isDead() {
        return mage == null ? false : mage.isDead();
    }

    public Player getPlayer() {
        return mage == null ? null : mage.getPlayer();
    }

    public String getDisplayName() {
        String display = displayName;
        if (displayName == null) {
            displayName = mage.getDisplayName();
            display = displayName;
        }
        if (path != null) {
            display = display + " " + ChatColor.GRAY + "(" + ChatColor.GOLD + path.getName() + ChatColor.GRAY + ")";
        }
        return display;
    }

    public void won() {
        Player player = getPlayer();
        if (player != null) {
            int xp = arena.getWinXP();
            if (xp > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(xp) + ChatColor.AQUA + " experience!");
                mage.giveExperience(xp);
            }
            int sp = arena.getWinSP();
            if (sp > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(sp) + ChatColor.AQUA + " spell points!");
                mage.addSkillPoints(sp);
            }
            int money = arena.getWinMoney();
            if (money > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded $" + ChatColor.DARK_AQUA + Integer.toString(money) + ChatColor.AQUA + "!");
                mage.addVaultCurrency(money);
            }

            wins = increment("won");
        }
    }

    public void lost() {
        Player player = getPlayer();
        if (player != null) {
            int xp = arena.getLoseXP();
            if (xp > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(xp) + ChatColor.AQUA + " experience!");
                mage.giveExperience(xp);
            }
            int sp = arena.getLoseSP();
            if (sp > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(sp) + ChatColor.AQUA + " spell points!");
                mage.addSkillPoints(sp);
            }
            int money = arena.getLoseMoney();
            if (money > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded $" + ChatColor.DARK_AQUA + Integer.toString(money) + ChatColor.AQUA + "!");
                mage.addVaultCurrency(money);
            }

            losses = increment("lost");
        }
    }

    public void quit() {
        Player player = getPlayer();
        if (player != null) {
            quits = increment("quit");
        }
    }

    public void joined() {
        Player player = getPlayer();
        if (player != null) {
            joins = increment("joined");
        }
    }

    public void draw() {
        heal();
        teleport(arena.getLoseLocation());
        Player player = getPlayer();
        if (player != null) {
            int xp = arena.getDrawXP();
            if (xp > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(xp) + ChatColor.AQUA + " experience!");
                mage.giveExperience(xp);
            }
            int sp = arena.getDrawSP();
            if (sp > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(sp) + ChatColor.AQUA + " spell points!");
                mage.addSkillPoints(sp);
            }
            int money = arena.getDrawMoney();
            if (money > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded $" + ChatColor.DARK_AQUA + Integer.toString(money) + ChatColor.AQUA + "!");
                mage.addVaultCurrency(money);
            }

            draws = increment("draw");
        }
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getQuits() {
        return quits;
    }

    public int getJoins() {
        return joins;
    }

    public int getDraws() {
        return draws;
    }

    public int getValidMatches() {
        return wins + losses;
    }

    public Arena getArena() {
        return arena;
    }

    protected int increment(String statName) {
        if (mage == null) {
            arena.getController().getPlugin().getLogger().warning("Cannot increment statistic " + statName + ", no mage present.");
            return 0;
        }

        String arenaKey = "arena." + arena.getKey() + "." + statName;
        ConfigurationSection data = mage.getData();
        int currentValue = data.getInt(arenaKey, 0);
        int newValue = currentValue + 1;
        data.set(arenaKey, newValue);

        return newValue;
    }

    protected int get(String statName) {
        if (mage == null) {
            arena.getController().getPlugin().getLogger().warning("Cannot get statistic " + statName + ", no mage present.");
            return 0;
        }

        String arenaKey = "arena." + arena.getKey() + "." + statName;
        ConfigurationSection data = mage.getData();
        return data.getInt(arenaKey, 0);
    }

    public float getWinRatio() {
        if (losses == 0 && wins == 0) {
            return 0;
        }
        return (float)wins / (losses + wins);
    }

    /**
     * @return The lower bound of a 95% confidence interval for a Bernoulli
     *         parameter. (Read: Statistics voodo)
     * @see <a href="http://www.evanmiller.org/how-not-to-sort-by-average-rating.html">Reference</a>
     */
    public double getWinConfidence() {
        int nGames = wins + losses;

        if (nGames <= 0) {
            return 0;
        }

        double z = 1.96;
        double winRate = wins / (double) nGames;
        double b = (winRate *  (1 - winRate) + z * z / (4 * nGames)) / nGames;
        double a = winRate + z * z / (2 * nGames) -  z *  Math.sqrt(b);
        return a / (1 + z * z  / nGames);
    }

    @Override
    public int compareTo(ArenaPlayer other) {
        return uuid.compareTo(other.getUUID());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Player) {
            return equals((Player)other);
        }
        if (other instanceof UUID) {
            return equals((UUID)other);
        }
        if (!(other instanceof ArenaPlayer)) {
            return false;
        }
        return uuid.equals(((ArenaPlayer) other).uuid);
    }

    public boolean equals(Player player) {
        return (player != null && player.getUniqueId().equals(uuid));
    }

    public boolean equals(UUID id) {
        return (id != null && id.equals(uuid));
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void teleport(Location location) {
        Player player = getPlayer();
        if (player != null && location != null) {
            if (player.isDead()) {
                player.setMetadata("respawnLocation", new FixedMetadataValue(arena.getController().getPlugin(), location));
            } else {
                player.setMetadata("allow_teleport", new FixedMetadataValue(arena.getController().getPlugin(), true));
                player.teleport(location);
            }
        }
    }

    public void sendMessage(String message) {
        Mage mage = getMage();
        if (mage != null) {
            mage.sendMessage(message);
        }
    }

    public double getHealth() {
        Player player = getPlayer();
        return player == null ? 0.0 : player.getHealth();
    }

    public void heal() {
        Player player = getPlayer();
        if (player != null) {
            double maxHealth = CompatibilityLib.getCompatibilityUtils().getMaxHealth(player);
            player.setHealth(maxHealth);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setExhaustion(0);
            player.setFireTicks(0);
            for (PotionEffect pt : player.getActivePotionEffects()) {
                if (pt.getDuration() < Integer.MAX_VALUE / 4)
                {
                    player.removePotionEffect(pt.getType());
                }
            }
            mage.deactivateAllSpells(true, true);
        }
    }

    public void reset() {
        wins = 0;
        losses = 0;
        quits = 0;
        joins = 0;
        draws = 0;
        if (mage != null) {
            String arenaKey = "arena." + arena.getKey();
            ConfigurationSection data = mage.getData();
            data.set(arenaKey, null);
        }
    }

    public Mage getMage() {
        return mage;
    }

    public boolean isBattling() {
        return arena != null && arena.isBattling(this);
    }
}
