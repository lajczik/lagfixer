package xyz.lychee.lagfixer.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MapCommand extends CommandManager.Subcommand {
    private MapHandler mapHandler;

    public MapCommand(CommandManager commandManager) {
        // 翻译：服务器负载监控地图（含MSPT图表）
        super(commandManager, "map", "服务器负载监控地图（含MSPT图表）");
    }

    @Override
    public void load() {
        boolean enabled = this.getCommandManager().getPlugin().getConfig().getBoolean("main.map.enabled", true);
        if (enabled) {
            try {
                this.mapHandler = new MapHandler(this.getCommandManager().getPlugin());
                this.mapHandler.load();
            } catch (Throwable ignored) {}
        }
        else if (this.mapHandler != null) {
            this.mapHandler.unload();
            this.mapHandler = null;
        }
    }

    @Override
    public void unload() {
        if (this.mapHandler != null) {
            this.mapHandler.unload();
        }
    }

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.mapHandler == null) {
            // 翻译：地图当前不可用...
            MessageUtils.sendMessage(true, sender,
                    "&7地图当前不可用。" +
                            "\n您需要在启动应用时添加 &e&n-Djava.awt.headless=true&7 标志来解决此问题。" +
                            "\n这将启用无头模式，避免X11显示连接问题。" +
                            "\n请使用此标志重新启动服务器，并检查问题是否仍然存在。");
            return false;
        }

        if (!(sender instanceof Player)) {
            Component text = Language.getMainValue("player_only", true);
            if (text != null) {
                LagFixer.getInstance().getAudiences().sender(sender).sendMessage(text);
            }
            return false;
        }

        this.mapHandler.giveMap((Player) sender);
        return true;
    }

    private static class MapHandler extends MapRenderer {
        private static final int MAP_SIZE = 128;
        private static final int PADDING = 5;
        private static final Color GRID_COLOR = new Color(50, 50, 50, 100);
        private static final BasicStroke STROKE = new BasicStroke(2.0f);
        private static final int MAX_DATA_POINTS = 20;
        private static final LinearGradientPaint PAINT = new LinearGradientPaint(
                0.0f,
                5.0f,
                0.0f,
                123.0f,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{Color.RED, Color.YELLOW, Color.GREEN}
        );

        private final LagFixer plugin;
        private final ItemStack mapItem;
        private final MapView mapView;
        private final byte[] bytes = new byte[16384];
        private final int[] pixels;
        private final Graphics2D g2d;
        private final int[] valuesBuffer = new int[MAX_DATA_POINTS];
        private final Path2D.Double path = new Path2D.Double();

        private int bufferIndex;
        private int dataCount;
        private volatile boolean shouldRender = true;
        private ScheduledFuture<?> task;

        public MapHandler(LagFixer plugin) throws AWTError {
            this.plugin = plugin;
            this.mapItem = new ItemStack(Material.FILLED_MAP);

            FileConfiguration config = plugin.getConfig();
            MapView tempMap = null;
            if (config.isSet("main.map.id")) {
                tempMap = Bukkit.getServer().getMap(config.getInt("main.map.id"));
            }

            if (tempMap == null) {
                this.mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
                config.set("main.map.id", this.mapView.getId());
                plugin.saveConfig();
            } else {
                this.mapView = tempMap;
            }

            BufferedImage image = new BufferedImage(MAP_SIZE, MAP_SIZE, BufferedImage.TYPE_INT_RGB);
            this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            this.g2d = image.createGraphics();
            this.g2d.setColor(Color.GRAY);
            this.g2d.fillRect(0, 0, MAP_SIZE, MAP_SIZE);

            MapMeta meta = (MapMeta) this.mapItem.getItemMeta();
            meta.setMapView(this.mapView);
            // 翻译：⚡ 服务器监控！ ⚡
            meta.setDisplayName(MessageUtils.fixColors(null, "&e⚡ &f服务器监控！ &e⚡"));
            this.mapItem.setItemMeta(meta);

            this.mapView.getRenderers().clear();
            this.mapView.addRenderer(this);
        }

        public void load() {
            int interval = this.plugin.getConfig().getInt("main.map.interval");

            SupportManager support = SupportManager.getInstance();
            task = support.getExecutor().scheduleWithFixedDelay(() -> {
                AbstractMonitor monitor = support.getMonitor();
                boolean supportMspt = support.getFork().isSupportMspt();
                int pixelY = supportMspt ? msptToPixelY(monitor.getMspt()) : tpsToPixelY(monitor.getTps());

                if (this.dataCount < MAX_DATA_POINTS) {
                    this.valuesBuffer[this.dataCount++] = pixelY;
                } else {
                    this.valuesBuffer[this.bufferIndex] = pixelY;
                    this.bufferIndex = (this.bufferIndex + 1) % MAX_DATA_POINTS;
                }

                if (!this.shouldRender) return;

                renderGraph();
                // 翻译：文本格式
                renderText(supportMspt ?
                        String.format("MSPT: %.1f TPS: %.1f", monitor.getMspt(), monitor.getTps())
                        : String.format("TPS: %.1f, CPU: %.1f", monitor.getTps(), monitor.getCpuProcess())
                );

                for (int i = 0; i < this.pixels.length; i++) {
                    int rgb = this.pixels[i];
                    this.bytes[i] = MapPalette.matchColor(new Color(rgb));
                }

                this.shouldRender = false;
            }, 10L, interval, TimeUnit.SECONDS);
        }

        private void renderGraph() {
            this.g2d.setColor(Color.WHITE);
            this.g2d.fillRect(PADDING, PADDING, 118, 118);

            this.path.reset();

            int validPoints = Math.min(this.dataCount, MAX_DATA_POINTS);
            for (int i = 0; i < validPoints; i++) {
                int dataIndex = (this.bufferIndex + i) % MAX_DATA_POINTS;
                int y = this.valuesBuffer[dataIndex];
                double x = PADDING + i * 118.0 / (validPoints - 1);

                if (i == 0) {
                    this.path.moveTo(x, y);
                } else {
                    this.path.lineTo(x, y);
                }
            }
            this.path.lineTo(PADDING + 118, 123);
            this.path.lineTo(PADDING, 123);
            this.path.closePath();

            this.g2d.setPaint(PAINT);
            this.g2d.fill(this.path);

            this.g2d.setStroke(STROKE);
            this.g2d.setColor(GRID_COLOR);
            for (int i = 1; i < 5; i++) {
                int y = (int) (PADDING + i * 118.0 / 5.0);
                this.g2d.drawLine(123, y, PADDING, y);
            }

            this.g2d.setColor(Color.BLACK);
            this.g2d.drawRect(PADDING, PADDING, 118, 118);
        }

        private int msptToPixelY(double mspt) {
            double clamped = Math.min(mspt, 200.0);
            double scale = (clamped - 10.0) / 125.0;
            scale = Math.max(0.0, Math.min(1.0, scale));
            return (int) Math.round(3 + (1.0 - scale) * 118.0);
        }

        private int tpsToPixelY(double tps) {
            double clamped = Math.max(15.0, Math.min(20.0, tps));
            double scale = (clamped - 15.0) / 5.0;
            return (int) Math.round(3 + scale * 118.0);
        }

        private void renderText(String text) {
            MinecraftFont font = MinecraftFont.Font;
            int x = 10, y = 10;
            int color = Color.BLACK.getRGB();

            for (char ch : text.toCharArray()) {
                MapFont.CharacterSprite sprite = font.getChar(ch);
                if (sprite == null) continue;

                int width = sprite.getWidth();
                for (int row = 0; row < font.getHeight(); row++) {
                    for (int col = 0; col < width; col++) {
                        if (sprite.get(row, col)) {
                            int pixelIndex = (y + row) * MAP_SIZE + (x + col);
                            pixels[pixelIndex] = color;
                        }
                    }
                }
                x += width + 1;
            }
        }

        public void unload() {
            if (this.task != null) this.task.cancel(true);
            this.g2d.dispose();
        }

        @Override
        public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
            if (!mapView.equals(this.mapView)) return;

            PlayerInventory inv = player.getInventory();
            if (inv.getItemInMainHand().getType() != Material.FILLED_MAP &&
                    inv.getItemInOffHand().getType() != Material.FILLED_MAP) return;

            this.shouldRender = true;
            mapCanvas.setCursors(new MapCursorCollection());

            for (int i = 0; i < 16384; ) {
                int y = i >> 7;
                int x = i & 0x7F;
                mapCanvas.setPixel(x, y, this.bytes[i++]);
            }
        }

        public void giveMap(Player p) {
            p.getInventory().addItem(this.mapItem);
        }
    }
}