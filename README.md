# PlotsX

PlotsX to zaawansowany system zarządzania działkami dla serwerów Minecraft. Plugin pozwala graczom na zakładanie i zarządzanie własnymi działkami z pełnym systemem ochrony i flag.

## Funkcje

- 🏠 System działek z GUI do potwierdzania zakładania
- 🛡️ Zaawansowany system ochrony terenu
- 👥 System współwłaścicieli działek
- ⚙️ Konfigurowalne flagi działek (PvP, ochrona przed mobami, interakcje)
- 🔄 Integracja z CoreProtect
- 🔑 Opcjonalna integracja z LuckPerms
- 🌐 Wsparcie dla Folia

## Wymagania

- Minecraft 1.20.2 lub nowszy
- Paper/Spigot/Sponge
- CoreProtect (wymagane)
- LuckPerms (opcjonalne)

## Instalacja

1. Pobierz najnowszą wersję pluginu z sekcji Releases
2. Skopiuj plik .jar do folderu `plugins` na serwerze
3. Uruchom serwer
4. Plugin automatycznie utworzy pliki konfiguracyjne

## Komendy

- `/plot` lub `/claim` - Załóż nową działkę
- `/plot info` - Wyświetl informacje o swojej działce
- `/plot delete` - Usuń swoją działkę
- `/plot add <gracz>` - Dodaj współwłaściciela działki
- `/plot remove <gracz>` - Usuń współwłaściciela działki
- `/plot flags` - Zarządzaj flagami działki

## Konfiguracja

Wszystkie ustawienia można zmienić w pliku `config.yml`:

```yaml
default-plot-radius: 16
plot:
  max-co-owners: 5
  allow-overlap: false
  require-deletion-confirmation: true
protection:
  protect-pvp: true
  protect-mob-damage: true
  allow-basic-interactions: true
```

## Rozwój

Aby skompilować projekt:

```bash
mvn clean package
```

## Licencja

Ten projekt jest licencjonowany na warunkach MIT - zobacz plik [LICENSE](LICENSE) po szczegóły.

## Autor

Twórca: Wupas94

## Wsparcie

Jeśli napotkasz problemy lub masz sugestie, utwórz issue w repozytorium GitHub. 
