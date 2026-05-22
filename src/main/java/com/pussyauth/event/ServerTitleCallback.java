package com.pussyauth.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;

/**
 * 当客户端 ping 服务器列表（server list ping）时触发的回调，
 * 允许检测和/或修改显示的"标题"（MOTD/description）。
 *
 * <p>用于指示服务器的认证状态，例如显示
 * "🔒 需要认证"或"✅ 已认证服务器"。</p>
 */
@FunctionalInterface
public interface ServerTitleCallback {

    Event<ServerTitleCallback> EVENT = EventFactory.createArrayBacked(
            ServerTitleCallback.class,
            // 基础调用器：若无监听器注册，则返回原始 metadata
            listeners -> metadata -> {
                ServerMetadata current = metadata;
                for (ServerTitleCallback listener : listeners) {
                    current = listener.onMetadataCreated(current);
                }
                return current;
            }
    );

    /**
     * 当服务器构建列表 ping 响应的 metadata 时调用。
     *
     * @param originalMetadata Minecraft 创建的原始 metadata
     * @return 实际使用的 metadata（可以是原始对象，也可以是修改后的对象）
     */
    ServerMetadata onMetadataCreated(ServerMetadata originalMetadata);

    // — 构造修改后 metadata 的工具方法 —

    /**
     * 创建一个替换了 description 的新 {@link ServerMetadata}。
     * 保留所有其他字段（players、version、favicon 等）。
     */
    static ServerMetadata withDescription(ServerMetadata original, Text newDescription) {
        return new ServerMetadata(
                newDescription,
                original.players(),
                original.version(),
                original.favicon(),
                original.secureChatEnforced()
        );
    }
}
