# mcontain

A server-side Fabric mod for modern, non-obfuscated Minecraft versions (the
year-based lines). Ships one jar per Minecraft version via GitHub Actions.

Two tools in one:

- **Gate** — prevents unverified players from leaving the spawn area using
  teleport containment, and forces them into adventure mode so they can't
  break or place anything while inside.
- **Jail** — locks a player into an exploration-proof cell, forced into
  adventure mode, with an optional sentence length.

## Features

- **Gate**: keeps the region around world spawn configured for the gate. Any
  player who isn't verified gets teleported back inside and switched to
  adventure mode, so they can't break or place anything or wander off. The
  gate radius is configurable.
- **Jail**: locks a player into an exploration-proof cell, forced into
  adventure mode, with an optional sentence length.

## Commands

All commands require operator permission.

| Command | Description |
| --- | --- |
| `/mcontain gate [radius]` | Enable gate at world spawn (default radius 16) |
| `/mcontain gate disable` | Disable the gate |
| `/mcontain gate verify [player]` | Verify a player (or self) to leave spawn |
| `/mcontain gate unverify [player]` | Revoke a player's verification |
| `/mcontain jail save <name> [radius]` | Save a jail cell at your position |
| `/mcontain jail <name> <player> [minutes]` | Send a player to a jail cell |
| `/mcontain unjail <player>` | Release a player from jail |
| `/mcontain jail list` | List jail cells and current sentences |
| `/mcontain jail clear` | Release everyone and remove all cells |
| `/mcontain reload` | Reload `config/mcontain.json` |
| `/mcontain status` | Show gate and jail state |

## Configuration

Config is written to `config/mcontain.json` on first run.

## Supported versions

One jar per exact Minecraft release. Grab the jar matching your server's
exact Minecraft version from the latest release and drop it into `mods/`.

<!-- MC-TABLE:START -->

| Version | Java | Download |
| --- | --- | --- |
| `26.1` | 25 | [mcontain-v4-26.1](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v4-26.1.jar) |
| `26.1.1` | 25 | [mcontain-v4-26.1.1](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v4-26.1.1.jar) |
| `26.1.2` | 25 | [mcontain-v4-26.1.2](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v4-26.1.2.jar) |
| `26.2` | 25 | [mcontain-v4-26.2](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v4-26.2.jar) |

<!-- MC-TABLE:END -->

## License

[PolyForm Noncommercial License 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0).
Personal and noncommercial use is free; selling this software is not allowed.