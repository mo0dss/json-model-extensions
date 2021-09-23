/*
 *  Copyright 2019, 2020 grondag
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package grondag.jmx.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import grondag.jmx.json.v1.JmxTexturesExtV1;

@Mixin(TextureAtlas.class)
public class MixinTextureAtlas {
	@Redirect(
		method = "getBasicSpriteInfos",
		at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"),
		require = 1
	)
	Object blockDummySpriteLoad(Iterator<?> it) {
		final Object result = it.next();
		return (ResourceLocation) result == JmxTexturesExtV1.DUMMY_ID ? MissingTextureAtlasSprite.getLocation() : result;
	}
}
