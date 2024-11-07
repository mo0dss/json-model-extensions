package io.vram.jmx.json.material;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.vram.jmx.json.util.JsonBoolean;
import io.vram.jmx.json.util.JsonEnum;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;

import java.lang.reflect.Type;

public record RenderMaterialJson(
        BlendMode blendMode, boolean disableColorIndex, boolean emissive, boolean disableDiffuse,
        TriState ambientOcclusion, TriState glint, ShadeMode shadeMode
) {
    public static class Deserializer implements JsonDeserializer<RenderMaterialJson> {

        @Override
        public RenderMaterialJson deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            final var object = json.getAsJsonObject();

            var blendMode = JsonEnum.read(object, "blendMode", BlendMode.class, BlendMode.DEFAULT);
            var disableColorIndex = JsonBoolean.read(object, "disableColorIndex", false);
            var emissive = JsonBoolean.read(object, "emissive", false);
            var disableDiffuse = JsonBoolean.read(object, "disableDiffuse", false);
            var ambientOcclusion = JsonEnum.read(object, "ambientOcclusion", TriState.class, TriState.DEFAULT);
            var glint = JsonEnum.read(object, "glint", TriState.class, TriState.DEFAULT);
            var shadeMode = JsonEnum.read(object, "shadeMode", ShadeMode.class, ShadeMode.VANILLA);

            return new RenderMaterialJson(
                    blendMode,
                    disableColorIndex, emissive, disableDiffuse,
                    ambientOcclusion,
                    glint, shadeMode
            );
        }
    }
}
