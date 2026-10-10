package io.github.ronicohen97.ae2compactingfix;

import java.util.concurrent.Future;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import appeng.api.config.Actionable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import appeng.menu.me.crafting.CraftingPlanSummary;
import io.github.ronicohen97.ae2compactingfix.model.PoolSnapshotHolder;

/**
 * Dev only: with {@code -Dae2compactingfix.selfTest=x,y,z} (a block of an AE2 network), runs crafting simulations
 * against that network once the server is up, logs what they see, and stops the server.
 * <ol>
 * <li>Raw extractions from the simulation state: 9 iron ingots then 64 nuggets, and the other way round.</li>
 * <li>A real crafting calculation for each craftable item of the network, and the summary the plan screen shows.</li>
 * </ol>
 */
final class DevSelfTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SETUP_TICK = 50;
    private static final int START_TICK = 100;
    private static final int TIMEOUT_TICKS = 400;

    private static int ticks;
    @Nullable
    private static IGridNode node;
    @Nullable
    private static Future<ICraftingPlan> calculation;

    private DevSelfTest() {
    }

    static void registerIfRequested() {
        var target = System.getProperty("ae2compactingfix.selfTest");
        if (target == null)
            return;
        var parts = target.split(",");
        var pos = new BlockPos(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim()));
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> {
            ticks++;
            boolean done = true;
            try {
                if (ticks == SETUP_TICK)
                    placeDrawer(event.getServer().overworld());
                if (ticks < START_TICK)
                    return;
                if (ticks == START_TICK)
                    done = !start(event.getServer().overworld(), pos);
                else
                    done = !poll() || ticks > START_TICK + TIMEOUT_TICKS;
            } catch (Throwable t) {
                LOGGER.error("SELFTEST failed", t);
            }
            if (done) {
                LOGGER.info("SELFTEST finished");
                event.getServer().halt(false);
            }
        });
    }

    /**
     * With {@code -Dae2compactingfix.selfTestDrawer=x,y,z}, puts there a compacting drawer holding 81 iron nuggets
     * (in front of a storage bus, presumably): Functional Storage's, or Storage Drawers' with
     * {@code -Dae2compactingfix.selfTestDrawerMod=storagedrawers}.
     */
    private static void placeDrawer(ServerLevel level) {
        var target = System.getProperty("ae2compactingfix.selfTestDrawer");
        if (target == null)
            return;
        var parts = target.split(",");
        var pos = new BlockPos(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim()));
        level.getChunk(pos);
        boolean storageDrawers = "storagedrawers".equals(System.getProperty("ae2compactingfix.selfTestDrawerMod"));
        var blockId = storageDrawers ? "storagedrawers:compacting_drawers_3" : "functionalstorage:compacting_drawer";
        var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .get(net.minecraft.resources.ResourceLocation.parse(blockId));
        level.setBlockAndUpdate(pos, block.defaultBlockState());
        var handler = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, pos,
                null);
        if (handler == null) {
            LOGGER.error("SELFTEST could not place {} at {}", blockId, pos);
            return;
        }
        if (handler instanceof com.buuz135.functionalstorage.inventory.CompactingInventoryHandler compacting) {
            var util = new com.buuz135.functionalstorage.util.CompactingUtil(level, 3);
            util.setup(new net.minecraft.world.item.ItemStack(Items.IRON_NUGGET));
            compacting.setup(util);
        }
        // Start from an empty drawer (the world may already have one there)
        for (int i = 0; i < handler.getSlots(); i++) {
            while (!handler.extractItem(i, Integer.MAX_VALUE, false).isEmpty()) {
            }
        }
        // Storage Drawers: slot 0 accepts any item and sets an empty compacting drawer up
        var left = handler.insertItem(0, new net.minecraft.world.item.ItemStack(Items.IRON_NUGGET, 81), false);
        var slots = new StringBuilder();
        for (int i = 0; i < handler.getSlots(); i++)
            slots.append(' ').append(handler.getStackInSlot(i));
        LOGGER.info("SELFTEST placed {} at {}: slots{} (not inserted: {})", blockId, pos, slots, left);
    }

    /** @return whether a crafting calculation is running and must be polled */
    private static boolean start(ServerLevel level, BlockPos pos) {
        level.getChunk(pos);
        var host = GridHelper.getNodeHost(level, pos);
        node = host == null ? null : host.getGridNode(null);
        for (var side : Direction.values()) {
            if (node == null && host != null)
                node = host.getGridNode(side);
        }
        if (node == null) {
            LOGGER.error("SELFTEST no grid node at {}", pos);
            return false;
        }

        var grid = node.getGrid();
        var storage = grid.getStorageService();
        for (var entry : storage.getInventory().getAvailableStacks())
            LOGGER.info("SELFTEST network has {} x{}", entry.getKey(), entry.getLongValue());

        var root = new NetworkCraftingSimulationState(storage, null);
        LOGGER.info("SELFTEST pools: {}", ((PoolSnapshotHolder) root).ae2compactingfix$getPoolSnapshot());

        var ingot = AEItemKey.of(Items.IRON_INGOT);
        var nugget = AEItemKey.of(Items.IRON_NUGGET);
        var child = new ChildCraftingSimulationState(root);
        LOGGER.info("SELFTEST extract 9 ingots: {}", child.extract(ingot, 9, Actionable.MODULATE));
        LOGGER.info("SELFTEST then 64 nuggets (simulate): {}", child.extract(nugget, 64, Actionable.SIMULATE));
        var other = new ChildCraftingSimulationState(root);
        LOGGER.info("SELFTEST other order, extract 64 nuggets: {}", other.extract(nugget, 64, Actionable.MODULATE));
        LOGGER.info("SELFTEST then 9 ingots (simulate): {}", other.extract(ingot, 9, Actionable.SIMULATE));

        var craftables = grid.getCraftingService().getCraftables(key -> true);
        if (craftables.isEmpty()) {
            LOGGER.info("SELFTEST no craftable item on this network");
            return false;
        }
        var output = craftables.iterator().next();
        LOGGER.info("SELFTEST crafting calculation for 1x {}", output);
        var requester = new ICraftingSimulationRequester() {
            @Override
            public IActionSource getActionSource() {
                return IActionSource.empty();
            }

            @Override
            public IGridNode getGridNode() {
                return node;
            }
        };
        calculation = grid.getCraftingService().beginCraftingCalculation(level, requester, output, 1,
                CalculationStrategy.REPORT_MISSING_ITEMS);
        return true;
    }

    /** @return whether to keep polling */
    private static boolean poll() throws Exception {
        if (calculation == null || !calculation.isDone())
            return calculation != null;

        var plan = calculation.get();
        LOGGER.info("SELFTEST plan: simulation={} used={} missing={}", plan.simulation(), plan.usedItems(),
                plan.missingItems());
        var summary = CraftingPlanSummary.fromJob(node.getGrid(), IActionSource.empty(), plan);
        for (var entry : summary.getEntries()) {
            LOGGER.info("SELFTEST screen: {} stored={} missing={} craft={}", entry.getWhat(),
                    entry.getStoredAmount(), entry.getMissingAmount(), entry.getCraftAmount());
        }
        return false;
    }
}
