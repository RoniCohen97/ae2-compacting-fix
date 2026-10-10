package io.github.ronicohen97.ae2compactingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;

@Mixin(value = ChildCraftingSimulationState.class, remap = false)
public interface ChildCraftingSimulationStateAccessor {
    @Accessor("parent")
    ICraftingInventory ae2compactingfix$getParent();
}
