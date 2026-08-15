package com.roboo.mineshafttycoonutils.features.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UpdateChecker {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final String MOD_ID = "mineshafttycoonutils";
    private static final String REPO = "NotRoboo/MineshaftTycoonUtils";
    private static final String RELEASES_API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final String RELEASES_PAGE_URL = "https://github.com/" + REPO + "/releases/latest";

    private static boolean checked = false;

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (checked || !ConfigManager.config.general.checkForUpdates) return;
            checked = true;
            Thread.ofVirtual().name("mstu-update-check").start(UpdateChecker::check);
        });
    }

    private static void check() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RELEASES_API_URL))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "MineshaftTycoonUtils-UpdateChecker")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return;

            JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!body.has("tag_name")) return;

            String latest = body.get("tag_name").getAsString().trim();
            String current = currentVersion();

            if (!normalize(latest).equalsIgnoreCase(normalize(current))) {
                mc.execute(() -> notify(latest));
            }
        } catch (Exception ignored) {
        }
    }

    private static String currentVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static String normalize(String version) {
        String v = version.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        return v;
    }

    private static void notify(String latestVersion) {
        if (mc.player == null) return;

        MutableComponent message = Component.literal(" A new update is available: ")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA));

        message.append(Component.literal(latestVersion)
                .withStyle(Style.EMPTY.withColor(0xFFFF55)));

        message.append(Component.literal(" (")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA)));

        message.append(Component.literal("Click here")
                .withStyle(Style.EMPTY
                        .withColor(0x55FFFF)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(RELEASES_PAGE_URL)))));

        message.append(Component.literal(")")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA)));

        mc.player.displayClientMessage(SystemMessages.get().append(message), false);
    }
}