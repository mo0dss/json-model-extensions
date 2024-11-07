package io.vram.jmx.json.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class JsonInteger {

  private static int read$0(JsonElement element, String name) {
    if (element.isJsonPrimitive()) {
      return element.getAsInt();
    }

    throw JsonHelper.invalidSyntaxException("Expected %s to be a Integer, instead it was %s", name, JsonHelper.getJsonType(element));
  }

  public static int read(JsonObject object, String key, int defaultValue) {
    if (!object.has(key)) {
      return defaultValue;
    }

    return read$0(object.get(key), key);
  }

  public static int read(JsonObject object, String key) {
    if (!object.has(key)) {
      throw new JsonParseException("Missing argument " + key);
    }

    return read$0(object.get(key), key);
  }

  public static void write(JsonObject object, String key, int value) {
    object.addProperty(key, value);
  }
}
