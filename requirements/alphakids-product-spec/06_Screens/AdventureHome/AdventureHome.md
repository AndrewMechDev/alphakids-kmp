# AdventureHome

**Objetivo:** dashboard principal del niño, punto de entrada a todas las actividades de aprendizaje.

**Usuario:** niño (perfil activo seleccionado).

**Contenido:** header (avatar + saludo "¡Hola, Lucas!" + rank badge + contador de monedas + contador de estrellas + notificación), Level Badge con XP bar, 2 tarjetas hero ("Jugar", "Logros"), "Objetivo del día" (progreso + recompensa), "Racha de aprendizaje" (7 días, checks diarios + regalo), "Mi mascota" (nombre, nivel, barra de felicidad), "Mi progreso" (XP total, logros/lecciones/desafíos), banner "Siguiente actividad" con CTA "Continuar", footer de confianza, bottom navigation (Inicio/Diccionario/Tienda/Mascota/Copas).

**Secciones:** Header · Stats row (2 hero cards) · Grid 2x2 de widgets · Banner de actividad sugerida · Footer · Bottom Nav.

**Componentes:** `Headers` (Home Header), `Cards` (Hero Card, Stat Card), `ProgressBars` (XP, Objetivo, Felicidad), `XPComponents`, `CoinComponents`, `BottomNavigation`, `RewardCards` (tags +XP/+monedas/+estrellas en el banner).

**Estados:** streak del día actual no completado (ícono gris con letra del día), notificación con/sin punto rojo.

**Navegación:** → `OCRChallenge`/`SpellingChallenge` (vía "Jugar" o banner "Continuar" — ⏳ pantallas pendientes) · → `HallOfChampions` (vía "Logros") · → `WordTreasureChest` (vía nav "Diccionario", ⏳ pendiente) · → `PetMarket` (vía nav "Tienda", ⏳ pendiente) · → `PetKingdom` (vía nav "Mascota" o card "Mi mascota", ⏳ pendiente).

**Eventos:** `home_viewed`, `activity_card_tap`, `continue_activity_tap`, `nav_tab_change`.

**Analytics:** engagement por widget, tasa de clic en "Continuar", tiempo en home antes de iniciar actividad.
