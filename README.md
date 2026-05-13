# 🎮 Mincraft Ss3 SMP Mod

**Server-side Fabric mod** — เจ้าของ server ลง 1 ครั้ง = ผู้เล่นทุกคนได้ feature ครบทันที **โดยไม่ต้องลงอะไรเลย**!

ใช้ได้กับ **Essential Mod** (Singleplayer World host) หรือ Dedicated Fabric Server

---

## ✨ Features

### 1️⃣ Real-time HUD Sidebar
แสดงด้านขวาหน้าจอ — update ทุก 1 วินาที:

```
        Mincraft Ss3
        ─────────────
        $ Money     7.08K
        ⚔ Kills        12
        ☠ Deaths        3
        ⏱ Playtime  4d 10h
                          
        Asia         (30ms)
```

### 2️⃣ /shop — Redstone Shop
- เปิด GUI chest พร้อมไอเทม **redstone ทุกชนิด**
- มี: dust, torch, repeater, comparator, observer, piston, hopper, dispenser, sculk sensor, crafter ฯลฯ
- ราคาคงที่ — คลิกซ้าย ซื้อ 1, Shift+คลิก ซื้อ stack

### 3️⃣ /shopsell — ขาย Bone Meal
- ถือ Bone Meal ในมือ → พิมพ์ `/shopsell`
- ได้ **$10 ต่อชิ้น**
- ระบบหาเงินหลัก!

### 4️⃣ TPA System
- `/tpa <ชื่อ>` — ส่งคำขอวาป
- เป้าหมายได้ **chat + เสียง bell**
- คลิก **[✓ ยอมรับ]** หรือพิมพ์ `/yes` → ยอมรับ
- `/no` → ปฏิเสธ
- หมดอายุใน 30 วิ

### 5️⃣ 🎯 Bounty System (ค่าหัว)
- **สุ่มทุก 15 นาที** อัตโนมัติ
- เลือก player ใน server → set ค่าหัว **$50,000 - $1,000,000**
- เมื่อ trigger:
  - 🔥 **Title สีแดง "💀 BOUNTY 💀"** กลางจอทุกคน
  - 🎵 **เสียง Wither spawn** + Ender Dragon growl
  - 💬 ข้อความใน chat บอกชื่อ + ค่าหัว
  - ⚠️ เป้าหมายได้รับการแจ้งเตือนพิเศษ
- ฆ่าเป้าหมาย = ได้เงินรางวัล (กลางจอแสดง "💰 BOUNTY CLAIMED 💰")
- หมดอายุใน 1 ชั่วโมงถ้าไม่ถูกฆ่า

### 6️⃣ Admin Rank System
- `/admin <player>` — ให้ยศแอดมิน (เฉพาะ server **op** ใช้ได้)
- Admin name = **สีฟ้า** + **prefix "(Admin)"** ทุกที่
  - Tab list
  - Chat
  - Nameplate เหนือหัว
- ใช้ scoreboard team (vanilla feature)

### 7️⃣ /gift money <player> <amount>
- **Admin only** — ให้เงินผู้เล่นจากอากาศ
- ผู้รับเห็นข้อความ "(Gift) ได้รับ $X จาก <ชื่อแอดมิน>"

### 8️⃣ /invit — Admin Invisibility
- **Admin only** — เปิด invisibility (vanilla effect)
- `/no invit` — ปิด invisibility
- เก็บสถานะแม้ออกจากเกม

### 9️⃣ /no pro — Admin Toggle Bounty
- **Admin only** — เปิด/ปิดระบบค่าหัว
- ปิดแล้ว = ไม่มีค่าหัวใหม่ + ล้างค่าหัวที่ active

### 🔟 Auxiliary Commands
- `/money [player]` — ดูเงิน
- `/pay <player> <amount>` — โอนเงิน
- `/bounty` — ดูค่าหัวที่ active
- `/yes` / `/no` — รับ/ปฏิเสธ TPA

---

## 📋 คำสั่งทั้งหมด

| คำสั่ง | ใครใช้ได้ | ใช้ทำอะไร |
|---|---|---|
| `/shop` | ทุกคน | เปิดร้านขาย redstone |
| `/shopsell` | ทุกคน | ขาย bone meal ในมือ |
| `/tpa <player>` | ทุกคน | ขอวาปหา |
| `/yes` | ทุกคน | รับ TPA |
| `/no` | ทุกคน | ปฏิเสธ TPA |
| `/money [player]` | ทุกคน | ดูเงิน |
| `/pay <player> <amount>` | ทุกคน | โอนเงิน |
| `/bounty` | ทุกคน | ดูค่าหัว |
| `/admin <player>` | OP only | ให้/ถอน admin |
| `/gift money <player> <amount>` | Admin | ให้เงิน |
| `/invit` | Admin | หายตัว |
| `/no invit` | Admin | หยุดหายตัว |
| `/no pro` | Admin | toggle ระบบค่าหัว |

