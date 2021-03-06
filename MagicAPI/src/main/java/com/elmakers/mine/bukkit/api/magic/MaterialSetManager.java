package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Container of material sets.
 */
public interface MaterialSetManager {
    /**
     * @return An unmodifiable collection of the keys of the available material
     *         sets.
     */
    Collection<String> getMaterialSets();

    /**
     * Looks up a material set by its name.
     *
     * @param name
     *            The name of the material set. One of
     *            {@link #getMaterialSets()}.
     * @return The material set, or null when it is not available.
     */
    @Nullable
    MaterialSet getMaterialSet(@Nonnull String name);

    @Nonnull
    MaterialSet getMaterialSet(
            @Nonnull String name,
            @Nonnull MaterialSet fallback);

    @Nonnull
    MaterialSet getMaterialSetEmpty(@Nonnull String name);

    /**
     * Looks up a material map by its name.
     *
     * @param name
     *            The name of the material map. One of
     *            {@link #getMaterialSets()}.
     * @return The material map, or null when it is not available or if it is not a map
     */
    @Nullable
    MaterialMap getMaterialMap(@Nonnull String name);

    /**
     * Loads or creates a material set from a configuration string.
     *
     * @param name
     *            The material set name or parameters.
     * @return
     *          When the name is empty, or an empty string null is returned.
     *          In all other cases a {@link MaterialSet} is returned. It will
     *          either be one named exactly like {@code name}, or parsed by the
     *          string.
     */
    @Nullable
    MaterialSet fromConfig(@Nullable String name);

    @Nonnull
    MaterialSet fromConfig(
            @Nullable String name,
            @Nonnull MaterialSet fallback);

    /**
     * Loads or creates a material set from a configuration string.
     *
     * @param name
     *            The material set name or parameters.
     * @return Instead of returning null, this method returns an empty material
     *         set.
     * @see #fromConfig(String)
     */
    @Nonnull
    MaterialSet fromConfigEmpty(@Nullable String name);

    /**
     * Loads or creates a material map from a configuration section.
     *
     * @param configuration
     *            The configuration to load from.
     * @param key
     *            The configuration key containing the config to load from.
     *            Must be a string (referencing a configured material map)
     *            or a configuration section (creating an ad-hoc map)
     */
    @Nullable
    MaterialMap mapFromConfig(ConfigurationSection configuration, String key);
}
