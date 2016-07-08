package com.elmakers.mine.bukkit.action;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.elmakers.mine.bukkit.action.ActionFactory.ActionConstructor;
import com.elmakers.mine.bukkit.action.ActionFactory.ActionFactoryException;
import com.elmakers.mine.bukkit.action.ActionFactory.ActionResolver;
import com.elmakers.mine.bukkit.action.builtin.AbsorbAction;

public class ActionFactoryTest {
    @Test(expected = ActionFactoryException.class)
    public void testInvalidClass() throws ActionFactoryException {
        ActionFactory.construct("invalid-class%name");
    }

    @Test
    public void testInternalClass() throws ActionFactoryException {
        assertTrue(ActionFactory.construct(
                "Absorb") instanceof AbsorbAction);
        assertTrue(ActionFactory.construct(
                "AbsorbAction") instanceof AbsorbAction);
        assertTrue(ActionFactory.construct(
                "com.elmakers.mine.bukkit.action.builtin.Absorb") instanceof AbsorbAction);
        assertTrue(ActionFactory.construct(
                "com.elmakers.mine.bukkit.action.builtin.AbsorbAction") instanceof AbsorbAction);
    }

    @Test
    public void testAliasClass() throws ActionFactoryException {
        ActionFactory.registerActionClass("0123", AbsorbAction.class);
        assertTrue(ActionFactory.construct("0123") instanceof AbsorbAction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAliasClass() throws ActionFactoryException {
        ActionFactory.registerActionClass("0123", Object.class);
    }

    @Test
    public void testCustomResolver() throws ActionFactoryException {
        final ActionFactoryException e = new ActionFactoryException("");
        ActionResolver resolver = new ActionResolver() {
            @Override
            public ActionConstructor resolve(String className,
                    List<String> attempts) {
                return new ActionConstructor() {
                    @Override
                    public BaseSpellAction construct()
                            throws ActionFactoryException {
                        throw e;
                    }
                };
            }
        };
        ActionFactory.registerResolver(resolver);

        try {
            ActionFactory.construct("non-existing");
        } catch (ActionFactoryException e2) {
            assertEquals(e, e2);
            return;
        } finally {
            ActionFactory.removeResolver(resolver);
        }

        fail();
    }
}
