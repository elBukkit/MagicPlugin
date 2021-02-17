package com.elmakers.mine.bukkit.configuration;

import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicConfiguration extends ParameterizedConfiguration {
    private final MagicController controller;

    public MagicConfiguration(MagicController controller, ConfigurationSection copy) {
        this(controller);
        ConfigurationUtils.addConfigurations(this, copy);
    }

    public MagicConfiguration(MagicController controller) {
        super("magic controller");
        this.controller = controller;
    }

    public MagicConfiguration(MagicConfiguration copy) {
        super(copy);
        this.controller = copy.controller;
    }

    @Override
    protected double getParameter(String parameter) {
        Double value = controller.getBuiltinAttribute(parameter);
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }

    @Override
    protected Set<String> getParameters() {
        return controller.getBuiltinAttributes();
    }
}
