package com.elmakers.mine.bukkit.magic.command.config;

import java.util.ArrayList;
import java.util.List;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class NewSessionRequest extends Session {
    protected List<String> attributes;
    protected List<String> classes;
    protected List<String> crafting;
    protected List<String> effects;
    protected List<String> items;
    protected List<String> materials;
    protected List<String> mobs;
    protected List<String> modifiers;
    protected List<String> paths;
    protected List<String> spells;
    protected List<String> wands;
    protected List<String> currencies;

    public NewSessionRequest(MageController controller, String type) {
        this.type = type;

        attributes = new ArrayList<>(controller.getInternalAttributes());
        classes = new ArrayList<>(controller.getMageClassKeys());
        crafting = new ArrayList<>(controller.getRecipeKeys());
        effects = new ArrayList<>(controller.getEffectKeys());
        items = new ArrayList<>(controller.getItemKeys());
        materials = new ArrayList<>(controller.getMaterialSetManager().getMaterialSets());
        mobs = new ArrayList<>(controller.getMobKeys());
        modifiers = new ArrayList<>(controller.getModifierTemplateKeys());
        paths = new ArrayList<>(controller.getWandPathKeys());
        spells = new ArrayList<>(controller.getSpellTemplateKeys());
        wands = new ArrayList<>(controller.getWandTemplateKeys());
        currencies = new ArrayList<>(controller.getCurrencyKeys());
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public List<String> getCrafting() {
        return crafting;
    }

    public void setCrafting(List<String> crafting) {
        this.crafting = crafting;
    }

    public List<String> getEffects() {
        return effects;
    }

    public void setEffects(List<String> effects) {
        this.effects = effects;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<String> getMaterials() {
        return materials;
    }

    public void setMaterials(List<String> materials) {
        this.materials = materials;
    }

    public List<String> getMobs() {
        return mobs;
    }

    public void setMobs(List<String> mobs) {
        this.mobs = mobs;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getSpells() {
        return spells;
    }

    public void setSpells(List<String> spells) {
        this.spells = spells;
    }

    public List<String> getWands() {
        return wands;
    }

    public void setWands(List<String> wands) {
        this.wands = wands;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<String> currencies) {
        this.currencies = currencies;
    }
}
