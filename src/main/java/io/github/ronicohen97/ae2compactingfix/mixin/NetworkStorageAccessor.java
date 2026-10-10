package io.github.ronicohen97.ae2compactingfix.mixin;

import java.util.List;
import java.util.NavigableMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;

/** The storages mounted on a network (drives, storage buses...), by priority. */
@Mixin(value = NetworkStorage.class, remap = false)
public interface NetworkStorageAccessor {
    @Accessor("priorityInventory")
    NavigableMap<Integer, List<MEStorage>> ae2compactingfix$getPriorityInventory();
}
