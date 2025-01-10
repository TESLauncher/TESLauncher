/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2025 TESLauncher
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.utils.json.type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Will deserialize a single value to a list in the model
 * <a href="https://stackoverflow.com/a/43412985/19857533">Make GSON accept single objects where it expects arrays</a>
 */
public final class AlwaysListTypeAdapterFactory<E> implements TypeAdapterFactory {

    // Gson can instantiate it itself
    private AlwaysListTypeAdapterFactory() {

    }

    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        // If it's not a List -- just delegate the job to Gson and let it pick the best type adapter itself
        if (!List.class.isAssignableFrom(typeToken.getRawType())) {
            return null;
        }
        // Resolving the list parameter type
        final Type elementType = resolveTypeArgument(typeToken.getType());
        @SuppressWarnings("unchecked") final TypeAdapter<E> elementTypeAdapter = (TypeAdapter<E>) gson.getAdapter(TypeToken.get(elementType));
        // Note that the always-list type adapter is made null-safe, so we don't have to check nulls ourselves
        @SuppressWarnings("unchecked") final TypeAdapter<T> alwaysListTypeAdapter = (TypeAdapter<T>) new AlwaysListTypeAdapter<>(elementTypeAdapter).nullSafe();
        return alwaysListTypeAdapter;
    }

    private static Type resolveTypeArgument(final Type type) {
        // The given type is not parameterized?
        if (!(type instanceof ParameterizedType)) {
            // No, raw
            return Object.class;
        }
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        return parameterizedType.getActualTypeArguments()[0];
    }

    private static final class AlwaysListTypeAdapter<E>
            extends TypeAdapter<List<E>> {

        private final TypeAdapter<E> elementTypeAdapter;

        private AlwaysListTypeAdapter(final TypeAdapter<E> elementTypeAdapter) {
            this.elementTypeAdapter = elementTypeAdapter;
        }

        @Override
        public void write(final JsonWriter out, final List<E> list) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<E> read(final JsonReader in)
                throws IOException {
            // This is where we detect the list "type"
            final List<E> list = new ArrayList<>();
            final JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    // If it's a regular list, just consume [, <all elements>, and ]
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(this.elementTypeAdapter.read(in));
                    }
                    in.endArray();
                    break;
                case BEGIN_OBJECT:
                case STRING:
                case NUMBER:
                case BOOLEAN:
                    // An object or a primitive? Just add the current value to the result list
                    list.add(this.elementTypeAdapter.read(in));
                    break;
                case NULL:
                    throw new AssertionError("Must never happen: check if the type adapter configured with .nullSafe()");
                case NAME:
                case END_ARRAY:
                case END_OBJECT:
                case END_DOCUMENT:
                    throw new MalformedJsonException("Unexpected token: " + token);
                default:
                    throw new AssertionError("Must never happen: " + token);
            }
            return list;
        }
    }
}