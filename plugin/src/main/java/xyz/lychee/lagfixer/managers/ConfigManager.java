package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
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
    @Getter
    private static ConfigManager instance;
    private TextComponent prefix;

    public ConfigManager(LagFixer plugin) {
        super(plugin);
        instance = this;
    }

    @Override
    public void load() throws Exception {
        // Load LagFixer language
        FileConfiguration lang = Language.getYaml();
        File langFile = new File(this.getPlugin().getDataFolder(), "lang.yml");
        this.loadConfig(lang, langFile);

        Language.getMainValues().clear();
        lang.getConfigurationSection("messages.Main").getValues(true).forEach((key, val) -> {
            if (val instanceof String) {
                Language.getMainValues().put(key, (String) val);
            }
        });

        // Load LagFixer config
        FileConfiguration config = this.getPlugin().getConfig();
        File configFile = new File(this.getPlugin().getDataFolder(), "config.yml");
        this.loadConfig(config, configFile);

        this.prefix = MessageUtils.colors(null, config.getString("main.prefix", ""))
                .clickEvent(ClickEvent.openUrl("https://bit.ly/lagfixer-modrinth"));

        if (config.getBoolean("main.prefix_hover")) {
            TextComponent description = MessageUtils.colors(null, "&fLagFixer &e" + this.getPlugin().getDescription().getVersion() + "\n &8{*} &7点击在modrinth上打开!");
            this.prefix = this.prefix.hoverEvent(HoverEvent.showText(Component.empty().append(this.prefix).append(description)));
        }
    }

    private void loadConfig(FileConfiguration cfg, File file) {
        try {
            if (file.exists()) {
                cfg.load(file);
            }
            try (InputStream defConfigStream = this.getPlugin().getResource(file.getName())) {
                if (defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)
                    );
                    cfg.setDefaults(defConfig);
                }
            }
            cfg.options().copyDefaults(true);
            cfg.save(file);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
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