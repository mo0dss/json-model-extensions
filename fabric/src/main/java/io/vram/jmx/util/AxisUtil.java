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


package io.vram.jmx.util;

import net.minecraft.core.Direction;

public class AxisUtil {

    public static final int AXIS_X = Direction.Axis.X.ordinal();
    public static final int AXIS_Y = Direction.Axis.Y.ordinal();
    public static final int AXIS_Z = Direction.Axis.Z.ordinal();

    public static int getX(Direction.Axis axis) {
        return getX(axis.ordinal());
    }

    public static int getX(int axis) {
        if(axis == AXIS_X) {
            return 1;
        }

        return 0;
    }

    public static int getY(Direction.Axis axis) {
        return getY(axis.ordinal());
    }

    public static int getY(int axis) {
        if(axis == AXIS_Y) {
            return 1;
        }

        return 0;
    }

    public static int getZ(Direction.Axis axis) {
        return getZ(axis.ordinal());
    }

    public static int getZ(int axis) {
        if(axis == AXIS_Z) {
            return 1;
        }

        return 0;
    }

    public static int getOppositeX(Direction.Axis axis) {
        return getOppositeX(axis.ordinal());
    }

    public static int getOppositeX(int axis) {
        if(axis == AXIS_X) {
            return 0;
        }

        return 1;
    }

    public static int getOppositeY(Direction.Axis axis) {
        return getOppositeY(axis.ordinal());
    }

    public static int getOppositeY(int axis) {
        if(axis == AXIS_Y) {
            return 0;
        }

        return 1;
    }

    public static int getOppositeZ(Direction.Axis axis) {
        return getOppositeZ(axis.ordinal());
    }

    public static int getOppositeZ(int axis) {
        if(axis == AXIS_Z) {
            return 0;
        }

        return 1;
    }

    public static void calculateWinding() {

    }
}
