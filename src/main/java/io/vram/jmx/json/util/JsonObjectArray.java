package io.vram.jmx.json.util;

import com.google.gson.*;

public class JsonObjectArray {
  private static <T> JsonArray read$0(JsonElement element, Class<T> clazz, String name) {
    if (element.isJsonArray()) {
      return element.getAsJsonArray();
    }

    throw JsonHelper.invalidSyntaxException("Expected %s to be a ObjectArray (%s), instead it was %s", name, clazz.getSimpleName(), JsonHelper.getJsonType(element));
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] read(JsonObject object, String key, JsonDeserializationContext ctx, Class<T> clazz, T[] defaultValue) {
    if (!object.has(key)) {
      return defaultValue;
    }

    final var array = read$0(object.get(key), clazz, key);

    T[] out = (T[]) new Object[clazz.getEnumConstants().length];
    for (var i = 0; i < array.size(); i++) {
      out[i] = ctx.deserialize(array.get(i), clazz);
    }

    return out;
  }

  public static <T> void write(JsonObject object, String key, JsonSerializationContext ctx, Class<T> clazz, T[] values) {
    JsonArray array = new JsonArray();
    for (var i = 0; i < values.length; i++) {
      array.add(ctx.serialize(values[i], clazz));
    }

    object.add(key, array);
  }

}
