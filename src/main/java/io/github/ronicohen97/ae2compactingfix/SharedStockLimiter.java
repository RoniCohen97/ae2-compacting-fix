package io.github.ronicohen97.ae2compactingfix;

import java.util.ArrayList;
import java.util.List;

import appeng.api.stacks.AEKey;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;
import io.github.ronicohen97.ae2compactingfix.mixin.ChildCraftingSimulationStateAccessor;
import io.github.ronicohen97.ae2compactingfix.mixin.CraftingSimulationStateAccessor;
import io.github.ronicohen97.ae2compactingfix.model.PoolSnapshot;
import io.github.ronicohen97.ae2compactingfix.model.PoolSnapshotHolder;

/**
 * Applies the {@link PoolSnapshot} rule to AE2's crafting simulation.
 * <p>
 * A simulation is a chain of states: each branch of the plan works in a child state on top of its parent, down to the
 * root state built from the network contents. What the plan has taken of a key so far is the sum, over the chain, of
 * what each state took from its parent.
 */
public final class SharedStockLimiter {
    /**
     * Set while a child state merges into its parent. That merge replays extractions the child already checked, in an
     * arbitrary order; limiting them could fail the merge, which AE2 reports as a bug.
     */
    private static final ThreadLocal<int[]> MERGE_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private SharedStockLimiter() {
    }

    public static void enterMerge() {
        MERGE_DEPTH.get()[0]++;
    }

    public static void exitMerge() {
        MERGE_DEPTH.get()[0]--;
    }

    /** How much of {@code what} {@code state} may extract, at most {@code amount}. */
    public static long limit(CraftingSimulationState state, AEKey what, long amount) {
        if (MERGE_DEPTH.get()[0] > 0)
            return amount;

        List<CraftingSimulationState> chain = new ArrayList<>(4);
        ICraftingInventory current = state;
        while (current instanceof CraftingSimulationState simState) {
            chain.add(simState);
            current = simState instanceof ChildCraftingSimulationState child
                    ? ((ChildCraftingSimulationStateAccessor) child).ae2compactingfix$getParent()
                    : null;
        }

        if (!(chain.getLast() instanceof PoolSnapshotHolder holder))
            return amount;
        var snapshot = holder.ae2compactingfix$getPoolSnapshot();
        if (!snapshot.contains(what))
            return amount;

        return snapshot.limit(what, amount, key -> netExtracted(chain, key));
    }

    private static long netExtracted(List<CraftingSimulationState> chain, AEKey key) {
        long total = 0;
        for (var state : chain) {
            var accessor = (CraftingSimulationStateAccessor) state;
            total += accessor.ae2compactingfix$getUnmodifiedCache().get(key)
                    - accessor.ae2compactingfix$getModifiableCache().get(key);
        }
        return total;
    }
}
