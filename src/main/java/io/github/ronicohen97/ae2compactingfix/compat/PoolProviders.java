package io.github.ronicohen97.ae2compactingfix.compat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.neoforge.items.IItemHandler;

import io.github.ronicohen97.ae2compactingfix.model.PoolCollector;

/** The providers of the drawer mods that are installed. */
public final class PoolProviders {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<PoolProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private PoolProviders() {
    }

    public static void register(PoolProvider provider) {
        PROVIDERS.add(provider);
    }

    public static void collect(IItemHandler handler, PoolCollector out) {
        for (var provider : PROVIDERS) {
            try {
                provider.collect(handler, out);
            } catch (Throwable t) {
                // A drawer mod changed under us: give up on it rather than break AE2's crafting
                LOGGER.error("Compacting pool provider {} failed and is now disabled", provider.getClass().getName(), t);
                PROVIDERS.remove(provider);
            }
        }
    }
}
