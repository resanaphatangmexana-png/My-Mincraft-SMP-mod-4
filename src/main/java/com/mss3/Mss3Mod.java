package com.mss3;

import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mss3Mod implements ModInitializer {
    public static final String MOD_ID = "mss3smp";
    public static final String DISPLAY_TITLE = "Mincraft Ss3";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mss3SMP");
    
    // บรรทัดที่ขาดไปซึ่งทำให้บิลด์ล้มเหลวในรูป image_1cc177
    public static final String DEFAULT_REGION = "Spawn"; 

    private static final int HUD_UPDATE_INTERVAL = 20;
    private static final int BOUNTY_CHECK_INTERVAL = 200;
    private int tickCounter = 0;

    public static MinecraftServer SERVER;
    public static HudManager HUD;
    public static TpaManager TPA;
    public static BountyManager BOUNTY;
    public static AdminManager ADMIN;

    @Override
    public void onInitialize() {
        LOGGER.info("[Mincraft Ss3] All Systems Online!");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
            HUD = new HudManager();
            TPA = new TpaManager();
            BOUNTY = new BountyManager();
            ADMIN = new AdminManager();
            ADMIN.initialize(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            HUD = null; TPA = null; BOUNTY = null; ADMIN = null; SERVER = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            PlayerData data = Mss3State.get(server).getOrCreatePlayer(player.getUuid());
            if (HUD != null) HUD.attach(player);
            if (data.isAdmin && ADMIN != null) ADMIN.applyTeam(player);
            if (data.isInvisible) applyInvisibility(player, true);
            player.sendMessage(Text.literal("§e§lMincraft Ss3 §rReady!"), false);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (HUD != null && tickCounter % HUD_UPDATE_INTERVAL == 0) {
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) { HUD.update(p); }
            }
            if (TPA != null && tickCounter % 20 == 0) TPA.tickExpiry(server);
            if (BOUNTY != null && tickCounter % BOUNTY_CHECK_INTERVAL == 0) BOUNTY.tick(server);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(source.getAttacker() instanceof ServerPlayerEntity killer)) return;
            if (killer.equals(entity)) return;
            if (entity instanceof ServerPlayerEntity victim) {
                if (BOUNTY != null && BOUNTY.hasActiveBounty(victim.getUuid())) {
                    long reward = BOUNTY.claimBounty(victim.getUuid());
                    PlayerData kData = Mss3State.get(SERVER).getOrCreatePlayer(killer.getUuid());
                    kData.money += reward;
                    Mss3State.get(SERVER).markDirty();
                    BOUNTY.announceClaim(SERVER, killer, victim, reward);
                }
            }
        });

        registerCommands();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(CommandManager.literal("money").executes(ctx -> {
                ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                long bal = Mss3State.get(SERVER).getOrCreatePlayer(p.getUuid()).money;
                p.sendMessage(Text.literal("§eBalance: §f$" + bal), false);
                return 1;
            }));
            
            dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("target", EntityArgumentType.player()).executes(ctx -> {
                    TPA.sendRequest(ctx.getSource().getPlayerOrThrow(), EntityArgumentType.getPlayer(ctx, "target"));
                    return 1;
                })));

            dispatcher.register(CommandManager.literal("yes").executes(ctx -> TPA.accept(ctx.getSource().getPlayerOrThrow()) ? 1 : 0));
            dispatcher.register(CommandManager.literal("no").executes(ctx -> TPA.deny(ctx.getSource().getPlayerOrThrow()) ? 1 : 0));
        });
    }

    public static void applyInvisibility(ServerPlayerEntity p, boolean on) {
        if (on) p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 0, false, false, false));
        else p.removeStatusEffect(StatusEffects.INVISIBILITY);
    }
}
