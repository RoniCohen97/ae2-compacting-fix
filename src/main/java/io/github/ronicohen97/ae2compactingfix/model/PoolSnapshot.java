package io.github.ronicohen97.ae2compactingfix.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

import appeng.api.stacks.AEKey;

/**
 * The compacting pools of a network, taken when a crafting calculation starts, and the rule that keeps a crafting
 * plan from using a pool's stock more than once.
 * <p>
 * The rule assumes the worst: whatever AE2's storage priorities, an item that a pool shows may be taken from that pool
 * first. For each pool, every tier's possible use must then fit in the stock:
 * <pre>  sum over the tiers K of  min(extracted(K), view(K)) * factor(K)  &lt;=  baseAmount</pre>
 * With that, the real extraction at launch succeeds in any order. The price is that a plan may craft or report as
 * missing items that another inventory could have supplied.
 */
public final class PoolSnapshot {
    public static final PoolSnapshot EMPTY = new PoolSnapshot(List.of());

    private final Map<AEKey, List<CompactingPool>> poolsByKey = new HashMap<>();

    public PoolSnapshot(Collection<CompactingPool> pools) {
        for (var pool : pools) {
            for (var key : pool.factors().keySet()) {
                poolsByKey.computeIfAbsent(key, k -> new ArrayList<>(1)).add(pool);
            }
        }
    }

    @Override
    public String toString() {
        return poolsByKey.toString();
    }

    public boolean isEmpty() {
        return poolsByKey.isEmpty();
    }

    public boolean contains(AEKey key) {
        return poolsByKey.containsKey(key);
    }

    /**
     * How much of {@code what} the plan may still take, at most {@code amount}.
     *
     * @param netExtracted what the plan has taken of a key so far (negative when it produced more than it used)
     */
    public long limit(AEKey what, long amount, ToLongFunction<AEKey> netExtracted) {
        var pools = poolsByKey.get(what);
        if (pools == null || amount <= 0)
            return amount;

        long ownExtracted = netExtracted.applyAsLong(what);
        long limit = amount;
        for (var pool : pools) {
            // Stock the other tiers may already have used up
            long usedByOthers = 0;
            for (var entry : pool.factors().object2LongEntrySet()) {
                var other = entry.getKey();
                if (other.equals(what))
                    continue;
                long extracted = netExtracted.applyAsLong(other);
                if (extracted > 0) {
                    long factor = entry.getLongValue();
                    usedByOthers += Math.min(extracted, pool.baseAmount() / factor) * factor;
                }
            }

            // Items of this tier the pool can still give without starving the others
            long allowed = Math.floorDiv(pool.baseAmount() - usedByOthers, pool.factor(what));
            if (allowed >= pool.view(what))
                continue; // the pool can give all it shows: no conflict with the other tiers
            limit = Math.min(limit, Math.max(0, allowed - ownExtracted));
        }
        return limit;
    }
}
