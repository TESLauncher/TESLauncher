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

package me.theentropyshard.teslauncher.utils;

import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class CallUnwrapAdapter<T> implements CallAdapter<T, T> {
    private final Type returnType;

    public CallUnwrapAdapter(Type returnType) {
        this.returnType = returnType;
    }

    @NotNull
    @Override
    public Type responseType() {
        return this.returnType;
    }

    @NotNull
    @Override
    public T adapt(@NotNull Call<T> call) {
        try {
            return call.execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class Factory extends CallAdapter.Factory {
        @Override
        public CallAdapter<?, ?> get(@NotNull Type returnType, @NotNull Annotation[] annotations, @NotNull Retrofit retrofit) {
            return new CallUnwrapAdapter<>(returnType);
        }
    }
}