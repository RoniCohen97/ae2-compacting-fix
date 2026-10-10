package io.github.ronicohen97.ae2compactingfix.model;

import java.util.IdentityHashMap;
import java.util.Map;

import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import io.github.ronicohen97.ae2compactingfix.StorageWalker;
import io.github.ronicohen97.ae2compactingfix.compat.PoolProviders;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

/** Gathers the compacting pools behind a network's storage buses. */
public final class PoolCollector {
    /** Keyed by the drawer's own storage object: the same drawer seen through two buses is one pool. */
    private final Map<Object, CompactingPool> pools = new IdentityHashMap<>();

    /** The pools a network's storage can reach. Must run on the server thread. */
    public static PoolSnapshot collect(MEStorage network) {
        var collector = new PoolCollector();
        StorageWalker.forEachItemHandler(network, handler -> PoolProviders.collect(handler, collector));
        return new PoolSnapshot(collector.pools.values());
    }

    /**
     * @param identity   the object holding the drawer's stock, identical whichever way the drawer is reached
     * @param baseAmount the stock, in the smallest unit
     * @param factors    smallest units per item, for each tier
     */
    public void add(Object identity, long baseAmount, Object2LongMap<AEKey> factors) {
        // A single tier shares its stock with nothing
        if (factors.size() < 2 || baseAmount < 0)
            return;
        pools.putIfAbsent(identity, new CompactingPool(baseAmount, factors));
    }
}
