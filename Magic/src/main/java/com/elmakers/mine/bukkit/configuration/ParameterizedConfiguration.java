package com.elmakers.mine.bukkit.configuration;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;
import de.slikey.effectlib.math.EquationVariableProvider;

public abstract class ParameterizedConfiguration extends ParameterizedConfigurationSection implements Configuration, EquationVariableProvider {
    private static class Options extends ConfigurationOptions {
        protected Options(Configuration configuration) {
            super(configuration);
        }
    }

    private static String PLACEHOLDER_PATTERN_STRING = "%([a-zA-Z0-9_]+)%";
    private static Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_PATTERN_STRING);

    private Options options;
    private String context;
    private String contextField;
    private Set<String> workingParameters;

    protected ParameterizedConfiguration(String context) {
        this.context = context;
    }

    protected ParameterizedConfiguration(ParameterizedConfiguration copy) {
        this(copy.context);
    }

    @Nullable
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

    @Nullable
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

    protected String parsePlaceholders(String expression) {
        // We need to clear placeholders here since we can't evaluate them unless this is a MageParameters
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(expression);
        if (!matcher.matches()) return expression;

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "0");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Nullable
    protected Double evaluate(String expression, String field) {
        contextField = field;
        Double result = evaluate(expression);
        contextField = null;
        return result;
    }

    @Nullable
    protected Double evaluate(String expression) {
        // This is a hack to fix an issue with 1.21.5 NBT having saved Optional classes to a config
        // in place of boolean values.
        // Unfortunately once the string is in the config there was not a good fix, so I'm
        // putting this here as a stopgap to avoid the affected wands being permanently broken.
        if (expression.equals("Optional[1]")) {
            expression = "1";
        } else if (expression.equals("Optional[0]")) {
            expression = "0";
        }

        workingParameters = getParameters();
        if (workingParameters == null || workingParameters.isEmpty()) return null;

        // We don't currently use $ as an operator, and removing it lets us keep compatibility with
        // the old system that let you reference parameters like $range
        expression = expression.replace("$", "");
        expression = parsePlaceholders(expression);
        EquationTransform transform = EquationStore.getInstance().getTransform(expression, workingParameters);
        transform.setVariableProvider(this);
        double value = transform.get();
        transform.setVariableProvider(null);
        Exception ex = transform.getException();
        if (ex != null) {
            String thisContext = context == null ? "unknown" : context;
            if (contextField != null) {
                thisContext += "." + contextField;
            }
            warn("Error evaluating transform in " + thisContext + ": '" + expression + "': " + ex.getMessage());
        }
        return Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }

    @Override
    @Nullable
    public Double getVariable(String variable) {
        return workingParameters != null && workingParameters.contains(variable) ? getParameter(variable) : null;
    }

    protected abstract Set<String> getParameters();
    protected abstract double getParameter(String parameter);
}
