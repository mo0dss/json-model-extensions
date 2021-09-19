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

import net.minecraft.resources.ResourceLocation;

import grondag.jmx.api.RetexturedModelBuilder;

public class RexturedModelBuilderImpl implements RetexturedModelBuilder {
	public static RetexturedModelBuilder builder(ResourceLocation sourceModel, ResourceLocation targetModel) {
		return new RexturedModelBuilderImpl(sourceModel, targetModel);
	}

	RetexturedModelTransformer.Builder builder;

	private void checkNotComplete() {
		if (builder == null) {
			throw new IllegalStateException("Attempt to modify RetextureModelBuilder after complete().");
		}
	}

	RexturedModelBuilderImpl(ResourceLocation sourceModel, ResourceLocation targetModel) {
		builder = RetexturedModelTransformer.builder(sourceModel, targetModel);
	}

	@Override
	public RetexturedModelBuilder mapSprite(ResourceLocation from, ResourceLocation to) {
		checkNotComplete();
		builder.mapSprite(from, to);
		return this;
	}

	@Override
	public RetexturedModelBuilder mapSprite(String from, String to) {
		checkNotComplete();
		builder.mapSprite(from, to);
		return this;
	}

	public RetexturedModelTransformer build() {
		checkNotComplete();
		final RetexturedModelTransformer result = builder.build();
		builder = null;
		return result;
	}

	@Override
	public void completeBlock() {
		final RetexturedModelTransformer transform = build();
		DerivedModelRegistryImpl.INSTANCE.addBlock(transform.targetModel.toString(), transform.sourceModel.toString(), transform);
	}

	@Override
	public void completeItem() {
		final RetexturedModelTransformer transform = build();
		DerivedModelRegistryImpl.INSTANCE.addItem(transform.targetModel.toString(), transform.sourceModel.toString(), transform);
	}

	@Override
	public void completeBlockWithItem() {
		final RetexturedModelTransformer transform = build();
		DerivedModelRegistryImpl.INSTANCE.addBlockWithItem(transform.targetModel.toString(), transform.sourceModel.toString(), transform);
	}
}
