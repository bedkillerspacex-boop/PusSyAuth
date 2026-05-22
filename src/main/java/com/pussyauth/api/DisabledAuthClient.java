package com.pussyauth.api;

import com.pussyauth.PusSyAuth;

/**
 * 认证功能已禁用时的空实现。
 *
 * <p>配置 {@code authEnabled=false} 时使用此实现，
 * 所有调用静默返回 {@code null}。</p>
 */
public final class DisabledAuthClient implements AuthApiClient {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String open() {
        PusSyAuth.LOGGER.info("[PusSyAuth] 认证功能已禁用，跳过 API 请求");
        return null;
    }
}
