package io.vram.jmx.json.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonBoolean {

  private static boolean read$0(JsonElement element, String name) {
    if (element.isJsonPrimitive()) {
      return element.getAsBoolean();
    }

    throw JsonHelper.invalidSyntaxException("Expected %s to be a Boolean, instead it was %s", name, JsonHelper.getJsonType(element));
  }

  public static boolean read(JsonObject json, String name, boolean defaultValue) {
    if (!json.has(name)) {
      return defaultValue;
    }

    return read$0(json.get(name), name);
  }

  public static void write(JsonObject json, String name, boolean value) {
    json.addProperty(name, value);
  }
}
