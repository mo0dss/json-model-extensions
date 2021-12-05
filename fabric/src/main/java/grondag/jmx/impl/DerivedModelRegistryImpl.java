/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.jmx.impl;

import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.server.packs.resources.ResourceManager;

import io.vram.frex.api.model.provider.ModelProvider;
import io.vram.frex.api.model.provider.SubModelLoader;

import grondag.jmx.JsonModelExtensions;
import grondag.jmx.json.model.LazyModelDelegate;

public class DerivedModelRegistryImpl implements DerivedModelRegistry, ModelProvider<ModelResourceLocation>, Function<ResourceManager, ModelProvider<ModelResourceLocation>> {
	private DerivedModelRegistryImpl() { }

	public static final DerivedModelRegistryImpl INSTANCE = new DerivedModelRegistryImpl();

	private final Object2ObjectOpenHashMap<String, Pair<String, ModelTransformer>> blockModels = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<String, Pair<String, ModelTransformer>> itemModels = new Object2ObjectOpenHashMap<>();
	private boolean isEmpty = true;

	public boolean isEmpty() {
		return isEmpty;
	}

	@Override
	public void addBlock(String targetModel, String sourceModel, ModelTransformer transform) {
		isEmpty = false;
		blockModels.put(targetModel, Pair.of(sourceModel, transform));
	}

	@Override
	public void addItem(String targetModel, String sourceModel, ModelTransformer transform) {
		isEmpty = false;
		itemModels.put(targetModel, Pair.of(sourceModel, transform));
	}

	@Override
	public void addBlockWithItem(String targetModel, String sourceModel, ModelTransformer transform) {
		addBlock(targetModel, sourceModel, transform);
		addItem(targetModel, sourceModel, transform);
	}

	@Override
	public ModelProvider<ModelResourceLocation> apply(ResourceManager t) {
		JsonModelExtensions.initializeEndpointsOnce();
		return this;
	}

	@Override
	public @Nullable UnbakedModel loadModel(ModelResourceLocation path, SubModelLoader subModelLoader) {
		final String fromString = path.getNamespace() + ":" + path.getPath();
		final Pair<String, ModelTransformer> match = path.getVariant().equals("inventory")
				? itemModels.get(fromString) : blockModels.get(fromString);

		if (match != null) {
			final ModelResourceLocation templateId = new ModelResourceLocation(match.getLeft(), path.getVariant());
			return new LazyModelDelegate(templateId, match.getRight());
		}

		return null;
	}
}
