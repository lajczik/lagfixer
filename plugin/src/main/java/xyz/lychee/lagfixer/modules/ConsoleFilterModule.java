package xyz.lychee.lagfixer.modules;

import com.google.common.collect.Iterators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ZipUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConsoleFilterModule extends AbstractModule {
    private final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[ -/]*[@-~]");
    private File logs;
    private FileWriter filewriter;
    private BufferedWriter bufferedwriter;
    private PrintWriter printwriter;
    private int logslimit;
    private boolean filtering;
    private boolean savefiltered;
    private boolean errorfiltering;
    private List<Pattern> patterns;
    private final AbstractFilter filter = new AbstractFilter() {
        public Filter.Result filter(LogEvent event) {
            if (!ConsoleFilterModule.this.filtering
                    || event.getLoggerName().equals("ErrorFilter")
                    || event.getLoggerName().equals("LagFixer")) {
                return Filter.Result.NEUTRAL;
            }

            String rawMsg = event.getMessage().getFormattedMessage();
            String cleanMsg = ANSI_PATTERN.matcher(rawMsg).replaceAll("");

            if (errorfiltering && event.getMessage().getThrowable() != null) {
                LogManager.getLogger("ErrorFilter").error(cleanMsg);
                write(cleanMsg);
                event.getMessage().getThrowable().printStackTrace(printwriter);
                printwriter.flush();
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
    };

    public ConsoleFilterModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, AbstractModule.Impact.VISUAL_ONLY, "ConsoleFilter",
                new String[]{
                        "Filters console messages based on predefined rules.",
                        "Enhances clarity by selectively displaying essential messages.",
                        "Reduces clutter and improves readability in multiplayer servers.",
                        "Facilitates efficient server administration and enhances the user experience for both administrators and players."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNjNzg5ZjIzMDc5NGY5MGUzM2M0ZjlhZDAwNjk0YmMyYTJmZjVlOGI5YjM3NWRjMzUzMjQwMWIyODFmM2U1OCJ9fX0="
        );
    }

    public void clearLogs(File directory, String extension) {
        if (this.logslimit < 1) {
            return;
        }
        Arrays.stream(directory.listFiles())
                .filter(f -> f.getName().endsWith(extension))
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .skip(this.logslimit)
                .forEach(File::deleteOnExit);
    }

    public void write(String text) {
        try {
            this.bufferedwriter.write(text);
            this.bufferedwriter.newLine();
            this.bufferedwriter.flush();
        } catch (Exception ex) {
            this.getPlugin().printError(ex);
        }
    }

    @Override
    public void load() throws IOException {
        this.logs = new File(this.getPlugin().getDataFolder() + "/logs", "filtered_logs.txt");
        this.logs.getParentFile().mkdirs();
        this.logs.createNewFile();
        Files.write(this.logs.toPath(), Collections.emptyList(), Charset.defaultCharset());
        this.filewriter = new FileWriter(this.logs, true);
        this.bufferedwriter = new BufferedWriter(this.filewriter);
        this.printwriter = new PrintWriter(this.bufferedwriter);
        File bukkitlogs = new File("logs");
        this.clearLogs(bukkitlogs, ".gz");
        Logger logger = (Logger) LogManager.getRootLogger();
        if (!Iterators.any(logger.getFilters(), f -> f.hashCode() == this.filter.hashCode())) {
            logger.addFilter(this.filter);
        }
    }

    @Override
    public boolean loadConfig() {
        this.logslimit = this.getSection().getInt("logs_limit");
        this.filtering = this.getSection().getBoolean("filter.enabled");
        this.savefiltered = this.getSection().getBoolean("filter.save_filtered");
        this.errorfiltering = this.getSection().getBoolean("filter.error_filtering");
        this.patterns = this.getSection().getStringList("filter.patterns").stream().map(Pattern::compile).collect(Collectors.toList());
        return true;
    }

    @Override
    public void disable() throws IOException {
        this.filtering = false;
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        LoggerConfig rootLogger = loggerContext.getConfiguration().getRootLogger();
        Filter filter = rootLogger.getFilter();
        if (filter != null) {
            rootLogger.removeFilter(filter);
            loggerContext.updateLoggers();
        }
        File directory = new File(this.getPlugin().getDataFolder() + "/logs");
        this.bufferedwriter.close();
        this.filewriter.close();
        this.printwriter.close();
        if (this.savefiltered) {
            String name = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            long number = Arrays.stream(Objects.requireNonNull(directory.listFiles())).filter(file -> file.getName().startsWith(name)).count();
            ZipUtils.zipFile(this.logs, new File(directory, name + " [" + number + "].zip"));
            this.clearLogs(directory, ".zip");
        }
    }
}

