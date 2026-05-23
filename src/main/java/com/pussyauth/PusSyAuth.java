package com.pussyauth;

import com.pussyauth.api.AuthApiClient;
import com.pussyauth.api.DisabledAuthClient;
import com.pussyauth.api.MiracleAuthApiClient;
import com.pussyauth.config.PusSyAuthConfig;
import com.pussyauth.counter.VictoryCounter;
import com.pussyauth.detection.TitleDetector;
import com.pussyauth.event.ServerTitleCallback;
import com.pussyauth.gui.PusSyAuthScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PusSyAuth implements ClientModInitializer {

    public static final Logger LOGGER =
            LoggerFactory.getLogger(PusSyAuth.class);

    public static final String MOD_ID = "pussyauth";
    public static final String MOD_NAME = "PusSyAuth";

    public static final String MOD_VERSION =
            FabricLoader.getInstance()
                    .getModContainer(MOD_ID)
                    .orElseThrow(() -> new RuntimeException(
                            "Cannot find mod container: " + MOD_ID))
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();

    private static PusSyAuthConfig config;
    private static VictoryCounter victoryCounter;
    private static TitleDetector titleDetector;
    private static AuthApiClient authApiClient;
    private static KeyBinding miracleKeyBinding;
    private static KeyBinding settingsKeyBinding;
    private static boolean authActive = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing {} v{}...", MOD_ID, MOD_NAME, MOD_VERSION);

        config = PusSyAuthConfig.load();

        victoryCounter = new VictoryCounter();
        titleDetector = new TitleDetector(victoryCounter);
        authApiClient = createAuthApiClient();

        // Register key binding for settings screen (default Y key)
        settingsKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pussyauth.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.pussyauth"
        ));

        // Register key binding (no default key)
        miracleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pussyauth.miracle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.pussyauth"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (settingsKeyBinding.wasPressed()) {
                openSettingsScreen(client.currentScreen);
            }
            if (miracleKeyBinding.wasPressed()) {
                LOGGER.info("[{}] Key pressed: Miracle Auth API call", MOD_ID);
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("\u00a7e[PusSyAuth] \u6b63\u5728\u8c03\u7528 MiracleAuth API..."),
                            false
                    );
                }
                victoryCounter.reset();
                if (authApiClient instanceof MiracleAuthApiClient miracleClient) {
                    miracleClient.openAsync(result -> {
                        if (config.isShowChatMessage() && client.player != null) {
                            if (result != null) {
                                client.player.sendMessage(
                                        Text.literal("\u00a7a[PusSyAuth] API Response: " + result),
                                        false
                                );
                            } else {
                                client.player.sendMessage(
                                        Text.literal("\u00a7c[PusSyAuth] API request failed, check logs"),
                                        false
                                );
                            }
                        }
                    });
                } else {
                    String result = authApiClient.open();
                    if (config.isShowChatMessage() && client.player != null) {
                        if (result != null) {
                            client.player.sendMessage(
                                    Text.literal("\u00a7a[PusSyAuth] API Response: " + result),
                                    false
                            );
                        } else {
                            client.player.sendMessage(
                                    Text.literal("\u00a7c[PusSyAuth] API request failed, check logs"),
                                    false
                            );
                        }
                    }
                }
            }
        });

        // Register MOTD callback
        ServerTitleCallback.EVENT.register(metadata -> {
            Text currentTitle = metadata.description();
            if (isDebug()) {
                LOGGER.info(
                        "[{}] MOTD detected: \"{}\"",
                        MOD_ID, currentTitle.getString()
                );
            }
            if (authActive) {
                return ServerTitleCallback.withDescription(
                        metadata,
                        Text.literal("\u00a7c\uD83D\uDD12 \u00a7lPusSyAuth \u00a77- \u00a7oAuth Active")
                );
            }
            return metadata;
        });


        LOGGER.info("[{}] PusSyAuth framework loaded successfully.", MOD_ID);
    }

    private static AuthApiClient createAuthApiClient() {
        if (!config.isAuthEnabled()) {
            LOGGER.info("[{}] Auth disabled", MOD_ID);
            return new DisabledAuthClient();
        }
        String provider = config.getActiveApiProvider();
        LOGGER.info("[{}] Creating auth client: provider={}", MOD_ID, provider);
        return switch (provider) {
            case "miracle" -> new MiracleAuthApiClient(
                    config.getMiracleApiKey(),
                    config.getTimeoutSeconds()
            );
            default -> {
                LOGGER.warn("[{}] Unknown provider: {}, falling back to disabled", MOD_ID, provider);
                yield new DisabledAuthClient();
            }
        };
    }

    public static VictoryCounter getVictoryCounter() { return victoryCounter; }
    public static TitleDetector getTitleDetector() { return titleDetector; }
    public static AuthApiClient getAuthApiClient() { return authApiClient; }

    public static boolean isDebug() { return config != null && config.isDebug(); }
    public static PusSyAuthConfig getConfig() { return config; }

    public static void setAuthActive(boolean active) {
        authActive = active;
        LOGGER.info("[{}] Auth active set to: {}", MOD_ID, active);
    }
    public static boolean isAuthActive() { return authActive; }

    public static void openSettingsScreen(net.minecraft.client.gui.screen.Screen parent) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client != null) {
            client.send(() -> client.setScreen(new PusSyAuthScreen(parent)));
        }
    }
}
