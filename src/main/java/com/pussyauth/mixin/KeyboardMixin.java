package com.pussyauth.mixin;

import com.pussyauth.PusSyAuth;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 注入到 {@link Keyboard#onKey(long, int, int, int, int)}，
 * 监听 P 键打开 PusSyAuth 设置页面。
 */
@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(
        method = "onKey",
        at = @At("HEAD")
    )
    private void pussyauth$onKey(
            long window, int key, int scancode, int action, int modifiers,
            CallbackInfo ci) {
        // 仅在按下（PRESS / REPEAT）时触发
        if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) {
            return;
        }

        if (key == GLFW.GLFW_KEY_Y) {
            PusSyAuth.openSettingsScreen(
                    net.minecraft.client.MinecraftClient.getInstance().currentScreen
            );
        }
    }
}
