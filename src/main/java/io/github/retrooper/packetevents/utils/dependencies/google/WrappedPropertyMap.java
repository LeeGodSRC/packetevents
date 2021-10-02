/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.utils.dependencies.google;

import io.github.retrooper.packetevents.utils.dependencies.gameprofile.GameProfileProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface WrappedPropertyMap {
    Map<String, Collection<GameProfileProperty>> asMap();

    void clear();

    boolean containsEntry(String key, GameProfileProperty value);

    boolean containsKey(String key);

    boolean containsValue(@Nullable Object value);

    Collection<Map.Entry<String, GameProfileProperty>> entries();

    Collection<GameProfileProperty> get(@Nullable String key);

    boolean isEmpty();

    //Multiset<String> keys()
    Set<String> keySet();

    boolean put(String key, GameProfileProperty value);

    boolean putAll(String key, Iterable<? extends GameProfileProperty> values);

    boolean remove(String key, GameProfileProperty value);

    Collection<GameProfileProperty> removeAll(@Nullable Object key);
    Collection<GameProfileProperty> replaceValues(String key, Iterable<? extends GameProfileProperty> values);
    int size();
    boolean equals(@Nullable Object object);
    int hashCode();
}
