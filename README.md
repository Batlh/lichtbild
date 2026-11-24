# Lichtbild

Offline-Kamera für Android 10 und neuer. Anwendungspaket `io.github.batlh.lichtbild`. Für Erwachsene und Jugendliche ab zehn Jahren. Front- und Hauptkamera werden unterstützt.

## Aufnahme

Profil wählen und die automatische Aufnahme starten. Die Kamera sucht eine einzelne Person und prüft Kopfhaltung, Augenöffnung, Mund, Gesichtsausdruck, Ausschnitt, Helligkeit und Schärfe. Nach mehreren passenden Kamerabildern verarbeitet die App genau den geprüften Frame. Beide ML-Kit-Modelle sind im APK enthalten. Die App ersetzt den Hintergrund durch Hellgrau, richtet die Augenachse aus und skaliert den geschätzten Scheitel-Kinn-Abstand auf 34 mm bei Ausgabegröße 35 × 45 mm. Haare und Kopfbedeckungen können diese Schätzung verfälschen.

Die Vorschau dient zur Sichtprüfung von Blickrichtung, Brillenreflexen, Schatten und Freistellkanten. Erst mit Bestätigung entsteht ein JPEG mit 700 × 900 Pixeln in Downloads/Lichtbild. Beim Druck explizit 35 × 45 mm einstellen. Es gibt keinen eingebauten Upload. Andere Apps mit Dateizugriff und vom Nutzer eingerichtete Synchronisierung liegen außerhalb der Kontrolle der App.

## Wofür eignet sich das Foto?

Das JPEG hat 700 × 900 Pixel im Verhältnis 7:9. Bei einem Ausdruck mit 35 × 45 mm beträgt die angestrebte Kopfhöhe 34 mm. Die App prüft die Aufnahme technisch. Ob das einzelne Bild angenommen wird, entscheidet die empfangende Stelle.

| Verwendung | Anforderungen und Einreichung |
|---|---|
| Gesundheitskarte · energie-BKK | Die Kasse bietet einen eigenen Online-Fotoupload für die Gesundheitskarte an. [Foto einreichen](https://www.energie-bkk.de/service/elektronischen-gesundheitskarte-egk/) |
| Gesundheitskarte · BARMER | Handyfotos sind ausdrücklich möglich. Gefordert sind unter anderem eine frontale Aufnahme, gleichmäßiges Licht, erkennbare Augen und 70–80 % Gesichtshöhe. Die App zielt auf rund 76 %. [Fotovorgaben und Upload](https://www.barmer.de/unsere-leistungen/leistungen-a-z/online-services/elektronische-gesundheitskarte/foto-gesundheitskarte-1278240) |
| Gesundheitskarte · DAK-Gesundheit | Selbst aufgenommene Fotos sind zulässig. Das Foto muss aktuell und die Person eindeutig erkennbar sein. Einreichung per Upload oder Post. [DAK-Fotohinweise](https://www.dak.de/dak/ihr-anliegen/elektronische-gesundheitskarte/was-muss-ich-beim-foto-fuer-die-gesundheitskarte-beachten-_41414) |
| Gesundheitskarte · Techniker Krankenkasse | Die TK nennt 35 × 45 mm für das Foto und 300 × 400 Pixel für digitale Bilder. JPEG ist zugelassen. Für das digitale TK-Format muss der App-Export separat auf 3:4 zugeschnitten und auf 300 × 400 Pixel skaliert werden. [TK-Fotovorgaben](https://www.tk.de/techniker/versicherung/tk-leistungen/versichertenkarte/gesundheitskarte-bild/aussehen-passbild-versichertenkarte-2002316) |
| Führerschein | Die App orientiert sich am biometrischen Hochformat 35 × 45 mm. Für einen Papierabzug beim Druck diese Größe einstellen. Die Fahrerlaubnisbehörde gibt den Einreichungsweg vor. [§ 21 FeV](https://www.gesetze-im-internet.de/fev_2010/__21.html) · [Lichtbildschablone](https://www.bundesdruckerei-gmbh.de/files/dokumente/pdf/lichtbildschablone.pdf) |
| Personalausweis in Deutschland | Die App eignet sich als Aufnahmeprobe. Für den regulären Antrag erfolgt die Aufnahme bei der Behörde oder über einen zugelassenen Fotodienstleister mit sicherer elektronischer Übermittlung. [§ 9 PAuswG](https://www.gesetze-im-internet.de/pauswg/__9.html) · [§ 5a PAuswV](https://www.gesetze-im-internet.de/pauswv/__5a.html) |

## Bauen und Fehler melden

Projekt in Android Studio öffnen oder mit JDK 17, Android SDK 35 und Gradle 8.9 bauen: `gradle :app:assembleDebug`. Für Releases `gradle :app:assembleRelease` ausführen und die ausgerichtete APK mit Android Build Tools / apksigner und dem projektspezifischen Keystore signieren.

Bei Kamerafehlern bitte Modell, Android-Version, Kameraseite, Profil und angezeigten Hinweis angeben.
