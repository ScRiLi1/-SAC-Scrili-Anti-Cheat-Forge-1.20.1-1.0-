package com.scrili.sac.forge;

import com.scrili.sac.forge.antiesp.AntiESP;
import com.scrili.sac.forge.command.SACCommand;
import com.scrili.sac.core.SACCore;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.forge.adapter.ForgeConfig;
import com.scrili.sac.forge.adapter.ForgePlayer;
import com.scrili.sac.forge.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("sac")
public class SACForge {

    private static SACCore core;

    public SACForge() {
        ForgeConfig.register();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.register();
        core = new SACCore(new ForgeConfig());
        setupAlertSender();
        AntiESP antiESP = new AntiESP();
        MinecraftForge.EVENT_BUS.register(antiESP);
        MinecraftForge.EVENT_BUS.register(new SACCommand());
    }

    public static SACCore getCore() { return core; }

    public static void reloadCore() {
        // Пересоздаём только конфиг-зависимые чеки, alert sender не трогаем
        var oldSender = core.getPunishmentManager().getAlertSender();
        core = new SACCore(new ForgeConfig());
        core.getPunishmentManager().setAlertSender(oldSender);
    }

    private static void setupAlertSender() {
        core.getPunishmentManager().setAlertSender(message -> {
            if (!core.getConfig().isAdminAlertsEnabled()) return;
            var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            for (ServerPlayer op : server.getPlayerList().getPlayers()) {
                if (server.getPlayerList().isOp(op.getGameProfile())) {
                    op.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
                }
            }
        });
    }

    // --- тик-проверки ---

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ForgePlayer fp = new ForgePlayer(player);
        if (core.getConfig().isSpeedCheckEnabled()) core.getSpeedCheck().check(fp);
        if (core.getConfig().isFlyCheckEnabled())   core.getFlyCheck().check(fp);
        core.getJesusCheck().check(fp);
        core.getAimAssistCheck().onTick(fp);
    }

    // --- NoFall ---

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (event.getDistance() > 10.0f && event.isCanceled()) {
            event.setCanceled(false);
            core.getPunishmentManager().warn(
                new ForgePlayer(player),
                "NoFall (высота: " + String.format("%.1f", event.getDistance()) + " блоков)"
            );
        }
    }

    // --- Reach + FastAttack + KillAura ---

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;

        // Только прямой удар — игнорируем стрелы, магию, взрывы и т.д.
        // getDirectEntity() — это сам игрок при melee, или стрела/снаряд при ranged
        if (event.getSource().getDirectEntity() != attacker) return;

        ForgePlayer fp = new ForgePlayer(attacker);
        Entity target = event.getEntity();

        // Reach
        if (core.getConfig().isReachCheckEnabled())
            core.getReachCheck().check(fp, target.getX(), target.getY(), target.getZ());

        // KillAura (wall + angle)
        if (core.getConfig().isKillAuraCheckEnabled())
            core.getKillAuraCheck().onAttack(fp, target.getX(), target.getY(), target.getZ());

        // AimAssist
        core.getAimAssistCheck().onAttack(fp, target.getX(), target.getY(), target.getZ());

        // FastAttack
        if (core.getConfig().isFastAttackCheckEnabled())
            core.getFastAttackCheck().onAttack(fp);

        // AutoClicker серверный подсчёт
        if (core.getConfig().isAutoClickerCheckEnabled())
            core.getAutoClickerCheck().onAttack(fp);
    }

    // --- Scaffold + Freecam/Place ---

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ForgePlayer fp = new ForgePlayer(player);
        core.getScaffoldCheck().onBlockPlace(fp);
        if (core.getConfig().isFreecamCheckEnabled()) {
            var pos = event.getPos();
            core.getFreecamCheck().onBlockPlace(fp, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    // --- Freecam/Interact ---

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!core.getConfig().isFreecamCheckEnabled()) return;
        var pos = event.getPos();
        core.getFreecamCheck().onBlockInteract(
            new ForgePlayer(player), pos.getX(), pos.getY(), pos.getZ()
        );
    }

    // --- выход ---

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerData.remove(player.getUUID());
        }
    }

    // --- респаун ---

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        resetMovementData(player);
    }

    // --- телепорт (смена измерения) ---

    @SubscribeEvent
    public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        resetMovementData(player);
    }

    // --- клон (при входе из другого измерения) ---

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        resetMovementData(player);
    }

    // --- смена gamemode ---

    @SubscribeEvent
    public void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        resetMovementData(player);
    }

    private void resetMovementData(ServerPlayer player) {
        PlayerData data = PlayerData.get(player.getUUID());
        data.posInitialized = false;
        data.airTicks = 0;
        data.speedSampleIndex = 0;
    }
}
