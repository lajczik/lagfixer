package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.menu.ConfigMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public abstract class AbstractModule {
    private final LagFixer plugin;
    private final ModuleManager manager;
    private final HashSet<String> worlds = new HashSet<>();
    private final Impact impact;
    private final String name;
    private final String[] description;
    private final ItemBuilder baseSkull;
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
        this.baseSkull = ItemBuilder.createSkull(texture);
        this.config = new YamlConfiguration();
        this.language = new Language(this);

        try {
            this.loadConfigSection();
        } catch (Throwable ex) {
            this.plugin.printError(ex);
        }

        long valueCount = this.section.getValues(true)
                .values()
                .stream()
                .filter(v -> !(v instanceof ConfigurationSection))
                .count();

        int size = Math.clamp(((valueCount + 8) / 9) * 9, 9, 45) + 9;

        this.menu = new ConfigMenu(this.plugin, size, this);
    }

    public boolean loadAllConfig() throws Exception {
        this.loadConfigSection();
        this.language.loadMessages();
        return this.loadConfig();
    }

    public void loadConfigSection() throws IOException, InvalidConfigurationException {
        ConfigManager.loadConfig(this.plugin, this.config, "modules/" + this.name + ".yml");

        this.section = this.config.getConfigurationSection(this.name + ".values");

        this.worlds.clear();

        List<String> list = this.config.getStringList(this.name + ".worlds");
        this.canContinue = list.isEmpty() ? -1 : list.contains("*") ? 1 : 0;
        if (this.canContinue == 0) this.worlds.addAll(list);
    }

    public boolean canContinue(World w) {
        return this.canContinue == 0 ? this.worlds.contains(w.getName()) : this.canContinue == 1;
    }

    public Set<World> getAllowedWorlds() {
        HashSet<World> set = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            if (this.canContinue(world))
                set.add(world);
        }
        return Collections.unmodifiableSet(set);
    }

    public abstract void load() throws Exception;

    public abstract boolean loadConfig() throws Exception;

    public abstract void disable() throws Exception;

    @Getter
    public enum Impact {
        VERY_HIGH("<bold><gradient:#069e00:#0aff00>VERY HIGH</gradient>"),
        HIGH("<bold><gradient:#1fab1a:#3dff2b>HIGH</gradient>"),
        MEDIUM("<bold><gradient:#a6ab1a:#ffe32b>MEDIUM</gradient>"),
        LOW("<bold><gradient:#ab591a:#ff6b2b>LOW</gradient>"),
        VERY_LOW("<bold><gradient:#ab1e1a:#ff322b>VERY LOW</gradient>"),
        VISUAL_ONLY("<bold><gradient:#1a5eab:#26baff>VISUAL ONLY</gradient>");

        private final Component component;

        Impact(String text) {
            this.component = MiniMessage.miniMessage().deserialize(text);
        }
    }
}

