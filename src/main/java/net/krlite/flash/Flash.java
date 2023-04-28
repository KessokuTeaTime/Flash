package net.krlite.flash;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.krlite.equator.input.Window;
import net.krlite.equator.math.algebra.Curves;
import net.krlite.equator.visual.animation.Animation;
import net.krlite.equator.visual.color.AccurateColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Flash implements ModInitializer {
	public static final String NAME = "Flash", ID = "flash";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final double MIN_WIDTH = 0.764, MIN_HEIGHT = 0.672, MIN_SCALAR = 0.875;
	public static final float BORDER = 1;
	public static final KeyBinding CLEAR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.flash.clear",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_LEFT_ALT,
			"key.flash.category"
	));

	public static class Input {
		static void listenInput(MinecraftClient client) {
			if (client == null) return;
			if (CLEAR.wasPressed()) clear();
		}
	}

	public static class Sounds {
		public static final SoundEvent CAMERA_SHUTTER = SoundEvent.of(new Identifier(ID, "camera_shutter"));

		static void register() {
			Registry.register(Registries.SOUND_EVENT, CAMERA_SHUTTER.getId(), CAMERA_SHUTTER);
		}

		public static void playCameraShutter() {
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(CAMERA_SHUTTER, 1));
		}
	}

	private static final Animation
			shrink = new Animation(0, 1, 572, Curves.Exponential.Quintic.OUT),
			drop = new Animation(0, 1, 620, Curves.TwoBasedExponential.IN);
	private static IntBuffer screenshot = null;
	private static int width, height;

	static {
		Window.Callbacks.Resize.EVENT.register(size -> {
			clear();
		});
	}

	@Override
	public void onInitialize() {
		ClientTickEvents.END_CLIENT_TICK.register(Input::listenInput);
		Sounds.register();
	}

	public static boolean available() {
		return screenshot() != null && (shrink.isRunning() && !shrink.isCompleted() || drop.isRunning() && !drop.isCompleted());
	}

	public static double shrink() {
		return shrink.value();
	}

	public static double drop() {
		return drop.value();
	}

	@Nullable
	public static IntBuffer screenshot() {
		return screenshot;
	}

	public static int width() {
		return width;
	}

	public static int height() {
		return height;
	}

	public static AccurateColor getBorderColor() {
		if (MinecraftClient.getInstance().world != null) {
			Vec3d color = MinecraftClient.getInstance().world.getSkyColor(MinecraftClient.getInstance().gameRenderer.getCamera().getBlockPos().toCenterPos(), 0);
			float[] hsb = Color.RGBtoHSB((int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255), null);
			return AccurateColor.fromHSB(hsb[0], hsb[1], 1 - hsb[2], 1);
		}
		return AccurateColor.WHITE;
	}

	public static void screenshot(Framebuffer framebuffer) {
		screenshot = toIntBuffer(framebuffer);
		width = framebuffer.textureWidth;
		height = framebuffer.textureHeight;
		shrink.restart();
		drop.restart();
	}

	public static void clear() {
		screenshot = null;
		width = 0;
		height = 0;
	}

	public static IntBuffer toIntBuffer(Framebuffer framebuffer) {
		NativeImage nativeImage = ScreenshotRecorder.takeScreenshot(framebuffer);
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(nativeImage.getWidth() * nativeImage.getHeight() * 4);
		for (int y = 0; y < nativeImage.getHeight(); y++) {
			for (int x = 0; x < nativeImage.getWidth(); x++) {
				byteBuffer.put(nativeImage.getRed(x, y));
				byteBuffer.put(nativeImage.getGreen(x, y));
				byteBuffer.put(nativeImage.getBlue(x, y));
				byteBuffer.put(nativeImage.getOpacity(x, y));
			}
		}
		nativeImage.close();
		return byteBuffer.flip().asIntBuffer();
	}
}
