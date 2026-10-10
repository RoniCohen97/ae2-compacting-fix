package io.github.ronicohen97.ae2compactingfix.model;

import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;

/**
 * The single stock of a compacting drawer, shared by all its tiers (nugget, ingot, block...).
 * <p>
 * The drawer shows every tier as if it held the whole stock, so AE2 sees e.g. 81 nuggets AND 9 ingots AND 1 block
 * while taking any of them empties the others.
 *
 * @param baseAmount the stock, in the smallest unit (e.g. nuggets)
 * @param factors    for each tier, how many smallest units one item is worth (nugget 1, ingot 9, block 81)
 */
public record CompactingPool(long baseAmount, Object2LongMap<AEKey> factors) {

    public CompactingPool {
        factors = Object2LongMaps.unmodifiable(factors);
    }

    public long factor(AEKey key) {
        return factors.getLong(key);
    }

    /** How many items of this tier the drawer shows: the whole stock, converted. */
    public long view(AEKey key) {
        return baseAmount / factor(key);
    }
}
