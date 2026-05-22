package com.pussyauth.mixin;

import com.pussyauth.PusSyAuth;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 注入到 {@link TitleS2CPacket} 的 apply 方法中。
 *
 * <p>当客户端收到服务端的 title 包时，提取文本内容
 * 交由 {@link PusSyAuth#getTitleDetector()} 检测。</p>
 */
@Mixin(TitleS2CPacket.class)
public abstract class TitleS2CPacketMixin {

    /**
     * 在 {@code apply(ClientPlayPacketListener)} 返回后注入，
     * 此时文本已经解析完毕，可直接读取。
     */
    @Inject(
        method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V",
        at = @At("RETURN")
    )
    private void pussyauth$onApply(CallbackInfo ci) {
        // (T) this 安全：mixin 的目标就是 TitleS2CPacket
        TitleS2CPacket self = (TitleS2CPacket) (Object) this;
        Text titleText = self.text();

        PusSyAuth.getTitleDetector().onTitleReceived(titleText);
    }
}
