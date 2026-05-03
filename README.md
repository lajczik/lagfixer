<div align="center">
  <img src="https://i.imgur.com/hElpNHD.png" alt="LagFixer Logo" width="600"/>

  <br>
  <br>

  <p>
    <a href="https://modrinth.com/plugin/lagfixer"><img src="https://img.shields.io/badge/Download-Modrinth-00AF5C?style=for-the-badge&logo=modrinth" alt="Modrinth"></a>
    <a href="https://github.com/lajczik/lagfixer"><img src="https://img.shields.io/badge/Source-GitHub-181717?style=for-the-badge&logo=github" alt="GitHub"></a>
    <a href="https://discord.gg/CFmzJjgZdu"><img src="https://img.shields.io/badge/Support-Discord-5865F2?style=for-the-badge&logo=discord" alt="Discord"></a>
  </p>

  <p><b>LagFixer helps improve server performance by optimizing the Minecraft engine.</b><br>
    While the paper itself is well optimized, some of its functions still require improvement.
    It won’t fix all lag by itself, but it can noticeably reduce it when used alongside well coded plugins and a solid server setup.
    It is not intended to resolve issues caused by low quality or poorly configured plugins.</p>
</div>

---

## ⚡ At a Glance

| ⚙️ Requirements | 📦 Supported Versions |
| :--- | :--- |
| **Java:** 8 or later | **Main:** `1.16.5`, `1.17.1`, `1.18.2`, `1.19.4`, `1.20 - 26.1.2` |
| **Server:** `1.13.2` - `26.1.2` | **Range:** Most modules run on a wider range `[1.13.2 - 26.1.2]` |

## 🧩 Modules Overview

| Module | Impact | Description & Features |
| :--- | :---: | :--- |
| **MobAiReducer** | 🔴 **VERY HIGH** | Replaces creature movement to optimize behavior. Disables unnecessary PathFinders. Crucial for massive animal farms. |
| **EntityLimiter** | 🔴 **HIGH** | Restricts the number of entities per chunk. Prevents excessive entity accumulation. |
| **LagShield** | 🔴 **HIGH** | Monitors server load and dynamically adjusts settings during latency spikes. |
| **ExplosionOptimizer**| 🔴 **HIGH** | Limits explosion power and chain reactions (TNT, creepers, End Crystals). |
| **ItemsCleaner** | 🟡 **MEDIUM** | Cleans up old items on the ground. Includes `/abyss` command for players to retrieve lost items. |
| **RedstoneLimiter** | 🟡 **MEDIUM** | Disables demanding Redstone clocks to prevent server overload and crashes. |
| **VehicleMotion** | 🟡 **MEDIUM** | Optimizes boats and minecarts. Automatically removes chest minecarts spawned in mineshafts. |
| **AbilityLimiter** | 🟡 **MEDIUM** | Limits rapid Trident and Elytra usage to prevent excessive, fast-paced chunk loading. |
| **ConsoleFilter** | 🟢 **VISUAL** | Filters console messages based on predefined rules. Enhances log clarity and reduces console clutter. |

## 🛠️ Features & Integration

<details>
<summary><b>🔌 Supported Plugins</b></summary>
<br>

- [PlaceholderAPI](https://www.spigotmc.org/resources/6245/)
- [WildStacker](https://bg-software.com/wildstacker/)
- [UltimateStacker](https://songoda.com/product/16)
- [RoseStacker](https://www.spigotmc.org/resources/82729/)
- [LevelledMobs](https://www.spigotmc.org/resources/74304/)
- [Spark](https://spark.lucko.me/download)
</details>

<details>
<summary><b>📊 Placeholders</b></summary>
<br>

| Placeholder | Description |
| :--- | :--- |
| `%lagfixer_tps%` | Current ticks per second (TPS) |
| `%lagfixer_mspt%` | Current milliseconds per tick (MSPT) |
| `%lagfixer_cpuprocess%`| Current process CPU usage |
| `%lagfixer_cpusystem%` | Current system CPU usage |
| `%lagfixer_worldcleaner%`| Countdown to the next world clean |
</details>

<details>
<summary><b>💻 Commands</b></summary>
<br>

- `/lagfixer` - Main plugin command
- `/abyss` - The place where deleted items go (recovery)
</details>

## 📈 Metrics

![bStats](https://bstats.org/signatures/bukkit/LagFixer.svg)

## 🌌 Other Plugins

Check out my other projects:

<div align="center">
  <a href="https://modrinth.com/plugin/gatekeeper-mc">
    <img src="https://i.imgur.com/YHGjHR4.png" alt="Gatekeeper" width="45%">
  </a>
  &nbsp;&nbsp;
  <a href="https://modrinth.com/plugin/dynamicdns">
    <img src="https://i.imgur.com/BikoONq.png" alt="DynamicDNS" width="45%">
  </a>
</div>
