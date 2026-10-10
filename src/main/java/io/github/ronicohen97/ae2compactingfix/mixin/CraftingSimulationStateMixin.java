package io.github.ronicohen97.ae2compactingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.crafting.inv.CraftingSimulationState;
import io.github.ronicohen97.ae2compactingfix.SharedStockLimiter;

/** Keeps the crafting simulation from taking a compacting drawer's stock once per tier. */
@Mixin(value = CraftingSimulationState.class, remap = false)
public abstract class CraftingSimulationStateMixin {

    @Inject(method = "extract", at = @At("HEAD"), cancellable = true)
    private void ae2compactingfix$limitSharedStock(AEKey what, long amount, Actionable mode,
            CallbackInfoReturnable<Long> cir) {
        var self = (CraftingSimulationState) (Object) this;
        long limit = SharedStockLimiter.limit(self, what, amount);
        if (limit < amount) {
            // Extract the allowed amount instead; this nested call is not limited further since limit == amount
            cir.setReturnValue(self.extract(what, limit, mode));
        }
    }

    @WrapMethod(method = "applyDiff")
    private void ae2compactingfix$mergeWithoutLimit(CraftingSimulationState parent, Operation<Void> original) {
        SharedStockLimiter.enterMerge();
        try {
            original.call(parent);
        } finally {
            SharedStockLimiter.exitMerge();
        }
    }
}
