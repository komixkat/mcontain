# mcontain

Server-side Fabric mod that encloses the world spawn and jail griefers.
Adventure-mode containment for every Minecraft version that supports
teleporting and adventure mode. Ships one jar per Minecraft version via
GitHub Actions.

## Supported versions

A jar is built for every release line from Minecraft 1.14.4 through 26.2:

1.14.4 · 1.15.2 · 1.16.5 · 1.17.1 · 1.18.2 · 1.19.2 · 1.19.4 · 1.20.1 ·
1.20.4 · 1.20.6 · 1.21.1 · 1.21.4 · 1.21.7 · 1.21.10 · 1.21.11 · 26.1 · 26.2

Grab the jar matching your server's Minecraft version from the latest
release and drop it into `mods/`.

## Features

- **Gate**: keeps the area around world spawn in adventure mode until a
  player is verified, and returns unverified players to the spawn area.
- **Jail**: locks a griefing player into an exploration-proof cell, forced
  to adventure mode, with an optional sentence length.

## Commands

All commands require operator permission.

| Command | Description |
| --- | --- |
| `/mcontain gate set [radius]` | Set the gate region around world spawn |
| `/mcontain gate unset` | Disable the gate |
| `/mcontain gate verify <player>` | Let a player leave the spawn region |
| `/mcontain gate unverify <player>` | Revoke a player's gate pass |
| `/mcontain jail set <name> [radius]` | Save a jail cell at your position |
| `/mcontain jail <name> <player> [minutes]` | Send a player to a jail cell |
| `/mcontain unjail <player>` | Release a player |
| `/mcontain jail list` | List jail cells and current sentences |
| `/mcontain jail clear` | Release everyone and forget all cells |
| `/mcontain reload` | Reload `config/mcontain.json` |
| `/mcontain status` | Show gate and jail state |

## Configuration

Config is written to `config/mcontain.json` on first run.

## License

[PolyForm Noncommercial License 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0).
Personal and noncommercial use is free; selling this software is not allowed.