package com.pussyauth.gui.category;

import com.pussyauth.PusSyAuth;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;

/**
 * "MiracleApi"分类的设置页面（二级菜单）。
 *
 * <p>包含：
 * <ul>
 *   <li>MiracleApiKey — 文本输入框</li>
 *   <li>timeoutSeconds — 数值输入框（简易滑块后续可加）</li>
 * </ul>
 * 新增配置项只需在 {@link #init()} 中添加对应控件。</p>
 */
public class MiracleAuthCategoryScreen extends Screen {

    private static final Text TITLE = Text.literal("MiracleApi");

    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    // 控件缓存，用于刷新
    private TextFieldWidget apiKeyField;
    private TextFieldWidget timeoutField;

    public MiracleAuthCategoryScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        layout.addHeader(TITLE, textRenderer);

        // ———— MiracleApiKey ————
        Text apiKeyLabel = Text.literal("MiracleApiKey");
        // 原版风格：用 ButtonWidget 作为静态标签 + TextFieldWidget

        apiKeyField = new TextFieldWidget(
                textRenderer,
                180, 20,
                Text.literal("输入 API 密钥")
        );
        apiKeyField.setMaxLength(64);
        apiKeyField.setText(PusSyAuth.getConfig().getMiracleApiKey());
        apiKeyField.setChangedListener(value ->
                PusSyAuth.LOGGER.info("[PusSyAuth] GUI 修改 MiracleApiKey -> {}", value)
        );

        // 用一个 GridWidget 垂直排列 body 控件
        var bodyGrid = new AxisGridWidget(0, 0, 200, 200, AxisGridWidget.DisplayAxis.VERTICAL);
        bodyGrid.getMainPositioner().alignHorizontalCenter();

        bodyGrid.add(apiKeyField);

        // ———— timeoutSeconds ————
        Text timeoutLabel = Text.literal("超时(秒)");

        timeoutField = new TextFieldWidget(
                textRenderer,
                60, 20,
                Text.literal("超时秒数")
        );
        timeoutField.setMaxLength(4);
        timeoutField.setText(String.valueOf(PusSyAuth.getConfig().getTimeoutSeconds()));
        timeoutField.setTextPredicate(input ->
                input.isEmpty() || input.matches("\\d{1,4}")
        );
        timeoutField.setChangedListener(value -> {
            try {
                int sec = Integer.parseInt(value);
                PusSyAuth.LOGGER.info("[PusSyAuth] GUI 修改 timeout -> {}s", sec);
            } catch (NumberFormatException ignored) {
            }
        });

        bodyGrid.add(timeoutField);

        // ———— 测试连接按钮 ————
        // ———— 自动复制开关 ————
        boolean autoCopy = PusSyAuth.getConfig().isAutoCopy();
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[ON]"),
                Text.literal("§7[OFF]")
        ).initially(autoCopy).build(
                0, 0, 200, 20,
                Text.literal("自动复制到剪贴板"),
                (btn, value) -> PusSyAuth.getConfig().setAutoCopy(value)
        ));


        // ———— 聊天消息通知开关 ————
        boolean showChat = PusSyAuth.getConfig().isShowChatMessage();
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[ON]"),
                Text.literal("§7[OFF]")
        ).initially(showChat).build(
                0, 0, 200, 20,
                Text.literal("聊天消息通知"),
                (btn, value) -> PusSyAuth.getConfig().setShowChatMessage(value)
        ));

        bodyGrid.add(ButtonWidget.builder(
                Text.literal("测试连接"),
                btn -> {
                    String key = apiKeyField.getText();
                    PusSyAuth.LOGGER.info("[PusSyAuth] 测试连接 (apiKey={})", key);
                    if (PusSyAuth.isDebug()) {
                        if (client.player != null) {
                            client.player.sendMessage(
                                    Text.literal("§7[PusSyAuth] 正在 Ping MiracleAuth..."),
                                    false
                            );
                        }
                    }
                    boolean showMsg = com.pussyauth.PusSyAuth.getConfig().isShowChatMessage();
                    java.net.URI pingUri = java.net.URI.create("https://cookie.meowow.org/api/accounts/sauth/open");
                    java.net.http.HttpRequest pingReq = java.net.http.HttpRequest.newBuilder()
                            .uri(pingUri)
                            .header("X-Api-Key", key)
                            .timeout(java.time.Duration.ofSeconds(10))
                            .method("GET", java.net.http.HttpRequest.BodyPublishers.noBody())
                            .build();
                    java.net.http.HttpClient.newHttpClient().sendAsync(pingReq, java.net.http.HttpResponse.BodyHandlers.discarding())
                            .thenAccept(response -> {
                                int status = response.statusCode();
                                String msg = status >= 200 && status < 300
                                        ? "§a[PusSyAuth] Ping 成功 (HTTP " + status + ")"
                                        : "§e[PusSyAuth] Ping 返回 (HTTP " + status + ")";
                                if (client.player != null) {
                                    if (showMsg) client.player.sendMessage(Text.literal(msg), false);
                                }
                            })
                            .exceptionally(e -> {
                                if (client.player != null) {
                                    if (showMsg) client.player.sendMessage(
                                            Text.literal("§c[PusSyAuth] Ping 失败: " + e.getMessage()),
                                            false
                                    );
                                }
                                return null;
                            });
                }
        ).width(200).build());

        layout.addBody(bodyGrid);

        // ———— 底部返回 ————
        layout.addFooter(ButtonWidget.builder(
                Text.literal("§7返回"),
                btn -> close()
        ).width(200).build());

        layout.forEachChild(this::addDrawableChild);
        refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        layout.refreshPositions();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
