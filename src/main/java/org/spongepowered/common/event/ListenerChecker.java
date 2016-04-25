/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event;

import com.google.common.base.CaseFormat;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.event.Event;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class ListenerChecker {

    private final Class<?> clazz;
    private Map<String, Field> fields = new HashMap<>();
    private LoadingCache<Class<?>, Map<Class<?>, Field>> cache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, Map<Class<?>, Field>>() {

        @SuppressWarnings("unchecked")
        @Override
        public Map<Class<?>, Field> load(Class<?> clazz) throws Exception {
            Set<Class<?>> types = (Set) TypeToken.of(clazz).getTypes().rawTypes();
            ImmutableMap.Builder<Class<?>, Field> builder = ImmutableMap.builder();

            for (Class<?> eventClazz: types) {
                String name = this.getName(eventClazz);
                if (fields.containsKey(name)) {
                    builder.put(eventClazz, fields.get(name));
                }
            }
            return builder.build();
        }

        private String getName(Class<?> clazz) {
            // Properly account for inner classes. Class#getName uses a $
            // to separate inner classes, so the last '.' is the end of the package name
            //
            String name = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1).replace("$", "");
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
        }
    });

    public ListenerChecker(Class<?> clazz) {
        this.clazz = clazz;
        for (Field field: this.clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                this.fields.put(field.getName(), field);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void updateFields(Class<?> clazz, Predicate<Class<? extends Event>> enable) {
        this.cache.getUnchecked(clazz).forEach((k, f) -> {
            try {
                f.set(null, enable.test((Class) k));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private class FieldCache {

        private final Optional<Field> directField;
        private final ImmutableSet<Field> allFields;

        public FieldCache(@Nullable  Field directField, ImmutableSet<Field> allFields) {
            this.directField = Optional.ofNullable(directField);
            this.allFields = allFields;
        }
        public Optional<Field> getDirectField() {
            return directField;
        }

        public ImmutableSet<Field> getAllFields() {
            return allFields;
        }

    }

}
