# Drobiazgi

`Minecraft 1.7.10` mod with different small features and interop fixes.

[![hub](images/badges/github.png)](https://github.com/JackOfNoneTrades/Drobiazgi/releases)
[![maven](images/badges/maven.png)](https://maven.fentanylsolutions.org/#/TODO)
![forge](images/badges/forge.png)

<!--
[![modrinth](images/badges/modrinth.png)](https://modrinth.com/mod/waystones-x)
[![curse](images/badges/curse.png)](https://www.curseforge.com/minecraft/mc-mods/waystones-x)
-->

# Features
* Option to make the compass point to the North instead of Spawn
* Alternate Custom NPCs spawning system, compatible with LOTR biomes
* Configurable leaf regrowth. Broken leaves regrow if still connected to a log
* Doggy Talents throw fix. Adjustable pitch offset and force for thrown bones/sticks, improves arc when ItemPhysic is installed

## Leaf Regrowth
Tracks broken leaves and replaces them after a configurable delay, as long as the position is still connected to a matching log block through other leaves.

Enable with `regrowable_leaves.enabled=true`. Rules are defined in `regrowable_leaves.rules`, one per line:
```text
id=vanilla_leaves;leaf=minecraft:leaves;log=minecraft:log;leafMeta=0-3;chance=0.75
```

Keys: `id`, `enabled`, `leaf`/`log` (block selector), `leafMeta` (single values, comma lists, or ranges), `chance` (0..1).

Block selectors use registry names (e.g. `minecraft:leaves`). For mods that register blocks with a `tile.` prefix (e.g. LOTR), the short form `lotr:leaves` is automatically resolved to `lotr:tile.leaves`.

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
- `cave=true` spawns NPCs underground in caves instead of at the surface. Useful with a low `y` range and low `light` range.
- `water=true` allows spawning on liquid surfaces (water, lava). By default NPCs only spawn on solid ground.
- Spawner looks in world clone storage first, then falls back to global `customnpcs/clones`.

## Psychedelicraft Compat
If Psychedelicraft is present, Drobiazgi can add Psychedelicraft drug effects to consumed food, drinks, and smoking items through `psychedelicraft_alcohol.rules`.

Defaults:
- all LOTR alcoholic mugs get the Psychedelicraft `Alcohol` effect, scaled from the mug's built-in alcoholicity
- the LOTR hobbit pipe gets the Psychedelicraft `Tobacco` effect
- LOTR mug nausea is suppressed by default while this compat is enabled

Main keys:
- `item` / `items`: item selector by registry name
- `drug`: Psychedelicraft drug name, for example `Alcohol` or `Tobacco`
- `potency` / `maxInfluence`: total Psychedelicraft influence to add
- `delay`, `speed`, `speedPlus`: timing and ramp values for `DrugInfluence`
- `meta`: optional metadata filter
- `requireMod`: only load the rule if that mod is present
- `lotrAlcoholicMugs=true`: include every LOTR mug with positive alcoholicity
- `lotrAlcoholicityScale`: derive potency from LOTR mug alcoholicity and mug strength

Examples:
```text
id=lotr_alcoholic_mugs;requireMod=lotr;lotrAlcoholicMugs=true;drug=Alcohol;lotrAlcoholicityScale=0.2
id=lotr_hobbit_pipe;requireMod=lotr;item=lotr:hobbitPipe;drug=Tobacco;potency=0.15;delay=0
```

## Dependencies
* [UniMixins](https://modrinth.com/mod/unimixins) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/unimixins)  [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/unimixins/versions) [![git](images/icons/git.png)](https://github.com/LegacyModdingMC/UniMixins/releases)
* [FentLib](https://modrinth.com/mod/gtnhlib)   [![git](images/icons/git.png)](https://github.com/JackOfNoneTrades/FentLib)

## License

`LgplV3 + SNEED`.

## Buy me some creatine

* [ko-fi.com](https://ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`

<br>

![license](images/lgplsneed_small.png)
