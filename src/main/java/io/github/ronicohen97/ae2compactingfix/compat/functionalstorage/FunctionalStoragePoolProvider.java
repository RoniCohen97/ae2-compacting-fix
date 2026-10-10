package io.github.ronicohen97.ae2compactingfix.compat.functionalstorage;

import net.neoforged.neoforge.items.IItemHandler;

import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import io.github.ronicohen97.ae2compactingfix.compat.PoolProvider;
import io.github.ronicohen97.ae2compactingfix.compat.PoolProviders;
import io.github.ronicohen97.ae2compactingfix.model.PoolCollector;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Functional Storage: a compacting drawer's item handler is a {@link CompactingInventoryHandler} holding its stock in
 * the smallest unit; a controller's handler flattens the handlers of its drawers.
 */
public final class FunctionalStoragePoolProvider implements PoolProvider {

    public static void register() {
        PoolProviders.register(new FunctionalStoragePoolProvider());
    }

    @Override
    public void collect(IItemHandler handler, PoolCollector out) {
        if (handler instanceof CompactingInventoryHandler compacting) {
            addPool(compacting, out);
        } else if (handler instanceof ControllerInventoryHandler controller) {
            for (var drawer : controller.getDrawers().getItemHandlers()) {
                if (drawer instanceof CompactingInventoryHandler compacting)
                    addPool(compacting, out);
            }
        }
    }

    private static void addPool(CompactingInventoryHandler handler, PoolCollector out) {
        // Creative drawers never run out
        if (handler.isCreative() || !handler.isSetup())
            return;

        // getNeeded(): smallest units per item (nugget 1, ingot 9, block 81)
        var factors = new Object2LongOpenHashMap<AEKey>();
        for (var result : handler.getResultList()) {
            var key = AEItemKey.of(result.getResult());
            if (key != null && result.getNeeded() > 0)
                factors.put(key, result.getNeeded());
        }
        out.add(handler, handler.getAmount(), factors);
    }
}
