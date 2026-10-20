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

Paper API, Vault API and PlaceholderAPI are declared as Maven dependencies with `provided` scope. Vault and PlaceholderAPI remain optional at server runtime: Vault supplies economy/group services, while PlaceholderAPI resolves configured external placeholders. Factions and MassiveCore integration, `<faction_name>` replacement, bStats, and the old Commons Lang dependency were removed. Existing aliases that use `<faction_name>` must be updated.

## Manual regression checklist

1. Start on Paper 1.21.11 with Java 25. Confirm `/alias help` and `/sa help`.
2. Test a pre-existing alias from `plugins/SimpleAlias/aliases` and its permission, actions, cooldown and console settings.
3. Create, rename and remove aliases. With `Command_Sync` enabled, verify commands and tab-completion after the next tick and the data reload.
4. Test `/alias reload`, `/alias reload <name>`, suggestion permissions and collisions with vanilla commands.
5. Test with and without Vault present. Test economy-backed costs, permission groups and message escaping.


## PlaceholderAPI (optional)

PlaceholderAPI **2.12.3** is a **provided/compile-only** Maven dependency and an
optional Paper server dependency; do not bundle it in the ArmaAlias jar. With
PlaceholderAPI absent, existing aliases continue to work and external
placeholder text remains unchanged. Relevant PlaceholderAPI expansions must be
installed separately.

- **Command actions:** `%player_name%`, `%vault_prefix%`, and other normal
  PlaceholderAPI expressions are parsed for the **player invoking the alias**
  even when `Executor: CONSOLE`. Player parameters (`<params>`) are interpolated
  *after* PAPI parsing to avoid interpreting untrusted command arguments as
  new expressions. Console-invoked aliases have no player context, so normal
  player placeholders remain unparsed.
- **Message actions:** normal `%...` placeholders refer to the **actor**;
  `%rel_<expansion>_<param>%` compares the **actor** to each receiving player;
  `{viewer:player_name}` or `{viewer:vault_prefix}` explicitly resolves a
  normal PAPI expansion for the **recipient**. The `viewer:` syntax intentionally
  uses no percent signs so actor parsing cannot consume it.
- **Broadcast:** relational/viewer messages are rendered separately for each
  online recipient. Non-relational messages use the original broadcast API.
  The console receives an unchanged copy of per-viewer tokens because it has no
  Player context. Console-invoked broadcasts cannot compute relational values.
- **Direct message:** when an alias sender is a player, they are also the
  viewer, so relational placeholders use the same player in both positions.

Example alias action:
```yaml
General_Settings:
  Actions:
    Announce:
      Type: MESSAGE
      Message: '<green>%player_name%</green> <gray>-> {viewer:player_name} (%rel_example_relation%)</gray>'
      Broadcast: true
```

`example` above denotes an installed relational expansion, not a bundled one.
For relational substitutions to work, the matching expansion must implement
PlaceholderAPI's relational interface.

Test manually on Paper 1.21.11 / Java 25 with PAPI installed and without PAPI;
test normal command placeholders with both command executor modes, broadcasts to
multiple viewers, no-player console invocations, and reload of PAPI expansions.

## Adventure / MiniMessage formatting

All player-facing messages use **MiniMessage only**. Configure styling with tags
such as `<red>`, `<bold>`, `<#aabbcc>`, `<gradient:green:aqua>`,
`<hover:show_text:"...">` and `<click:run_command:"...">`.

Legacy codes (`&a`, `§a`, `&#aabbcc`, `§x§a§a...`) are **not supported**.
Old formatting sequences are discarded from configured messages and
PlaceholderAPI values, not converted to MiniMessage. Dynamic values are inserted
as plain-text components; external MiniMessage tags cannot create click/hover
events. Migrate existing alias configs and custom message bundles to MiniMessage.

The removed `Translate_Color_Codes` option and `color` action-setting
abbreviation are no longer accepted. Command actions remain plain strings,
and PlaceholderAPI results are sanitized before command dispatch.

```yaml
Message: '<gradient:green:aqua><bold>Olá, <sender_name>!</bold></gradient> <gray>%player_name%</gray>'
```
