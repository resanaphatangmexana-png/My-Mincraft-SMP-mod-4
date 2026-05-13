# 🚀 วิธี Build .jar ด้วย GitHub Actions (ฟรี)

> สำหรับคนที่ไม่อยาก install Java/Gradle ในเครื่อง — GitHub สร้าง .jar ให้ฟรีใน cloud

---

## 📋 สิ่งที่ต้องมี

- ✅ Email ใช้สมัคร GitHub (ฟรี)
- ✅ Browser
- ❌ ไม่ต้องลง Java
- ❌ ไม่ต้องลง Gradle

⏱️ **เวลาทั้งหมด: 10 นาที**

---

## 🔢 Step-by-Step

### Step 1: สมัคร GitHub Account (ถ้ายังไม่มี)

1. ไปที่ **https://github.com**
2. กด **Sign Up**
3. ใส่ email + password + username
4. Verify email
5. เลือก free plan

> ⚠️ **Username สำคัญ** — ต้องจดจำ จะใช้ใน URL repository

---

### Step 2: สร้าง Repository ใหม่

1. เข้า GitHub แล้ว → กดปุ่ม **"+"** ขวาบน → **New repository**
2. กรอกข้อมูล:
   - **Repository name:** `mincraft-ss3-smp` (ห้ามมีช่องว่าง)
   - **Description:** `My Mincraft SMP mod` (ไม่ต้องก็ได้)
   - **Public** หรือ **Private** เลือก Private ก็ได้ (ฟรีเหมือนกัน)
   - ❌ **ห้ามติ๊ก** "Initialize with README"
3. กด **Create repository**

---

### Step 3: Upload โค้ดเข้า GitHub

**วิธีง่ายสุด — Drag & Drop:**

1. หน้า repository ใหม่ที่สร้าง — มี link **"uploading an existing file"**
2. คลิก
3. **Drag ทุกไฟล์ใน folder `mss3smp/`** ทิ้งลงไป (ยกเว้นไฟล์ build ถ้ามี)
   - ✅ build.gradle, settings.gradle, gradle.properties, README.md, LICENSE
   - ✅ folder `src/`, `gradle/`, `.github/`
4. เลื่อนลงล่าง → กด **"Commit changes"**

> ⚠️ **สำคัญ:** ต้องอัพ `.github/workflows/build.yml` ด้วย — ถ้ามองไม่เห็นใน upload area
> ให้ปิด "show hidden files" ใน Windows Explorer / Finder

**Alternative: ใช้ GitHub Desktop**
1. Download **GitHub Desktop** จาก desktop.github.com
2. Clone repo → drag folder → commit & push

---

### Step 4: รอ GitHub Build (อัตโนมัติ!)

1. หลัง upload → กด tab **"Actions"** ด้านบน repository
2. จะเห็น **"Build Mincraft Ss3 SMP Mod"** กำลังทำงาน (มีจุดสีเหลือง)
3. รอ ~2-5 นาที
4. เมื่อจุดเป็น ✅ สีเขียว = Build เสร็จ!

---

### Step 5: Download .jar

1. คลิกที่ workflow run ที่ทำงานเสร็จแล้ว (สีเขียว)
2. เลื่อนลงล่าง → ส่วน **"Artifacts"**
3. กด **"mincraft-ss3-smp-mod"** → download .zip
4. แตก .zip → ได้ไฟล์ **`mincraft-ss3-smp-1.0.0.jar`**

✅ **เสร็จแล้ว!** นี่คือ mod file ของคุณ

---

### Step 6: ติดตั้ง Mod

**สำหรับ Essential Mod (Singleplayer / hosted world):**

1. หา folder mods ของ Essential profile:
   - Windows: `C:\Users\<You>\AppData\Roaming\.minecraft\mods\`
   - หรือถ้าใช้ Essential launcher: ดู profile path ใน launcher
2. Copy ไฟล์ **`mincraft-ss3-smp-1.0.0.jar`** ลงใน folder mods
3. ตรวจมี **Fabric API** ใน mods แล้วด้วย (ถ้ายัง → download จาก modrinth.com/mod/fabric-api)
4. เปิด Minecraft → join world → ✅ ใช้ได้!

**สำหรับ Dedicated Fabric Server:**
- วาง .jar ใน `server/mods/` ของเซิร์ฟ
- Restart server

---

## 🐛 ถ้า Build ล้มเหลว (สีแดง ❌)

### ดู Error
1. คลิก workflow run ที่ล้มเหลว
2. กด **"build"** ในรายชื่อ jobs
3. ดูบรรทัด error สีแดง

### ส่งให้ผมดู
- Copy error message มาเลย
- ผมแก้ให้ — มัก fix ในรอบเดียว

### ปัญหายอดฮิต
| Error | สาเหตุ | แก้ |
|---|---|---|
| `cannot find symbol` | API version mismatch | บอก MC version ที่จะใช้ ผมแก้ build.gradle ให้ |
| `permission denied` | gradlew ไม่ executable | workflow แก้แล้ว แต่ถ้าเจอบอกผม |
| `out of memory` | Heap เล็กไป | เพิ่ม `-Xmx4G` ใน gradle.properties |

---

## 🔄 อัพเดท Mod (ภายหลัง)

ถ้าอยากแก้โค้ด/เพิ่ม feature:

1. แก้โค้ดในเครื่อง
2. เข้า repo บน GitHub → upload ไฟล์ที่แก้ใหม่ (เลือก "Replace this file")
3. รอ build อัตโนมัติ
4. Download .jar ใหม่
5. ใส่ทับใน mods folder

---

## 💡 Pro Tips

✅ **Workflow trigger เอง** — ใน tab Actions มีปุ่ม "Run workflow" ถ้าอยาก build ใหม่
✅ **Tag สร้าง release อัตโนมัติ** — สร้าง tag เช่น `v1.0.0` → GitHub สร้าง Release พร้อม .jar ให้ดาวน์โหลด
✅ **Build cache** — Build ครั้งที่ 2+ จะเร็วกว่า (~30 วินาที)
✅ **Private repo ก็ build ได้** — GitHub Actions 2,000 นาที/เดือนฟรี

---

## 🆘 ต้องการช่วย?

ถ้าติดขั้นไหน:
1. ถ่ายรูปหน้าจอตรงนั้น
2. ส่งมาให้ผม
3. ผมจะบอกว่าทำอะไรต่อ

หรือถามใน Discord:
- Fabric: https://discord.gg/v6v4pMv
- Essential: https://discord.gg/essential

---

## 🎯 ทำไม GitHub Actions?

| ทางเลือก | เวลา | ความยาก | ต้องลงอะไร |
|---|---|---|---|
| **Install JDK + build เอง** | 30+ นาที | ⭐⭐⭐⭐ | JDK 21 + Gradle (~2GB) |
| **GitHub Actions** | 10 นาที | ⭐⭐ | แค่ browser! |
| **จ้าง Fiverr** | 1-2 วัน | ⭐ | เสียเงิน $5+ |

= **GitHub Actions คุ้มสุด** สำหรับคนไม่มี dev environment

---

Happy modding! 🎮
