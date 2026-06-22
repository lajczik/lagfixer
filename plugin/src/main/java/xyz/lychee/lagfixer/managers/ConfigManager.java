package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Getter
public class ConfigManager extends AbstractManager implements Listener {
    private static @Getter ConfigManager instance;
    private TextComponent prefix;

    public ConfigManager(LagFixer plugin) {
        super(plugin);
        instance = this;
    }

    public static void loadConfig(Plugin plugin, FileConfiguration cfg, String path) throws IOException, InvalidConfigurationException {
        File file = new File(plugin.getDataFolder(), path);

        try (InputStream defConfigStream = plugin.getResource(path)) {
            if (defConfigStream == null) {
                return;
            }

            boolean formatter = plugin.getConfig().getBoolean("main.config_formatter");
            if (formatter) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)
                );

                if (file.exists()) {
                    YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(file);

                    for (String key : defConfig.getKeys(true)) {
                        Object userValue = userConfig.get(key);
                        Object defaultValue = defConfig.get(key);
                        if (!(userValue instanceof ConfigurationSection || defaultValue instanceof ConfigurationSection)
                                && defaultValue != null
                                && userValue != null
                                && !userValue.equals(defaultValue)) {
                            defConfig.set(key, userValue);
                        }
                    }
                }
                defConfig.save(file);
            }

            cfg.load(file);
        }
    }

    @Override
    public void load() throws Exception {
        // Load LagFixer language
        FileConfiguration lang = Language.getYaml();
        ConfigManager.loadConfig(this.getPlugin(), lang, "lang.yml");

        Language.getMainValues().clear();
        lang.getConfigurationSection("messages.Main").getValues(true).forEach((key, val) -> {
            if (val instanceof String) {
                Language.getMainValues().put(key, (String) val);
            }
        });

        // Load LagFixer config
        FileConfiguration config = this.getPlugin().getConfig();
        ConfigManager.loadConfig(this.getPlugin(), config, "config.yml");

        this.prefix = MessageUtils.colors(null, config.getString("main.prefix", ""))
                .clickEvent(ClickEvent.openUrl("https://bit.ly/lagfixer-modrinth"));

        if (config.getBoolean("main.prefix_hover")) {
            TextComponent description = MessageUtils.colors(null, "&fLagFixer &e" + this.getPlugin().getDescription().getVersion() + "\n &8{*} &7Click to open plugin in modrinth!");
            this.prefix = this.prefix.hoverEvent(HoverEvent.showText(Component.empty().append(this.prefix).append(description)));
        }
    }

    @Override
    public void disable() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}