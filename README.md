# AE2 Compacting Fix

An addon for [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) that stops autocrafting
jobs from failing at launch because of **compacting drawers**.

## The problem

A compacting drawer holds one stock but shows every tier of it: 81 iron nuggets appear as 81 nuggets, 9 ingots and
1 block at the same time. AE2 reads each tier as a separate stock. A pattern that needs 64 nuggets and 9 ingots looks
possible, so AE2 accepts the job. At launch, the 9 ingots empty the drawer and the job is refused: "missing
ingredient: 64 iron nuggets".

## What the addon does

When AE2 plans a crafting job, the addon finds the compacting drawers behind the network's storage buses and counts
the tiers of each drawer as the single stock they are. AE2 then sees what is really missing: it crafts it from your
patterns, or shows it as missing on the plan screen, instead of accepting a job that cannot start.

Nothing else changes: storage, the ME terminal and the drawers themselves work as before.

## Supported mods

| Mod | Directly | Through a controller |
|---|---|---|
| [Storage Drawers](https://modrinth.com/mod/storagedrawers) | yes | yes |
| [Functional Storage](https://modrinth.com/mod/functional-storage) | yes | yes |

Both are optional: the addon handles whichever is installed. Sophisticated Storage needs nothing: its compacting
upgrade really crafts the items, so they are separate stacks.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| Applied Energistics 2 | 19.2.x |

## Limitations

- The count is cautious. A drawer's items may be taken before those of your other storage, whatever the priorities,
  so if the same item also sits in a chest, a plan may craft it or report it missing although the chest would have
  covered it. A plan the addon accepts never fails at launch.
- The ME terminal still shows the inflated amounts: only crafting plans are corrected.

## Building

JDK 21, then:

```
./gradlew build
```

The jar is in `build/libs`. `./gradlew runClient` starts a game with AE2, both drawer mods, Sophisticated Storage and
JEI.

`./gradlew runSelfTest` checks the fix without playing. It needs a world in `run-selftest/world` holding an AE2
network with a storage bus, and a pattern that uses iron nuggets and ingots. The test places a compacting drawer of
81 nuggets in front of the bus, runs a crafting calculation and logs what the plan screen would show, then stops.
Positions are set with `-PselfTestPos=x,y,z` (a block of the network) and `-PselfTestDrawer=x,y,z`, and
`-PselfTestDrawerMod=storagedrawers` tests Storage Drawers instead of Functional Storage.

## How it works

- `NetworkCraftingSimulationStateMixin`: when a calculation starts, walks the network's storage down to the drawers
  (`StorageWalker`) and records each compacting drawer's stock and conversion rates (`PoolSnapshot`). A storage bus
  answers from a cache, so reading the network contents is not enough.
- `CraftingSimulationStateMixin`: limits each extraction of the simulation so that, for every drawer, what all its
  tiers may take fits in its stock.
- `CraftingPlanSummaryMixin`: the plan screen checks each item against the network again, one by one; for drawer
  items it shows what the calculation found missing instead.

## License

[MIT](LICENSE)
