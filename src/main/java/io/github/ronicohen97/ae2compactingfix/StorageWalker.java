package io.github.ronicohen97.ae2compactingfix;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.me.storage.ExternalStorageFacade;
import io.github.ronicohen97.ae2compactingfix.mixin.CompositeStorageAccessor;
import io.github.ronicohen97.ae2compactingfix.mixin.DelegatingMEInventoryAccessor;
import io.github.ronicohen97.ae2compactingfix.mixin.ResourceHandlerFacadeAccessor;
import io.github.ronicohen97.ae2compactingfix.mixin.NetworkStorageAccessor;

/**
 * Finds the item handlers a network's storage buses face.
 * <p>
 * Reading the network contents is not enough: a storage bus answers from a cache refreshed every tick, without
 * touching the block it faces. So we follow AE2's storage objects down to the handlers:
 * network &rarr; storage bus inventory &rarr; composite storage &rarr; item handler facade &rarr; handler.
 */
public final class StorageWalker {
    private StorageWalker() {
    }

    /** Must run on the server thread, which owns these objects. */
    public static void forEachItemHandler(MEStorage root, Consumer<ResourceHandler<ItemResource>> action) {
        walk(root, action, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static void walk(MEStorage storage, Consumer<ResourceHandler<ItemResource>> action, Set<MEStorage> visited) {
        if (storage == null || !visited.add(storage))
            return;

        if (storage instanceof NetworkStorageAccessor network) {
            for (var storages : network.ae2compactingfix$getPriorityInventory().values()) {
                for (var mounted : storages)
                    walk(mounted, action, visited);
            }
            for (var mounted : network.ae2compactingfix$getSecondPassInventories())
                walk(mounted, action, visited);
        } else if (storage instanceof DelegatingMEInventoryAccessor delegating) {
            walk(delegating.ae2compactingfix$getDelegate(), action, visited);
        } else if (storage instanceof CompositeStorageAccessor composite) {
            for (var part : composite.ae2compactingfix$getStorages().values())
                walk(part, action, visited);
        } else if (storage instanceof ResourceHandlerFacadeAccessor facade
                && ((ExternalStorageFacade) storage).getKeyType() == AEKeyType.items()) {
            @SuppressWarnings("unchecked") // an item facade wraps an item handler
            var handler = (ResourceHandler<ItemResource>) facade.ae2compactingfix$getHandler();
            action.accept(handler);
        }
    }
}
