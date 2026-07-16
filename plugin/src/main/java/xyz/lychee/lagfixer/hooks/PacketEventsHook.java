package xyz.lychee.lagfixer.hooks;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import lombok.Getter;
import lombok.Setter;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.modules.AFKOptimizerModule;
import xyz.lychee.lagfixer.objects.AbstractHook;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PacketEventsHook extends AbstractHook {
    private AFKOptimizerPacketsListener afkOptimizer;

    public PacketEventsHook(LagFixer plugin, HookManager manager) {
        super(plugin, "packetevents", manager);
    }

    public void register(AFKOptimizerModule module) {
        EventManager eventManager = PacketEvents.getAPI().getEventManager();
        if (this.afkOptimizer == null) {
            this.afkOptimizer = new AFKOptimizerPacketsListener(module);
            this.afkOptimizer.setListener(eventManager.registerListener(this.afkOptimizer, PacketListenerPriority.LOWEST));
        }
        this.afkOptimizer.reload();
    }

    public void unregisterAfkOptimizer() {
        if (this.afkOptimizer != null) {
            PacketEvents.getAPI().getEventManager()
                    .unregisterListener(this.afkOptimizer.getListener());
            this.afkOptimizer = null;
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
        this.unregisterAfkOptimizer();
    }

    @Getter
    @Setter
    public static class AFKOptimizerPacketsListener implements PacketListener {
        private final AFKOptimizerModule module;
        private final Map<PacketTypeCommon, Integer> cancelledPackets = Collections.synchronizedMap(new HashMap<>());
        private PacketListenerCommon listener;

        public AFKOptimizerPacketsListener(AFKOptimizerModule module) {
            this.module = module;
        }

        public void reload() {
            this.cancelledPackets.clear();
            this.module.getCancelled_packets().forEach((name, time) -> {
                try {
                    PacketType.Play.Server type = PacketType.Play.Server.valueOf(name);
                    this.cancelledPackets.put(type, time);
                } catch (IllegalArgumentException ignored) {
                }
            });
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            Integer time = this.cancelledPackets.get(event.getPacketType());
            if (time != null) {
                AFKOptimizerModule.AfkPlayer afkPlayer = this.module.getAfk_players().get(event.getUser().getUUID());
                if (afkPlayer != null && afkPlayer.getAfkTime().longValue() > time) {
                    event.setCancelled(true);
                }
            }
        }
    }
}