package com.pussyauth.api;

/**
 * 第三方认证 API 客户端接口。
 *
 * <p>所有认证提供者需实现此接口。
 * 新增 API 只需新建一个实现类并在配置中指定 {@code activeApiProvider}。</p>
 */
public interface AuthApiClient {

    /**
     * 判断此客户端是否可用（已正确配置）。
     *
     * @return {@code true} 表示可以调用 {@link #open()}。
     */
    boolean isEnabled();

    /**
     * 发送认证 open 请求。
     *
     * @return 服务器返回的响应体字符串，失败返回 {@code null}。
     */
    String open();
}
