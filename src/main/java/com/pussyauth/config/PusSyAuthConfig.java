package com.pussyauth.config;

import com.pussyauth.PusSyAuth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * PusSyAuth 配置文件。
 *
 * <p>从 {@code config/pussyauth.properties} 加载 / 保存键值对，
 * 所有字段均有默认值。若配置文件缺失或损坏，回退至默认值并打印警告。</p>
 */
public final class PusSyAuthConfig {

    private static final String CONFIG_FILE = "config/pussyauth.properties";

    private boolean debug;
    private boolean authEnabled;
    private String activeApiProvider;
    private String miracleApiKey;
    private int timeoutSeconds;
    private boolean autoCopy;
    private boolean showChatMessage;
    private boolean victoryCopyEnabled;


    /**
     * @return {@code true} 时输出调试日志，不影响计数业务逻辑。
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置 debug 状态，立即持久化到配置文件。
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
        save();
    }

    /**
     * @return 认证功能总开关。
     */
    public boolean isAuthEnabled() {
        return authEnabled;
    }

    /**
     * @return 当前激活的 API 提供者标识，如 {@code "miracle"}。
     */
    public String getActiveApiProvider() {
        return activeApiProvider;
    }

    /**
     * @return 奇迹认证 API 密钥，可能为空字符串。
     */
    public String getMiracleApiKey() {
        return miracleApiKey;
    }

    public void setMiracleApiKey(String miracleApiKey) {
        this.miracleApiKey = miracleApiKey;
    }

    /**
     * @return API 请求超时秒数，默认 20。
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isAutoCopy() {
        return autoCopy;
    }

    public void setAutoCopy(boolean autoCopy) {
        this.autoCopy = autoCopy;
        save();
    }

    public boolean isShowChatMessage() {
        return showChatMessage;
    }

    public void setShowChatMessage(boolean showChatMessage) {
        this.showChatMessage = showChatMessage;
        save();
    }

    public boolean isVictoryCopyEnabled() {
        return victoryCopyEnabled;
    }

    public void setVictoryCopyEnabled(boolean victoryCopyEnabled) {
        this.victoryCopyEnabled = victoryCopyEnabled;
        save();
    }

    /**
     * 加载位于游戏运行目录 {@code config/pussyauth.properties} 的配置文件。
     *
     * @return 新实例，所有未定义字段回退至默认值。
     */
    public static PusSyAuthConfig load() {
        PusSyAuthConfig cfg = new PusSyAuthConfig();
        cfg.debug = false;
        cfg.authEnabled = true;
        cfg.activeApiProvider = "miracle";
        cfg.miracleApiKey = "";
        cfg.timeoutSeconds = 20;
        cfg.autoCopy = true;
        cfg.showChatMessage = true;
        cfg.victoryCopyEnabled = true;

        Path configPath = Path.of(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            PusSyAuth.LOGGER.warn(
                    "[PusSyAuth] 配置文件 {} 不存在，使用默认值", CONFIG_FILE
            );
            return cfg;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            props.load(in);
        } catch (IOException e) {
            PusSyAuth.LOGGER.warn(
                    "[PusSyAuth] 读取配置文件失败，使用默认值: {}", e.getMessage()
            );
            return cfg;
        }

        // — 解析字段 —
        cfg.debug = Boolean.parseBoolean(
                props.getProperty("debug", "false")
        );
        cfg.authEnabled = Boolean.parseBoolean(
                props.getProperty("authEnabled", "true")
        );
        cfg.activeApiProvider = props.getProperty(
                "activeApiProvider", "miracle"
        );
        cfg.miracleApiKey = props.getProperty("MiracleApiKey", "");
        cfg.timeoutSeconds = parseIntSafe(
                props.getProperty("timeoutSeconds", "20"), 20
        );
        cfg.autoCopy = Boolean.parseBoolean(
                props.getProperty("autoCopy", "true")
        );
        cfg.showChatMessage = Boolean.parseBoolean(
                props.getProperty("showChatMessage", "true")
        );
        cfg.victoryCopyEnabled = Boolean.parseBoolean(
                props.getProperty("victoryCopyEnabled", "true")
        );

        PusSyAuth.LOGGER.info(
                "[PusSyAuth] 配置加载完成 " +
                "(debug={}, authEnabled={}, provider={}, timeout={}s)",
                cfg.debug, cfg.authEnabled, cfg.activeApiProvider,
                cfg.timeoutSeconds
        );
        if (cfg.authEnabled && cfg.miracleApiKey.isEmpty()) {
            PusSyAuth.LOGGER.warn(
                    "[PusSyAuth] miracleApiKey 未配置，MiracleAuth 将不会发送请求"
            );
        }
        return cfg;
    }

    /**
     * 将当前配置持久化到 {@code config/pussyauth.properties}。
     * <p>每次调用覆盖写入，保留注释风格。</p>
     */
    public void save() {
        Path configPath = Path.of(CONFIG_FILE);

        Properties props = new Properties();
        props.setProperty("debug", String.valueOf(debug));
        props.setProperty("authEnabled", String.valueOf(authEnabled));
        props.setProperty("activeApiProvider", activeApiProvider);
        props.setProperty("MiracleApiKey", miracleApiKey);
        props.setProperty("timeoutSeconds", String.valueOf(timeoutSeconds));
        props.setProperty("autoCopy", String.valueOf(autoCopy));
        props.setProperty("showChatMessage", String.valueOf(showChatMessage));
        props.setProperty("victoryCopyEnabled", String.valueOf(victoryCopyEnabled));

        try {
            // 确保 config 目录存在
            Files.createDirectories(configPath.getParent());
            try (OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "PusSyAuth Configuration");
            }
            PusSyAuth.LOGGER.info(
                    "[PusSyAuth] 配置已保存 (debug={}, timeout={}s)",
                    debug, timeoutSeconds
            );
        } catch (IOException e) {
            PusSyAuth.LOGGER.warn(
                    "[PusSyAuth] 保存配置文件失败: {}", e.getMessage()
            );
        }
    }

    /**
     * 安全解析整数，解析失败时返回默认值。
     */
    private static int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
