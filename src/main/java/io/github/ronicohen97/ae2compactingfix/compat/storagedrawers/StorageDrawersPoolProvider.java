package io.github.ronicohen97.ae2compactingfix.compat.storagedrawers;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.neoforged.neoforge.items.IItemHandler;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.api.storage.IFractionalDrawer;
import com.jaquadro.minecraft.storagedrawers.capabilities.DrawerItemHandler;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import io.github.ronicohen97.ae2compactingfix.compat.PoolProvider;
import io.github.ronicohen97.ae2compactingfix.compat.PoolProviders;
import io.github.ronicohen97.ae2compactingfix.model.PoolCollector;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Storage Drawers: the item handler of a drawer or a controller wraps an {@link IDrawerGroup}, whose compacting
 * drawers are {@link IFractionalDrawer}s. The drawers of one compacting block share a private {@code storage} object,
 * which is how we group them, also behind a controller.
 */
public final class StorageDrawersPoolProvider implements PoolProvider {
    private static final VarHandle HANDLER_GROUP;
    private static final Class<?> FRACTIONAL_DRAWER;
    private static final VarHandle DRAWER_STORAGE;

    static {
        try {
            HANDLER_GROUP = MethodHandles.privateLookupIn(DrawerItemHandler.class, MethodHandles.lookup())
                    .findVarHandle(DrawerItemHandler.class, "group", IDrawerGroup.class);
            FRACTIONAL_DRAWER = Class.forName(
                    "com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.FractionalDrawerGroup$FractionalDrawer");
            var storageClass = Class.forName(
                    "com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.FractionalDrawerGroup$FractionalStorage");
            DRAWER_STORAGE = MethodHandles.privateLookupIn(FRACTIONAL_DRAWER, MethodHandles.lookup())
                    .findVarHandle(FRACTIONAL_DRAWER, "storage", storageClass);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unsupported Storage Drawers version", e);
        }
    }

    public static void register() {
        PoolProviders.register(new StorageDrawersPoolProvider());
    }

    @Override
    public void collect(IItemHandler handler, PoolCollector out) {
        if (!(handler instanceof DrawerItemHandler))
            return;
        var group = (IDrawerGroup) HANDLER_GROUP.get(handler);
        if (group == null || !group.isGroupValid())
            return;

        Map<Object, List<IFractionalDrawer>> drawersByStorage = new IdentityHashMap<>();
        for (int i = 0; i < group.getDrawerCount(); i++) {
            if (group.getDrawer(i) instanceof IFractionalDrawer drawer && FRACTIONAL_DRAWER.isInstance(drawer)
                    && drawer.isEnabled() && !drawer.isEmpty()) {
                drawersByStorage.computeIfAbsent(DRAWER_STORAGE.get(drawer), s -> new ArrayList<>(3)).add(drawer);
            }
        }

        drawersByStorage.forEach((storage, drawers) -> addPool(storage, drawers, out));
    }

    private static void addPool(Object storage, List<IFractionalDrawer> drawers, PoolCollector out) {
        var smallest = drawers.stream().filter(IFractionalDrawer::isSmallestUnit).findFirst().orElse(null);
        if (smallest == null)
            return;
        long baseAmount = smallest.getStoredItemCount();
        // Unlimited vending: the stock never runs out
        if (baseAmount == Integer.MAX_VALUE)
            return;

        // getConversionRate() is relative to the most compressed tier: block 1, ingot 9, nugget 81
        int smallestRate = smallest.getConversionRate();
        var factors = new Object2LongOpenHashMap<AEKey>();
        for (var drawer : drawers) {
            int rate = drawer.getConversionRate();
            var key = AEItemKey.of(drawer.getStoredItemPrototype());
            if (rate <= 0 || smallestRate % rate != 0 || key == null)
                return;
            factors.put(key, smallestRate / rate);
        }
        out.add(storage, baseAmount, factors);
    }
}
