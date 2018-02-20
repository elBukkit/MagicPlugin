package com.elmakers.mine.bukkit.magic;

import de.slikey.effectlib.math.EquationTransform;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParameterizedConfiguration extends ParameterizedConfigurationSection implements Configuration {
    private Mage mage;
    private Options options;

    // Attribute handling
    protected static Set<String> attributes;
    protected static Map<String, EquationTransform> attributeTransforms;
    
    public ParameterizedConfiguration() {
        super();
    }

    public ParameterizedConfiguration(Mage mage) {
        super();
        this.mage = mage;
    }

    public ParameterizedConfiguration(ParameterizedConfiguration copy) {
        super();
        this.mage = copy.mage;
    }

    protected MageController getController() {
        return mage == null ? null : mage.getController();
    }

    protected Mage getMage() {
        return mage;
    }
    
    public void setMage(Mage mage) {
        this.mage = mage;
    }
    
    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override
    public void addDefaults(Map<String, Object> map) {

    }

    @Override
    public void addDefaults(Configuration configuration) {

    }

    @Override
    public void setDefaults(Configuration configuration) {

    }

    @Override
    public Configuration getDefaults() {
        return null;
    }

    @Override
    public ConfigurationOptions options() {
        if (options == null) {
            options = new Options(this);
        }

        return options;
    }

    private class Options extends ConfigurationOptions {
        protected Options(Configuration configuration) {
            super(configuration);
        }
    }

    public static void initializeAttributes(Set<String> attrs) {
        attributes = null;
        attributeTransforms = null;
        if (attrs == null || attrs.isEmpty()) return;

        attributes = new HashSet<>();
        for (String attr : attrs) {
            attributes.add(attr);
        }
        attributeTransforms = new HashMap<>();
    }

    protected Double evaluateParameter(String parameter) {
        if (attributes == null) return null;

        EquationTransform transform = attributeTransforms.get(parameter);
        if (transform == null) {
            transform = new EquationTransform(parameter, attributes);
            attributeTransforms.put(parameter, transform);
        }

        for (String attribute : attributes) {
            Double value = mage == null ? null : mage.getAttribute(attribute);
            transform.setVariable(attribute, value == null ? 0 : value);
        }

        return transform.get();
    }
}
