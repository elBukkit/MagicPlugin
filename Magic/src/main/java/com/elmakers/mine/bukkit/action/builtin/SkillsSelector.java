package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class SkillsSelector implements GUIAction {
    private int page;
    private List<SkillDescription> allSkills = new ArrayList<>();
    private final MagicAPI api;
    private final Player player;
    private String inventoryTitle;

    class SkillDescription implements Comparable<SkillDescription> {
        public String heroesSkill;
        public SpellTemplate spell;

        public SkillDescription(SpellTemplate spell, String heroesSkill) {
            this.heroesSkill = heroesSkill;
            this.spell = spell;
        }

        public String getSpellKey() {
            if (spell != null) return "skill:" + spell.getKey();
            return "skill:heroes*" + heroesSkill;
        }

        public String getSpellName() {
            if (spell != null) return spell.getName();
            return heroesSkill;
        }

        public boolean isHeroes() {
            return heroesSkill != null;
        }

        @Override
        public int compareTo(SkillDescription other) {
            return getSpellName().compareTo(other.getSpellName());
        }
    };

    public SkillsSelector(MagicAPI api, Player player) {
        this.api = api;
        this.player = player;
    }

    public void show(int page) {
        this.page = page;
        MageController apiController = api.getController();
        if (!(apiController instanceof MagicController)) return;
        MagicController controller = (MagicController) apiController;
        Messages messages = controller.getMessages();
        HeroesManager heroes = controller.getHeroes();
        allSkills.clear();
        if (controller.useHeroesSkills() && heroes != null) {
            @Nonnull String classString = heroes.getClassName(player);
            @Nonnull String class2String = heroes.getSecondaryClassName(player);
            String messageKey = !class2String.isEmpty() ? "skills.inventory_title_secondary" : "skills.inventory_title";
            inventoryTitle = api.getMessages().get(messageKey, "Skills ($page/$pages)");
            inventoryTitle = inventoryTitle
                    .replace("$class2", class2String)
                    .replace("$class", classString);

            List<String> heroesSkills = heroes.getSkillList(player, true, true);
            for (String heroesSkill : heroesSkills) {
                allSkills.add(new SkillDescription(null, heroesSkill));
            }
        } else {
            inventoryTitle = messages.get("skills.inventory_title");
        }

        if (controller.usePermissionSkills()) {
            boolean bypassHidden = player.hasPermission("Magic.bypass_hidden");
            Collection<SpellTemplate> spells = controller.getSpellTemplates(bypassHidden);
            for (SpellTemplate spell : spells) {
                SpellKey key = spell.getSpellKey();
                if (key.isVariant()) continue;
                if (key.getBaseKey().startsWith("heroes*")) continue;
                if (!spell.hasCastPermission(player)) continue;
                allSkills.add(new SkillDescription(spell, null));
            }
        }

        if (allSkills.size() == 0) {
            player.sendMessage(messages.get("skills.none"));
            return;
        }

        Collections.sort(allSkills);
        openInventory();
    }

    protected void openInventory() {
        MageController apiController = api.getController();
        if (!(apiController instanceof MagicController)) return;
        MagicController controller = (MagicController) apiController;
        HeroesManager heroes = controller.getHeroes();
        int inventorySize = 9 * controller.getSkillInventoryRows();
        int numPages = (int)Math.ceil((float)allSkills.size() / inventorySize);
        if (page < 1) page = numPages;
        else if (page > numPages) page = 1;
        Mage mage = controller.getMage(player);
        int pageIndex = page - 1;
        int startIndex = pageIndex * inventorySize;
        int maxIndex = (pageIndex + 1) * inventorySize - 1;

        List<SkillDescription> skills = new ArrayList<>();
        for (int i = startIndex; i <= maxIndex && i < allSkills.size(); i++) {
            skills.add(allSkills.get(i));
        }
        if (skills.size() == 0)
        {
            String messageTemplate = controller.getMessages().get("skills.none_on_page", "$page");
            player.sendMessage(messageTemplate.replace("$page", Integer.toString(page)));
            return;
        }
        int invSize = (int)Math.ceil(skills.size() / 9.0f) * 9;
        String title = inventoryTitle;
        title = title
                .replace("$pages", Integer.toString(numPages))
                .replace("$page", Integer.toString(page));
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, title);
        for (SkillDescription skill : skills)
        {
            ItemStack skillItem = api.createItem(skill.getSpellKey(), mage);
            if (skillItem == null) continue;
            if (skill.isHeroes() && heroes != null && !heroes.canUseSkill(player, skill.heroesSkill))
            {
                String nameTemplate = controller.getMessages().get("skills.item_name_unavailable", "$skill");
                CompatibilityUtils.setDisplayName(skillItem, nameTemplate.replace("$skill", skill.heroesSkill));
                // It would be cool to be able to do this, but would need to look up Skills Template,
                // and don't want to mess with that until I have a Heroes test environment set up.
                /*
                MaterialAndData disabledIcon = skill.spell.getDisabledIcon();
                if (disabledIcon != null) {
                    disabledIcon.applyToItem(skillItem);
                }*/
            }
            displayInventory.addItem(skillItem);
        }

        mage.deactivateGUI();
        mage.activateGUI(this, displayInventory);
    }

    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        if (action == InventoryAction.NOTHING) {
            int direction = event.getClick() == ClickType.LEFT ? 1 : -1;
            page = page + direction;
            openInventory();
            event.setCancelled(true);
            return;
        }
        Mage mage = api.getMage(player);
        if (action == InventoryAction.PICKUP_HALF && mage != null)
        {
            ItemStack clickedItem = event.getCurrentItem();
            Spell spell = mage.getSpell(Wand.getSpell(clickedItem));
            if (spell != null) {
                spell.cast();
            }
            player.closeInventory();
            event.setCancelled(true);
        }
    }

    @Override
    public void dragged(InventoryDragEvent event) {

    }
}
