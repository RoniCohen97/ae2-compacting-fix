# AE2 Compacting Fix

**Your AE2 autocrafting jobs stop failing at launch because of compacting drawers.**

A compacting drawer holds one stock but shows every tier of it: 81 iron nuggets appear as 81 nuggets, 9 ingots *and*
1 block. Applied Energistics 2 counts each tier separately, so it accepts a job that needs 64 nuggets and 9 ingots, then
refuses to start it: the ingots emptied the drawer, the nuggets are gone.

With this addon, AE2 knows the tiers of a drawer share one stock. It crafts what is really missing from your patterns,
or shows it as missing on the plan screen, before you press Start.

## Requirements

| | Version |
|---|---|
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.x |
| Applied Energistics 2 | 26.1.13-beta or newer |

## Supported drawers

| Mod | Directly | Through a controller |
|---|---|---|
| Storage Drawers | yes | yes |
| Functional Storage | yes | yes |

Both are optional. Sophisticated Storage needs nothing: its compacting upgrade really crafts the items.

## Good to know

- **Cautious count.** If the same item is also in a chest, AE2 may craft it or report it missing although the chest
  would have been enough. In exchange, a job it accepts never fails at launch.
- **The ME terminal is unchanged**: it still shows the inflated amounts. Only crafting plans are corrected.
- Safe to add to or remove from an existing world: the addon stores nothing.

## Links

- Source and issues: https://github.com/RoniCohen97/ae2-compacting-fix
- License: MIT
