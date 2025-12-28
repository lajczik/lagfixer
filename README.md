![logo](https://i.imgur.com/hElpNHD.png)

**LagFixer** is the ultimate performance boosting Minecraft plugin designed to optimize your server and eliminate unnecessary lag. By fine-tuning various server aspects and streamlining redundant features, LagFixer ensures a smoother and more enjoyable gameplay experience for all players.

## Requirements:
- Java 8 or later
- Server version 1.16.5 - 1.21.11

## Supported versions:
- 1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20-1.21.11
- Most modules run on a wider range of versions [1.16.5 - 1.21.11]
- Forge based spigot forks: [Mohist](https://mohistmc.com/), [Arclight](https://github.com/IzzelAliz/Arclight) etc.

<details>
<summary>Supported plugins</summary>
  
- [PlaceholderAPI](https://www.spigotmc.org/resources/6245/)
- [WildStacker](https://bg-software.com/wildstacker/)
- [UltimateStacker](https://songoda.com/product/16)
- [RoseStacker](https://www.spigotmc.org/resources/82729/)
- [LevelledMobs](https://www.spigotmc.org/resources/74304/)
- [Spark](https://spark.lucko.me/download)
  
</details>

<details>
<summary>Placeholders</summary>

- %lagfixer_tps% - Current ticks per second
- %lagfixer_mspt% - Current miliseconds per tick
- %lagfixer_cpuprocess% - Current process cpu usage
- %lagfixer_cpusystem% - Current system cpu usage
- %lagfixer_worldcleaner% - Countdown to world clean

</details>

<details>
<summary>Commands</summary>

- /lagfixer - main plugin command
- /abyss - the place where deleted items go

</details>

## Downloads:
- [Modrinth](https://modrinth.com/plugin/lagfixer) - (Use this for fresh updated versions)
- [SpigotMC](https://www.spigotmc.org/resources/lagfixer-1-13-1-21-10-%E2%9A%A1%EF%B8%8Fbest-performance-solution-%EF%B8%8F-2100-servers-%E2%9C%85-lightweight-and-asynchronous.111684/)

## Modules:
### ⭐ MobAiReducer: (Impact on performance: VERY HIGH)
- Replaces creature movement to optimize and reduce behavior.
- Addresses inefficiencies caused by default animal behavior like unnecessary random movements or constant looking around.
- LagFixer intervenes by disabling unnecessary PathFinders or replacing them with more efficient ones.
- Crucial in scenarios with numerous animals as even minor movements can strain server resources.

### ⭐ ItemsCleaner (Impact on performance: MEDIUM)
- Cleans up old items on the ground to accelerate server performance.
- Accumulation of items over time contributes to server lag, especially in densely populated or active servers.
- Removes extraneous items promptly to relieve server burden.
- Players can retrieve items from the Abyss inventory using the /abyss command.

### ⭐ EntityLimiter (Impact on performance: HIGH)
- Restricts the number of entities per chunk.
- Essential for survival servers with expansive animal farms.
- Prevents excessive entity accumulation and associated performance issues.
- Maintains stable performance levels even in environments with high entity density.

### ⭐ LagShield (Impact on performance: HIGH)
- Monitors server load and adjusts settings during latency spikes.
- Addresses fluctuations in server performance to mitigate delays and lag.
- Dynamically adjusts settings, disables unnecessary features, and optimizes resources.
- Ensures smooth gameplay by minimizing the impact of performance fluctuations.

### ⭐ RedstoneLimiter (Impact on performance: MEDIUM)
- Disables demanding Redstone clocks to prevent server overload.
- Certain Redstone configurations can lead to performance degradation and crashes.
- Activating AntiRedstone preserves server stability and ensures responsiveness.
- Facilitates uninterrupted gameplay even with complex Redstone contraptions.

### ⭐ ExplosionOptimizer (Impact on performance: HIGH)
- Limits explosion power and prevents chain reactions to reduce lag and destruction.
- Useful for servers with frequent TNT, creepers, or End Crystal usage.
- Prevents excessive explosions from causing performance issues.
- Maintains stable server performance while controlling destructive events.

### ⭐ ConsoleFilter (Impact on performance: VISUAL ONLY)
- Filters console messages based on predefined rules.
- Enhances clarity by selectively displaying essential messages.
- Reduces clutter and improves readability in multiplayer servers.
- Facilitates efficient server administration and enhances the user experience for both administrators and players.

### ⭐ VehicleMotionReducer (Impact on performance: MEDIUM)
- Optimizes all vehicles such as Boats and Minecarts.
- Removes minecarts with chests spawned in mineshafts.
- Particularly useful when minecarts are frequently used on the server.
- Enhances server performance by optimizing vehicle mechanics and removing unnecessary entities.

### ⭐ AbilityLimiter (Impact on performance: MEDIUM)
 - Limits rapid Trident and Elytra usage to prevent excessive chunk loading.
 - Frequent high-speed travel can cause server lag and instability.
 - AbilityLimiter allows adjusting the speed reduction to balance performance and player experience.
 - Activating AbilityLimiter ensures smoother world loading, stable server performance, and controlled mobility.

## Metrics bStats:
![bStats:](https://bstats.org/signatures/bukkit/LagFixer.svg)


# Other plugins:
[![gatekeeper](https://i.imgur.com/YHGjHR4.png)](https://modrinth.com/plugin/gatekeeper-mc)

[![dynamicdns](https://i.imgur.com/BikoONq.png)](https://modrinth.com/plugin/dynamicdns)
