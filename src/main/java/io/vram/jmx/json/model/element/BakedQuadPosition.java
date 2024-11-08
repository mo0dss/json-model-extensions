package io.vram.jmx.json.model.element;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class BakedQuadPosition {
  public static final int OFFSET_POSITION = DefaultVertexFormat.BLOCK.getOffset(VertexFormatElement.POSITION) / 4;

  public static float getX(int[] vertexData, int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex + OFFSET_POSITION + 0]);
  }

  public static float getY(int[] vertexData, int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex + OFFSET_POSITION + 1]);
  }

  public static float getZ(int[] vertexData, int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex + OFFSET_POSITION + 2]);
  }

  public static void setX(int[] vertexData, int vertexIndex, float x) {
    vertexData[vertexIndex + OFFSET_POSITION + 0] = Float.floatToRawIntBits(x);
  }

  public static void setY(int[] vertexData, int vertexIndex, float y) {
    vertexData[vertexIndex + OFFSET_POSITION + 1] = Float.floatToIntBits(y);
  }

  public static void setZ(int[] vertexData, int vertexIndex, float z) {
    vertexData[vertexIndex + OFFSET_POSITION + 2] = Float.floatToIntBits(z);
  }
}
