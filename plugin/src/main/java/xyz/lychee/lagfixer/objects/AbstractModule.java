package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.menu.ConfigMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Setter
public abstract class AbstractModule {
    private final LagFixer plugin;
    private final ModuleManager manager;
    private final HashSet<String> worlds = new HashSet<>();
    private final Impact impact;
    private final String name;
    private final String[] description;
    private final ItemStack baseSkull;
    private final YamlConfiguration config;
    private final ConfigMenu menu;
    private ConfigurationSection section;
    private boolean loaded = false;
    private Language language;
    private int canContinue;

    public AbstractModule(LagFixer plugin, ModuleManager manager, Impact impact, String name, String[] description, String texture) {
        this.plugin = plugin;
        this.manager = manager;
        this.impact = impact;
        this.name = name;
        this.description = description;
        this.baseSkull = ItemBuilder.createSkull(texture).build();
        this.config = new YamlConfiguration();
        this.language = new Language(this);

        try {
            this.loadConfigSection();
        } catch (Exception ex) {
            this.plugin.printError(ex);
        }

        ConfigurationSection defSection = this.section.getDefaultSection() == null ? this.section : this.section.getDefaultSection();

        long valueCount = defSection.getValues(true).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof ConfigurationSection))
                .count();

        int size = (int) Math.max(9, Math.min(45, ((valueCount + 8) / 9) * 9)) + 9;

        this.menu = new ConfigMenu(this.plugin, defSection, size, this);
    }

    public boolean loadAllConfig() throws Exception {
        this.loadConfigSection();
        this.language.loadMessages();
        return this.loadConfig();
    }

    public void loadConfigSection() throws Exception {
        this.loadConfigFile();

        this.section = this.config.getConfigurationSection(this.name + ".values");

        this.worlds.clear();
        this.worlds.addAll(this.config.getStringList(this.name + ".worlds"));
        this.canContinue = this.worlds.isEmpty() ? -1 : this.worlds.contains("*") ? 1 : 0;
    }

    public void loadConfigFile() throws Exception {
        String resourcePath = "modules/" + this.name + ".yml";

        InputStream defStream = this.plugin.getResource(resourcePath);
        if (defStream == null) {
            // 翻译：在资源中找不到配置文件
            this.plugin.getLogger().warning("在资源中找不到配置文件 " + this.name + ".yml");
            return;
        }

        YamlConfiguration defaultConfig;
        try (InputStreamReader r = new InputStreamReader(defStream, StandardCharsets.UTF_8)) {
            defaultConfig = YamlConfiguration.loadConfiguration(r);
        }

        File configFile = new File(this.plugin.getDataFolder(), resourcePath);

        if (configFile.exists()) {
            this.config.load(configFile);
            this.config.setDefaults(defaultConfig);
            this.config.options().copyDefaults(true);
            this.config.save(configFile);
            return;
        }

        File parent = configFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            // 翻译：无法创建目录
            this.plugin.getLogger().warning("无法创建目录: " + parent);
        }

        FileConfiguration mainCfg = this.plugin.getConfig();
        String sectionPath = "modules." + this.name;
        if (mainCfg.isConfigurationSection(sectionPath)) {
            ConfigurationSection section = mainCfg.getConfigurationSection(sectionPath);
            if (section != null) {
                this.config.set(this.name, section);
                this.config.setDefaults(defaultConfig);
                this.config.options().copyDefaults(true);
                this.config.save(configFile);

                // 翻译：配置已移动到
                mainCfg.set(sectionPath, "配置已移动到 \"LagFixer/modules/" + this.name + ".yml\"");
                return;
            }
        }

        this.plugin.saveResource(resourcePath, false);
        this.config.load(configFile);
        this.config.setDefaults(defaultConfig);
        this.config.options().copyDefaults(true);
        this.config.save(configFile);
    }

    public boolean canContinue(World w) {
        return this.canContinue == 0 ? this.worlds.contains(w.getName()) : this.canContinue == 1;
    }

    public Stream<World> getAllowedWorldsStream() {
        return Bukkit.getWorlds().stream().filter(this::canContinue);
    }

    public Set<World> getAllowedWorlds() {
        HashSet<World> set = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            if (this.canContinue(world)) {
                set.add(world);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    public abstract void load() throws Exception;

    public abstract boolean loadConfig() throws Exception;

    public abstract void disable() throws Exception;

    @Getter
    public enum Impact {
        // 翻译：非常高
        VERY_HIGH("<bold><gradient:#069e00:#0aff00>非常高</gradient>"),
        // 翻译：高
        HIGH("<bold><gradient:#1fab1a:#3dff2b>高</gradient>"),
        // 翻译：中
        MEDIUM("<bold><gradient:#a6ab1a:#ffe32b>中</gradient>"),
        // 翻译：低
        LOW("<bold><gradient:#ab591a:#ff6b2b>低</gradient>"),
        // 翻译：非常低
        VERY_LOW("<bold><gradient:#ab1e1a:#ff322b>非常低</gradient>"),
        // 翻译：仅视觉效果
        VISUAL_ONLY("<bold><gradient:#1a5eab:#26baff>仅视觉效果</gradient>");

        private final Component component;

        Impact(String text) {
            this.component = MiniMessage.miniMessage().deserialize(text);
        }
    }
}