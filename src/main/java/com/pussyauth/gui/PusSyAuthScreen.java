package com.pussyauth.gui;

import com.pussyauth.PusSyAuth;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;

/**
 * 极致单页面零层级“傻瓜化” PusSyAuth 设置面板。
 * 
 * <p>将所有的核心设置整合于此单一页面内，免除任何子菜单跳转的困扰。
 * 包含调试开关、自动复制选项、聊天框结果反馈选项，API 密钥及超时时间，外加一键 Ping 测试。
 * 针对输入框特别添加了 X/Y 动态捕获文本绘制标签与虚化 Placeholder，确保界面含义一目了然。</p>
 */
public class PusSyAuthScreen extends Screen {

    private static final Text TITLE = Text.literal("§d🛠 PusSyAuth 一站式极简设置面板");

    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    // 输入框缓存组件
    private TextFieldWidget apiKeyField;
    private TextFieldWidget timeoutField;

    // 暂存表单数据的本地临时状态，确保在窗口缩放重绘时数据不丢失
    private boolean tempDebug;
    private boolean tempAutoCopy;
    private boolean tempShowChat;
    private String tempApiKey;
    private int tempTimeout;
    private boolean tempVictoryCopy;

    public PusSyAuthScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
        
        // 从配置单例拉取初始配置
        var config = PusSyAuth.getConfig();
        this.tempDebug = config.isDebug();
        this.tempAutoCopy = config.isAutoCopy();
        this.tempShowChat = config.isShowChatMessage();
        this.tempApiKey = config.getMiracleApiKey();
        this.tempTimeout = config.getTimeoutSeconds();
        this.tempVictoryCopy = config.isVictoryCopyEnabled();
    }

    @Override
    protected void init() {
        layout.addHeader(TITLE, textRenderer);

        // 主体容器：极简防呆垂直列表，配置 margin 留下空隙以渲染文本标签
        var bodyGrid = new AxisGridWidget(0, 0, 200, 200, AxisGridWidget.DisplayAxis.VERTICAL);
        bodyGrid.getMainPositioner().alignHorizontalCenter().margin(8);

        // 1. API 密钥输入框（带清晰的引导文案与虚化 Placeholder）
        apiKeyField = new TextFieldWidget(
                textRenderer,
                180, 20,
                Text.literal("API 密钥输入框")
        );
        apiKeyField.setMaxLength(64);
        apiKeyField.setPlaceholder(Text.literal("请在此粘贴您的 API 密钥..."));
        apiKeyField.setText(tempApiKey);
        apiKeyField.setChangedListener(value -> tempApiKey = value.trim());
        bodyGrid.add(apiKeyField);

        // 2. 超时时间输入框（限制数字输入与虚化 Placeholder）
        timeoutField = new TextFieldWidget(
                textRenderer,
                60, 20,
                Text.literal("超时时间输入框")
        );
        timeoutField.setMaxLength(4);
        timeoutField.setPlaceholder(Text.literal("推荐: 20"));
        timeoutField.setText(String.valueOf(tempTimeout));
        timeoutField.setTextPredicate(input -> input.isEmpty() || input.matches("\\d{1,4}"));
        timeoutField.setChangedListener(value -> {
            try {
                if (!value.isEmpty()) {
                    tempTimeout = Integer.parseInt(value);
                }
            } catch (NumberFormatException ignored) {
            }
        });
        bodyGrid.add(timeoutField);

        // 3. Debug 开关（大按钮人话指引）
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[开启]"),
                Text.literal("§7[关闭]")
        ).initially(tempDebug).build(
                0, 0, 200, 20,
                Text.literal("输出 Debug 调试日志"),
                (btn, value) -> tempDebug = value
        ));

        // 4. 自动复制开关（人话指引）
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[开启 (推荐)]"),
                Text.literal("§7[关闭]")
        ).initially(tempAutoCopy).build(
                0, 0, 200, 20,
                Text.literal("自动复制结果到剪贴板"),
                (btn, value) -> tempAutoCopy = value
        ));

        // 5. 聊天消息通知开关（人话指引）
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[开启 (推荐)]"),
                Text.literal("§7[关闭]")
        ).initially(tempShowChat).build(
                0, 0, 200, 20,
                Text.literal("游戏内聊天框反馈结果"),
                (btn, value) -> tempShowChat = value
        ));

        // 5.5 3次胜利自动复制开关（人话指引）
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[开启 (推荐)]"),
                Text.literal("§7[关闭]")
        ).initially(tempVictoryCopy).build(
                0, 0, 200, 20,
                Text.literal("达到3次胜利后自动复制"),
                (btn, value) -> tempVictoryCopy = value
        ));

        // 6. 醒目的“一键网络测试”按钮（使用当前文本框内即时密钥，无需提前保存）
        bodyGrid.add(ButtonWidget.builder(
                Text.literal("§e⚡ 一键测试当前配置连接 ⚡"),
                btn -> {
                    String key = tempApiKey;
                    PusSyAuth.LOGGER.info("[PusSyAuth] 单页面测试连接 (apiKey={})", key);
                    if (client != null && client.player != null) {
                        client.player.sendMessage(
                                Text.literal("§7[PusSyAuth] 正在向 MiracleAuth 服务器发送 Ping 请求，请稍后..."),
                                false
                        );
                    }
                    java.net.URI pingUri = java.net.URI.create("https://cookie.meowow.org/api/accounts/sauth/open");
                    java.net.http.HttpRequest pingReq = java.net.http.HttpRequest.newBuilder()
                            .uri(pingUri)
                            .header("X-Api-Key", key)
                            .timeout(java.time.Duration.ofSeconds(10))
                            .method("GET", java.net.http.HttpRequest.BodyPublishers.noBody())
                            .build();
                    java.net.http.HttpClient.newBuilder()
                            .proxy(java.net.ProxySelector.getDefault())
                            .build()
                            .sendAsync(pingReq, java.net.http.HttpResponse.BodyHandlers.discarding())
                            .thenAccept(response -> {
                                int status = response.statusCode();
                                String msg = status >= 200 && status < 300
                                        ? "§a[✔ PusSyAuth] 测试成功！接口响应正常 (HTTP " + status + ")"
                                        : "§e[⚠ PusSyAuth] 连接成功，但接口返回异常错误 (HTTP " + status + ")";
                                if (client != null && client.player != null) {
                                    client.player.sendMessage(Text.literal(msg), false);
                                }
                            })
                            .exceptionally(e -> {
                                if (client != null && client.player != null) {
                                    client.player.sendMessage(
                                            Text.literal("§c[✘ PusSyAuth] 测试失败！网络无法连接或密钥格式有误: " + e.getMessage()),
                                            false
                                    );
                                }
                                return null;
                            });
                }
        ).width(200).build());

        layout.addBody(bodyGrid);

        // 页脚：大尺寸且直观的最终操作按钮
        var footerGrid = new AxisGridWidget(0, 0, 310, 20, AxisGridWidget.DisplayAxis.HORIZONTAL);
        footerGrid.getMainPositioner().alignHorizontalCenter();

        // 按钮 A：一站式保存并返回 (彻底防呆闭环)
        footerGrid.add(ButtonWidget.builder(
                Text.literal("§a✔ 保存修改并退出"),
                btn -> {
                    var config = PusSyAuth.getConfig();
                    config.setDebug(tempDebug);
                    config.setMiracleApiKey(tempApiKey);
                    config.setTimeoutSeconds(tempTimeout);
                    config.setAutoCopy(tempAutoCopy);
                    config.setShowChatMessage(tempShowChat);
                    config.setVictoryCopyEnabled(tempVictoryCopy);
                    config.save();
                    if (client != null && client.player != null) {
                        client.player.sendMessage(
                                Text.literal("§a[PusSyAuth] 设置保存成功，已应用到系统！"),
                                false
                        );
                    }
                    close();
                }
        ).width(150).build());

        // 按钮 B：恢复出厂设置
        footerGrid.add(ButtonWidget.builder(
                Text.literal("§e↺ 恢复出厂设置"),
                btn -> {
                    this.tempDebug = false;
                    this.tempApiKey = "";
                    this.tempTimeout = 20;
                    this.tempAutoCopy = true;
                    this.tempShowChat = true;
                    this.tempVictoryCopy = true;
                    if (client != null) {
                        client.setScreen(this);
                        if (client.player != null) {
                            client.player.sendMessage(
                                    Text.literal("§e[PusSyAuth] 已将所有选项重置为初始出厂默认值！(点击保存后生效)"),
                                    false
                            );
                        }
                    }
                }
        ).width(150).build());

        layout.addFooter(footerGrid);

        // 载入与渲染
        layout.forEachChild(this::addDrawableChild);
        refreshWidgetPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // 动态计算输入框物理坐标，并在其正上方 12 像素处绘制极具原版风格的高亮阴影文本标签
        if (apiKeyField != null) {
            context.drawTextWithShadow(
                    textRenderer,
                    Text.literal("§e🔑 Miracle API 密钥 (必填)"),
                    apiKeyField.getX() + 2,
                    apiKeyField.getY() - 12,
                    0xFFFFFFFF
            );
        }
        
        if (timeoutField != null) {
            context.drawTextWithShadow(
                    textRenderer,
                    Text.literal("§b⏱ 超时时长 (秒, 默认20)"),
                    timeoutField.getX() + 2,
                    timeoutField.getY() - 12,
                    0xFFFFFFFF
            );
        }
    }

    @Override
    protected void refreshWidgetPositions() {
        layout.refreshPositions();
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}