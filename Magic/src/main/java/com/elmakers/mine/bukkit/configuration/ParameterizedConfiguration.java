package com.elmakers.mine.bukkit.configuration;

import java.util.Map;
import java.util.Set;
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

    @Nullable
    protected Double evaluate(String expression, String field) {
        contextField = field;
        Double result = evaluate(expression);
        contextField = null;
        return result;
    }

    @Nullable
    protected Double evaluate(String expression) {
        workingParameters = getParameters();
        if (workingParameters == null || workingParameters.isEmpty()) return null;

        // We don't currently use $ as an operator, and removing it lets us keep compatibility with
        // the old system that let you reference parameters like $range
        expression = expression.replace("$", "");
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
