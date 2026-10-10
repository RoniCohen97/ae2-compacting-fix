package io.github.ronicohen97.ae2compactingfix.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.me.storage.CompositeStorage;

/** What a storage bus reads from the block it faces, one storage per key type (items, fluids...). */
@Mixin(value = CompositeStorage.class, remap = false)
public interface CompositeStorageAccessor {
    @Accessor("storages")
    Map<AEKeyType, MEStorage> ae2compactingfix$getStorages();
}
