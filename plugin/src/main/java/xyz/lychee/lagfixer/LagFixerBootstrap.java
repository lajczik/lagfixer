package xyz.lychee.lagfixer;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

@Getter
public class LagFixerBootstrap implements PluginBootstrap {
    private static final String FOLIA_CLASS = "io.papermc.paper.threadedregions.RegionizedServer";

    @Override
    public void bootstrap(BootstrapContext context) {}

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        Logger logger = context.getLogger();

        if (!isFoliaServer()) {
            logFoliaRequiredMessage(logger);
            throw new UnsupportedOperationException("LagFixer folia build is not supported on this server version");
        }

        return new LagFixer();
    }

    private boolean isFoliaServer() {
        try {
            Class.forName(FOLIA_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void logFoliaRequiredMessage(Logger logger) {
        String message = """
                
                ╔══════════════════════════════════════════════════════════╗
                ║  ❌ INCORRECT VERSION DETECTED! ❌                       ║
                ║                                                          ║
                ║  This version of LagFixer is intended ONLY for Folia     ║
                ║  servers (or Folia forks like MultiFolia).               ║
                ║                                                          ║
                ║  Download the Bukkit-compatible version from:            ║
                ║ → https://modrinth.com/plugin/lagfixer/versions?l=bukkit ║
                ║                                                          ║
                ║  Server will NOT start until you use correct version.    ║
                ╚══════════════════════════════════════════════════════════╝
                """;

        for (int i = 0; i < 3; i++) {
            logger.error(message);
        }
    }
}