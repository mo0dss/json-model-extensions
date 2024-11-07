package io.vram.jmx.json;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.resources.ResourceLocation;

public class JmxRendererAccess {

  public static Renderer getRenderer() {
    return Preconditions.checkNotNull(RendererAccess.INSTANCE.getRenderer(), "JMX requires an active renderer");
  }

  public static RenderMaterial getMaterial(ResourceLocation location) {
    return JmxRendererAccess.getRenderer().materialById(location);
  }
}
