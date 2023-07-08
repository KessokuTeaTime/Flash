package net.krlite.flash.mixin;

import net.krlite.flash.Flash;
import net.krlite.flash.FlashRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Unique
	private DrawContext context;

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/gui/DrawContext;)V"))
	private DrawContext getMatrixStack(DrawContext context) {
		this.context = context;
		return context;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.AFTER))
	private void renderScreenshotFlash(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		if (
				MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().currentScreen == null
						&& !MinecraftClient.getInstance().skipGameRender
						&& Flash.available() && context.getMatrices() != null
		) {
			context.getMatrices().push();
			context.getMatrices().translate(MinecraftClient.getInstance().getWindow().getScaledWidth() / 2.0, MinecraftClient.getInstance().getWindow().getScaledHeight() / 2.0, 0);

			FlashRenderer.render(context, Flash.screenshot());

			context.getMatrices().pop();
		}
	}
}
