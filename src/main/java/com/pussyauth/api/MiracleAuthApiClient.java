package com.pussyauth.api;

import com.pussyauth.PusSyAuth;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class MiracleAuthApiClient implements AuthApiClient {

    private static final String API_URL =
            "https://cookie.meowow.org/api/accounts/sauth/open";

    private final HttpClient httpClient;
    private final String apiKey;
    private final Duration requestTimeout;
    private final boolean enabled;

    public MiracleAuthApiClient(String miracleApiKey, int timeoutSeconds) {
        this.apiKey = miracleApiKey;
        this.requestTimeout = Duration.ofSeconds(timeoutSeconds);
        this.enabled = !miracleApiKey.isEmpty();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(
                        Math.min(timeoutSeconds, 10)))
                .build();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

        @Override
    public String open() {
        if (!enabled) {
            return null;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("X-Api-Key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(requestTimeout)
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            return response.body();
        } catch (Exception e) {
            PusSyAuth.LOGGER.warn(
                    "[PusSyAuth] MiracleAuth API 请求失败: {}", e.getMessage()
            );
            return null;
        }
    }

    /**
     * 异步调用，结果通过回调返回，避免阻塞渲染线程。
     */
    public void openAsync(java.util.function.Consumer<String> callback) {
        if (!enabled) {
            if (callback != null) callback.accept(null);
            return;
        }
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("X-Api-Key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(requestTimeout)
                    .build();
        } catch (Exception e) {
            PusSyAuth.LOGGER.warn("[PusSyAuth] MiracleAuth 构建请求失败: {}", e.getMessage());
            if (callback != null) callback.accept(null);
            return;
        }
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.send(() -> {
                            copyToClipboard(response.body());
                            if (callback != null) callback.accept(response.body());
                        });
                    }
                })
                .exceptionally(e -> {
                    PusSyAuth.LOGGER.warn("[PusSyAuth] MiracleAuth API 请求失败: {}", e.getMessage());
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.send(() -> {
                            if (callback != null) callback.accept(null);
                        });
                    }
                    return null;
                });
    }

    /**
     * Debug \u6a21\u5f0f\u4e0b\u53d1\u9001\u6d88\u606f\u5230\u804a\u5929\u680f\uff08\u4ec5\u672c\u5730\u53ef\u89c1\uff09\u3002
     */
    /**
     * 通过 GLFW 直接设置剪贴板文本。
     */
    private static void copyToClipboard(String text) {
        PusSyAuth.LOGGER.info("[PusSyAuth] copyToClipboard 被调用, autoCopy={}, text.length={}", 
                PusSyAuth.getConfig().isAutoCopy(), text != null ? text.length() : -1);
        if (!PusSyAuth.getConfig().isAutoCopy()) return;
        try {
            var mc = net.minecraft.client.MinecraftClient.getInstance();
            PusSyAuth.LOGGER.info("[PusSyAuth] mc={}, mc.getWindow={}", mc, mc != null ? mc.getWindow() : null);
            if (mc != null && mc.getWindow() != null) {
                long handle = mc.getWindow().getHandle();
                PusSyAuth.LOGGER.info("[PusSyAuth] window handle={}", handle);
                net.minecraft.client.util.Clipboard clipboard = new net.minecraft.client.util.Clipboard();
                clipboard.setClipboard(handle, text);
                PusSyAuth.LOGGER.info("[PusSyAuth] 复制到剪贴板成功 ({} chars)", text.length());
            } else {
                PusSyAuth.LOGGER.warn("[PusSyAuth] mc或window为null");
            }
        } catch (Exception e) {
            PusSyAuth.LOGGER.warn("[PusSyAuth] 复制到剪贴板失败: {} - {}", e.getClass().getName(), e.getMessage());
        }
    }

    private static void sendDebugMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }
}
