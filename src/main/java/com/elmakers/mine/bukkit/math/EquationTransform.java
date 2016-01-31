package com.elmakers.mine.bukkit.math;

import com.elmakers.mine.bukkit.api.math.Transform;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class EquationTransform implements Transform {
    private Expression expression;

    @Override
    public void load(ConfigurationSection parameters) {
        setEquation(parameters.getString("equation", ""));
    }

    public EquationTransform() {

    }

    public EquationTransform(String equation) {
        setEquation(equation);
    }

    public void setEquation(String equation) {
        try {
            expression = new ExpressionBuilder(equation)
                .variables("t")
                .build();
        } catch (Exception ex) {
            expression = null;
            org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error parsing equation " + equation, ex);
        }
    }

    @Override
    public double get(double t) {
        if (expression == null) {
            return 0;
        }
        expression.setVariable("t", t);
        return expression.evaluate();
    }
}
