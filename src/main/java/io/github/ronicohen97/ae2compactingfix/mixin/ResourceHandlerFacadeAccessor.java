package io.github.ronicohen97.ae2compactingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.neoforged.neoforge.transfer.ResourceHandler;

/**
 * How a storage bus sees an item or fluid handler (a drawer, a chest, a tank...). Its key type tells which one:
 * {@code ExternalStorageFacade#getKeyType()}.
 */
@Mixin(targets = "appeng.me.storage.ExternalStorageFacade$ResourceHandlerFacade", remap = false)
public interface ResourceHandlerFacadeAccessor {
    @Accessor("handler")
    ResourceHandler<?> ae2compactingfix$getHandler();
}
