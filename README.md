# Drobiazgi

Minecraft 1.7.10 mod with different small features and interop fixes.

# Features
* Option to make the compass point to the North instead of Spawn
* Alternate Custom NPCs spawning system, compatible with LOTR biomes

## CustomNPC Spawner
Quick setup:
1. Enable `custom_npcs_spawning.enabled=true` in config.
2. Save an NPC as a CustomNPC clone (name + tab).
3. Add one rule to `custom_npcs_spawning.rules`.

Example rule:
```text
id=desert_frequent;enabled=true;clone=MYNPC;tab=1;weight=200;chance=1.0;group=2-4;dims=0;dimMode=whitelist;biomes=Desert,DesertHills,2,17;biomeMode=whitelist;time=any;light=0-15;y=50-140
```

Notes:
- `biomes` accepts both biome names and numeric biome IDs (including LOTR biomes when LOTR is loaded).
- Spawner looks in world clone storage first, then falls back to global `customnpcs/clones`.

## Dependencies
* [UniMixins](https://modrinth.com/mod/unimixins) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/unimixins)  [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/unimixins/versions) [![git](images/icons/git.png)](https://github.com/LegacyModdingMC/UniMixins/releases)
* [FentLib](https://modrinth.com/mod/gtnhlib)   [![git](images/icons/git.png)](https://github.com/JackOfNoneTrades/FentLib)
