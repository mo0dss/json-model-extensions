package io.vram.jmx.json;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ResourceFunction<V> extends Function<ResourceLocation, V> {

  @Override
  V apply(ResourceLocation location);
}
