package com.pussyauth.mixin;

import com.pussyauth.PusSyAuth;
import com.pussyauth.event.ServerTitleCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 注入到 {@link MinecraftServer#createMetadata()} 方法的 Mixin。
 * 该方法负责构建 Minecraft 服务器列表 ping 所展示的响应。
 *
 * <p>在此处进行标题检测：拦截服务器列表的 metadata，
 * 以读取/修改 MOTD（服务器"标题"），用于认证目的。</p>
 */
@Mixin(MinecraftServer.class)
public abstract class ServerMetadataMixin {

    /**
     * 在 {@code createMetadata()} 返回后注入。
     * 允许读取当前服务器标题，并在必要时替换 metadata
     * 以反映认证状态。
     */
    @Inject(
        method = "createMetadata",
        at = @At("RETURN"),
        cancellable = true
    )
    private void pussyauth$onCreateMetadata(CallbackInfoReturnable<ServerMetadata> cir) {
        ServerMetadata original = cir.getReturnValue();
        Text description = original.description();

        // — 标题检测 —
        // 记录当前 MOTD/标题用于审计/调试
        PusSyAuth.LOGGER.info(
            "[{}] 检测到服务器列表 ping — MOTD: \"{}\"",
            PusSyAuth.MOD_ID, description.getString()
        );

        // 触发回调，使 PusSyAuth 的其他模块可以
        // 对 metadata 做出反应或修改。
        ServerMetadata modified = ServerTitleCallback.EVENT.invoker()
                .onMetadataCreated(original);

        // 如果回调返回了不同的 metadata，则替换
        if (modified != original) {
            PusSyAuth.LOGGER.info(
                "[{}] 服务器列表 MOTD 已被认证处理器修改",
                PusSyAuth.MOD_ID
            );
            cir.setReturnValue(modified);
        }
    }
}
