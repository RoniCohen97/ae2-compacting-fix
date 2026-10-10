package io.github.ronicohen97.ae2compactingfix;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import io.github.ronicohen97.ae2compactingfix.compat.functionalstorage.FunctionalStoragePoolProvider;
import io.github.ronicohen97.ae2compactingfix.compat.storagedrawers.StorageDrawersPoolProvider;

@Mod(Ae2CompactingFix.MOD_ID)
public final class Ae2CompactingFix {
    public static final String MOD_ID = "ae2compactingfix";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Ae2CompactingFix(IEventBus modBus) {
        // The compat classes are only loaded when their mod is present
        register("storagedrawers", () -> StorageDrawersPoolProvider.register());
        register("functionalstorage", () -> FunctionalStoragePoolProvider.register());

        if (!FMLEnvironment.production) {
            modBus.addListener(FMLCommonSetupEvent.class, event -> loadMixinTargets());
            DevSelfTest.registerIfRequested();
        }
    }

    /**
     * In the dev environment, loads the classes our mixins patch, so that a mixin that no longer applies (after an
     * AE2 update) fails at startup instead of at the first crafting calculation.
     */
    private static void loadMixinTargets() {
        for (var name : new String[] {
                "appeng.crafting.inv.CraftingSimulationState",
                "appeng.crafting.inv.ChildCraftingSimulationState",
                "appeng.crafting.inv.NetworkCraftingSimulationState",
                "appeng.me.storage.ExternalStorageFacade$ItemHandlerFacade",
                "appeng.me.storage.NetworkStorage",
                "appeng.me.storage.DelegatingMEInventory",
                "appeng.me.storage.CompositeStorage",
                "appeng.menu.me.crafting.CraftingPlanSummary" }) {
            try {
                Class.forName(name, false, Ae2CompactingFix.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Mixin target not found: " + name, e);
            }
        }
        LOGGER.info("All mixin targets loaded");
    }

    private static void register(String modId, Runnable registration) {
        if (!ModList.get().isLoaded(modId))
            return;
        try {
            registration.run();
            LOGGER.info("Compacting drawers of {} are now counted as one stock by AE2 crafting", modId);
        } catch (Throwable t) {
            LOGGER.error("Could not enable the compacting drawer support for {}", modId, t);
        }
    }
}
