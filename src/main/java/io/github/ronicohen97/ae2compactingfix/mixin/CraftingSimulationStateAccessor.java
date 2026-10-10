package io.github.ronicohen97.ae2compactingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.CraftingSimulationState;

@Mixin(value = CraftingSimulationState.class, remap = false)
public interface CraftingSimulationStateAccessor {
    /** The parent's amounts when this state first looked at them. */
    @Accessor("unmodifiedCache")
    KeyCounter ae2compactingfix$getUnmodifiedCache();

    /** The same amounts, after this state's extractions and insertions. */
    @Accessor("modifiableCache")
    KeyCounter ae2compactingfix$getModifiableCache();
}
