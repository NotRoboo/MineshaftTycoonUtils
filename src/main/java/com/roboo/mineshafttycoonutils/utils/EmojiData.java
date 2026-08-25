package com.roboo.mineshafttycoonutils.utils;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EmojiData {

    private static final Logger LOGGER = LoggerFactory.getLogger("MineshaftTycoonUtils");
    private static final String MAPPING_PATH = "/assets/mineshafttycoonutils/emoji_mapping.json";

    private static final Map<String, Integer> SHORTCODE_TO_CODEPOINT = new LinkedHashMap<>();
    private static final Map<String, Integer> FLAG_TO_CODEPOINT = new LinkedHashMap<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static class Mapping {
        Map<String, Integer> emotes = new LinkedHashMap<>();
        Map<String, Integer> flags = new LinkedHashMap<>();
    }

    static {
        load();
    }

    private EmojiData() {}

    private static void load() {
        try (InputStream stream = EmojiData.class.getResourceAsStream(MAPPING_PATH)) {
            if (stream == null) {
                LOGGER.warn("Emoji mapping resource not found at {}", MAPPING_PATH);
                return;
            }

            Mapping mapping = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Mapping.class);
            if (mapping == null) return;

            if (mapping.emotes != null) {
                SHORTCODE_TO_CODEPOINT.putAll(mapping.emotes);
            }
            if (mapping.flags != null) {
                FLAG_TO_CODEPOINT.putAll(mapping.flags);
                for (String flagName : mapping.flags.keySet()) {
                    if (SHORTCODE_TO_CODEPOINT.containsKey(flagName)) {
                        LOGGER.warn("Flag shortcode '{}' collides with an emote shortcode; emote will take precedence", flagName);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load emoji mapping", e);
        }
    }

    public static Integer codepointFor(String shortcode) {
        if (shortcode == null) return null;
        Integer codepoint = SHORTCODE_TO_CODEPOINT.get(shortcode);
        if (codepoint != null) return codepoint;
        return FLAG_TO_CODEPOINT.get(shortcode);
    }

    public static List<String> allShortcodes() {
        List<String> combined = new ArrayList<>(SHORTCODE_TO_CODEPOINT.size() + FLAG_TO_CODEPOINT.size());
        combined.addAll(SHORTCODE_TO_CODEPOINT.keySet());
        combined.addAll(FLAG_TO_CODEPOINT.keySet());
        return combined;
    }
}