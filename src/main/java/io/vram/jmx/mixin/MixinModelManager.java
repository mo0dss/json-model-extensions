/*
 * This file is part of JSON Model Extensions and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.vram.jmx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.vram.jmx.Configurator;
import io.vram.jmx.client.JsonModelExtensions;
import io.vram.jmx.client.MaterialResourceManager;
import io.vram.jmx.json.v1.JmxModelExtV1;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public class MixinModelManager {
	 @WrapOperation(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;getBakedTopLevelModels()Ljava/util/Map;"))
   Map<ModelResourceLocation, BakedModel> logErrorPresence(ModelBakery instance, Operation<Map<ModelResourceLocation, BakedModel>> original) {
     if (!Configurator.logResolutionErrors && JmxModelExtV1.HAS_ERROR) {
       JsonModelExtensions.LOG.warn("One or more errors occurred in JMX model(s). Enable `log-resolution-errors` in config/jmx.properties to display all errors.");
     }
     return original.call(instance);
   }

  @WrapOperation(
    method = "reload",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/resources/model/ModelManager;loadBlockModels(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
    )
  )
  private CompletableFuture<Map<ResourceLocation, UnbakedModel>> onLoadBlockModels(
    ResourceManager resourceManager, Executor executor, Operation<CompletableFuture<Map<ResourceLocation, UnbakedModel>>> original
  ) {
    // A major feature that had ended up been noticed while in the absence of FREX
    // Render Material Serialization
    // Ive managed to push a simple resource manager to initialise these materials before
    // any block models load otherwise it's a bunch of threading problems
    return CompletableFuture.supplyAsync(() -> MaterialResourceManager.prepare(resourceManager, Profiler.get()), executor)
      .thenAcceptAsync((materials) -> {
        MaterialResourceManager.apply(materials, resourceManager, Profiler.get());
      }, executor).thenCompose((void_) -> original.call(resourceManager, executor));
  }
}
