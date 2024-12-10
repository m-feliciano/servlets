package com.dev.servlet.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unchecked")
public final class CloneUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private CloneUtils() {
    }

    public static <T extends Serializable> T clone(T object) {
        try {
            return SerializationUtils.clone(object);
        } catch (Exception e) {
            return forceClone(object);
        }
    }

    private static <T extends Serializable> T forceClone(T object) {
        return gson.fromJson(gson.toJson(object), (Class<T>) object.getClass());
    }

    public static <T extends Serializable> Collection<T> cloneList(Collection<T> objects) {
        List<T> list = new ArrayList<>();
        for (T t : objects) {
            list.add(clone(t));
        }
        return list;
    }
}
