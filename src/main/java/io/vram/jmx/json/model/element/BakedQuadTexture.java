package io.vram.jmx.json.model.element;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class BakedQuadTexture {
  public static final int OFFSET_TEXTURE = DefaultVertexFormat.BLOCK.getOffset(VertexFormatElement.UV0) / 4;

  public static float getU(int[] vertexData, int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex + OFFSET_TEXTURE + 0]);
  }

  public static float getV(int[] vertexData, int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex + OFFSET_TEXTURE + 1]);
  }

  public static void setU(int[] vertexData, int vertexIndex, float u) {
    vertexData[vertexIndex + OFFSET_TEXTURE + 0] = Float.floatToIntBits(u);
  }

  public static void setV(int[] vertexData, int vertexIndex, float v) {
    vertexData[vertexIndex + OFFSET_TEXTURE + 1] = Float.floatToIntBits(v);
  }
}
