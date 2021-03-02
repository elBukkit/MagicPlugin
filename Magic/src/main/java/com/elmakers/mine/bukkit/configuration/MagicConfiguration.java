package com.elmakers.mine.bukkit.configuration;

import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicConfiguration extends ParameterizedConfiguration {
    private final MagicController controller;

    public MagicConfiguration(MagicController controller, ConfigurationSection copy, String logContext) {
        this(controller, logContext);
        ConfigurationUtils.addConfigurations(this, copy);
    }

    public MagicConfiguration(MagicController controller, String logContext) {
        super(logContext);
        this.controller = controller;
    }

    public MagicConfiguration(MagicConfiguration copy) {
        super(copy);
        this.controller = copy.controller;
    }

    public static MagicConfiguration getKeyed(MagicController controller, ConfigurationSection copy, String logPrefix, String key) {
        return new MagicConfiguration(controller, copy, logPrefix + "." + key);
    }

    public static MagicConfiguration getKeyed(MagicController controller, ConfigurationSection copy, String logContext) {
        return new MagicConfiguration(controller, copy, logContext);
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
