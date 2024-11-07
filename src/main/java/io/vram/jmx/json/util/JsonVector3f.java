package io.vram.jmx.json.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;

public class JsonVector3f {

  public static void write(JsonObject object, String name, Vector3f vector) {

  }

  public static Vector3f read(JsonObject object, String name) {
    JsonArray jsonArray = GsonHelper.getAsJsonArray(object, name);
    if (jsonArray.size() != 3) {
      throw new JsonParseException("Expected 3 " + name + " values, found: " + jsonArray.size());
    } else {
      float[] fs = new float[3];

      for (int i = 0; i < fs.length; i++) {
        fs[i] = GsonHelper.convertToFloat(jsonArray.get(i), name + "[" + i + "]");
      }

      return new Vector3f(fs[0], fs[1], fs[2]);
    }
  }
}
