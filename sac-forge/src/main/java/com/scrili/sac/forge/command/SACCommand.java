package com.scrili.sac.forge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.forge.SACForge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SACCommand {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("sac")
                .requires(src -> src.hasPermission(2)) // только операторы

                // /sac status [player]
                .then(Commands.literal("status")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> statusPlayer(ctx, EntityArgument.getPlayer(ctx, "player")))
                    )
                    .executes(SACCommand::statusAll)
                )

                // /sac reload
                .then(Commands.literal("reload")
                    .executes(SACCommand::reload)
                )

                // /sac reset <player>
                .then(Commands.literal("reset")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> reset(ctx, EntityArgument.getPlayer(ctx, "player")))
                    )
                )
        );
    }

    // -------------------------------------------------------------------------

    private static int statusAll(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        Map<UUID, PlayerData> all = PlayerData.getAll();

        if (all.isEmpty()) {
            src.sendSuccess(() -> Component.literal("§7[SAC] Нет данных об игроках."), false);
            return 1;
        }

        src.sendSuccess(() -> Component.literal("§6[SAC] Статус всех игроков:"), false);

        for (Map.Entry<UUID, PlayerData> entry : all.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();
            if (data.warnings == 0) continue;

            // Пробуем найти имя игрока
            String name = uuid.toString().substring(0, 8) + "...";
            ServerPlayer player = src.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) name = player.getName().getString();

            final String displayName = name;
            final int warns = data.warnings;
            src.sendSuccess(() -> Component.literal(
                "§e" + displayName + " §7— §c" + warns + " варн(ов)"
            ), false);
        }
        return 1;
    }

    private static int statusPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        CommandSourceStack src = ctx.getSource();
        PlayerData data = PlayerData.get(target.getUUID());
        String name = target.getName().getString();

        src.sendSuccess(() -> Component.literal(
            "§6[SAC] Статус игрока §e" + name + "§6:"
        ), false);
        src.sendSuccess(() -> Component.literal(
            "§7Варнов: §c" + data.warnings
        ), false);

        List<String> history = data.getWarnHistory();
        if (history.isEmpty()) {
            src.sendSuccess(() -> Component.literal("§7История: пусто"), false);
        } else {
            src.sendSuccess(() -> Component.literal("§7История нарушений:"), false);
            for (String entry : history) {
                src.sendSuccess(() -> Component.literal("§8  " + entry), false);
            }
        }
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        try {
            // ForgeConfig перечитывается автоматически через Forge config system
            // Принудительно пересоздаём SACCore с новым конфигом
            SACForge.reloadCore();
            src.sendSuccess(() -> Component.literal("§a[SAC] Конфиг перезагружен."), true);
        } catch (Exception e) {
            src.sendFailure(Component.literal("§c[SAC] Ошибка перезагрузки: " + e.getMessage()));
        }
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        CommandSourceStack src = ctx.getSource();
        PlayerData data = PlayerData.get(target.getUUID());
        int old = data.warnings;
        data.warnings = 0;
        data.warnHistory.clear();
        String name = target.getName().getString();
        src.sendSuccess(() -> Component.literal(
            "§a[SAC] Варны игрока §e" + name + " §aсброшены (было: " + old + ")."
        ), true);
        return 1;
    }
}
