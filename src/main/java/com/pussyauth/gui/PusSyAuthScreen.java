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
        boolean debug = PusSyAuth.getConfig().isDebug();
        bodyGrid.add(CyclingButtonWidget.onOffBuilder(
                Text.literal("§a[ON]"),
                Text.literal("§7[OFF]")
        ).initially(debug).build(
                0, 0, 200, 20,
                Text.literal("Debug 日志"),
                (btn, value) -> PusSyAuth.getConfig().setDebug(value)
        ));

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



}