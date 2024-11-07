package io.vram.jmx.json.util;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

public class JsonObjectList {
  private static <T> JsonArray read$0(JsonElement element, Class<T> clazz, String name) {
    if (element.isJsonArray()) {
      return element.getAsJsonArray();
    }

    throw JsonHelper.invalidSyntaxException("Expected %s to be a ObjectArray (%s), instead it was %s", name, clazz.getSimpleName(), JsonHelper.getJsonType(element));
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> read(JsonObject object, String key, JsonDeserializationContext ctx, Class<T> clazz, List<T> defaultValue) {
    if (!object.has(key)) {
      return defaultValue;
    }

    final var array = read$0(object.get(key), clazz, key);

    var out = new ArrayList<T>();
    for (var i = 0; i < array.size(); i++) {
      out.add(ctx.deserialize(array.get(i), clazz));
    }

    return out;
  }

  public static <T> void write(JsonObject object, String key, JsonSerializationContext ctx, Class<T> clazz, List<T> values) {
    JsonArray array = new JsonArray();
    for (var i = 0; i < values.size(); i++) {
      array.add(ctx.serialize(values.get(i), clazz));
    }

    object.add(key, array);
  }
}
