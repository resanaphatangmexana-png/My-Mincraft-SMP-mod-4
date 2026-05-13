package com.mss3;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * TPA system — manages teleport requests between players.
 */
public class TpaManager {
    private static final long EXPIRY_MS = 30_000L; // 30 seconds

    /** target UUID -> pending request from sender. */
    private final Map<UUID, Request> pending = new HashMap<>();

    private static class Request {
        final UUID senderId;
        final String senderName;
        final long createdAt;

        Request(UUID senderId, String senderName) {
            this.senderId = senderId;
            this.senderName = senderName;
            this.createdAt = System.currentTimeMillis();
        }
    }

    public void sendRequest(ServerPlayerEntity sender, ServerPlayerEntity target) {
        pending.put(target.getUuid(), new Request(sender.getUuid(), sender.getName().getString()));

        sender.sendMessage(
            Text.literal("§aส่งคำขอ TPA ไปหา §e" + target.getName().getString() + " §aแล้ว §7(หมดอายุใน 30 วินาที)"),
            false
        );

        MutableText line1 = Text.literal("§e§l" + sender.getName().getString() + " §r§eอยากวาปมาหาคุณ");
        MutableText accept = Text.literal(" §a§l[✓ ยอมรับ]")
            .styled(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/yes"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("คลิกเพื่อยอมรับ หรือพิมพ์ /yes"))));
        MutableText deny = Text.literal(" §c§l[✗ ปฏิเสธ]")
            .styled(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/no"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("คลิกเพื่อปฏิเสธ หรือพิมพ์ /no"))));

        target.sendMessage(Text.literal("§7§m─────────────────────────"), false);
        target.sendMessage(line1, false);
        target.sendMessage(accept.append(deny), false);
        target.sendMessage(Text.literal("§7§m─────────────────────────"), false);

        target.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
            SoundCategory.PLAYERS, 1.0f, 1.2f);
        target.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
            SoundCategory.PLAYERS, 0.8f, 1.5f);

        target.sendMessage(Text.literal("§e⚡ TPA Request from " + sender.getName().getString()), true);
    }

    public boolean accept(ServerPlayerEntity target) {
        Request req = pending.remove(target.getUuid());
        if (req == null) {
            target.sendMessage(Text.literal("§cไม่มีคำขอ TPA ที่รอตอบ"), false);
            return false;
        }
        ServerPlayerEntity sender = target.getServer().getPlayerManager().getPlayer(req.senderId);
        if (sender == null) {
            target.sendMessage(Text.literal("§c" + req.senderName + " ออกจากเซิร์ฟไปแล้ว"), false);
            return false;
        }

        // แก้ไขจุดนี้: ใช้คำสั่ง teleport แบบ 6 พารามิเตอร์สำหรับ Minecraft 1.21+
        sender.teleport(
            target.getServerWorld(),
            target.getX(), 
            target.getY(), 
            target.getZ(),
            target.getYaw(), 
            target.getPitch()
        );
        
        sender.sendMessage(Text.literal("§aTeleported to §e" + target.getName().getString()), false);
        target.sendMessage(Text.literal("§a" + sender.getName().getString() + " §aวาปมาหาคุณแล้ว"), false);
        target.playSoundToPlayer(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.5f);
        return true;
    }

    public boolean deny(ServerPlayerEntity target) {
        Request req = pending.remove(target.getUuid());
        if (req == null) {
            target.sendMessage(Text.literal("§cไม่มีคำขอ TPA ที่รอตอบ"), false);
            return false;
        }
        target.sendMessage(Text.literal("§7ปฏิเสธคำขอ TPA จาก " + req.senderName), false);
        ServerPlayerEntity sender = target.getServer().getPlayerManager().getPlayer(req.senderId);
        if (sender != null) {
            sender.sendMessage(Text.literal("§c" + target.getName().getString() + " ปฏิเสธคำขอวาปของคุณ"), false);
        }
        return true;
    }

    public void tickExpiry(MinecraftServer server) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Request>> it = pending.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Request> e = it.next();
            if (now - e.getValue().createdAt > EXPIRY_MS) {
                it.remove();
                ServerPlayerEntity target = server.getPlayerManager().getPlayer(e.getKey());
                if (target != null) {
                    target.sendMessage(Text.literal("§7คำขอ TPA จาก " + e.getValue().senderName + " หมดอายุ"), false);
                }
                ServerPlayerEntity sender = server.getPlayerManager().getPlayer(e.getValue().senderId);
                if (sender != null) {
                    sender.sendMessage(Text.literal("§7คำขอ TPA หมดอายุ"), false);
                }
            }
        }
    }

    public void cleanup(UUID playerId) {
        pending.remove(playerId);
        pending.entrySet().removeIf(e -> e.getValue().senderId.equals(playerId));
    }
}
