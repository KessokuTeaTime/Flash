package net.krlite.flash;

import com.mojang.blaze3d.systems.RenderSystem;
import net.krlite.equator.math.algebra.Theory;
import net.krlite.equator.visual.color.AccurateColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

public class FlashRenderer {
	public static void render(DrawContext context, IntBuffer intBuffer) {
		AccurateColor textureColor = AccurateColor.WHITE, borderColor = Flash.getBorderColor();

		int textureId = GL11.glGenTextures();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, Flash.width(), Flash.height(),
				0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, intBuffer);

		RenderSystem.enableBlend();

		float width = MinecraftClient.getInstance().getWindow().getScaledWidth(), height = MinecraftClient.getInstance().getWindow().getScaledHeight();
		float minWidth = width * (float) Theory.lerp(1, Flash.MIN_WIDTH, Flash.shrink()), minHeight = height * (float) Theory.lerp(1, Flash.MIN_HEIGHT, Flash.shrink());
		float scalar = (float) Theory.lerp(1, Flash.MIN_SCALAR, Flash.shrink());

		context.getMatrices().push();
		context.getMatrices().translate(0, height * Flash.drop(), 0);
		context.getMatrices().scale(scalar, scalar, scalar);

		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

		// Background
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

		color(builder, matrix, -(width / 2 + Flash.BORDER), -(minHeight / 2 + Flash.BORDER), borderColor);	// Top left
		color(builder, matrix, -(minWidth / 2 + Flash.BORDER), height / 2 + Flash.BORDER, borderColor);	// Bottom left
		color(builder, matrix, minWidth / 2 + Flash.BORDER, height / 2 + Flash.BORDER, borderColor);	// Bottom right
		color(builder, matrix, width / 2 + Flash.BORDER, -(minHeight / 2 + Flash.BORDER), borderColor);	// Top right

		BufferRenderer.drawWithGlobalProgram(builder.end());

		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.setShaderTexture(0, textureId);

		builder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_TEXTURE_COLOR);

		for (int y = 0; y < height; y++) {
			textureColor(
					builder, matrix,
					(float) -Theory.lerp(width, minWidth,  y / height) / 2,
					(float) -Theory.lerp(minHeight, height,  y / height) / 2 + y,
					0, y / height, textureColor
			);	// Left

			textureColor(
					builder, matrix,
					(float) Theory.lerp(width, minWidth, y / height) / 2,
					(float) -Theory.lerp(minHeight, height, y / height) / 2 + y,
					1, y / height, textureColor
			);	// Right
		}

		BufferRenderer.drawWithGlobalProgram(builder.end());
		RenderSystem.enableCull();

		context.getMatrices().pop();
	}

	private static void textureColor(BufferBuilder builder, Matrix4f matrix, float x, float y, float u, float v, AccurateColor color) {
		builder.vertex(matrix, x, y, 0).texture(u, v).color(color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat(), color.opacityAsFloat()).next();
	}

	private static void color(BufferBuilder builder, Matrix4f matrix, float x, float y, AccurateColor color) {
		builder.vertex(matrix, x, y, 0).color(color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat(), color.opacityAsFloat()).next();
	}
}
