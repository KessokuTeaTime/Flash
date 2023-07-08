package net.krlite.flash.mixin;

import net.krlite.flash.Flash;
import net.krlite.flash.FlashRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Unique
	private MatrixStack matrixStack;

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V"))
	private MatrixStack getMatrixStack(MatrixStack matrixStack) {
		this.matrixStack = matrixStack;
		return matrixStack;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.AFTER))
	private void renderScreenshotFlash(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		if (!MinecraftClient.getInstance().skipGameRender && Flash.available() && matrixStack != null) {
			matrixStack.push();
			matrixStack.translate(MinecraftClient.getInstance().getWindow().getScaledWidth() / 2.0, MinecraftClient.getInstance().getWindow().getScaledHeight() / 2.0, 0);

			FlashRenderer.render(matrixStack, Flash.screenshot());

			matrixStack.pop();
		}
	}
}
