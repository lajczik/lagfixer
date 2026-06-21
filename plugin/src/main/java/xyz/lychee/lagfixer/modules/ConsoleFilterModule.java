package xyz.lychee.lagfixer.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class ConsoleFilterModule extends AbstractModule {
    private final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[ -/]*[@-~]");

    private final CustomFilter filter = new CustomFilter();
    private Path logPath;
    private BufferedWriter writer;
    private PrintWriter printWriter;

    private int logsLimit;
    private boolean filtering;
    private boolean saveFiltered;
    private boolean errorFiltering;
    private List<Pattern> patterns;

    public ConsoleFilterModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.VISUAL_ONLY, "ConsoleFilter",
                new String[]{
                        "Filters console messages based on predefined rules.",
                        "Enhances clarity by selectively displaying essential messages.",
                        "Reduces clutter and improves readability in multiplayer servers."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNjNzg5ZjIzMDc5NGY5MGUzM2M0ZjlhZDAwNjk0YmMyYTJmZjVlOGI5YjM3NWRjMzUzMjQwMWIyODFmM2U1OCJ9fX0=");
    }

    private void clearLogs(Path directory) {
        if (logsLimit < 1 || !Files.exists(directory)) return;

        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.toString().endsWith(".log.gz"))
                    .sorted(Comparator.comparingLong((Path p) -> {
                        try {return Files.getLastModifiedTime(p).toMillis();} catch (IOException e) {return 0L;}
                    }).reversed())
                    .skip(logsLimit)
                    .forEach(path -> {
                        try {Files.deleteIfExists(path);} catch (IOException ignored) {}
                    });
        } catch (IOException e) {
            getPlugin().printError(e);
        }
    }

    public synchronized void write(String text) {
        try {
            writer.write(text);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            getPlugin().printError(ex);
        }
    }

    @Override
    public void load() throws IOException {
        Path folder = getPlugin().getDataFolder().toPath().resolve("logs");
        Files.createDirectories(folder);
        this.logPath = folder.resolve("filtered_logs.txt");

        if (!Files.exists(logPath)) {
            Files.createFile(logPath);
        }

        this.writer = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        this.printWriter = new PrintWriter(writer);

        clearLogs(Paths.get("logs"));

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
    }

    @Override
    public boolean loadConfig() {
        this.logsLimit = getSection().getInt("logs_limit");
        this.filtering = getSection().getBoolean("filter.enabled");
        this.saveFiltered = getSection().getBoolean("filter.save_filtered");
        this.errorFiltering = getSection().getBoolean("filter.error_filtering");
        this.patterns = getSection().getStringList("filter.patterns").stream()
                .map(Pattern::compile)
                .toList();
        return true;
    }

    @Override
    public void disable() throws IOException {
        this.filtering = false;

        if (writer != null) {
            writer.close();
            printWriter.close();
        }

        if (saveFiltered) {
            Path folder = getPlugin().getDataFolder().toPath().resolve("logs");
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            long count;
            try (Stream<Path> files = Files.list(folder)) {
                count = files.filter(f -> f.getFileName().toString().startsWith(date)).count();
            }

            Path archivePath = folder.resolve(date + " [" + count + "].log.gz");
            try (InputStream fis = Files.newInputStream(this.logPath);
                 GZIPInputStream gis = new GZIPInputStream(fis);
                 OutputStream os = Files.newOutputStream(archivePath)
            ) {
                gis.transferTo(os);
            }

            clearLogs(folder);
        }
    }

    private class CustomFilter extends AbstractFilter {
        @Override
        public Filter.Result filter(LogEvent event) {
            if (!filtering || event.getLoggerName().equals("ErrorFilter") || event.getLoggerName().equals("LagFixer")) {
                return Filter.Result.NEUTRAL;
            }

            String rawMsg = event.getMessage().getFormattedMessage();
            String cleanMsg = ANSI_PATTERN.matcher(rawMsg).replaceAll("");

            if (errorFiltering && event.getMessage().getThrowable() != null) {
                LogManager.getLogger("ErrorFilter").error(cleanMsg);
                write(cleanMsg);
                event.getMessage().getThrowable().printStackTrace(printWriter);
                printWriter.flush();
                return Filter.Result.DENY;
            }

            for (Pattern pat : patterns) {
                if (pat.matcher(cleanMsg).find()) {
                    write(cleanMsg);
                    return Filter.Result.DENY;
                }
            }
            return Filter.Result.NEUTRAL;
        }
    }
}