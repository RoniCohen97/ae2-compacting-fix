package io.github.ronicohen97.ae2compactingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.menu.me.crafting.CraftingPlanSummary;
import io.github.ronicohen97.ae2compactingfix.model.PoolCollector;

/**
 * The plan screen of a crafting job with missing ingredients. For each item, AE2 checks again, item by item, how much
 * the network holds, which counts a compacting drawer's stock once per tier and hides what is missing. For items of a
 * compacting drawer, we show what the crafting calculation found missing instead.
 */
@Mixin(value = CraftingPlanSummary.class, remap = false)
public abstract class CraftingPlanSummaryMixin {

    @WrapOperation(method = "fromJob", at = @At(value = "INVOKE",
            target = "Lappeng/api/storage/MEStorage;extract(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J"))
    private static long ae2compactingfix$sharedStockAvailable(MEStorage storage, AEKey what, long amount,
            Actionable mode, IActionSource source, Operation<Long> original,
            @Local(argsOnly = true) ICraftingPlan job) {
        long available = original.call(storage, what, amount, mode, source);
        if (!PoolCollector.collect(storage).contains(what))
            return available;
        // amount is what the plan needs of this item: what it takes from storage plus what it lacks
        long missing = job.missingItems().get(what);
        return Math.min(available, Math.max(0, amount - missing));
    }
}
