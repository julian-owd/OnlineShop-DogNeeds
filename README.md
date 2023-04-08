# DogNeeds

## Setup
### Frontend
Ordner in Konsole öffnen
```
npm install
```

### Backend
Ordner in einer IDE (getestet mit __VSCode / IntelliJ__) öffnen.<br />
Die __Maven__-Dependencies müssen heruntergeladen werden, meist geschieht dies beim Öffnen automatisch.<br />
Vor dem Start muss eine Datenbank namens __"DogNeeds"__ vorhanden sein!<br />
Test-Daten sind über die Datei __dogneeds.sql__ zu importieren!<br />
Es muss eine Internet-Verbindung herrschen, da sonst keine Bilder von Firebase heruntergeladen werden können!

## Start
### Frontend
```
npm run dev
```

Um die Svelte-App auf dem Handy zu öffnen, führe Folgendes beim Starten aus:
```
npm run dev -- --host
```

### Backend
Das Projekt als normales Java-Projekt ausführen, nachdem die Maven-Imports abgeschlossen sind.<br />
Die Hauptklasse ist __DogNeedsApplication.java__.