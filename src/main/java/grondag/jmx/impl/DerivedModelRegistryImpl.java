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

package grondag.jmx.impl;

import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;

import grondag.jmx.JsonModelExtensions;
import grondag.jmx.json.model.LazyModelDelegate;

public class DerivedModelRegistryImpl implements DerivedModelRegistry, ModelVariantProvider, Function<ResourceManager, ModelVariantProvider> {
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
	public UnbakedModel loadModelVariant(ModelResourceLocation modelId, ModelProviderContext context) throws ModelProviderException {
		final String fromString = modelId.getNamespace() + ":" + modelId.getPath();
		final Pair<String, ModelTransformer> match = modelId.getVariant().equals("inventory")
				? itemModels.get(fromString) : blockModels.get(fromString);

		if (match != null) {
			final ModelResourceLocation templateId = new ModelResourceLocation(match.getLeft(), modelId.getVariant());
			return new LazyModelDelegate(templateId, match.getRight());
		}

		return null;
	}

	@Override
	public ModelVariantProvider apply(ResourceManager resourceManager) {
		JsonModelExtensions.initializeEndpointsOnce();
		return this;
	}
}
