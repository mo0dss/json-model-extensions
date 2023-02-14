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

import com.mojang.math.Transformation;
import io.vram.jmx.json.JMXFaceBakery;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FaceBakery.class)
public abstract class MixinFaceBakery {
    /**
     * @author Mo0dss
     * @reason Significantly better
     */
    @Overwrite
    public static Direction calculateFacing(int[] vertexData) {
        float x0 = vertexData[0];
        float y0 = vertexData[1];
        float z0 = vertexData[2];

        float x1 = vertexData[(1 * 8) + 0];
        float y1 = vertexData[(1 * 8) + 1];
        float z1 = vertexData[(1 * 8) + 2];

        float x2 = vertexData[(2 * 8) + 0];
        float y2 = vertexData[(2 * 8) + 1];
        float z2 = vertexData[(2 * 8) + 2];

        float highestX = x2 - x0;
        float highestY = y2 - y0;
        float highestZ = z2 - z0;

        float lowestX = x0 - x1;
        float lowestY = y0 - y1;
        float lowestZ = z0 - z1;

        float x = Math.fma(highestY, lowestZ, -highestZ * lowestY);
        float y = Math.fma(highestZ, lowestX, -highestX * lowestZ);
        float z = Math.fma(highestX, lowestY, -highestY * lowestX);

        float crossScale = Math.fma(x, x, Math.fma(y, y, z * z));
        if(crossScale != 0F) {
            float norm = 1F / crossScale;
            x *= norm;
            y *= norm;
            z *= norm;
        }

        if(!Math.isFinite(x) && !Math.isFinite(y) && !Math.isFinite(z)) {
            return Direction.UP;
        }

        return Direction.getNearest(x, y, z);
    }

    /**
     * @author Mo0dss
     * @reason Significantly better
     */
    @Overwrite
    public void recalculateWinding(int[] is, Direction direction) {
        JMXFaceBakery.recalculateWinding(is, direction);
    }

    /**
     * @author Mo0dss
     * @reason Significantly better
     */
    @Overwrite
    public void applyModelRotation(Vector3f vertices, Transformation transformation) {
        JMXFaceBakery.applyModelRotation(vertices, transformation);
    }

    /**
     * @author Mo0dss
     * @reason Significantly better
     */
    @Overwrite
    public void applyElementRotation(Vector3f vertices, BlockElementRotation rotation) {
        JMXFaceBakery.applyElementRotation(vertices, rotation);
    }
}
