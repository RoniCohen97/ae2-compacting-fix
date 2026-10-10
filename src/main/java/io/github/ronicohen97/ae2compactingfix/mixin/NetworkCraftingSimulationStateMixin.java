package io.github.ronicohen97.ae2compactingfix.mixin;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.logging.LogUtils;

import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import io.github.ronicohen97.ae2compactingfix.model.PoolCollector;
import io.github.ronicohen97.ae2compactingfix.model.PoolSnapshot;
import io.github.ronicohen97.ae2compactingfix.model.PoolSnapshotHolder;

/**
 * Records the network's compacting pools along with the snapshot of its contents that AE2 takes when a crafting
 * calculation starts. This runs on the server thread, before the calculation moves to its own thread.
 */
@Mixin(value = NetworkCraftingSimulationState.class, remap = false)
public abstract class NetworkCraftingSimulationStateMixin implements PoolSnapshotHolder {
    @Unique
    private static final Logger ae2compactingfix$LOGGER = LogUtils.getLogger();
    @Unique
    private PoolSnapshot ae2compactingfix$poolSnapshot = PoolSnapshot.EMPTY;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2compactingfix$collectPools(IStorageService storage, @Nullable IActionSource src,
            CallbackInfo ci) {
        ae2compactingfix$poolSnapshot = PoolCollector.collect(storage.getInventory());
        ae2compactingfix$LOGGER.debug("Crafting calculation started with compacting pools: {}",
                ae2compactingfix$poolSnapshot);
    }

    @Override
    public PoolSnapshot ae2compactingfix$getPoolSnapshot() {
        return ae2compactingfix$poolSnapshot;
    }
}
