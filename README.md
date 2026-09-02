# mcontain

A server-side Fabric mod built for every Minecraft version that supports
teleporting and adventure mode. Ships one jar per Minecraft version via
GitHub Actions.

Two tools in one:

- **Gate** — prevents unverified players from leaving the spawn area using
  teleport containment, and forces them into adventure mode so they can't
  break or place anything while inside.
- **Jail** — locks a player into an exploration-proof cell, forced into
  adventure mode, with an optional sentence length.

## Supported versions

A single jar works across every sub-version of its Minecraft line. Grab the
jar matching your server's Minecraft version from the latest release and
drop it into `mods/`.

<!-- MC-TABLE:START -->

| Minecraft | Built from | Java | Download |
| --- | --- | --- | --- |
| `1.14.x` | `1.14.4` | 8 | [mcontain-v1-1.14.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.14.x.jar) |
| `1.15.x` | `1.15.2` | 8 | [mcontain-v1-1.15.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.15.x.jar) |
| `1.16.x` | `1.16.5` | 8 | [mcontain-v1-1.16.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.16.x.jar) |
| `1.17.x` | `1.17.1` | 16 | [mcontain-v1-1.17.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.17.x.jar) |
| `1.18.x` | `1.18.2` | 16 | [mcontain-v1-1.18.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.18.x.jar) |
| `1.19.x` | `1.19.4` | 17 | [mcontain-v1-1.19.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.19.x.jar) |
| `1.20.x` | `1.20.6` | 21 | [mcontain-v1-1.20.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.20.x.jar) |
| `1.21.x` | `1.21.11` | 21 | [mcontain-v1-1.21.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-1.21.x.jar) |
| `26.1.x` | `26.1.2` | 25 | [mcontain-v1-26.1.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-26.1.x.jar) |
| `26.2.x` | `26.2` | 25 | [mcontain-v1-26.2.x.jar](https://github.com/komixkat/mcontain/releases/latest/download/mcontain-v1-26.2.x.jar) |

<!-- MC-TABLE:END -->

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