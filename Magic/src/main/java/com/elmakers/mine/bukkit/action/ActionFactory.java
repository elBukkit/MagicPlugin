package com.elmakers.mine.bukkit.action;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

/**
 * Creates new action instances from a "class" parameter usually obtained from a
 * configuration file. Third party systems/plugins can register custom resolvers
 * to play nice with actions that require parameters when constructing.
 */
public class ActionFactory {
    private static List<ActionResolver> resolvers = new ArrayList<>();

    /**
     * @return An unmodifiable list of action resolvers.
     */
    public static List<ActionResolver> getActionResolvers() {
        return Collections.unmodifiableList(resolvers);
    }

    public static void registerResolver(ActionResolver actionResolver) {
        registerResolver(actionResolver, false);
    }

    /**
     * Registers an action resolver.
     *
     * @param actionResolver
     *            The action resolver to register.
     * @param highPriority
     *            When this is set to true, the resolver is registered such that
     *            it is used before any of the currently registered resolvers.
     * @throws NullPointerException
     *             When actionResolver is null.
     */
    public static void registerResolver(ActionResolver actionResolver,
            boolean highPriority) {
        Preconditions.checkNotNull(actionResolver);

        if (!resolvers.contains(actionResolver)) {
            if (highPriority) {
                resolvers.add(0, actionResolver);
            } else {
                resolvers.add(actionResolver);
            }
        }
    }

    /**
     * Unregister a resolver.
     *
     * @param actionResolver
     *            The action resolver to remove.
     * @throws NullPointerException
     *             When actionResolver is null.
     */
    public static void removeResolver(ActionResolver actionResolver) {
        Preconditions.checkNotNull(actionResolver);
        Iterator<ActionResolver> it = resolvers.iterator();

        while (it.hasNext()) {
            if (it.next().equals(actionResolver)) {
                it.remove();
            }
        }
    }

    static {
        resolvers.add(new InternalActionResolver());
    }

    // Cannot create instances
    private ActionFactory() {
    }

    /**
     * Registers an action class.
     *
     * @param name
     *            The name to register the action as.
     * @param clazz
     *            The class to register.
     */
    public static void registerActionClass(String name, Class<?> clazz) {
        if (!BaseSpellAction.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Must extend SpellAction");
        }

        ActionConstructor constructor = InternalActionResolver
                .createConstructor(clazz);

        if (constructor == null) {
            throw new IllegalArgumentException(
                    "Class does not have a valid no-args constructor");
        }

        InternalActionResolver.actionClasses.put(name, constructor);
    }

    public interface ActionResolver {
        /**
         * Attempts to resolve a constructor from a class.
         * @param className The class to resolve.
         * @param attempts Appendable list of attempted classes.
         * @return The constructor of the class name or null.
         */
        ActionConstructor resolve(String className, List<String> attempts);
    }

    /**
     * A basic implementation of the {@link ActionResolver} interface. Provides
     * a simple name to resolver mapping.
     */
    public static class NamedActionResolver
            extends ForwardingMap<String, ActionConstructor>
            implements ActionResolver {
        private final Map<String, ActionConstructor> delegate;

        public NamedActionResolver(Map<String, ActionConstructor> delegate) {
            this.delegate = delegate;
        }

        public NamedActionResolver() {
            this(Maps.<String, ActionConstructor>newHashMap());
        }

        @Override
        public ActionConstructor resolve(String className,
                List<String> attempts) {
            return get(className);
        }

        @Override
        protected Map<String, ActionConstructor> delegate() {
            return delegate;
        }
    }

    public interface ActionConstructor {
        /**
         * Instantiates a new action.
         *
         * @return The instantiated action.
         * @throws ActionFactoryException If something went wrong.
         */
        BaseSpellAction construct() throws ActionFactoryException;
    }

    /**
     * Resolves constructors from BaseSpellAction classes with a no-args
     * constructor.
     */
    private static final class InternalActionResolver
            implements ActionResolver {
        private static final String ACTION_BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.action.builtin";
        private static Map<String, ActionConstructor> actionClasses = new HashMap<>();

        // Paths to search through
        private static final String[] patterns = {
                ACTION_BUILTIN_CLASSPATH + ".%sAction",
                ACTION_BUILTIN_CLASSPATH + ".%s",
                "%sAction",
                "%s",
        };

        @Nullable
        @Override
        public ActionConstructor resolve(String className,
                List<String> attempts) {
            ActionConstructor constructor = actionClasses.get(className);

            if (constructor != null) {
                return constructor;
            }

            for (String pattern : patterns) {
                Class<?> clazz;
                String path = String.format(pattern, className);

                attempts.add(path);

                try {
                    clazz = Class.forName(path);
                } catch (ClassNotFoundException e) {
                    continue;
                }

                // If not a base spell action, ignore
                if (!BaseSpellAction.class.isAssignableFrom(clazz)) {
                    continue;
                }

                constructor = createConstructor(clazz);

                if (constructor != null) {
                    actionClasses.put(className, constructor);
                    return constructor;
                }
            }

            // Could not find anything
            return null;
        }

        @Nullable
        private static ActionConstructor createConstructor(Class<?> clazz) {
            Constructor<?> constructor;

            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                return null;
            } catch (SecurityException e) {
                return null;
            }

            if (!Modifier.isPublic(constructor.getModifiers())) {
                return null;
            }

            return new InternalActionConstructor(constructor);
        }
    }

    /**
     * Helper class for {@link InternalActionResolver} that does the actual
     * instantiation.
     */
    private static final class InternalActionConstructor
            implements ActionConstructor {

        private Constructor<?> constructor;

        public InternalActionConstructor(Constructor<?> constructor) {
            this.constructor = constructor;
        }

        @Override
        public BaseSpellAction construct() throws ActionFactoryException {
            try {
                return (BaseSpellAction) constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new ActionFactoryException(
                        "Failed to instantiate with reflection", e);
            }
        }

    }

    /**
     * Action that is thrown when instantiating an action fails.
     */
    public static final class ActionFactoryException extends Exception {
        public ActionFactoryException(String msg, Throwable e) {
            super(msg, e);
        }

        public ActionFactoryException(String msg) {
            super(msg);
        }
    }

    /**
     * Constructs a new action from a class name.
     *
     * @param actionClassName
     *            The class name of the action.
     * @return The constructed action.
     * @throws ActionFactoryException
     *             If no action could be constructed.
     */
    public static BaseSpellAction construct(String actionClassName)
            throws ActionFactoryException {
        List<String> attempts = new ArrayList<>();

        for (ActionResolver resolver : resolvers) {
            ActionConstructor constructor = resolver.resolve(actionClassName,
                    attempts);

            if (constructor != null) {
                return constructor.construct();
            }
        }

        throw new ActionFactoryException(
                "Failed to resolve class: " + actionClassName + "\nTried: "
                        + attempts);
    }
}
