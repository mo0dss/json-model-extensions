package io.vram.jmx.client;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.vram.jmx.json.JmxRendererAccess;
import io.vram.jmx.json.material.RenderMaterialJson;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;

public class MaterialResourceManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final FileToIdConverter MATERIALS = FileToIdConverter.json("materials");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(RenderMaterialJson.class, new RenderMaterialJson.Deserializer())
            .create();

    public static MaterialResourceSet prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return new MaterialResourceSet(MATERIALS.listMatchingResources(resourceManager));
    }

    public static void apply(MaterialResourceSet object, ResourceManager resourceManager, ProfilerFiller profiler) {
        for (var entries : object.map().entrySet()) {
            var name = MATERIALS.fileToId(entries.getKey());

            try (var reader = entries.getValue().openAsReader()) {
                var material = GSON.fromJson(reader, RenderMaterialJson.class);
                if (material != null) {
                  registerMaterial(name, material);
                }
            } catch (IOException ex) {
                LOGGER.warn("Unable to load material {}", name, ex);
            }
        }
    }

    private static void registerMaterial(ResourceLocation name, RenderMaterialJson json) {
        final var renderer = JmxRendererAccess.getRenderer();
        renderer.registerMaterial(name,
          renderer.materialFinder()
            .blendMode(json.blendMode())
            .disableColorIndex(json.disableColorIndex())
            .emissive(json.emissive())
            .disableDiffuse(json.disableDiffuse())
            .ambientOcclusion(json.ambientOcclusion())
            .glint(json.glint())
            .shadeMode(json.shadeMode())
            .find()
        );
    }

  public static record MaterialResourceSet(Map<ResourceLocation, Resource> map) {

    }
}
