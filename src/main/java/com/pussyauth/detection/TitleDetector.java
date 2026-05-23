package com.pussyauth.detection;

import com.pussyauth.PusSyAuth;
import com.pussyauth.counter.VictoryCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * 游戏内标题检测器。
 *
 * <p>当客户端收到 {@code TitleS2CPacket} 时由 mixin 调用。
 * 检测标题文本是否包含关键词"胜利"，
 * 若匹配则递增计数器并在聊天栏发送仅自己可见的消息。</p>
 */
public final class TitleDetector {

    /** 要检测的关键词 */
    private static final String KEYWORD = "胜利";

    private final VictoryCounter counter;

    public TitleDetector(VictoryCounter counter) {
        this.counter = counter;
    }

    /**
     * 检测给定的标题文本。
     *
     * @param titleText 从 TitleS2CPacket 中提取的 {@link Text}。
     */
    public void onTitleReceived(Text titleText) {
        String raw = titleText.getString();

        if (!raw.contains(KEYWORD)) {
            return;
        }

        // — 匹配成功 —
        int current = counter.increment();

        if (PusSyAuth.isDebug()) {
            PusSyAuth.LOGGER.debug(
                    "[PusSyAuth] 检测到标题包含关键词: \"{}\" (计数={})",
                    raw, current
            );
        }

        // 发送仅自己可见的聊天消息
        String message = String.format(
                "[PSauth]检测到胜利!计数:%d", current
        );
        sendClientMessage(message);

        // — 达到3次时自动调用 API —
        if (current >= 3) {
            counter.reset();
            if (PusSyAuth.getConfig().isVictoryCopyEnabled()) {
                callApi();
            }
        }
    }

    /**
     * 自动调用 MiracleAuth API（达到3次胜利时触发）。
     */
    private void callApi() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        com.pussyauth.api.AuthApiClient apiClient = PusSyAuth.getAuthApiClient();
        if (apiClient == null || !apiClient.isEnabled()) return;

        client.player.sendMessage(
                Text.literal("§e[PusSyAuth] 正在调用 MiracleAuth API..."),
                false
        );

        if (apiClient instanceof com.pussyauth.api.MiracleAuthApiClient miracleClient) {
            miracleClient.openAsync(result -> {
                if (client.player != null) {
                    if (PusSyAuth.getConfig().isShowChatMessage()) {
                        if (result != null) {
                            client.player.sendMessage(
                                    Text.literal("§a[PusSyAuth] API Response: " + result),
                                    false
                            );
                        } else {
                            client.player.sendMessage(
                                    Text.literal("§c[PusSyAuth] API request failed, check logs"),
                                    false
                            );
                        }
                    }
                }
            });
        } else {
            String result = apiClient.open();
            if (client.player != null && PusSyAuth.getConfig().isShowChatMessage()) {
                if (result != null) {
                    client.player.sendMessage(
                            Text.literal("§a[PusSyAuth] API Response: " + result),
                            false
                    );
                } else {
                    client.player.sendMessage(
                            Text.literal("§c[PusSyAuth] API request failed, check logs"),
                            false
                    );
                }
            }
        }
    }

    /**
     * 发送仅当前客户端可见的系统消息（不广播给服务器）。
     *
     * @param message 要显示的消息文本。
     */
    private static void sendClientMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.sendMessage(
                    Text.literal(message),
                    false   // false = 不存入聊天历史（仅本地显示）
            );
        }
    }
}
