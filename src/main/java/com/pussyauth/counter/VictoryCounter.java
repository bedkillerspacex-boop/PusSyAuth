package com.pussyauth.counter;

import com.pussyauth.PusSyAuth;

/**
 * 胜利次数计数器。
 *
 * <p>线程安全（使用 {@code synchronized}），
 * 因为 {@code TitleS2CPacket} 回调可能在网络线程触发。
 * 提供 {@link #increment()}、{@link #getCount()}、{@link #reset()} 三个基础操作。</p>
 */
public final class VictoryCounter {

    private int num;


    /**
     * 计数器 +1，返回递增后的值。
     * <p>当 {@link PusSyAuth#isDebug()} 为 {@code true} 时输出调试日志。</p>
     *
     * @return 递增后的计数。
     */
    public synchronized int increment() {
        num++;
        if (PusSyAuth.isDebug()) {
            PusSyAuth.LOGGER.debug(
                    "[PusSyAuth] VictoryCounter 递增 -> {}", num
            );
        }
        return num;
    }

    /**
     * @return 当前计数（默认 0）。
     */
    public synchronized int getCount() {
        return num;
    }

    /**
     * 计数器归零。
     */
    public synchronized void reset() {
        num = 0;
        PusSyAuth.LOGGER.info("[PusSyAuth] VictoryCounter 已重置");
    }
}
