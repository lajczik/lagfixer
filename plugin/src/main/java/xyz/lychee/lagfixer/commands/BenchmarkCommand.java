package xyz.lychee.lagfixer.commands;

import lombok.Data;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ErrorsManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BenchmarkCommand extends CommandManager.Subcommand {
    private volatile boolean benchmark = false;

    public BenchmarkCommand(CommandManager commandManager) {
        super(commandManager, "benchmark", "run benchmark and compare it with other servers", "test");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.benchmark) {
            return MessageUtils.sendMessage(true, sender, "&7Benchmark is running, wait for results in console!");
        }

        AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
        if (monitor.getMspt() > 10.0) {
            return MessageUtils.sendMessage(true, sender, "&7Server MSPT is too &chigh&7, the result may be incorrect!");
        }

        long availableRam = monitor.getRamFree() + (monitor.getRamMax() - monitor.getRamTotal());
        if (availableRam < 2048) {
            return MessageUtils.sendMessage(true, sender, "&7Server available RAM is too low, you need &c" + availableRam + "&8/&c2048MB");
        }

        BukkitTask task = SupportManager.getInstance().getFork().runTimer(true, () -> {
            if (this.benchmark) {
                MessageUtils.sendMessage(true, sender, "&7Async benchmark in progress, wait for results...");
            }
        }, 1, 2, TimeUnit.SECONDS);

        this.benchmark = true;
        Thread thread = new Thread(() -> {
            try {
                TimingUtil t = TimingUtil.startNew();
                System.gc();

                Benchmark b = this.runBenchmarks(10, 20, 100_000_000, 10);

                task.cancel();

                String result = b.getResult().toString();
                ErrorsManager.getInstance().sendBenchmark(b);

                MessageUtils.sendMessage(true, sender, "&7Benchmark done in &f" + t.stop().getExecutingTime() + "&7ms, results:&f" + result);
                this.getCommandManager().getPlugin().getLogger().info(result);
            } catch (Exception e) {
                MessageUtils.sendMessage(true, sender, "&cBenchmark error: " + e.getMessage());
            }
            this.benchmark = false;
        });
        thread.setName("LagFixer Benchmark");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

        return true;
    }

    public Benchmark runBenchmarks(int warmup, int cpu, int arrayLength, int memoryPasses) {
        Benchmark benchmark = new Benchmark(cpu);

        benchmark.getResult().append("\n \n&8&m    &r&8[ &eLagFixer Advanced CPU Benchmark &8]&m    &r\n ");
        for (int i = 0; i < warmup; i++) {
            cpuTest(1_000_000);
        }
        double totalScore = 0;
        long bestScore = Long.MAX_VALUE;
        long worstScore = Long.MIN_VALUE;
        double checksum = 0;

        for (int i = 0; i < cpu; i++) {
            long startTime = System.nanoTime();
            double result = cpuTest(10_000_000);
            long duration = System.nanoTime() - startTime;

            double score = 10_000_000_000.0 / duration;
            benchmark.scores[i] = score;
            totalScore += score;
            bestScore = Math.min(bestScore, duration);
            worstScore = Math.max(worstScore, duration);
            checksum += result;
        }

        benchmark.getResult()
                .append("\n &8• &fAverage performance: &e").append(totalScore / cpu).append(" Gop/s")
                .append("\n &8• &fBest time: &e").append(bestScore / 1_000_000_000D).append(" s")
                .append("\n &8• &fWorst time: &e").append(worstScore / 1_000_000_000D).append(" s");

        benchmark.setCpu_checksum(checksum);
        benchmark.setTotalScore(totalScore / cpu);
        benchmark.setBestScore(bestScore);
        benchmark.setWorstScore(worstScore);

        // RAM Benchmark
        benchmark.getResult().append("\n \n&8&m    &r&8[ &eLagFixer Advanced RAM Benchmark &8]&m    &r\n ");

        long[] array = new long[arrayLength];
        int[] randomIndices = new int[arrayLength];
        Random rand = new Random(2137);

        for (int i = 0; i < arrayLength; i++) {
            randomIndices[i] = rand.nextInt(arrayLength);
        }

        // Sequential Write
        long writeTime = 0;
        for (int pass = 0; pass < memoryPasses; pass++) {
            long start = System.nanoTime();
            for (int i = 0; i < arrayLength; i++) {
                array[i] = i + pass;
            }
            writeTime += System.nanoTime() - start;
        }
        double writeSpeed = (arrayLength * 4D * memoryPasses) / (1024D * 1024D) / (writeTime / 1_000_000_000D);
        benchmark.getResult().append(String.format("\n &8• &fSequential write: &e%.2f MB/s", writeSpeed));
        benchmark.setWriteSpeed(writeSpeed);

        // Sequential Read
        long readTime = 0;
        long readChecksum = 0;
        for (int pass = 0; pass < memoryPasses; pass++) {
            long start = System.nanoTime();
            for (int i = 0; i < arrayLength; i++) {
                readChecksum += array[i];
            }
            readTime += System.nanoTime() - start;
        }
        double readSpeed = (arrayLength * 4D * memoryPasses) / (1024D * 1024D) / (readTime / 1_000_000_000D);
        benchmark.getResult().append(String.format("\n &8• &fSequential read: &e%.2f MB/s", readSpeed));
        benchmark.setReadSpeed(readSpeed);

        // Random Access
        long randomTime = 0;
        long randomChecksum = 0;
        for (int pass = 0; pass < memoryPasses; pass++) {
            long start = System.nanoTime();
            for (int i = 0; i < arrayLength; i++) {
                randomChecksum += array[randomIndices[i]];
            }
            randomTime += System.nanoTime() - start;
        }
        double randomSpeed = (arrayLength * 4D * memoryPasses) / (1024D * 1024D) / (randomTime / 1_000_000_000D);
        benchmark.getResult().append(String.format("\n &8• &fRandom access: &e%.2f MB/s\n ", randomSpeed));
        benchmark.setRandomSpeed(randomSpeed);

        return benchmark;
    }

    private double cpuTest(int iterations) {
        double sum = 0;
        for (int i = 1; i <= iterations; i++) {
            sum += Math.sqrt(i);
            sum -= Math.sin(i);
            sum *= Math.cos(i);
            sum /= Math.log(i + 1);
        }
        return sum;
    }

    @Data
    public static class Benchmark {
        //Cpu benchmark
        private final double[] scores;
        private StringBuilder result = new StringBuilder();
        private double cpu_checksum;
        private double bestScore;
        private double worstScore;
        private double totalScore;

        //Memory benchmark
        private double memory_checksum;
        private double writeSpeed;
        private double readSpeed;
        private double randomSpeed;

        //Compression benchmark
        private double compressionSpeed;
        private double decompressionSpeed;

        public Benchmark(int cpu) {
            this.scores = new double[cpu];
        }
    }
}