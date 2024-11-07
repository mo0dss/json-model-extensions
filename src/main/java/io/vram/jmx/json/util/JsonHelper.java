package io.vram.jmx.json.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.GsonHelper;

public class JsonHelper {
    public static String getJsonType(JsonElement element) {
        return GsonHelper.getType(element);
    }

    public static JsonSyntaxException invalidSyntaxException(String message, Object... args) {
        return new JsonSyntaxException(String.format(message, args));
    }
}
