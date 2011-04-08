package com.elmakers.mine.bukkit.wands.dao;

import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;

@PersistClass(schema="magic", name="rule")
public class WandRule
{
    protected int           id;
    protected WandCondition condition;
    protected WandOperator  operator;
    protected double        min;
    protected double        max;

    @PersistField(id=true, auto=true)
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    @PersistField
    public WandCondition getCondition()
    {
        return condition;
    }

    public void setCondition(WandCondition condition)
    {
        this.condition = condition;
    }

    @PersistField
    public WandOperator getOperator()
    {
        return operator;
    }

    public void setOperator(WandOperator operator)
    {
        this.operator = operator;
    }

    @PersistField
    public double getMin()
    {
        return min;
    }

    public void setMin(double min)
    {
        this.min = min;
    }

    @PersistField
    public double getMax()
    {
        return max;
    }

    public void setMax(double max)
    {
        this.max = max;
    }
}
