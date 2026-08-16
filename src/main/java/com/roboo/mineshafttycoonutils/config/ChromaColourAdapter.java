package com.roboo.mineshafttycoonutils.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.notenoughupdates.moulconfig.ChromaColour;

import java.awt.Color;
import java.lang.reflect.Type;

public class ChromaColourAdapter implements JsonSerializer<ChromaColour>, JsonDeserializer<ChromaColour> {

    @Override
    public JsonElement serialize(ChromaColour src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getEffectiveColourRGB());
    }

    @Override
    public ChromaColour deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            int argb = json.getAsInt();
            int a = (argb >>> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            if (a == 0) a = 255;
            return ChromaColour.fromStaticRGB(r, g, b, a);
        }

        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("hue") && obj.has("saturation") && obj.has("brightness")) {
                float hue = obj.get("hue").getAsFloat();
                float saturation = obj.get("saturation").getAsFloat();
                float brightness = obj.get("brightness").getAsFloat();
                int alpha = obj.has("alpha") ? obj.get("alpha").getAsInt() : 255;

                int rgb = Color.HSBtoRGB(hue, saturation, brightness);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                return ChromaColour.fromStaticRGB(r, g, b, alpha);
            }
        }

        return ChromaColour.fromStaticRGB(255, 255, 255, 255);
    }
}