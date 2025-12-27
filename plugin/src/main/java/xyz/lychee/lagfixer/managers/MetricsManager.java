package xyz.lychee.lagfixer.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

public class MetricsManager
        extends AbstractManager {
    private static final Gson gson = new Gson();
    private MetricsBase metricsBase;
    private String uuid;

    public MetricsManager(LagFixer plugin) {
        super(plugin);
    }

    public void addCustomChart(CustomChart chart) {
        this.metricsBase.addCustomChart(chart);
    }

    @Override
    public void load() throws Exception {
        File bStatsFolder = new File(this.getPlugin().getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
        if (!cfg.isSet("serverUuid")) {
            this.uuid = UUID.randomUUID().toString();
            cfg.set("serverUuid", this.uuid);
            cfg.save(configFile);
        } else {
            this.uuid = cfg.getString("serverUuid");
        }
        this.metricsBase = new MetricsBase(19292, Runnable::run);
        this.addCustomChart(new SingleLineChart("entities", () -> SupportManager.getInstance().getEntities()));
        this.addCustomChart(new SingleLineChart("creatures", () -> SupportManager.getInstance().getCreatures()));
        this.addCustomChart(new SingleLineChart("items", () -> SupportManager.getInstance().getItems()));
        this.addCustomChart(new SingleLineChart("projectiles", () -> SupportManager.getInstance().getProjectiles()));
        this.addCustomChart(new SingleLineChart("vehicles", () -> SupportManager.getInstance().getVehicles()));
        this.addCustomChart(new AdvancedPie("modules", () -> {
            Map<String, Integer> values = new HashMap<>();
            Set<AbstractModule> modules = new HashSet<>(ModuleManager.getInstance().getModules().values());
            for (AbstractModule module : modules) {
                if (module.isLoaded()) {
                    values.put(module.getName(), 1);
                }
            }
            return values;
        }));
    }

    @Override
    public void disable() throws Exception {
        if (this.metricsBase != null) {
            this.metricsBase.shutdown();
        }
    }

    @Override
    public boolean isEnabled() {
        return this.getPlugin().getConfig().getBoolean("main.bStats");
    }

    public static abstract class CustomChart {
        private final String chartId;

        protected CustomChart(String chartId) {
            if (chartId == null) {
                throw new IllegalArgumentException("chartId must not be null");
            }
            this.chartId = chartId;
        }

        public JsonObject getRequestJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("chartId", this.chartId);
            try {
                JsonObject data = this.getChartData();
                if (data == null) {
                    return null;
                }
                jsonObject.add("data", data);
            } catch (Throwable t) {
                return null;
            }
            return jsonObject;
        }

        protected abstract JsonObject getChartData() throws Exception;
    }

    public static class SingleLineChart
            extends CustomChart {
        private final Callable<Integer> callable;

        public SingleLineChart(String chartId, Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            int value = this.callable.call();
            if (value == 0) {
                return null;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("value", value);
            return jsonObject;
        }
    }

    public static class AdvancedPie extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObject getChartData() throws Exception {
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    continue;
                }
                allSkipped = false;
                values.addProperty(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                return null;
            }
            data.add("values", values);
            return data;
        }
    }

    public class MetricsBase {
        private final ScheduledExecutorService scheduler;
        private final int serviceId;
        private final Consumer<Runnable> submitTaskConsumer;
        private final Set<CustomChart> customCharts = new HashSet<CustomChart>();

        public MetricsBase(int serviceId, Consumer<Runnable> submitTaskConsumer) {
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, task -> new Thread(task, "bStats-Metrics"));
            scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            this.scheduler = scheduler;
            this.serviceId = serviceId;
            this.submitTaskConsumer = submitTaskConsumer;
            this.startSubmitting();
        }

        private byte[] compress(String str) throws IOException {
            if (str == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return outputStream.toByteArray();
        }

        public void addCustomChart(CustomChart chart) {
            this.customCharts.add(chart);
        }

        public void shutdown() {
            this.scheduler.shutdown();
        }

        private void startSubmitting() {
            Runnable submitTask = () -> {
                if (this.submitTaskConsumer != null) {
                    this.submitTaskConsumer.accept(this::submitData);
                } else {
                    this.submitData();
                }
            };
            long initialDelay = (long) (60000.0 * (3.0 + Math.random() * 3.0));
            long secondDelay = (long) (60000.0 * (Math.random() * 30.0));
            this.scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
            this.scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1800000L, TimeUnit.MILLISECONDS);
        }

        private void submitData() {
            JsonObject baseJson = new JsonObject();
            baseJson.addProperty("playerAmount", Bukkit.getOnlinePlayers().size());
            baseJson.addProperty("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
            baseJson.addProperty("bukkitVersion", Bukkit.getVersion());
            baseJson.addProperty("bukkitName", Bukkit.getName());
            baseJson.addProperty("javaVersion", System.getProperty("java.version"));
            baseJson.addProperty("osName", System.getProperty("os.name"));
            baseJson.addProperty("osArch", System.getProperty("os.arch"));
            baseJson.addProperty("osVersion", System.getProperty("os.version"));
            baseJson.addProperty("coreCount", Runtime.getRuntime().availableProcessors());
            JsonObject serviceJson = new JsonObject();
            serviceJson.addProperty("pluginVersion", MetricsManager.this.getPlugin().getDescription().getVersion());
            serviceJson.addProperty("id", this.serviceId);
            JsonObject[] chartData = this.customCharts.stream().map(CustomChart::getRequestJsonObject).filter(Objects::nonNull).toArray(JsonObject[]::new);
            serviceJson.add("customCharts", gson.toJsonTree(chartData));
            baseJson.add("service", serviceJson);
            baseJson.addProperty("serverUUID", MetricsManager.this.uuid);
            baseJson.addProperty("metricsVersion", "3.0.2");
            this.scheduler.execute(() -> {
                try {
                    this.sendData(baseJson);
                } catch (Exception exception) {
                    // empty catch block
                }
            });
        }

        private void sendData(JsonObject data) throws Exception {
            HttpsURLConnection connection = (HttpsURLConnection) URI.create("https://bStats.org/api/v2/data/bukkit").toURL().openConnection();
            byte[] compressedData = this.compress(data.toString());
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Connection", "close");
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.addRequestProperty("Content-Length", Integer.toString(compressedData.length));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Metrics-Service/1");
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(compressedData);
            }
            connection.getInputStream().close();
        }
    }
}

