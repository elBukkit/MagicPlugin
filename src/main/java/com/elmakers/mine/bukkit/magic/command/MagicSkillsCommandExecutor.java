package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MagicSkillsCommandExecutor extends MagicTabExecutor {
    public static int INVENTORY_SIZE = 27;

	public MagicSkillsCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mskills"))
        {
            sendNoPermission(sender);
            return true;
        }
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return true;
        }
        Player player = (Player)sender;

        MageController apiController = api.getController();
        if (!(apiController instanceof MagicController)) return true;
        MagicController controller = (MagicController)apiController;
        HeroesManager heroes = controller.getHeroes();
        if (heroes == null) {
            sender.sendMessage(ChatColor.RED + "This command requires Heroes");
            return true;
        }

        List<String> allSkills = heroes.getSkillList(player, true);
        if (allSkills.size() == 0)
        {
            sender.sendMessage(ChatColor.RED + "You have no skills");
            return true;
        }
        int page = 0;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Expect page number, got " + args[0]);
                return true;
            }
        }
        int startIndex = page * INVENTORY_SIZE;
        int maxIndex = (page + 1) * INVENTORY_SIZE - 1;

        List<String> skills = new ArrayList<String>();
        for (int i = startIndex; i <= maxIndex && i < allSkills.size(); i++) {
            skills.add(allSkills.get(i));
        }
        if (skills.size() == 0)
        {
            sender.sendMessage(ChatColor.RED + "No skills on page " + page);
            return true;
        }

        Mage mage = controller.getMage(player);
        String inventoryTitle = "Skills";
        int invSize = (int)Math.ceil((float)skills.size() / 9.0f) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (String skill : skills)
        {
            ItemStack skillItem = api.createItem("skill:heroes*" + skill, mage);
            if (!heroes.canUseSkill(player, skill))
            {
                ItemMeta meta = skillItem.getItemMeta();
                String displayName = meta.getDisplayName();
                displayName = ChatColor.RED + ChatColor.stripColor(displayName);
                meta.setDisplayName(displayName);
                CompatibilityUtils.setDisplayName(skillItem, displayName);
                InventoryUtils.setCount(skillItem, 0);
            }
            displayInventory.addItem(skillItem);
        }

        player.openInventory(displayInventory);

        return true;
	}

	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		return new ArrayList<String>();
	}
}