---

## 🚀 ทาง Build .jar (เพราะผม build ในนี้ไม่ได้)

### Option 1: GitHub Actions Auto-Build (แนะนำ!)

ดูไฟล์ **`HOW_TO_BUILD_WITH_GITHUB.md`** — สอน step-by-step สำหรับคนไม่เขียนโค้ด

**Summary:**
1. สมัคร GitHub (ฟรี)
2. สร้าง repo ใหม่
3. Upload โค้ดทั้งหมด
4. GitHub Actions build .jar ให้ใน 2-5 นาที
5. Download .jar จาก Artifacts

⏱️ ใช้เวลาตั้งครั้งแรก **~10 นาที**, ครั้งต่อๆ ไป **0 นาที** (อัตโนมัติ)

### Option 2: Build เองบนเครื่อง

```bash
# ต้องมี JDK 21
cd mss3smp
gradle build
# ได้ไฟล์ build/libs/mincraft-ss3-smp-1.0.0.jar
```

---

## 📥 ติดตั้งใน Essential Mod

หลัง build ได้ .jar:

1. ปิด Minecraft
2. หา folder mods ของ Essential profile:
   - Windows: `%appdata%\.minecraft\mods\`
   - หรือถ้าใช้ Essential launcher: ดู profile path ใน launcher settings
3. Copy `mincraft-ss3-smp-1.0.0.jar` ลงใน mods folder
4. ตรวจมี **Fabric API** ใน mods แล้วด้วย:
   - Download: https://modrinth.com/mod/fabric-api
5. เปิด Minecraft → host world ผ่าน Essential
6. เพื่อนต่อเข้า → ใช้ feature ได้เลย!

---

## ⚙️ Customize

### เพิ่ม-ลด item ใน /shop
แก้ใน `ShopHandler.java` ส่วน `static { REDSTONE_SHOP.put(...) }`:
```java
REDSTONE_SHOP.put(Items.ELYTRA, 5000L);  // เพิ่ม elytra ราคา 5000
```

### เปลี่ยนราคา bone meal
ใน `ShopHandler.java`:
```java
public static final long BONEMEAL_PRICE = 10;  // ราคาต่อชิ้น
```

### ปรับช่วงเวลา bounty
ใน `BountyManager.java`:
```java
private static final long BOUNTY_INTERVAL_MS = 15L * 60L * 1000L;  // 15 นาที
private static final long MIN_BOUNTY =   50_000L;
private static final long MAX_BOUNTY = 1_000_000L;
```

### ปรับสีของ admin
ใน `AdminManager.java`:
```java
team.setPrefix(Text.literal("(Admin) ").styled(s -> 
    s.withColor(Formatting.AQUA).withBold(true)));  // เปลี่ยน AQUA เป็นสีอื่น
```

---

## 💾 Data Storage

ข้อมูลทั้งหมด (เงิน, admin, invisibility, bounty) save อัตโนมัติที่:
```
world/data/mss3smp.dat
```

Backup file นี้เพื่อ backup ของทุกคน

---

## 🐛 Troubleshooting

### Build error: `cannot find symbol`
- บอกบรรทัด error → ผมแก้ให้
- ส่วนใหญ่เกิดจาก API method name เปลี่ยนใน version ใหม่

### HUD ไม่ขึ้น
- ตรวจ Fabric API ลงแล้ว
- เช็ค console ว่ามี `[Mincraft Ss3] Ready!` หรือไม่

### Admin rank ไม่เป็นสีฟ้า
- ตรวจว่าคน grant `/admin` เป็น OP จริง
- Restart server ถ้า team ค้าง

### TPA ไม่มีเสียง
- ผู้เล่นต้องไม่ปิดเสียง notification

### Bounty ไม่เกิด
- ต้องมีคน online อย่างน้อย **2 คน**
- ทุก 15 นาที rolls ครั้งแรก (ครั้งแรกหลัง server start อาจช้า)
- ตรวจ `/bounty` ดู active ไหม

---

## 🎯 Spec

- **Mod loader:** Fabric
- **MC version:** 1.21.4 (เปลี่ยน build.gradle ได้)
- **Java:** 21
- **Side:** Server-only (vanilla client ใช้ได้)
- **License:** MIT
- **Code size:** ~1,500 lines, 7 Java files

---

## 🆘 Support

ถ้าเจอ bug:
1. เปิด `logs/latest.log` ของ server
2. Copy error → ส่งให้ผม
3. ผมแก้ให้ในรอบเดียว

Happy SMP-ing! 🎮
