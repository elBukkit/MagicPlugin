package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A container of an immutable set of tags.
 */
public interface TagContainer {
    @Nonnull
    Set<String> getTags();

    boolean hasTag(@Nonnull String tag);

    boolean hasAnyTag(@Nonnull Collection<String> tags);

    boolean hasAllTags(@Nonnull Collection<String> tagSet);

    /**
     * Determines which tags are not present in the container but are in the
     * specified set.
     *
     * @param tagSet
     *            The set of tags to consider.
     * @return The set of tags that is in {@code tagSet} but not in the
     *         container.
     */
    Set<String> getMissingTags(@Nonnull Collection<String> tagSet);
}
