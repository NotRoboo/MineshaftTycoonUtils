package com.roboo.mineshafttycoonutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis;
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver;
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("ALL")
public class ConfigManager {

    private static final File configFile = new File("config/mineshafttycoonutils/mineshafttycoonutils.json");

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ChromaColour.class, new ChromaColourAdapter())
            .create();

    public static MSTUConfig config;
    public MoulConfigProcessor<MSTUConfig> processor;

    public void firstLoad() {
        if (configFile.exists()) {
            try {
                String json = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
                config = gson.fromJson(json, MSTUConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
                config = new MSTUConfig();
            }
        } else {
            config = new MSTUConfig();
            saveConfig();
        }
        recreateProcessor();
    }

    public void saveConfig() {
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory: " + parent);
            }

            FileUtils.writeStringToFile(configFile, gson.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recreateProcessor() {
        processor = new MoulConfigProcessor<>(config);
        BuiltinMoulConfigGuis.addProcessors(processor);
        ConfigProcessorDriver driver = new ConfigProcessorDriver(processor);
        driver.warnForPrivateFields = false;
        driver.processConfig(config);
    }
}
