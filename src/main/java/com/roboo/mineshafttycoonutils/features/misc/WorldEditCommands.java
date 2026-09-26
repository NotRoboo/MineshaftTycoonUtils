package com.roboo.mineshafttycoonutils.features.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

public class WorldEditCommands {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int THRU_RANGE = 50;
    private static final double RAY_STEP = 0.2;
    private static final double JUMP_RANGE = 400.0;
    private static final int MAX_LEVELS = 256;

    private interface Finder {
        BlockPos find(Level level, Player player, int amount);
    }

    private interface Step {
        BlockPos next(Level level, BlockPos from);
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            register(dispatcher, List.of("thru"), false, (level, player, amount) -> thru(level, player));
            register(dispatcher, List.of("top"), false, (level, player, amount) -> top(level, player));
            register(dispatcher, List.of("jumpto", "j"), false, (level, player, amount) -> jumpTo(level, player));
            register(dispatcher, List.of("ceil"), false, (level, player, amount) -> ceil(level, player));
            register(dispatcher, List.of("ascend"), true,
                    (level, player, amount) -> repeat(level, player.blockPosition(), amount, WorldEditCommands::ascendOnce));
            register(dispatcher, List.of("descend", "desc"), true,
                    (level, player, amount) -> repeat(level, player.blockPosition(), amount, WorldEditCommands::descendOnce));
        });
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, List<String> names,
                                 boolean takesLevels, Finder finder) {
        for (String name : names) {
            var root = ClientCommandManager.literal(name)
                    .executes(ctx -> run(name, null, finder));

            if (takesLevels) {
                root.then(ClientCommandManager.argument("levels", IntegerArgumentType.integer(1, MAX_LEVELS))
                        .executes(ctx -> run(name, IntegerArgumentType.getInteger(ctx, "levels"), finder)));
            }

            dispatcher.register(root);
        }
    }

    private static int run(String name, Integer amount, Finder finder) {
        if (mc.player == null || mc.level == null) return 0;

        if (!ConfigManager.config.misc.worldEditCommands || mc.getSingleplayerServer() != null) {
            String raw = amount == null ? name : name + " " + amount;
            mc.player.connection.send(new ServerboundChatCommandPacket(raw));
            return 1;
        }

        BlockPos target = finder.find(mc.level, mc.player, amount == null ? 1 : amount);
        if (target == null) {
            mc.player.displayClientMessage(SystemMessages.buildPrefix().append(
                    Component.literal(" No free spot found.").withStyle(Style.EMPTY.withColor(0xFF5555))), false);
            return 0;
        }

        String command = String.format(Locale.ROOT, "tp %.1f %d %.1f",
                target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        mc.player.connection.send(new ServerboundChatCommandPacket(command));
        return 1;
    }

    private static BlockPos thru(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        boolean airborne = player.getAbilities().flying || !player.onGround();
        boolean passedSolid = !isFree(level, BlockPos.containing(eye));

        for (double distance = RAY_STEP; distance <= THRU_RANGE; distance += RAY_STEP) {
            BlockPos pos = BlockPos.containing(eye.add(direction.scale(distance)));

            if (!isFree(level, pos)) {
                passedSolid = true;
                continue;
            }
            if (!passedSolid) continue;

            if (airborne) {
                if (hasRoom(level, pos)) return pos;
            } else {
                BlockPos feet = pos;
                while (feet.getY() > level.getMinY() && isFree(level, feet.below())) {
                    feet = feet.below();
                }
                if (feet.getY() > level.getMinY() && hasRoom(level, feet)) return feet;
            }
        }

        return null;
    }

    private static BlockPos top(Level level, Player player) {
        BlockPos column = player.blockPosition();

        for (int y = level.getMaxY(); y >= level.getMinY(); y--) {
            BlockPos pos = column.atY(y);
            if (isFree(level, pos)) continue;

            BlockPos feet = pos.above();
            if (hasRoom(level, feet)) return feet;
        }

        return null;
    }

    private static BlockPos jumpTo(Level level, Player player) {
        HitResult hit = player.pick(JUMP_RANGE, 1.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return null;

        BlockPos start = blockHit.getBlockPos();
        int free = 0;

        for (int y = start.getY(); y <= level.getMaxY() + 1; y++) {
            free = isFree(level, start.atY(y)) ? free + 1 : 0;
            if (free == 2) return start.atY(y - 1);
        }

        return null;
    }

    private static BlockPos ceil(Level level, Player player) {
        BlockPos feet = player.blockPosition();

        for (int y = feet.getY() + 2; y <= level.getMaxY(); y++) {
            BlockPos pos = feet.atY(y);
            if (isFree(level, pos)) continue;

            BlockPos spot = pos.below(2);
            if (spot.getY() <= feet.getY()) return null;
            return hasRoom(level, spot) ? spot : null;
        }

        return null;
    }

    private static BlockPos repeat(Level level, BlockPos from, int levels, Step step) {
        BlockPos current = from;

        for (int i = 0; i < levels; i++) {
            BlockPos next = step.next(level, current);
            if (next == null) break;
            current = next;
        }

        return current.equals(from) ? null : current;
    }

    private static BlockPos ascendOnce(Level level, BlockPos from) {
        boolean passedSolid = false;

        for (int y = from.getY() + 1; y <= level.getMaxY() + 1; y++) {
            BlockPos pos = from.atY(y);
            if (!isFree(level, pos)) {
                passedSolid = true;
            } else if (passedSolid && hasRoom(level, pos)) {
                return pos;
            }
        }

        return null;
    }

    private static BlockPos descendOnce(Level level, BlockPos from) {
        int state = 0;

        for (int y = from.getY() - 1; y >= level.getMinY(); y--) {
            BlockPos pos = from.atY(y);
            boolean free = isFree(level, pos);

            if (state == 0) {
                if (!free) state = 1;
            } else if (state == 1) {
                if (free) state = 2;
            } else if (!free) {
                BlockPos feet = pos.above();
                if (hasRoom(level, feet)) return feet;
                state = 1;
            }
        }

        return null;
    }

    private static boolean hasRoom(Level level, BlockPos feet) {
        return isFree(level, feet) && isFree(level, feet.above());
    }

    private static boolean isFree(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}