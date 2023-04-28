package net.krlite.flash.mixin;

import net.krlite.equator.render.frame.FrameInfo;
import net.krlite.flash.Flash;
import net.krlite.flash.FlashRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	private MatrixStack matrixStack;

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V"))
	private MatrixStack getMatrixStack(MatrixStack matrixStack) {
		this.matrixStack = matrixStack;
		return matrixStack;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.AFTER))
	private void renderScreenshotFlash(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		if (Flash.available()) {
			matrixStack.push();
			matrixStack.translate(FrameInfo.scaled().w() / 2, FrameInfo.scaled().h() / 2, 0);

			FlashRenderer.render(matrixStack, Flash.screenshot());

			matrixStack.pop();
		}
	}
}
