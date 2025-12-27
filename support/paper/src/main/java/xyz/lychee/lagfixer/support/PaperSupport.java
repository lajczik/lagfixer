package xyz.lychee.lagfixer.support;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.objects.AbstractFork;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PaperSupport extends AbstractFork {
    public PaperSupport(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isSupported() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Bukkit.getAsyncScheduler();
            Bukkit.getGlobalRegionScheduler();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public double getMspt() {
        return Bukkit.getAverageTickTime();
    }

    @Override
    public boolean isSupportMspt() {
        return true;
    }

    @Override
    public PluginCommand registerCommand(Plugin plugin, String name, List<String> alliases, CommandExecutor executor) {
        try {
            Constructor<PluginCommand> declaredConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            declaredConstructor.setAccessible(true);
            PluginCommand cmd = declaredConstructor.newInstance(name, plugin);
            cmd.setAliases(alliases);
            cmd.setExecutor(executor);
            if (executor instanceof TabCompleter)
                cmd.setTabCompleter((TabCompleter) executor);
            Bukkit.getCommandMap().register(name, cmd);
            return cmd;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public BukkitTask runNow(boolean async, @Nullable Location loc, Runnable run) {
        if (!getPlugin().isEnabled()) {
            return new FoliaTask(getPlugin(), null);
        }
        Plugin plugin = getPlugin();
        ScheduledTask task;
        if (async) {
            task = Bukkit.getAsyncScheduler().runNow(getPlugin(), s -> run.run());
        } else {
            if (loc == null) {
                task = Bukkit.getGlobalRegionScheduler().run(getPlugin(), s -> run.run());
            } else {
                task = Bukkit.getRegionScheduler().run(getPlugin(), loc, s -> run.run());
            }
        }
        return new FoliaTask(plugin, task);
    }

    @Override
    public BukkitTask runLater(boolean async, Runnable run, long delayInMs) {
        if (this.checkTask(delayInMs)) return new FoliaTask(this.getPlugin(), null);

        return new FoliaTask(this.getPlugin(), async ?
                Bukkit.getAsyncScheduler().runDelayed(this.getPlugin(), (s) -> run.run(), delayInMs, TimeUnit.MILLISECONDS) :
                Bukkit.getGlobalRegionScheduler().runDelayed(this.getPlugin(), (s) -> run.run(), delayInMs / 50));
    }

    @Override
    public BukkitTask runTimer(boolean async, Runnable run, long initialDelayInMs, long delayInMs) {
        if (this.checkTask(initialDelayInMs, delayInMs)) return new FoliaTask(this.getPlugin(), null);

        return new FoliaTask(this.getPlugin(), async ?
                Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), (s) -> run.run(), initialDelayInMs, delayInMs, TimeUnit.MILLISECONDS) :
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.getPlugin(), (s) -> run.run(), initialDelayInMs / 50, delayInMs / 50));
    }

    @Override
    public BukkitTask runLater(boolean async, Runnable run, long delay, TimeUnit unit) {
        if (this.checkTask(delay)) return new FoliaTask(this.getPlugin(), null);

        return new FoliaTask(this.getPlugin(), async ?
                Bukkit.getAsyncScheduler().runDelayed(this.getPlugin(), (s) -> run.run(), delay, unit) :
                Bukkit.getGlobalRegionScheduler().runDelayed(this.getPlugin(), (s) -> run.run(), unit.toMillis(delay) / 50));
    }

    @Override
    public BukkitTask runTimer(boolean async, Runnable run, long initialDelay, long delay, TimeUnit unit) {
        if (this.checkTask(initialDelay, delay)) return new FoliaTask(this.getPlugin(), null);

        return new FoliaTask(this.getPlugin(), async ?
                Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), (s) -> run.run(), initialDelay, delay, unit) :
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.getPlugin(), (s) -> run.run(), unit.toMillis(initialDelay) / 50, unit.toMillis(delay) / 50));
    }

    public static class FoliaTask implements BukkitTask {
        private final @Nullable ScheduledTask task;
        private final Plugin plugin;

        public FoliaTask(Plugin plugin, @Nullable ScheduledTask task) {
            this.task = task;
            this.plugin = plugin;
        }

        @Override
        public int getTaskId() {
            return this.task == null ? -1 : this.task.hashCode();
        }

        @Override
        public @NotNull Plugin getOwner() {
            return this.plugin;
        }

        @Override
        public boolean isSync() {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return this.task == null || this.task.isCancelled();
        }

        @Override
        public void cancel() {
            if (this.task != null && !this.task.isCancelled()) {
                this.task.cancel();
            }
        }
    }
}