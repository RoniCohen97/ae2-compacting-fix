package io.github.ronicohen97.ae2compactingfix.compat;

import net.neoforged.neoforge.items.IItemHandler;

import io.github.ronicohen97.ae2compactingfix.model.PoolCollector;

/** Finds the compacting pools behind an item handler that a storage bus reads, for one drawer mod. */
public interface PoolProvider {
    /** Adds to {@code out} the pools {@code handler} gives access to; does nothing if it is not this mod's. */
    void collect(IItemHandler handler, PoolCollector out);
}
