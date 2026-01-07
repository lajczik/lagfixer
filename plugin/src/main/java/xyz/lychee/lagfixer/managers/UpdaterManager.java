package xyz.lychee.lagfixer.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public class UpdaterManager extends AbstractManager implements Listener {
    private static @Getter UpdaterManager instance;
    private final VersionComparator comparator = new VersionComparator();
    private final Gson gson = new Gson();
    private int compared = 0;
    private int difference = 0;
    private int behind = -1;
    private String latestVersion = "";
    private String currentVersion = "";
    private BukkitTask task;
    private boolean updater;

    public UpdaterManager(LagFixer plugin) {
        super(plugin);
        instance = this;
    }

    @EventHandler
    public void onClick(PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && this.compared < 0 && this.updater) {
            SupportManager.getInstance().getFork().runLater(true, () -> {
                MessageUtils.sendMessage(true, e.getPlayer(),
                        "\nPlugin needs update, latest version: &f" + this.latestVersion +
                                "\n &8- &ehttps://modrinth.com/plugin/lagfixer/version/" + this.latestVersion +
                                "\n"
                );
            }, 3, TimeUnit.SECONDS);
        }
    }

    @Override
    public void load() throws IOException {
        this.updater = this.getPlugin().getConfig().getBoolean("main.updater");

        this.task = SupportManager.getInstance().getFork().runTimer(true, () -> {
            try {
                this.currentVersion = this.getPlugin().getDescription().getVersion().split(" ")[0].trim();

                URL url = new URL("https://api.modrinth.com/v2/project/lagfixer/version");
                InputStreamReader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                Type listType = new TypeToken<ArrayList<ModrinthVersion>>() {}.getType();
                List<ModrinthVersion> versions = gson.fromJson(reader, listType);
                versions.removeIf(version -> version.getVersion_number().matches(".*[^0-9.].*"));

                if (!versions.isEmpty()) {
                    versions.sort((v1, v2) -> this.comparator.compare(v2.getVersion_number(), v1.getVersion_number()));

                    ModrinthVersion latest = versions.get(0);
                    this.latestVersion = latest.getVersion_number();
                    this.behind = this.calculateBuildsBehind(versions);
                } else {
                    this.latestVersion = this.getPlugin().getDescription().getVersion();
                    this.behind = 0;
                }

                this.difference = this.comparator.difference(this.currentVersion, this.latestVersion);
                this.compared = this.comparator.compare(this.currentVersion, this.latestVersion);

                if ((this.difference >= 0 && this.difference < 2) || this.behind > 5) {
                    this.updater = true;
                }
            } catch (IOException ignored) {}

            if (this.updater && this.compared < 0) {
                this.getPlugin().getLogger().info(
                        String.format("\n&8∘₊✧────────────────────────────────✧₊∘" +
                                        "\n&c&lLagFixer needs an update!" +
                                        "\n&fVersion: &e&n%s&r -> &e&n%s&r" +
                                        "\n&ahttps://modrinth.com/plugin/lagfixer/version/%s" +
                                        "\n" +
                                        "\n&6⚠ &7Updating this plugin is crucial! &6⚠" +
                                        "\n&8∘₊✧────────────────────────────────✧₊∘",
                                this.currentVersion, this.latestVersion, this.latestVersion
                        )
                );
            }
        }, 1L, 30L, TimeUnit.MINUTES);
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    private int calculateBuildsBehind(List<ModrinthVersion> versions) {
        if (this.currentVersion.equals(this.latestVersion)) {
            return 0;
        }

        int currentIndex = -1;
        int latestIndex = -1;

        for (int i = 0; i < versions.size(); i++) {
            ModrinthVersion version = versions.get(i);
            if (version.getVersion_number().equals(this.currentVersion)) {
                currentIndex = i;
            }
            if (version.getVersion_number().equals(this.latestVersion)) {
                latestIndex = i;
            }
        }

        if (currentIndex != -1 && latestIndex != -1) {
            return currentIndex - latestIndex;
        }

        return -1;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static class VersionComparator implements Comparator<String> {
        @Override
        public int compare(String current, String latest) {
            String[] parts1 = current.split("\\.");
            String[] parts2 = latest.split("\\.");

            int length = Math.max(parts1.length, parts2.length);

            for (int i = 0; i < length; i++) {
                int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
                int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            }

            return 0;
        }

        public int difference(String current, String latest) {
            String[] parts1 = current.split("\\.");
            String[] parts2 = latest.split("\\.");

            int length = Math.max(parts1.length, parts2.length);
            for (int i = 0; i < length; i++) {
                int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
                int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

                if (num1 < num2) {
                    return i;
                }
            }
            return -1;
        }
    }

    @Getter
    @Setter
    public static class ModrinthVersion {
        private List<String> game_versions;
        private List<String> loaders;
        private String id;
        private String project_id;
        private String author_id;
        private boolean featured;
        private String name;
        private String version_number;
        private String changelog;
        private String changelog_url;
        private String date_published;
        private int downloads;
        private String version_type;
        private String status;
        private String requested_status;
        private List<VersionFile> files;
        private List<Dependency> dependencies;
    }

    @Getter
    @Setter
    public static class VersionFile {
        private Map<String, String> hashes;
        private String url;
        private String filename;
        private boolean primary;
        private int size;
        private String file_type;
    }

    @Getter
    @Setter
    public static class Dependency {
        public String version_id;
        public String project_id;
        public String file_name;
        public String dependency_type;
    }
}

