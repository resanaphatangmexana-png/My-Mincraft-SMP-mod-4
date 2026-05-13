package com.mss3;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Per-player persistent data: money, admin rank, invisibility state, region, and shop listings.
 */
public class PlayerData {
    public UUID uuid;
    public long money = 0L;
    public String region = Mss3Mod.DEFAULT_REGION;
    public boolean isAdmin = false;
    public boolean isInvisible = false;
    public long lastSeen = System.currentTimeMillis();
    
    // เพิ่มตัวแปรเก็บรายการร้านค้า
    public List<ShopListing> listings = new ArrayList<>();

    public PlayerData() {}
    public PlayerData(UUID uuid) { this.uuid = uuid; }

    // --- เพิ่มคลาส ShopListing ไว้ตรงนี้เพื่อให้ Mss3State เรียกใช้ได้ ---
    public static class ShopListing {
        // คุณสามารถเพิ่มตัวแปรที่จำเป็นสำหรับร้านค้าตรงนี้ เช่นไอเทมหรือราคา
        // ตัวอย่างพื้นฐาน:
        public String itemName = "Unknown";
        public long price = 0;

        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            nbt.putString("itemName", itemName);
            nbt.putLong("price", price);
            return nbt;
        }

        public static ShopListing fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            ShopListing s = new ShopListing();
            s.itemName = nbt.getString("itemName");
            s.price = nbt.getLong("price");
            return s;
        }
    }

    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        if (uuid != null) nbt.putUuid("uuid", uuid);
        nbt.putLong("money", money);
        nbt.putString("region", region == null ? Mss3Mod.DEFAULT_REGION : region);
        nbt.putBoolean("isAdmin", isAdmin);
        nbt.putBoolean("isInvisible", isInvisible);
        nbt.putLong("lastSeen", lastSeen);

        // เพิ่มการเซฟข้อมูล listings
        NbtList list = new NbtList();
        for (ShopListing s : listings) {
            list.add(s.writeNbt(new NbtCompound(), registries));
        }
        nbt.put("listings", list);

        return nbt;
    }

    public static PlayerData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        PlayerData d = new PlayerData();
        if (nbt.contains("uuid")) d.uuid = nbt.getUuid("uuid");
        d.money = nbt.getLong("money");
        d.region = nbt.contains("region") ? nbt.getString("region") : Mss3Mod.DEFAULT_REGION;
        d.isAdmin = nbt.contains("isAdmin") && nbt.getBoolean("isAdmin");
        d.isInvisible = nbt.contains("isInvisible") && nbt.getBoolean("isInvisible");
        d.lastSeen = nbt.contains("lastSeen") ? nbt.getLong("lastSeen") : System.currentTimeMillis();

        // เพิ่มการโหลดข้อมูล listings
        if (nbt.contains("listings", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("listings", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                d.listings.add(ShopListing.fromNbt(list.getCompound(i), registries));
            }
        }
        return d;
    }
}
