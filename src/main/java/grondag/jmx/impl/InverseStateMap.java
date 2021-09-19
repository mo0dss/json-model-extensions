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

import net.minecraft.world.level.block.state.BlockState;

/**
 * Used in transformed multi-part models to map the new model's block
 * states back to the original model's block states so that the original
 * model's predicate functions can be reused without modification.
 */
@FunctionalInterface
public interface InverseStateMap {
	BlockState invert(BlockState fromState);
}
