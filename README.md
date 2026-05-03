<p align="center">
  <b><a>Welcome to FlameRTP's repository!</a></b><br/>
  <i>High-performance, fully async Random Teleport plugin with pre-cached locations.</i>
</p>

## Features
- **Fully Async** — Location finding runs entirely off the main thread, keeping your server lag-free.
- **Pre-cached Locations** — Locations are found in the background before players even request a teleport, making `/rtp` near-instant.
- **Folia Support** — Built from the ground up with Folia's region scheduler, compatible with both Paper and Folia servers.
- **Multi-world** — Configure independent profiles per world with unique radius, biome blacklists, and unsafe block lists.
- **Economy Support** — Optional Vault integration to charge players per teleport.
- **Cooldowns** — Per-player cooldown system with bypass permissions.
- **Countdown** — Configurable pre-teleport countdown with movement and damage cancellation.
- **Biome Blacklisting** — Prevent players from landing in oceans, rivers, or any other biomes you choose.
- **Shape Support** — Square or circular teleport regions.
- **Nether Roof Support** — Special handling for teleporting under the nether roof.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rtp [world]` | Teleport to a random location | `flamertp.use` |
| `/rtpadmin <clearcd\|clearcache\|force> ...` | Admin operations | `flamertp.admin` |
| `/rtpreload` | Reload configuration | `flamertp.admin` |

Aliases for `/rtp`: `/wild`, `/randomtp`

## Permissions

| Permission | Description | Default |
|---|---|---|
| `flamertp.use` | Use /rtp | true |
| `flamertp.admin` | Use /rtpadmin and /rtpreload | op |
| `flamertp.cooldown.bypass` | Bypass cooldown | op |
| `flamertp.cost.bypass` | Bypass economy cost | op |
| `flamertp.bypass.move` | Don't cancel countdown on move | false |
| `flamertp.bypass.damage` | Don't cancel countdown on damage | false |

## Libraries
FlameRTP uses and is compiled with the following libraries:
- [FoliaLib](https://github.com/TechnicallyCoded/FoliaLib) (included) — Cross-platform scheduler library for Paper and Folia compatibility.
- [Vault](https://github.com/MilkBowl/VaultAPI) (soft dependency) — Economy API for optional teleport costs.

## Build Instructions
```
./gradlew shadowJar
```
The output jar will be in `build/libs/`.

## Requirements
- Java 21+
- Paper or Folia 1.20+
- (Optional) Vault + an Economy Plugin for cost features

## Authors
- **uspigot (bytezlol)**