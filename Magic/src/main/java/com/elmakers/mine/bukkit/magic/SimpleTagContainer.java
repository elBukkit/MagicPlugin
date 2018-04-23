package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.magic.TagContainer;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class SimpleTagContainer implements TagContainer {
    private static final SimpleTagContainer EMPTY = new SimpleTagContainer(
            ImmutableSet.<String>of());

    @Nonnull
    private final ImmutableSet<String> tags;

    private SimpleTagContainer(@Nonnull ImmutableSet<String> tags) {
        this.tags = tags;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public boolean hasTag(String tag) {
        checkNotNull(tag);
        return tags.contains(tag);
    }

    @Override
    public boolean hasAnyTag(Collection<String> tags) {
        checkNotNull(tags);
        return !Collections.disjoint(tags, tags);
    }

    @Override
    public boolean hasAllTags(Collection<String> tagSet) {
        return tags.containsAll(tagSet);
    }

    @Override
    public Set<String> getMissingTags(Collection<String> tagSet) {
        return Sets.difference(ImmutableSet.copyOf(tagSet), tags);
    }

    @Nonnull
    public static TagContainer fromConfig(
            @Nullable Collection<String> tagList) {
        if (tagList == null) {
            return EMPTY;
        }

        return new SimpleTagContainer(ImmutableSet.copyOf(tagList));
    }

    public static TagContainer empty() {
        return EMPTY;
    }

    @Nonnull
    public static TagContainer appendFromConfig(
            @Nonnull TagContainer tags,
            @Nullable Collection<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return tags;
        }

        return new SimpleTagContainer(
                ImmutableSet.<String>builder()
                        .addAll(tags.getTags())
                        .addAll(tagList)
                        .build());
    }
}
