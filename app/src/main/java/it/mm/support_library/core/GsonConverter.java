package it.mm.support_library.core;

/**
 * Created by Giovanni Accetta on 10/02/17.
 * Copyright (c) 2015 Dott. Ing. Giovanni Accetta. All rights reserved.
 */


import com.github.pwittchen.prefser.library.JsonConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public final class GsonConverter implements JsonConverter {

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Override public <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    @Override public <T> String toJson(T object, Type typeOfT) {
        return gson.toJson(object, typeOfT);
    }
}