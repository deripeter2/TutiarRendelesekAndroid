# TutiÁr Rendelések – Android

Ez egy működő első verzió:

- Jetpack Compose + Material 3
- szerveres API-szinkron
- helyi SQLite-adatbázis
- offline keresés rendelési számra, névre, telefonra és e-mailre
- rendelésrészletek és terméklista
- telefonszámra kattintva tárcsázó
- e-mailre kattintva levelező

## A legegyszerűbb telepítés a már létrehozott projektedbe

1. Zárd be az Android Studióban a jelenlegi projektet.
2. Készíts róla biztonsági másolatot.
3. A ZIP tartalmát másold a projekted gyökérmappájába.
4. A következő meglévő fájlokat NE töröld, ha a ZIP-ben nem szerepelnek:
   - `gradlew`
   - `gradlew.bat`
   - `gradle/wrapper/gradle-wrapper.jar`
   - `local.properties`
5. Nyisd meg újra a projektet Android Studióban.
6. Várd meg a Gradle Sync végét.

## API-token beállítása

Nyisd meg:

`app/src/main/java/hu/tutiar/tutirrendelsek/AppConfig.kt`

Ezt:

`IDE_IRD_A_PHP_FAJLBAN_LEVO_API_TOKENT`

cseréld a `tutiar-api-sync.php` fájlban beállított tokenre.

A tokent ne küldd el nyilvánosan.

## Első futtatás

1. Indítsd el az appot a telefonon.
2. Nyomd meg az **Adatok frissítése** gombot.
3. Az első szinkron hosszabb lehet, mert az összes időszakba eső rendelést letölti.
4. Ezután internet nélkül is működik a keresés.

## Megjegyzés

A ZIP szándékosan nem tartalmazza a gépedhez kötött `local.properties` fájlt, és a meglévő projekted Gradle wrapper fájljait kell megtartani.
