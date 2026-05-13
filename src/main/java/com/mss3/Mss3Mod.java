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

    private static final int HUD_UPDATE_INTERVAL = 20;     // 1 sec
    private static final int BOUNTY_CHECK_INTERVAL = 200;  // 10 sec
    private int tickCounter = 0;

    public static MinecraftServer SERVER;
    public static HudManager HUD;
    public static TpaManager TPA;
    public static BountyManager BOUNTY;
    public static AdminManager ADMIN;

    @Override
    public void onInitialize() {
        LOGGER.info("[Mincraft Ss3] Initializing full system for 1.21.11...");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
            HUD = new HudManager();
            TPA = new TpaManager();
            BOUNTY = new BountyManager();
            ADMIN = new AdminManager();
            ADMIN.initialize(server);
            LOGGER.info("[Mincraft Ss3] All Systems Active!");
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

            player.sendMessage(Text.literal("§e§lMincraft Ss3 §rพร้อมเล่น!"), false);
            player.sendMessage(Text.literal("§7คำสั่ง: §e/shop §7| §e/tpa §7| §e/shopsell §7| §e/money"), false);
            if (BOUNTY != null && BOUNTY.hasActiveBounty(player.getUuid())) {
                player.sendMessage(Text.literal("§c⚠ คุณมีค่าหัวบนหัว! ระวังตัว!"), false);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (HUD != null) HUD.detach(player);
            if (TPA != null) TPA.cleanup(player.getUuid());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (HUD != null && tickCounter % HUD_UPDATE_INTERVAL == 0) {
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                    HUD.update(p);
                }
            }
            if (TPA != null && tickCounter % 20 == 0) TPA.tickExpiry(server);
            if (BOUNTY != null && tickCounter % BOUNTY_CHECK_INTERVAL == 0) BOUNTY.tick(server);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(source.getAttacker() instanceof ServerPlayerEntity killer)) return;
            if (killer.equals(entity)) return;

            if (!(entity instanceof ServerPlayerEntity victim)) {
                PlayerData killerData = Mss3State.get(SERVER).getOrCreatePlayer(killer.getUuid());
                killerData.money += 5;
                Mss3State.get(SERVER).markDirty();
                return;
            }

            if (BOUNTY != null && BOUNTY.hasActiveBounty(victim.getUuid())) {
                long reward = BOUNTY.claimBounty(victim.getUuid());
                PlayerData killerData = Mss3State.get(SERVER).getOrCreatePlayer(killer.getUuid());
                killerData.money += reward;
                Mss3State.get(SERVER).markDirty();
                BOUNTY.announceClaim(SERVER, killer, victim, reward);
            } else {
                PlayerData killerData = Mss3State.get(SERVER).getOrCreatePlayer(killer.getUuid());
                killerData.money += 100;
                Mss3State.get(SERVER).markDirty();
                killer.sendMessage(Text.literal("§a+$100 §7(PvP kill)"), true);
            }
        });

        registerCommands();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {

            dispatcher.register(CommandManager.literal("shop").executes(ctx -> {
                ShopHandler.openMainMenu(ctx.getSource().getPlayerOrThrow());
                return 1;
            }));

            dispatcher.register(CommandManager.literal("shopsell").executes(ctx -> {
                return ShopHandler.sellBonemeal(ctx.getSource().getPlayerOrThrow());
            }));

            dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("target", EntityArgumentType.player()).executes(ctx -> {
                    ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                    if (sender.equals(target)) {
                        sender.sendMessage(Text.literal("§cคุณวาปไปหาตัวเองไม่ได้!"), false);
                        return 0;
                    }
                    TPA.sendRequest(sender, target);
                    return 1;
                })));

            dispatcher.register(CommandManager.literal("yes").executes(ctx -> TPA.accept(ctx.getSource().getPlayerOrThrow()) ? 1 : 0));

            dispatcher.register(CommandManager.literal("no")
                .executes(ctx -> TPA.deny(ctx.getSource().getPlayerOrThrow()) ? 1 : 0)
                .then(CommandManager.literal("pro").requires(this::requireAdmin).executes(ctx -> {
                    boolean nowEnabled = BOUNTY.toggle();
                    ctx.getSource().sendFeedback(() -> Text.literal("§7[Admin] " + (nowEnabled ? "§aเปิด" : "§cปิด") + " §7ระบบค่าหัว"), true);
                    return 1;
                })));

            dispatcher.register(CommandManager.literal("money")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    long bal = Mss3State.get(SERVER).getOrCreatePlayer(p.getUuid()).money;
                    p.sendMessage(Text.literal("§eเงินของคุณ: §f$" + formatMoney(bal)), false);
                    return 1;
                }));

            dispatcher.register(CommandManager.literal("pay")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .then(CommandManager.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> {
                        return payCommand(ctx.getSource().getPlayerOrThrow(), EntityArgumentType.getPlayer(ctx, "player"), LongArgumentType.getLong(ctx, "amount"));
                    }))));

            dispatcher.register(CommandManager.literal("admin")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                    PlayerData data = Mss3State.get(SERVER).getOrCreatePlayer(target.getUuid());
                    data.isAdmin = !data.isAdmin;
                    if (data.isAdmin) { ADMIN.applyTeam(target); } else { ADMIN.removeFromTeam(target); }
                    Mss3State.get(SERVER).markDirty();
                    return 1;
                })));

            dispatcher.register(CommandManager.literal("invit").requires(this::requireAdmin).executes(ctx -> {
                ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                PlayerData data = Mss3State.get(SERVER).getOrCreatePlayer(p.getUuid());
                data.isInvisible = true;
                applyInvisibility(p, true);
                Mss3State.get(SERVER).markDirty();
                return 1;
            }));
            
            dispatcher.register(CommandManager.literal("bounty").executes(ctx -> {
                BOUNTY.listBounties(ctx.getSource().getPlayerOrThrow());
                return 1;
            }));
        });
    }

    private int payCommand(ServerPlayerEntity sender, ServerPlayerEntity target, long amount) {
        if (sender.equals(target)) return 0;
        PlayerData sData = Mss3State.get(SERVER).getOrCreatePlayer(sender.getUuid());
        if (sData.money < amount) return 0;
        PlayerData tData = Mss3State.get(SERVER).getOrCreatePlayer(target.getUuid());
        sData.money -= amount; tData.money += amount;
        Mss3State.get(SERVER).markDirty();
        return 1;
    }

    private boolean requireAdmin(ServerCommandSource src) {
        if (src.getPlayer() == null) return src.hasPermissionLevel(2);
        return src.hasPermissionLevel(2) || Mss3State.get(SERVER).getOrCreatePlayer(src.getPlayer().getUuid()).isAdmin;
    }

    public static void applyInvisibility(ServerPlayerEntity p, boolean on) {
        if (on) { p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 0, false, false, false)); }
        else { p.removeStatusEffect(StatusEffects.INVISIBILITY); }
    }

    public static String formatMoney(long m) {
        if (m >= 1_000_000L) return String.format("%.2fM", m / 1_000_000.0);
        if (m >= 1_000L) return String.format("%.2fK", m / 1_000.0);
        return String.valueOf(m);
    }
}
