<div align="center">
  <img src="https://i.imgur.com/hElpNHD.png" alt="LagFixer Logo" width="600"/>

  <br>
  <br>

  <p>
    <a href="https://lajczik.gitbook.io/lagfixer"><img src="https://img.shields.io/badge/Wiki-Gitbook-969393?style=for-the-badge&logo=gitbook" alt="Wiki"></a>
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

## ⚡ Requirements & Compatibility
- **Java:** 8 or later
- **Server:** Bukkit/Spigot/Paper, hybrid Forge, etc.
- **Version range:** `1.13.2` – `26.1.2`

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

## 📈 Performance Metrics
| Placeholder | Description |
| :--- | :--- |
| `%lagfixer_tps%` | Current ticks per second (TPS) |
| `%lagfixer_tps_color%` | TPS with color coding (green ≥18, yellow ≥15, red <15) |
| `%lagfixer_mspt%` | Current milliseconds per tick (MSPT) |
| `%lagfixer_mspt_color%` | MSPT with color coding (green ≤40, yellow ≤50, red >50) |
| `%lagfixer_cpuprocess%` | Current process CPU usage |
| `%lagfixer_cpusystem%` | Current system CPU usage |

## 🧬 Entity Counts
| Placeholder | Description |
| :--- | :--- |
| `%lagfixer_entities%` / `%lagfixer_entities_total%` | Total loaded entities |
| `%lagfixer_mobs%` / `%lagfixer_entities_mobs%` | Total mobs/creatures |
| `%lagfixer_items%` / `%lagfixer_entities_items%` | Total item entities |
| `%lagfixer_projectiles%` / `%lagfixer_entities_projectiles%` | Total projectiles |
| `%lagfixer_vehicles%` / `%lagfixer_entities_vehicles%` | Total vehicles |

## 💾 Memory Stats
| Placeholder | Description |
| :--- | :--- |
| `%lagfixer_memory_used%` / `%lagfixer_ram_used%` | Used memory (MB) |
| `%lagfixer_memory_max%` / `%lagfixer_ram_max%` | Maximum available memory (MB) |
| `%lagfixer_memory_free%` / `%lagfixer_ram_free%` | Free memory (MB) |
| `%lagfixer_memory_percent%` / `%lagfixer_ram_percent%` | Used memory percentage |
| `%lagfixer_memory_bar%` | Visual progress bar of memory usage |

## 🌐 Server Stats
| Placeholder | Description |
| :--- | :--- |
| `%lagfixer_players%` / `%lagfixer_online%` | Current online players |
| `%lagfixer_players_max%` / `%lagfixer_max_players%` | Maximum players allowed |
| `%lagfixer_worlds%` | Number of loaded worlds |
| `%lagfixer_chunks%` / `%lagfixer_loaded_chunks%` | Total loaded chunks |
| `%lagfixer_uptime%` | Server uptime (HH:MM:SS) |
| `%lagfixer_uptime_hours%` | Server uptime in hours |
| `%lagfixer_uptime_minutes%` | Server uptime in minutes |
| `%lagfixer_uptime_seconds%` | Server uptime in seconds |

## 🧹 WorldCleaner Timer
| Placeholder | Description |
| :--- | :--- |
| `%lagfixer_worldcleaner%` / `%lagfixer_worldcleaner_timer%` | Seconds until next world clean (e.g., `45s`) |
| `%lagfixer_worldcleaner_seconds%` | Raw seconds until next world clean |
| `%lagfixer_worldcleaner_formatted%` | Formatted countdown (MM:SS) |
| `%lagfixer_worldcleaner_interval%` | Interval between world cleans (seconds) |
| `%lagfixer_worldcleaner_enabled%` | Whether WorldCleaner is enabled (`true`/`false`) |
| `%lagfixer_worldcleaner_progress%` | Progress percentage towards next clean |
| `%lagfixer_worldcleaner_bar%` | Visual progress bar for world clean timer |

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