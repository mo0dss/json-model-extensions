package io.vram.jmx.json.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Locale;

public class JsonEnum {
  private static String read$0(JsonElement element, Class<?> clazz, String name) {
    if (element.isJsonPrimitive()) {
      return element.getAsString();
    }

    throw JsonHelper.invalidSyntaxException("Expected %s to be a Enum (%s), instead it was %s", name, clazz.getSimpleName(), JsonHelper.getJsonType(element));
  }

  public static <E extends Enum<E>> E read(JsonArray object, int index, Class<E> clazz, E defaultValue) {
    if (!object.get(index).isJsonPrimitive()) {
      return defaultValue;
    }

    final var universe = clazz.getEnumConstants();
    final var name     = read$0(object.get(index), clazz, String.valueOf(index));

    E result = null;
    for (var i = 0; i < universe.length && result == null; i++) {
      if (name.equals(universe[i].name().toLowerCase(Locale.ROOT))) {
        result = universe[i];
      }
    }

    if (result != null) {
      return result;
    }

    throw new RuntimeException("Unable to find enum by name: " + name);
  }

  public static <E extends Enum<E>> E read(JsonObject object, String key, Class<E> clazz, E defaultValue) {
    if (!object.has(key)) {
      return defaultValue;
    }

    final var universe = clazz.getEnumConstants();
    final var name     = read$0(object.get(key), clazz, key);

    E result = null;
    for (var i = 0; i < universe.length && result == null; i++) {
      if (name.equals(universe[i].name().toLowerCase(Locale.ROOT))) {
        result = universe[i];
      }
    };

    if (result != null) {
      return result;
    }

    throw new RuntimeException("Unable to find enum by name: " + name);
  }

  public static <E extends Enum<E>> E read(JsonArray object, int index, Class<E> clazz) {
    if (!object.get(index).isJsonPrimitive()) {
      throw new JsonParseException("Invalid argument at index " + index);
    }

    final var universe = clazz.getEnumConstants();
    final var name     = read$0(object.get(index), clazz, String.valueOf(index));

    E result = null;
    for (var i = 0; i < universe.length && result == null; i++) {
      if (name.equals(universe[i].name().toLowerCase(Locale.ROOT))) {
        result = universe[i];
      }
    };

    if (result != null) {
      return result;
    }

    throw new RuntimeException("Unable to find enum by name: " + name);
  }

  public static <E extends Enum<E>> E read(JsonObject object, String key, Class<E> clazz) {
    if (!object.has(key)) {
      throw new JsonParseException("Missing argument " + key);
    }

    final var universe = clazz.getEnumConstants();
    final var name     = read$0(object.get(key), clazz, key);

    E result = null;
    for (var i = 0; i < universe.length && result == null; i++) {
      if (name.equals(universe[i].name().toLowerCase(Locale.ROOT))) {
        result = universe[i];
      }
    };

    if (result != null) {
      return result;
    }

    throw new RuntimeException("Unable to find enum by name: " + name);
  }

  public static <E extends Enum<E>> void write(JsonObject object, String key, E value) {
    object.addProperty(key, value.name().toLowerCase(Locale.ROOT));
  }
}
