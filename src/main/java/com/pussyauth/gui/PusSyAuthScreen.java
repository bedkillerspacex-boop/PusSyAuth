package com.pussyauth.gui;

import com.pussyauth.PusSyAuth;
import com.pussyauth.gui.category.MiracleAuthCategoryScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;

/**
 * PusSyAuth 设置主页面。
 *
 * <p>每个一级入口对应一个按钮。点击后跳转到对应的二级设置页。
 * 新增分类只需在此处添加按钮 + 新建 {@code category/XxxCategoryScreen.java}。</p>
 */
public class PusSyAuthScreen extends Screen {

    private static final Text TITLE = Text.literal("PusSyAuth 设置");

    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public PusSyAuthScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        layout.addHeader(TITLE, textRenderer);

        // — 一级入口按钮列表 — 新增分类在此添加 —
        // 用一个 GridWidget 垂直排列 body 控件，避免 SimplePositioningWidget 重叠
        var bodyGrid = new AxisGridWidget(0, 0, 200, 200, AxisGridWidget.DisplayAxis.VERTICAL);
        bodyGrid.getMainPositioner().alignHorizontalCenter();

        // Debug 开关（直接放在主页面）
        bodyGrid.add(ButtonWidget.builder(
                Text.literal("§e⚙ Debug 设置"),
                btn -> client.setScreen(new DebugSubScreen(this))
        ).width(200).build());

        // Miracle Auth 二级菜单入口
        bodyGrid.add(ButtonWidget.builder(
                Text.literal("§b🔑 MiracleApi"),
                btn -> client.setScreen(new MiracleAuthCategoryScreen(this))
        ).width(200).build());

        // 聊天消息通知开关（放在一级菜单）
        boolean showChat = PusSyAuth.getConfig().isShowChatMessage();
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[ON]"),
                Text.literal("§7[OFF]")
        ).initially(showChat).build(
                0, 0, 200, 20,
                Text.literal("聊天消息通知"),
                (btn, value) -> PusSyAuth.getConfig().setShowChatMessage(value)
        ));

        layout.addBody(bodyGrid);

        // — 底部返回 —
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

    /**
     * Debug 子页面——直接在主页面显示一个切换按钮。
     */
    private static class DebugSubScreen extends Screen {

        private static final Text TITLE = Text.literal("Debug 设置");

        private final Screen parent;
        private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

        public DebugSubScreen(Screen parent) {
            super(TITLE);
            this.parent = parent;
        }

        @Override
        protected void init() {
            layout.addHeader(TITLE, textRenderer);

            // 当前状态
            boolean currentDebug = PusSyAuth.getConfig().isDebug();

            // Debug 开关（CyclingButton on/off），切换时直接持久化
            var debugBtn = net.minecraft.client.gui.widget.CyclingButtonWidget.onOffBuilder(
                    Text.literal("§a[ON]"),
                    Text.literal("§7[OFF]")
            ).initially(currentDebug).build(
                    0, 0, 180, 20,
                    Text.literal("Debug 日志"),
                    (btn, value) -> {
                        PusSyAuth.LOGGER.info(
                                "[PusSyAuth] GUI 切换 Debug -> {}", value
                        );
                        // 持久化到配置文件
                        PusSyAuth.getConfig().setDebug(value);
                    }
            );

            var debugGrid = new AxisGridWidget(0, 0, 200, 200, AxisGridWidget.DisplayAxis.VERTICAL);
            debugGrid.getMainPositioner().alignHorizontalCenter();
            debugGrid.add(debugBtn);

            // 状态提示
            debugGrid.add(ButtonWidget.builder(
                    Text.literal(
                            currentDebug
                                    ? "§a当前状态: 已启用"
                                    : "§7当前状态: 已关闭"
                    ),
                    btn -> {}
            ).width(200).build());

            layout.addBody(debugGrid);

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
}
