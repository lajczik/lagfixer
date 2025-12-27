package xyz.lychee.lagfixer.hooks;

import io.github.arcaneplugins.levelledmobs.LevelledMobs;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

@Getter
public class LevelledMobsHook extends AbstractHook {
    public LevelledMobsHook(LagFixer plugin, HookManager manager) {
        super(plugin, "LevelledMobs", manager);
    }

    public boolean isLevelled(LivingEntity e) {
        return LevelledMobs.getInstance().getLevelManager().isLevelled(e);
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
    }
}

