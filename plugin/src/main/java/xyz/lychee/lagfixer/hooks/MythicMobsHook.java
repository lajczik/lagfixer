package xyz.lychee.lagfixer.hooks;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Entity;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class MythicMobsHook extends AbstractHook implements HookManager.ModelContainer {
    public MythicMobsHook(LagFixer plugin, HookManager manager) {
        super(plugin, "MythicMobs", manager);
    }

    public boolean hasModel(Entity entity) {
        return MythicBukkit.inst().getMobManager().isMythicMob(entity);
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
    }
}

