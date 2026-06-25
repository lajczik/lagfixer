package xyz.lychee.lagfixer.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.BenchmarkCommand;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.objects.WorldsMonitor;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private final AbstractFilter filter = new CustomFilter();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentLinkedQueue<SendTask> sendQueue = new ConcurrentLinkedQueue<>();
    private boolean isSending = false;

    public ErrorsManager(LagFixer plugin) {
        super(plugin);
        instance = this;
    }

    @Override
    public void load() {
        Logger logger = (Logger) LogManager.getRootLogger();

        Iterator<Filter> it = logger.getFilters();
        boolean initialized = false;
        while (it.hasNext()) {
            Filter filter = it.next();
            if (filter instanceof CustomFilter) {
                initialized = true;
            }
        }

        if (!initialized) {
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
        if (stackTrace.isEmpty()) return true;

        StringBuilder message = new StringBuilder();
        message.append("\n");
        message.append("\n     &fAn error occurred in lagfixer:");
        message.append("\n        &4&n").append(t.getClass().getSimpleName()).append("&r&8: &4").append(t.getMessage());
        for (String str : stackTrace) {
            message.append("\n        &7^ &c").append(str);
        }
        message.append("\n");
        message.append("\n   &fOur support has been informed about it, it will be fixed soon.");
        message.append("\n   &fMake sure the LagFixer configuration is done correctly.");
        message.append("\n   &fIf you have any doubts, contact support: &nhttps://discord.gg/CFmzJjgZdu&r");
        message.append("\n");
        this.getPlugin().getLogger().warning(message.toString());

        if (this.errors.containsKey(key)) return false;

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
        ResourceMonitor resourceMonitor = support.getResourceMonitor();
        WorldsMonitor worldsMonitor = support.getWorldsMonitor();

        JsonObject jo = new JsonObject();
        jo.addProperty("bukkit", Bukkit.getName() + " " + Bukkit.getServer().getBukkitVersion());
        jo.addProperty("version", this.getPlugin().getDescription().getVersion());
        jo.addProperty("uuid", this.uuid.toString());
        jo.addProperty("entities", worldsMonitor.getEntities());
        jo.addProperty("creatures", worldsMonitor.getCreatures());
        jo.addProperty("items", worldsMonitor.getItems());
        jo.addProperty("projectiles", worldsMonitor.getProjectiles());
        jo.addProperty("vehicles", worldsMonitor.getVehicles());
        jo.addProperty("players", Bukkit.getOnlinePlayers().size());
        jo.addProperty("maxplayers", Bukkit.getMaxPlayers());
        jo.addProperty("cpuprocess", resourceMonitor.getCpuProcess());
        jo.addProperty("cpusystem", resourceMonitor.getCpuSystem());
        jo.addProperty("ramused", resourceMonitor.getRamUsed());
        jo.addProperty("ramtotal", resourceMonitor.getRamTotal());
        jo.addProperty("ramfree", resourceMonitor.getRamFree());
        jo.addProperty("tps", resourceMonitor.getTps());
        jo.addProperty("mspt", resourceMonitor.getMspt());
        jo.addProperty("current_version", updater.getCurrentVersion());
        jo.addProperty("latest_version", updater.getLatestVersion());
        jo.addProperty("difference_version", updater.getDifference());

        return jo;
    }

    private void connect(String params, JsonObject jsonObject) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sakuramc.pl" + params))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();

        SupportManager.getInstance().getClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private List<String> filterStackTrace(Throwable ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        List<String> list = new ArrayList<>();
        int maxLines = 10;
        int index = 0;

        for (StackTraceElement element : stackTrace) {
            if (index >= maxLines) {
                int tracesLeft = stackTrace.length - index;
                list.add("and " + tracesLeft + " traces more…");
                return list;
            }

            String trace = formatStackTraceElement(element);
            if (trace == null) continue;

            list.add(trace);
            index++;
        }
        return list;
    }

    private String formatStackTraceElement(StackTraceElement element) {
        if (element.getFileName() == null || element.getLineNumber() < 0)
            return null;

        return String.format("%s$%s(%d)",
                element.getClassName(),
                element.getMethodName(),
                element.getLineNumber()
        );
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
            if (!(o instanceof ThrowableKey that)) return false;
            return Objects.equals(type, that.type)
                    && Objects.equals(message, that.message)
                    && Objects.equals(causeKey, that.causeKey);
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

    private class CustomFilter extends AbstractFilter {
        @Override
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
    }
}