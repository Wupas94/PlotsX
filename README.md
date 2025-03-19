# PlotsX

PlotsX to zaawansowany plugin do zarządzania działkami na serwerach Minecraft.

## Funkcje

- Intuicyjny system tworzenia i zarządzania działkami
- Elastyczna konfiguracja wymiarów działek
- System uprawnień i współwłaścicieli
- Ochrona działek przed griefingiem
- System flag (PvP, obrażenia mobów, itp.)
- Integracja z popularnymi pluginami (WorldEdit, CoreProtect, Vault)
- System backupów i przywracania danych
- Asynchroniczne operacje dla lepszej wydajności
- Metryki i monitorowanie wydajności
- Wielojęzyczne wsparcie

## Instalacja

1. Pobierz najnowszą wersję pluginu z [releases](https://github.com/wupas94/plotsx/releases)
2. Umieść plik JAR w folderze `plugins` na serwerze
3. Uruchom/zrestartuj serwer
4. Skonfiguruj plugin według potrzeb w pliku `config.yml`

## Konfiguracja

Główne ustawienia w pliku `config.yml`:

```yaml
plot:
  # Domyślne wymiary działki (w blokach)
  default-width: 32
  default-height: 32
  
  # Minimalne i maksymalne wymiary działki
  min-width: 16
  min-height: 16
  max-width: 128
  max-height: 128
  
  # Odstęp między działkami
  spacing: 2
  
  # Ustawienia zajmowania działek
  claim:
    cost: 1000
    max-per-player: 3
```

## Komendy

- `/plot claim` - Zajmij działkę
- `/plot delete` - Usuń działkę
- `/plot info [gracz]` - Pokaż informacje o działce
- `/plot list [gracz]` - Pokaż listę działek
- `/plot tp <id>` - Teleportuj się do działki
- `/plot set <flaga> <wartość>` - Ustaw flagę
- `/plot trust <gracz>` - Dodaj zaufanego gracza
- `/plot untrust <gracz>` - Usuń zaufanego gracza
- `/plot add <gracz>` - Dodaj współwłaściciela
- `/plot remove <gracz>` - Usuń współwłaściciela
- `/plot home [nazwa]` - Teleportuj się do domu
- `/plot sethome [nazwa]` - Ustaw punkt domowy
- `/plot delhome <nazwa>` - Usuń punkt domowy

## Uprawnienia

- `plotsx.admin` - Dostęp do wszystkich komend administracyjnych
- `plotsx.claim` - Możliwość zajmowania działek
- `plotsx.delete` - Możliwość usuwania działek
- `plotsx.info` - Możliwość sprawdzania informacji o działkach
- `plotsx.list` - Możliwość przeglądania listy działek
- `plotsx.tp` - Możliwość teleportacji do działek
- `plotsx.set` - Możliwość ustawiania flag
- `plotsx.trust` - Możliwość zarządzania zaufanymi graczami
- `plotsx.add` - Możliwość zarządzania współwłaścicielami
- `plotsx.home` - Możliwość korzystania z punktów domowych

## API

Plugin udostępnia API dla innych pluginów:

```kotlin
val plotsX = server.pluginManager.getPlugin("PlotsX") as PlotsX
val plotManager = plotsX.plotManager

// Tworzenie działki
val result = plotManager.createPlot(player, location, width = 32, height = 32)

// Pobieranie działki
val plot = plotManager.getPlotAt(location)

// Sprawdzanie uprawnień
if (plot?.hasAccess(player.uniqueId) == true) {
    // Gracz ma dostęp do działki
}
```

## Współpraca

1. Forkuj repozytorium
2. Stwórz branch z nową funkcją (`git checkout -b feature/nazwa`)
3. Commituj zmiany (`git commit -am 'Dodano nową funkcję'`)
4. Pushuj do brancha (`git push origin feature/nazwa`)
5. Stwórz Pull Request

## Licencja

Ten projekt jest licencjonowany pod MIT License - zobacz plik [LICENSE](LICENSE) po szczegóły.

## Autor

Wupas94 - [GitHub](https://github.com/wupas94)

## Wsparcie

Jeśli masz problemy lub pytania:

1. Sprawdź [Wiki](https://github.com/wupas94/plotsx/wiki)
2. Zgłoś problem w [Issues](https://github.com/wupas94/plotsx/issues)
3. Dołącz do naszego [Discorda](https://discord.gg/plotsx) 