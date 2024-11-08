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

import io.vram.jmx.json.model.element.BakedQuadPosition;
import io.vram.jmx.json.model.element.BakedQuadTexture;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.math.Transformation;

import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import io.vram.jmx.json.model.BakedQuadFactoryExt;
import io.vram.jmx.json.model.BakedQuadFactoryHelper;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FaceBakery.class)
public abstract class MixinFaceBakery implements BakedQuadFactoryExt {
	@Shadow
	protected abstract void applyElementRotation(Vector3f vector3f_1, @Nullable net.minecraft.client.renderer.block.model.BlockElementRotation modelRotation_1);

	@Shadow
	protected abstract void recalculateWinding(int[] data, Direction face);

  @Shadow
  @Final
  private static int COLOR_INDEX;

  @Unique
  private final Vector3f transferVector = new Vector3f();

  @Unique
  private static final float[] VERTEX_FACINGS = new float[6];

  @Unique
  private static final int JMX_MAGIC_TAG = 0x27553f;

	@Override
	public void jmx_bake(QuadEmitter q, int spriteIndex, BlockElement element, BlockElementFace elementFace, BlockFaceUV tex, TextureAtlasSprite sprite, Direction face, ModelState bakeProps, ResourceLocation modelId) {
		final BakedQuadFactoryHelper help = BakedQuadFactoryHelper.get();
		final net.minecraft.client.renderer.block.model.BlockElementRotation modelRotation = element.rotation;

		if (bakeProps.isUvLocked()) {
			tex = FaceBakery.recomputeUVs(elementFace.uv(), face, bakeProps.getRotation());
		}

		// preserve tex data in case needed again (can have two passes)
		final float[] uvs = help.uv;
		System.arraycopy(tex.uvs, 0, uvs, 0, BakedQuadFactoryHelper.UV_LEN);

		final float uvCent = sprite.uvShrinkRatio();

		final float uAdj = (tex.uvs[0] + tex.uvs[0] + tex.uvs[2] + tex.uvs[2]) / 4.0F;
		final float vAdj = (tex.uvs[1] + tex.uvs[1] + tex.uvs[3] + tex.uvs[3]) / 4.0F;

		tex.uvs[0] = Mth.lerp(uvCent, tex.uvs[0], uAdj);
		tex.uvs[2] = Mth.lerp(uvCent, tex.uvs[2], uAdj);
		tex.uvs[1] = Mth.lerp(uvCent, tex.uvs[1], vAdj);
		tex.uvs[3] = Mth.lerp(uvCent, tex.uvs[3], vAdj);

    jmx_initVertexFacings(element.from, element.to);

		final int[] vertexData = jmx_buildVertexData(help.data, tex, sprite, face, bakeProps.getRotation(), modelRotation);
		final Direction nominalFace = FaceBakery.calculateFacing(vertexData);

		// restore tex data
		System.arraycopy(uvs, 0, tex.uvs, 0, BakedQuadFactoryHelper.UV_LEN);

		if (modelRotation == null) {
			recalculateWinding(vertexData, nominalFace);
		}

    for (var i = 0; i < 4; i++) {
      var cornerIndex = i * 8;

      q.pos(i,
        BakedQuadPosition.getX(vertexData, cornerIndex),
        BakedQuadPosition.getY(vertexData, cornerIndex),
        BakedQuadPosition.getZ(vertexData, cornerIndex)
      );

      q.uv(i,
        BakedQuadTexture.getU(vertexData, cornerIndex),
        BakedQuadTexture.getV(vertexData, cornerIndex)
      );
    }

		q.nominalFace(nominalFace);
		q.color(-1, -1, -1, -1);
		q.lightmap(0, 0, 0, 0);

    int lightEmission = element.lightEmission;
    if (lightEmission > 0 && !q.material().emissive()) {
      for(int i = 0; i < 4; ++i) {
        q.lightmap(i, LightTexture.lightCoordsWithEmission(q.lightmap(i), lightEmission));
      }
    }

		q.spriteBake(sprite, 0);
    q.tag(JMX_MAGIC_TAG);
	}

	private int[] jmx_buildVertexData(int[] target, BlockFaceUV tex, TextureAtlasSprite sprite, Direction face, Transformation texRotation, @Nullable net.minecraft.client.renderer.block.model.BlockElementRotation modelRotation) {
		for (int i = 0; i < 4; ++i) {
			jmx_bakeVertex(target, i, face, tex, sprite, texRotation, modelRotation, this.transferVector);
		}

		return target;
	}

	private void jmx_bakeVertex(int[] data, int vertexIn, Direction face, BlockFaceUV tex, TextureAtlasSprite sprite, Transformation modelRotation_1, @Nullable net.minecraft.client.renderer.block.model.BlockElementRotation modelRotation, Vector3f transferVector) {
		final FaceInfo.VertexInfo cubeFace$Corner_1 = FaceInfo.fromFacing(face).getVertexInfo(vertexIn);
		final Vector3f pos = transferVector.set(VERTEX_FACINGS[cubeFace$Corner_1.xFace], VERTEX_FACINGS[cubeFace$Corner_1.yFace], VERTEX_FACINGS[cubeFace$Corner_1.zFace]);
		applyElementRotation(pos, modelRotation);
		((FaceBakery) (Object) this).applyModelRotation(pos, modelRotation_1);
		jmx_packVertexData(data, vertexIn, pos, sprite, tex);
	}

	// NB: name must not conflict with vanilla names - somehow acts as an override if does, even though private
	private static void jmx_packVertexData(int[] vertices, int cornerIndex, Vector3f position, TextureAtlasSprite sprite, BlockFaceUV modelElementTexture) {
		final int i = cornerIndex * 8;

    BakedQuadPosition.setX(vertices, i, position.x());
    BakedQuadPosition.setY(vertices, i, position.y());
    BakedQuadPosition.setZ(vertices, i, position.z());

		vertices[i + COLOR_INDEX] = -1;

    BakedQuadTexture.setU(vertices, i, modelElementTexture.getU(cornerIndex));
    BakedQuadTexture.setV(vertices, i, modelElementTexture.getV(cornerIndex));
	}

	private static void jmx_initVertexFacings(Vector3f vector3f_1, Vector3f vector3f_2) {
    VERTEX_FACINGS[FaceInfo.Constants.MIN_X] = vector3f_1.x() / 16.0F;
    VERTEX_FACINGS[FaceInfo.Constants.MIN_Y] = vector3f_1.y() / 16.0F;
    VERTEX_FACINGS[FaceInfo.Constants.MIN_Z] = vector3f_1.z() / 16.0F;
    VERTEX_FACINGS[FaceInfo.Constants.MAX_X] = vector3f_2.x() / 16.0F;
    VERTEX_FACINGS[FaceInfo.Constants.MAX_Y] = vector3f_2.y() / 16.0F;
    VERTEX_FACINGS[FaceInfo.Constants.MAX_Z] = vector3f_2.z() / 16.0F;
	}
}
