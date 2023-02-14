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

package io.vram.jmx.json;

import com.mojang.math.Transformation;
import io.vram.jmx.util.AxisUtil;
import me.jellysquid.mods.sodium.common.util.MatrixHelper;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class JMXFaceBakery {
    private static final float RESCALE_22_5 = (1.0F / Mth.cos(Mth.PI / 8)) - 1.0F;
    private static final float RESCALE_45 = (1.0F / Mth.cos(Mth.PI / 4)) - 1.0F;

    private static final float POSITION_NORM = 1F / 16F;

    private static final ThreadLocal<BlockElementQuadData> blockElementData = ThreadLocal.withInitial(BlockElementQuadData::new);

    public static void bake(QuadEmitter emitter, int spriteIdx,
                            BlockElement element, BlockElementFace face,
                            BlockFaceUV uv, TextureAtlasSprite sprite,
                            Direction cullFace, ModelState state, ResourceLocation identifier) {
        var blockData = blockElementData.get();
        final BlockElementRotation modelRotation = element.rotation;

        if (state.isUvLocked()) {
            uv = FaceBakery.recomputeUVs(face.uv, cullFace, state.getRotation(), identifier);
        }

        // preserve tex data in case needed again (can have two passes)
        final float[] uvs = blockData.uv;
        System.arraycopy(uv.uvs, 0, uvs, 0, 4);

        float uCent = sprite.contents().width() / (sprite.getU1() - sprite.getU0());
        float vCent = sprite.contents().height() / (sprite.getV1() - sprite.getV0());
        float uvCent = 0.25F * Math.max(vCent, uCent);

        float uAdj = (uv.uvs[0] + uv.uvs[0] + uv.uvs[2] + uv.uvs[2]) * 0.25F;
        float vAdj = (uv.uvs[1] + uv.uvs[1] + uv.uvs[3] + uv.uvs[3]) * 0.25F;
        uv.uvs[0] = Mth.lerp(uvCent, uv.uvs[0], uAdj);
        uv.uvs[2] = Mth.lerp(uvCent, uv.uvs[2], uAdj);
        uv.uvs[1] = Mth.lerp(uvCent, uv.uvs[1], vAdj);
        uv.uvs[3] = Mth.lerp(uvCent, uv.uvs[3], vAdj);

        int[] vertexData = createVertices(blockData.data, uv, sprite, cullFace, normalize(blockData.pos, element.from, element.to), state.getRotation(), modelRotation);
        Direction nominalFace = FaceBakery.calculateFacing(vertexData);

        // restore tex data
        System.arraycopy(uvs, 0, uv.uvs, 0, 4);

        if (modelRotation == null) {
            recalculateWinding(vertexData, nominalFace);
        }

        writeVertices(emitter, spriteIdx, sprite, nominalFace, vertexData);
    }

    private static <T extends MutableQuadView> void writeVertices(T quad, int spriteIdx, TextureAtlasSprite sprite, Direction facing, int[] vertexData) {
        quad.nominalFace(facing);

        for(int i = 0; i < 4; i++) {
            int vertexIdx = (i * 8);

            float x = Float.intBitsToFloat(vertexData[vertexIdx + 0]);
            float y = Float.intBitsToFloat(vertexData[vertexIdx + 1]);
            float z = Float.intBitsToFloat(vertexData[vertexIdx + 2]);

            int color = vertexData[vertexIdx + 3];

            float u = Float.intBitsToFloat(vertexData[vertexIdx + 4]);
            float v = Float.intBitsToFloat(vertexData[vertexIdx + 5]);

            int light = vertexData[vertexIdx + 6];

            quad.pos(i, x, y, z);
            quad.spriteColor(i, spriteIdx, color);
            quad.sprite(i, spriteIdx, u, v);
            quad.lightmap(i, light);
        }

        quad.spriteBake(spriteIdx, sprite, 0b0);
    }

    //TODO: Finish optimise
    public static void recalculateWinding(int[] vertexData, Direction direction) {
        int[] js = new int[vertexData.length];
        System.arraycopy(vertexData, 0, js, 0, vertexData.length);
        float[] fs = new float[Direction.values().length];
        fs[FaceInfo.Constants.MIN_X] = 999.0F;
        fs[FaceInfo.Constants.MIN_Y] = 999.0F;
        fs[FaceInfo.Constants.MIN_Z] = 999.0F;
        fs[FaceInfo.Constants.MAX_X] = -999.0F;
        fs[FaceInfo.Constants.MAX_Y] = -999.0F;
        fs[FaceInfo.Constants.MAX_Z] = -999.0F;

        for(int i = 0; i < 4; ++i) {
            int j = 8 * i;
            float f = Float.intBitsToFloat(js[j]);
            float g = Float.intBitsToFloat(js[j + 1]);
            float h = Float.intBitsToFloat(js[j + 2]);
            if (f < fs[FaceInfo.Constants.MIN_X]) {
                fs[FaceInfo.Constants.MIN_X] = f;
            }

            if (g < fs[FaceInfo.Constants.MIN_Y]) {
                fs[FaceInfo.Constants.MIN_Y] = g;
            }

            if (h < fs[FaceInfo.Constants.MIN_Z]) {
                fs[FaceInfo.Constants.MIN_Z] = h;
            }

            if (f > fs[FaceInfo.Constants.MAX_X]) {
                fs[FaceInfo.Constants.MAX_X] = f;
            }

            if (g > fs[FaceInfo.Constants.MAX_Y]) {
                fs[FaceInfo.Constants.MAX_Y] = g;
            }

            if (h > fs[FaceInfo.Constants.MAX_Z]) {
                fs[FaceInfo.Constants.MAX_Z] = h;
            }
        }

        FaceInfo faceInfo = FaceInfo.fromFacing(direction);

        for(int j = 0; j < 4; ++j) {
            int k = 8 * j;
            FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(j);
            float h = fs[vertexInfo.xFace];
            float l = fs[vertexInfo.yFace];
            float m = fs[vertexInfo.zFace];
            vertexData[k] = Float.floatToRawIntBits(h);
            vertexData[k + 1] = Float.floatToRawIntBits(l);
            vertexData[k + 2] = Float.floatToRawIntBits(m);

            for(int n = 0; n < 4; ++n) {
                int o = 8 * n;
                float p = Float.intBitsToFloat(js[o]);
                float q = Float.intBitsToFloat(js[o + 1]);
                float r = Float.intBitsToFloat(js[o + 2]);
                if (Mth.equal(h, p) && Mth.equal(l, q) && Mth.equal(m, r)) {
                    vertexData[k + 4] = js[o + 4];
                    vertexData[k + 4 + 1] = js[o + 4 + 1];
                }
            }
        }
    }

    private static int[] createVertices(int[] target, BlockFaceUV tex, TextureAtlasSprite sprite, Direction face, float[] pos, Transformation texRotation, @Nullable net.minecraft.client.renderer.block.model.BlockElementRotation modelRotation) {
        var facing = FaceInfo.fromFacing(face);

        if(modelRotation != null) {
            var axis = modelRotation.axis();
            var mat = new Matrix4f()
                    .rotation(modelRotation.angle() * Mth.DEG_TO_RAD, AxisUtil.getX(axis), AxisUtil.getY(axis), AxisUtil.getZ(axis));

            for (int idx = 0; idx < 4; idx++) {
                var vertexInfo = facing.getVertexInfo(idx);

                float x = pos[vertexInfo.xFace];
                float y = pos[vertexInfo.yFace];
                float z = pos[vertexInfo.zFace];

                float x2 = applyElementRotationX(mat, x, y, z, modelRotation);
                float y2 = applyElementRotationY(mat, x, y, z, modelRotation);
                float z2 = applyElementRotationZ(mat, x, y, z, modelRotation);

                float x3 = applyModelRotationX(x2, y2, z2, texRotation);
                float y3 = applyModelRotationY(x2, y2, z2, texRotation);
                float z3 = applyModelRotationZ(x2, y2, z2, texRotation);

                setVertex(target, idx, x3, y3, z3, sprite, tex);
            }

            return target;
        }

        for (int idx = 0; idx < 4; idx++) {
            var vertexInfo = facing.getVertexInfo(idx);

            float x = pos[vertexInfo.xFace];
            float y = pos[vertexInfo.yFace];
            float z = pos[vertexInfo.zFace];

            float x2 = applyModelRotationX(x, y, z, texRotation);
            float y2 = applyModelRotationY(x, y, z, texRotation);
            float z2 = applyModelRotationZ(x, y, z, texRotation);

            setVertex(target, idx, x2, y2, z2, sprite, tex);
        }

        return target;
    }

    private static void setVertex(int[] vertexData, int idx, float x, float y, float z, TextureAtlasSprite sprite, BlockFaceUV uv) {
        var vertexIdx = (idx * 8);

        //Position
        vertexData[vertexIdx + 0] = Float.floatToRawIntBits(x);
        vertexData[vertexIdx + 1] = Float.floatToRawIntBits(y);
        vertexData[vertexIdx + 2] = Float.floatToRawIntBits(z);

        //Color
        vertexData[vertexIdx + 3] = -1;

        //Texture
        vertexData[vertexIdx + 4] = Float.floatToRawIntBits(sprite.getU(uv.getU(vertexIdx)));
        vertexData[vertexIdx + 5] = Float.floatToRawIntBits(sprite.getV(uv.getV(vertexIdx)));

        //Light
        vertexData[vertexIdx + 6] = 0;
    }

    private static float[] normalize(float[] targets, Vector3f min, Vector3f max) {
        targets[FaceInfo.Constants.MIN_X] = min.x() * POSITION_NORM;
        targets[FaceInfo.Constants.MIN_Y] = min.y() * POSITION_NORM;
        targets[FaceInfo.Constants.MIN_Z] = min.z() * POSITION_NORM;

        targets[FaceInfo.Constants.MAX_X] = max.x() * POSITION_NORM;
        targets[FaceInfo.Constants.MAX_Y] = max.y() * POSITION_NORM;
        targets[FaceInfo.Constants.MAX_Z] = max.z() * POSITION_NORM;

        return targets;
    }

    public static void applyModelRotation(Vector3f vertices, Transformation transformation) {
        if(transformation == Transformation.identity()) {
            return;
        }

        var mat = transformation.getMatrix();

        float x = vertices.x() - 0.5F;
        float y = vertices.y() - 0.5F;
        float z = vertices.z() - 0.5F;

        float x2 = MatrixHelper.transformPositionX(mat, x, y, z);
        float y2 = MatrixHelper.transformPositionY(mat, x, y, z);
        float z2 = MatrixHelper.transformPositionZ(mat, x, y, z);

        vertices.set(
                x2 + 0.5F,
                y2 + 0.5F,
                z2 + 0.5F
        );
    }

    public static float applyModelRotationX(float x, float y, float z, Transformation transformation) {
        if(transformation == Transformation.identity()) {
            return x;
        }

        var mat = transformation.getMatrix();

        float x2 = x - 0.5F;
        float y2 = y - 0.5F;
        float z2 = z - 0.5F;

        float x3 = MatrixHelper.transformPositionX(mat, x2, y2, z2);

        return x3 + 0.5F;
    }

    public static float applyModelRotationY(float x, float y, float z, Transformation transformation) {
        if(transformation == Transformation.identity()) {
            return y;
        }

        var mat = transformation.getMatrix();

        float x2 = x - 0.5F;
        float y2 = y - 0.5F;
        float z2 = z - 0.5F;

        float y3 = MatrixHelper.transformPositionY(mat, x2, y2, z2);

        return y3 + 0.5F;
    }

    public static float applyModelRotationZ(float x, float y, float z, Transformation transformation) {
        if(transformation == Transformation.identity()) {
            return z;
        }

        var mat = transformation.getMatrix();

        float x2 = x - 0.5F;
        float y2 = y - 0.5F;
        float z2 = z - 0.5F;

        float z3 = MatrixHelper.transformPositionZ(mat, x2, y2, z2);

        return z3 + 0.5F;
    }

    public static void applyElementRotation(Vector3f vertices, @Nullable BlockElementRotation rotation) {
        if(rotation == null) {
            return;
        }

        var axis = rotation.axis();
        var origin = rotation.origin();
        var mat = new Matrix4f()
                .rotation(rotation.angle() * Mth.DEG_TO_RAD, AxisUtil.getX(axis), AxisUtil.getY(axis), AxisUtil.getZ(axis));

        float x = vertices.x() - origin.x();
        float y = vertices.y() - origin.y();
        float z = vertices.z() - origin.z();

        float x2 = MatrixHelper.transformPositionX(mat, x, y, z);
        float y2 = MatrixHelper.transformPositionY(mat, x, y, z);
        float z2 = MatrixHelper.transformPositionZ(mat, x, y, z);

        //If the rotation doesn't get rescaled, exit early
        if(!rotation.rescale()) {
            vertices.set(
                    x2 + origin.x(),
                    y2 + origin.y(),
                    z2 + origin.z()
            );
            return;
        }

        float scaleX = AxisUtil.getOppositeX(axis);
        float scaleY = AxisUtil.getOppositeY(axis);
        float scaleZ = AxisUtil.getOppositeZ(axis);

        //Scale fixes
        if (Math.abs(rotation.angle()) == 22.5F) {
            scaleX *= RESCALE_22_5;
            scaleY *= RESCALE_22_5;
            scaleZ *= RESCALE_22_5;
        } else {
            scaleX *= RESCALE_45;
            scaleY *= RESCALE_45;
            scaleZ *= RESCALE_45;
        }

        //Scale back to normal
        scaleX += 1F;
        scaleY += 1F;
        scaleZ += 1F;

        //Finally scale vertex positions
        x2 *= scaleX;
        y2 *= scaleY;
        z2 *= scaleZ;

        vertices.set(
                x2 + origin.x(),
                y2 + origin.y(),
                z2 + origin.z()
        );
    }

    public static float applyElementRotationX(Matrix4f mat, float x, float y, float z, BlockElementRotation rotation) {
        var axis = rotation.axis();
        var origin = rotation.origin();

        float x2 = x - origin.x();
        float y2 = y - origin.y();
        float z2 = z - origin.z();

        float x3 = MatrixHelper.transformPositionX(mat, x2, y2, z2);

        //If the rotation doesn't get rescaled, exit early
        if(!rotation.rescale()) {
            return x3 + origin.x();
        }

        float scaleX = AxisUtil.getOppositeX(axis);

        //Scale fixes
        if (Math.abs(rotation.angle()) == 22.5F) {
            scaleX *= RESCALE_22_5;
        } else {
            scaleX *= RESCALE_45;
        }

        //Scale back to normal
        scaleX += 1F;

        //Finally scale vertex positions
        x3 *= scaleX;

        return x3 + origin.x();
    }

    public static float applyElementRotationY(Matrix4f mat, float x, float y, float z, BlockElementRotation rotation) {
        var axis = rotation.axis();
        var origin = rotation.origin();

        float x2 = x - origin.x();
        float y2 = y - origin.y();
        float z2 = z - origin.z();

        float y3 = MatrixHelper.transformPositionY(mat, x2, y2, z2);

        //If the rotation doesn't get rescaled, exit early
        if(!rotation.rescale()) {
            return y3 + origin.y();
        }

        float scaleY = AxisUtil.getOppositeY(axis);

        //Scale fixes
        if (Math.abs(rotation.angle()) == 22.5F) {
            scaleY *= RESCALE_22_5;
        } else {
            scaleY *= RESCALE_45;
        }

        //Scale back to normal
        scaleY += 1F;

        //Finally scale vertex positions
        y3 *= scaleY;

        return y3 + origin.y();
    }

    public static float applyElementRotationZ(Matrix4f mat, float x, float y, float z, BlockElementRotation rotation) {
        var axis = rotation.axis();
        var origin = rotation.origin();

        float x2 = x - origin.x();
        float y2 = y - origin.y();
        float z2 = z - origin.z();

        float z3 = MatrixHelper.transformPositionZ(mat, x2, y2, z2);

        //If the rotation doesn't get rescaled, exit early
        if(!rotation.rescale()) {
            return z3 + origin.z();
        }

        float scaleZ = AxisUtil.getOppositeZ(axis);

        //Scale fixes
        if (Math.abs(rotation.angle()) == 22.5F) {
            scaleZ *= RESCALE_22_5;
        } else {
            scaleZ *= RESCALE_45;
        }

        //Scale back to normal
        scaleZ += 1F;

        //Finally scale vertex positions
        z3 *= scaleZ;

        return z3 + origin.x();
    }
}
