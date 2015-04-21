package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HeroesSkillsSelector implements GUIAction {
    private int inventorySize = 54;
    private final MagicAPI api;
    private final Player player;

    public HeroesSkillsSelector(MagicAPI api, Player player) {
        this.api = api;
        this.player = player;
    }

    public void show(int page) {
        MageController apiController = api.getController();
        if (!(apiController instanceof MagicController)) return;
        MagicController controller = (MagicController)apiController;
        HeroesManager heroes = controller.getHeroes();
        if (heroes == null) {
            player.sendMessage(ChatColor.RED + "This command requires Heroes");
            return;
        }

        List<String> allSkills = heroes.getSkillList(player, true);
        if (allSkills.size() == 0)
        {
            player.sendMessage(ChatColor.RED + "You have no skills");
            return;
        }
        inventorySize = 9 * controller.getSkillInventoryRows();
        int pageIndex = page - 1;
        int startIndex = pageIndex * inventorySize;
        int maxIndex = (pageIndex + 1) * inventorySize - 1;
        int numPages = (int)Math.ceil((float)allSkills.size() / inventorySize);

        List<String> skills = new ArrayList<String>();
        for (int i = startIndex; i <= maxIndex && i < allSkills.size(); i++) {
            skills.add(allSkills.get(i));
        }
        if (skills.size() == 0)
        {
            player.sendMessage(ChatColor.RED + "No skills on page " + page);
            return;
        }

        Mage mage = controller.getMage(player);
        String classString = heroes.getClassName(player);
        String class2String = heroes.getSecondaryClassName(player);
        String messageKey = class2String != null && !class2String.isEmpty() ? "skills.inventory_title_secondary" : "skills.inventory_title";
        String inventoryTitle = api.getMessages().get(messageKey, "Skills ($page/$pages)");
        inventoryTitle = inventoryTitle
                .replace("$pages", Integer.toString(numPages))
                .replace("$page", Integer.toString(page))
                .replace("$class2", class2String)
                .replace("$class", classString);
        int invSize = (int)Math.ceil((float)skills.size() / 9.0f) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (String skill : skills)
        {
            ItemStack skillItem = api.createItem("skill:heroes*" + skill, mage);
            if (skillItem == null) continue;
            if (!heroes.canUseSkill(player, skill))
            {
                String nameTemplate = controller.getMessages().get("skills.item_name_unavailable", "$skill");
                CompatibilityUtils.setDisplayName(skillItem, nameTemplate.replace("$skill", skill));
                InventoryUtils.setCount(skillItem, 0);
            }
            displayInventory.addItem(skillItem);
        }

        mage.activateGUI(this);
        player.openInventory(displayInventory);
    }

    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event) {

    }

    @Override
    public void dragged(InventoryDragEvent event) {

    }
}
