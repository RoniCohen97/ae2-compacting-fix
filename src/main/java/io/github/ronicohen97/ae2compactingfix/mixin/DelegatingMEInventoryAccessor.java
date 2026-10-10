package io.github.ronicohen97.ae2compactingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;

/** A wrapper adding filters or access rules, such as the inventory a storage bus mounts. */
@Mixin(value = DelegatingMEInventory.class, remap = false)
public interface DelegatingMEInventoryAccessor {
    @Accessor("delegate")
    MEStorage ae2compactingfix$getDelegate();
}
