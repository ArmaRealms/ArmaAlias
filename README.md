# ArmaAlias (SimpleAlias)

Paper **1.21.11** plugin, Java **25**, Maven **3.9+**.

## Build

```bash
mvn -B clean package
```

Install `target/SimpleAlias-2.0.0.jar` on Paper 1.21.11. This is a **Paper plugin** using `paper-plugin.yml`; do not install it on Spigot. The plugin retains the name **SimpleAlias**, the `plugins/SimpleAlias/` folder and existing `config.yml` / `aliases/*.yml` formats.

## Brigadier commands

- `/alias` (also `/simplealias`, `/salias`, `/sa`) keeps the existing administration subcommands, help and completions.
- Each configured alias is registered as a Paper `BasicCommand` through `LifecycleEvents.COMMANDS`.
- The lifecycle registrar automatically rebuilds commands on server command-tree reloads. No reflection or access to `SimpleCommandMap.knownCommands` is used.
- An alias whose name collides with an existing command is skipped instead of overriding the existing root. Rename the alias to resolve the conflict.
- `General_Settings.Command_Sync: true` (default) schedules a **Minecraft data reload** on the next tick after creating, deleting, renaming or reloading an alias. This is heavier than the old command map sync; consider disabling it on busy production servers.
- With `Command_Sync: false`, existing commands pick up configuration changes immediately, but new/deleted/renamed command **labels** are reflected in Brigadier only at `/minecraft:reload` or server restart.

Only Paper API and Vault API are declared as Maven dependencies. Vault is an optional server integration for economy and permission groups. Factions and MassiveCore integration, `<faction_name>` replacement, bStats, and the old Commons Lang dependency were removed. Existing aliases that use `<faction_name>` must be updated.

## Manual regression checklist

1. Start on Paper 1.21.11 with Java 25. Confirm `/alias help` and `/sa help`.
2. Test a pre-existing alias from `plugins/SimpleAlias/aliases` and its permission, actions, cooldown and console settings.
3. Create, rename and remove aliases. With `Command_Sync` enabled, verify commands and tab-completion after the next tick and the data reload.
4. Test `/alias reload`, `/alias reload <name>`, suggestion permissions and collisions with vanilla commands.
5. Test with and without Vault present. Test economy-backed costs, permission groups and message escaping.
