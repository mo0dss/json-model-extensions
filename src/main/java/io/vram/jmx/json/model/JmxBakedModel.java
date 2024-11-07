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

package io.vram.jmx.json.model;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import io.vram.jmx.json.JmxRendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class JmxBakedModel implements BakedModel, FabricBakedModel {
	protected final Mesh mesh;
	protected WeakReference<List<BakedQuad>[]> quadLists = null;
	protected final boolean usesAo;
	protected final boolean isSideLit;
	protected final TextureAtlasSprite particleSprite;
	protected final ItemTransforms transformation;
	protected final BakedOverrides itemPropertyOverrides;
	protected final boolean hasDepth;

	public JmxBakedModel(Mesh mesh, boolean usesAo, boolean isSideLit, TextureAtlasSprite particleSprite, ItemTransforms transformation, BakedOverrides itemPropertyOverrides, boolean hasDepth) {
		this.mesh = mesh;
		this.usesAo = usesAo;
		this.isSideLit = isSideLit;
		this.particleSprite = particleSprite;
		this.transformation = transformation;
		this.itemPropertyOverrides = itemPropertyOverrides;
		this.hasDepth = hasDepth;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource rand) {
		List<BakedQuad>[] lists = quadLists == null ? null : quadLists.get();

		if (lists == null) {
			lists = toQuadLists(mesh, particleSprite);
			quadLists = new WeakReference<>(lists);
		}

		final List<BakedQuad> result = lists[face == null ? ModelHelper.NULL_FACE_ID : face.get3DDataValue()];
		return result == null ? ImmutableList.of() : result;
	}

	private static List<BakedQuad>[] toQuadLists(Mesh mesh, TextureAtlasSprite particleSprite) {
		try {
			return ModelHelper.toQuadLists(mesh);
		} catch (final Exception e) {
			return safeToQuadLists(mesh, particleSprite);
		}
	}

	/**
	 * Workaround for Fabric helper breaking when called before the sprite atlas is created.
	 * Triggered by AE2 when running with JSON mesh loading active.
	 *
	 * <p>Only difference is we use our particle sprite instead of looking one up.
	 */
	private static List<BakedQuad>[] safeToQuadLists(Mesh mesh, TextureAtlasSprite particleSprite) {
		@SuppressWarnings("unchecked")
		final ImmutableList.Builder<BakedQuad>[] builders = new ImmutableList.Builder[7];

		for (int i = 0; i < 7; i++) {
			builders[i] = ImmutableList.builder();
		}

		if (mesh != null) {
			mesh.forEach(q -> {
				final Direction face = q.cullFace();
				builders[face == null ? ModelHelper.NULL_FACE_ID : face.get3DDataValue()].add(q.toBakedQuad(particleSprite));
			});
		}

		@SuppressWarnings("unchecked")
		final List<BakedQuad>[] result = new List[7];

		for (int i = 0; i < 7; i++) {
			result[i] = builders[i].build();
		}

		return result;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return usesAo;
	}

	@Override
	public boolean isGui3d() {
		return hasDepth;
	}

	@Override
	public boolean usesBlockLight() {
		return isSideLit;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return particleSprite;
	}

	@Override
	public ItemTransforms getTransforms() {
		return transformation;
	}

	@Override
	public BakedOverrides overrides() {
		return itemPropertyOverrides;
	}

	public static class Builder {
		private final MeshBuilder meshBuilder;
		public final QuadEmitter emitter;
		private final BakedOverrides itemPropertyOverrides;
		public final boolean usesAo;
		private TextureAtlasSprite particleTexture;
		private final boolean isSideLit;
		private final ItemTransforms transformation;
		private final boolean hasDepth;
		@Nullable
		private final ResourceLocation quadTransformId;

		public Builder(BlockModel unbakedModel, BakedOverrides itemPropertyOverrides, boolean hasDepth, @Nullable ResourceLocation quadTransformId) {
			this(unbakedModel.hasAmbientOcclusion(), unbakedModel.getGuiLight().lightLikeBlock(), unbakedModel.getTransforms(), itemPropertyOverrides, hasDepth, quadTransformId);
		}

		private Builder(boolean usesAo, boolean isSideLit, ItemTransforms transformation, BakedOverrides itemPropertyOverrides, boolean hasDepth, @Nullable ResourceLocation quadTransformId) {
			meshBuilder = JmxRendererAccess.getRenderer().meshBuilder();
			emitter = meshBuilder.getEmitter();
			this.itemPropertyOverrides = itemPropertyOverrides;
			this.usesAo = usesAo;
			this.isSideLit = isSideLit;
			this.transformation = transformation;
			this.hasDepth = hasDepth;
			this.quadTransformId = quadTransformId;
		}

		public Builder setParticle(TextureAtlasSprite sprite) {
			particleTexture = sprite;
			return this;
		}

		public BakedModel build() {
			if (particleTexture == null) {
				throw new RuntimeException("Missing particle!");
			}

			return new JmxBakedModel(meshBuilder.build(), usesAo, isSideLit, particleTexture, transformation, itemPropertyOverrides, hasDepth);
		}
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (mesh != null) {
			mesh.outputTo(context.getEmitter());
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (mesh != null) {
			mesh.outputTo(context.getEmitter());
		}
	}
}

