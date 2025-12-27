package xyz.lychee.lagfixer.managers;

import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.BenchmarkCommand;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class ErrorsManager extends AbstractManager {
    private static @Getter ErrorsManager instance;
    private final Gson gson = new Gson();
    private final UUID uuid = UUID.randomUUID();
    private final HashMap<ThrowableKey, Error> errors = new HashMap<>();
    private final Pattern pattern = Pattern.compile("https://spark\\.lucko\\.me/.{10}");
    private final AbstractFilter filter;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentLinkedQueue<SendTask> sendQueue = new ConcurrentLinkedQueue<>();
    private boolean isSending = false;

    public ErrorsManager(LagFixer plugin) {
        super(plugin);
        instance = this;
        this.filter = new AbstractFilter() {
            public Filter.Result filter(LogEvent event) {
                if (event.getLoggerName().equals(getPlugin().getLogger().getName())) {
                    return Filter.Result.NEUTRAL;
                }

                if (event.getThrown() != null) {
                    return checkError(event.getThrown()) ? Filter.Result.NEUTRAL : Filter.Result.DENY;
                }

                Matcher matcher = pattern.matcher(event.getMessage().getFormattedMessage());
                if (matcher.find()) {
                    sendProfiler(matcher.group());
                    getPlugin().getLogger()
                            .info("&7Spark profiler has been sent to our support to improve LagFixer optimizations and investigate what loads the server the most.");
                }
                return Filter.Result.NEUTRAL;
            }
        };
    }

    @Override
    public void load() {
        Logger logger = (Logger) LogManager.getRootLogger();
        if (!Iterators.contains(logger.getFilters(), this.filter)) {
            logger.addFilter(this.filter);
        }

        this.executor.scheduleAtFixedRate(this::processQueue, 1, 1, TimeUnit.MINUTES);

        this.getPlugin().getLogger().info(" &8• &rStarted listening console for LagFixer errors!");
    }

    @Override
    public void disable() {
        this.executor.shutdownNow();
    }

    private void processQueue() {
        if (isSending || sendQueue.isEmpty()) {
            return;
        }

        SendTask task = sendQueue.poll();
        if (task != null) {
            isSending = true;
            try {
                task.send();
            } finally {
                isSending = false;
            }
        }
    }

    /**
     * Dodaje zadanie do kolejki jeśli nie ma go już w kolejce
     */
    private void addToQueue(SendTask task) {
        for (SendTask queuedTask : sendQueue) {
            if (queuedTask.equals(task)) {
                return;
            }
        }
        sendQueue.offer(task);
    }

    public boolean checkError(Throwable t) {
        if (t == null) return true;

        ThrowableKey key = new ThrowableKey(t);
        List<String> stackTrace = this.filterStackTrace(t);
        if (stackTrace.isEmpty() || this.errors.containsKey(key)) return true;

        StringBuilder message = new StringBuilder();
        message.append("LagFixer error message:\n");
        message.append("\n&8&m-------------------------------&r");
        message.append("\n");
        message.append("\n&fAn error occurred in lagfixer:");
        message.append("\n &7-> &c").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
        for (String str : stackTrace) {
            message.append("\n &7| &c").append(str);
        }
        message.append("\n");
        message.append("\n&fOur support has been informed about it, it will be fixed soon.");
        message.append("\n&fMake sure the LagFixer configuration is done correctly.");
        message.append("\n&fIf you have any doubts, contact support: &nhttps://discord.gg/CFmzJjgZdu&r");
        message.append("\n");
        message.append("\n&8&m-------------------------------\n");
        this.getPlugin().getLogger().warning(message.toString());

        this.errors.put(key, new Error(stackTrace, t));

        if (!this.errors.values().stream().allMatch(Error::isReported)) {
            sendStackTraces();
        }

        return false;
    }

    @Override
    public boolean isEnabled() {
        return this.getPlugin().getConfig().getBoolean("main.errors_reporter");
    }

    public void sendStackTraces() {
        addToQueue(new ErrorsSendTask());
    }

    public void sendProfiler(String url) {
        addToQueue(new ProfilerSendTask(url));
    }

    public void sendBenchmark(BenchmarkCommand.Benchmark benchmark) {
        addToQueue(new BenchmarkSendTask(benchmark));
    }

    private JsonObject createJson() {
        UpdaterManager updater = UpdaterManager.getInstance();
        SupportManager support = SupportManager.getInstance();
        AbstractMonitor monitor = support.getMonitor();
        JsonObject jo = new JsonObject();

        jo.addProperty("bukkit", Bukkit.getName() + " " + Bukkit.getServer().getBukkitVersion());
        jo.addProperty("version", this.getPlugin().getDescription().getVersion());
        jo.addProperty("uuid", this.uuid.toString());
        jo.addProperty("entities", support.getEntities());
        jo.addProperty("creatures", support.getCreatures());
        jo.addProperty("items", support.getItems());
        jo.addProperty("projectiles", support.getProjectiles());
        jo.addProperty("vehicles", support.getVehicles());
        jo.addProperty("players", Bukkit.getOnlinePlayers().size());
        jo.addProperty("maxplayers", Bukkit.getMaxPlayers());
        jo.addProperty("cpuprocess", monitor.getCpuProcess());
        jo.addProperty("cpusystem", monitor.getCpuSystem());
        jo.addProperty("ramused", monitor.getRamUsed());
        jo.addProperty("ramtotal", monitor.getRamTotal());
        jo.addProperty("ramfree", monitor.getRamFree());
        jo.addProperty("tps", monitor.getTps());
        jo.addProperty("mspt", monitor.getMspt());
        jo.addProperty("current_version", updater.getCurrentVersion());
        jo.addProperty("latest_version", updater.getLatestVersion());
        jo.addProperty("difference_version", updater.getDifference());

        return jo;
    }

    private void connect(String params, JsonObject jsonObject) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) URI.create("https://api.sakuramc.pl" + params).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(this.gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            byte[] data = new byte[4096];
            while (is.read(data, 0, data.length) != -1) {}
            is.close();
            conn.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private List<String> filterStackTrace(Throwable ex) {
        List<String> list = new ArrayList<>();
        for (StackTraceElement e : ex.getStackTrace()) {
            if (!e.getClassName().contains("lagfixer")) continue;

            list.add(String.format("%s -> %s() at %d line", e.getFileName(), e.getMethodName(), e.getLineNumber()));
        }
        return list;
    }

    private interface SendTask {
        void send();

        boolean equals(Object obj);
    }

    public static final class ThrowableKey {
        private final Class<? extends Throwable> type;
        private final String message;
        private final ThrowableKey causeKey;

        public ThrowableKey(Throwable t) {
            this.type = t.getClass();
            this.message = t.getMessage();
            this.causeKey = t.getCause() == null ? null : new ThrowableKey(t.getCause());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ThrowableKey)) return false;
            ThrowableKey that = (ThrowableKey) o;
            return type.equals(that.type) &&
                    Objects.equals(message, that.message) &&
                    Objects.equals(causeKey, that.causeKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, message, causeKey);
        }

        @Override
        public String toString() {
            return type.getSimpleName() + ": " + message + (causeKey != null ? " <- " + causeKey : "");
        }
    }

    private class ErrorsSendTask implements SendTask {
        @Override
        public void send() {
            JsonObject jsonObject = createJson();
            JsonArray errorsArray = new JsonArray();

            for (Error error : errors.values()) {
                if (!error.isReported())
                    error.handle(errorsArray);
            }

            jsonObject.add("errors", errorsArray);
            connect("/errors?plugin=" + getPlugin().getName(), jsonObject);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ErrorsSendTask;
        }
    }

    private class ProfilerSendTask implements SendTask {
        private final String url;

        public ProfilerSendTask(String url) {
            this.url = url;
        }

        @Override
        public void send() {
            JsonObject jsonObject = createJson();
            jsonObject.addProperty("profiler", url);
            connect("/profilers?plugin=" + getPlugin().getName(), jsonObject);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ProfilerSendTask;
        }
    }

    private class BenchmarkSendTask implements SendTask {
        private final BenchmarkCommand.Benchmark benchmark;

        public BenchmarkSendTask(BenchmarkCommand.Benchmark benchmark) {
            this.benchmark = benchmark;
        }

        @Override
        public void send() {
            JsonObject jsonObject = createJson();
            jsonObject.add("benchmark", gson.toJsonTree(benchmark));
            connect("/benchmarks?plugin=" + getPlugin().getName(), jsonObject);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BenchmarkSendTask;
        }
    }

    @Data
    public class Error {
        private final String message;
        private final String stackTrace;
        private final String fullStackTrace;
        private transient boolean reported;

        public Error(List<String> stackTrace, Throwable ex) {
            this.message = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            this.stackTrace = String.join("\n", stackTrace);
            this.fullStackTrace = ExceptionUtils.getStackTrace(ex);
            this.reported = false;
        }

        public void handle(JsonArray arr) {
            arr.add(gson.toJsonTree(this, Error.class));
            this.reported = true;
        }
    }
}