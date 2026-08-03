package xyz.lychee.lagfixer.hooks;

import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.modules.AFKOptimizerModule;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class PacketEventsHook extends AbstractHook {
    private PacketEventsBridge bridge;

    public PacketEventsHook(LagFixer plugin, HookManager manager) {
        super(plugin, "packetevents", manager);
    }

    @Override
    public void load() {
        this.bridge = new PacketEventsImplementation();
    }

    public void register(AFKOptimizerModule module) {
        if (this.bridge != null) {
            this.bridge.registerAfk(module);
        }
    }

    public void unregisterAfkOptimizer() {
        if (this.bridge != null) {
            this.bridge.unregisterAfk();
        }
    }

    @Override
    public void disable() {
        this.unregisterAfkOptimizer();
    }
}

interface PacketEventsBridge {
    void registerAfk(AFKOptimizerModule module);
    void unregisterAfk();
}

class PacketEventsImplementation implements PacketEventsBridge, com.github.retrooper.packetevents.event.PacketListener {

    private AFKOptimizerModule module;
    private final java.util.Map<com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon, Integer> cancelledPackets =
            java.util.Collections.synchronizedMap(new java.util.HashMap<>());
    private com.github.retrooper.packetevents.event.PacketListenerCommon listener;

    @Override
    public void registerAfk(AFKOptimizerModule module) {
        this.module = module;
        if (this.listener == null) {
            this.listener = com.github.retrooper.packetevents.PacketEvents.getAPI().getEventManager()
                    .registerListener(this, com.github.retrooper.packetevents.event.PacketListenerPriority.LOWEST);
        }
        this.reload();
    }

    @Override
    public void unregisterAfk() {
        if (this.listener != null) {
            com.github.retrooper.packetevents.PacketEvents.getAPI().getEventManager().unregisterListener(this.listener);
            this.listener = null;
        }
    }

    public void reload() {
        this.cancelledPackets.clear();
        this.module.getCancelled_packets().forEach((name, time) -> {
            try {
                com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server type =
                        com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.valueOf(name);
                this.cancelledPackets.put(type, time);
            } catch (IllegalArgumentException ignored) {}
        });
    }

    @Override
    public void onPacketSend(com.github.retrooper.packetevents.event.PacketSendEvent event) {
        Integer time = this.cancelledPackets.get(event.getPacketType());
        if (time != null) {
            AFKOptimizerModule.AfkPlayer afkPlayer = this.module.getAfk_players().get(event.getUser().getUUID());
            if (afkPlayer != null && afkPlayer.getAfkTime().longValue() > time) {
                event.setCancelled(true);
            }
        }
    }
}