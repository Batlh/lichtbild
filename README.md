# Lichtbild

Offline-Kamera für Android 10 und neuer. Anwendungspaket `io.github.batlh.lichtbild`. Für Erwachsene und Jugendliche ab zehn Jahren. Front- und Hauptkamera werden unterstützt.

## Aufnahme

Profil wählen und die automatische Aufnahme starten. Die Kamera sucht eine einzelne Person und prüft Kopfhaltung, Augenöffnung, Mund, Gesichtsausdruck, Ausschnitt, Helligkeit und Schärfe. Nach mehreren passenden Kamerabildern verarbeitet die App genau den geprüften Frame. Beide ML-Kit-Modelle sind im APK enthalten. Die App ersetzt den Hintergrund durch Hellgrau, richtet die Augenachse aus und skaliert den geschätzten Scheitel-Kinn-Abstand auf 34 mm bei Ausgabegröße 35 × 45 mm. Haare und Kopfbedeckungen können diese Schätzung verfälschen.

Die Vorschau dient zur Sichtprüfung von Blickrichtung, Brillenreflexen, Schatten und Freistellkanten. Erst mit Bestätigung entsteht ein JPEG mit 700 × 900 Pixeln in Downloads/Lichtbild. Beim Druck explizit 35 × 45 mm einstellen. Es gibt keinen eingebauten Upload. Andere Apps mit Dateizugriff und vom Nutzer eingerichtete Synchronisierung liegen außerhalb der Kontrolle der App.

## Dokumente

Die energie-BKK bietet einen eigenen Foto-Upload an. Für den Führerschein verlangt die Stadt Trier ein aktuelles biometrietaugliches Foto. Die App liefert eine technische Vorprüfung. Für den Personalausweis dient sie als Aufnahmeprobe. Der offizielle Aufnahme- und Übermittlungsweg führt über die Behörde oder einen registrierten Fotodienstleister.

- https://www.energie-bkk.de/service/elektronischen-gesundheitskarte-egk/
- https://www.trier.de/service/dienstleistungen-a-z/15763.Aemter-und-Dienststellen.html?detID=1352
- https://www.trier.de/service/dienstleistungen-a-z/15763.Aemter-und-Dienststellen.html?detID=1162
- https://www.bundesdruckerei-gmbh.de/files/dokumente/pdf/lichtbildschablone.pdf

## Bauen und Fehler melden

Projekt in Android Studio öffnen oder mit JDK 17, Android SDK 35 und Gradle 8.9 bauen: `gradle :app:assembleDebug`. Für Releases `gradle :app:assembleRelease` ausführen und die ausgerichtete APK mit Android Build Tools / apksigner und dem projektspezifischen Keystore signieren.

Bei Kamerafehlern bitte Modell, Android-Version, Kameraseite, Profil und angezeigten Hinweis angeben.
