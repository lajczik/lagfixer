package xyz.lychee.lagfixer;

import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lychee.lagfixer.managers.*;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class LagFixer extends JavaPlugin {
    private static @Getter LagFixer instance;
    private final ArrayList<AbstractManager> managers = new ArrayList<>();
    private final Logger logger = new ColoredLogger();
    private BukkitAudiences audiences;

    @Override
    public void onEnable() {
        instance = this;
        this.audiences = BukkitAudiences.create(this);
        this.managers.clear();
        this.sendHeader();
        this.loadManager(new ErrorsManager(this));
        this.loadManager(new ConfigManager(this));
        this.loadManager(new SupportManager(this));
        this.loadManager(new HookManager(this));
        this.loadManager(new MetricsManager(this));
        this.loadManager(new UpdaterManager(this));
        this.loadManager(new ModuleManager(this));
        this.loadManager(new CommandManager(this));
        this.getLogger().info("&fRemember to leave a rating!&r &e&l★ ★ ★ ★ ★");
        this.getLogger().info("&c❤ &fSupport us &e&nhttps://ko-fi.com/lajczik");
    }

    @Override
    public void onDisable() {
        Iterator<AbstractManager> it = this.managers.iterator();
        while (it.hasNext()) {
            AbstractManager manager = it.next();
            try {
                this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fDisabling manager...");
                TimingUtil t = TimingUtil.startNew();
                manager.disable();
                this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fDisabled manager in &e" + t.stop().getExecutingTime() + "ms&f!");
            } catch (Exception ex) {
                this.printError(ex);
            }
            it.remove();
        }
        SupportManager.getInstance().getMonitor().stop();
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }

    public void sendHeader() {
        this.getLogger().info("&fRemember to leave a rating!&r &e&l★ ★ ★ ★ ★" +
                "\n\n\n\n\n" +
                "\n&fLagFixer " + this.getDescription().getVersion() + " - Best Performance Solution!&r" +
                "\n░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░" +
                "\n░██╗░░░░░░█████╗░░██████╗░░░███████╗██╗██╗░░██╗███████╗██████╗░░" +
                "\n░██║░░░░░██╔══██╗██╔════╝░░░██╔════╝██║╚██╗██╔╝██╔════╝██╔══██╗░" +
                "\n░██║░░░░░███████║██║░░██╗░░░█████╗░░██║░╚███╔╝░█████╗░░██████╔╝░" +
                "\n░██║░░░░░██╔══██║██║░░╚██╗░░██╔══╝░░██║░██╔██╗░██╔══╝░░██╔══██╗░" +
                "\n░███████╗██║░░██║╚██████╔╝░░██║░░░░░██║██╔╝╚██╗███████╗██║░░██║░" +
                "\n░╚══════╝╚═╝░░╚═╝░╚═════╝░░░╚═╝░░░░░╚═╝╚═╝░░╚═╝╚══════╝╚═╝░░╚═╝░" +
                "\n░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░" +
                "\n\n\n\n\n");
    }

    public void loadManager(AbstractManager manager) {
        if (!manager.isEnabled()) {
            return;
        }
        this.managers.add(manager);
        try {
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fEnabling manager...");
            TimingUtil t = TimingUtil.startNew();
            manager.load();
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fEnabled manager in &e" + t.stop().getExecutingTime() + "ms&f!");
        } catch (Exception ex) {
            this.printError(ex);
        }
    }

    public void printError(Throwable ex) {
        if (ErrorsManager.getInstance() != null && ErrorsManager.getInstance().checkError(ex)) {
            this.getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
    }
}

