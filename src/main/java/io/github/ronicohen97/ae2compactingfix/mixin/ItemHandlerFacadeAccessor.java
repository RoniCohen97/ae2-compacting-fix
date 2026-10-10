package io.github.ronicohen97.ae2compactingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.neoforged.neoforge.items.IItemHandler;

/** How a storage bus sees an item handler (a drawer, a chest...). */
@Mixin(targets = "appeng.me.storage.ExternalStorageFacade$ItemHandlerFacade", remap = false)
public interface ItemHandlerFacadeAccessor {
    @Accessor("handler")
    IItemHandler ae2compactingfix$getHandler();
}
