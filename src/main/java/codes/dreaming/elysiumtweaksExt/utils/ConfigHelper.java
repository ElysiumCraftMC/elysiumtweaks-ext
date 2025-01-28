package codes.dreaming.elysiumtweaksExt.utils;

import codes.dreaming.elysiumtweaksExt.ElysiumtweaksExt;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class ConfigHelper {
    private final Logger logger;
    private final Path dataFolder;
    private ConfigurationNode configData;

    public ConfigHelper(Logger logger) {
        this.logger = logger;
        this.dataFolder = Path.of("plugins/"+ ElysiumtweaksExt.PLUGIN_ID);
    }

    public void loadConfiguration() {
        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            Path configFile = dataFolder.resolve("config.yml");
            YamlConfigurationLoader loader =
                    YamlConfigurationLoader.builder()
                            .path(configFile)
                            .nodeStyle(NodeStyle.BLOCK)
                            .build();

            if (!Files.exists(configFile)) {
                try (InputStream defaultConfigStream = this.getClass().getResourceAsStream("/config.yml")) {
                    if (defaultConfigStream != null) {
                        Files.copy(defaultConfigStream, configFile);
                    } else {
                        throw new IOException("Default config not found in resources!");
                    }
                }
            }
            configData = loader.load();
        } catch (IOException e) {
            logger.warning("Failed to load config.yml: " + e.getMessage());
        }
    }

    public String getChangeNickServer() throws SerializationException {
        return configData.node("changeNickServer").getString("queue");
    }

    public List<String> getBlacklistedNicks() throws SerializationException {
        return configData.node("blacklistedNames").getList(String.class, List.of());
    }

    public List<String> getBlacklistedNickParts() throws SerializationException {
        return configData.node("blacklistedNameParts").getList(String.class, List.of());
    }

}
