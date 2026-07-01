# Splash

**Objetivo:** pantalla de carga inicial que presenta la marca y prepara la siguiente experiencia (login si no hay sesión activa, o selector de perfil si ya existe sesión).

**Usuario:** cualquiera — primer momento de apertura de la app.

**Contenido:** logo AlphaKids (búho + wordmark), tagline "Aprendiendo juntos, creciendo siempre", mascota Alphi en pose de bienvenida, mensaje de carga "Preparando tu aventura educativa… Cargando tu mundo de aprendizaje", indicador de progreso tipo dots (8 puntos, 3 activos coloreados).

**Secciones:** Header de marca · Hero mascota + fondo ilustrado · Footer de estado de carga.

**Componentes:** `LoadingStates` (Splash Loading).

**Estados:** Loading (único estado observado).

**Navegación:** → `ParentLogin` (si no autenticado) o → `ChildProfileSelector` (si sesión activa). Lógica de destino a confirmar con auth/backend.

**Eventos:** `app_open`, `splash_loaded`.

**Analytics:** tiempo de carga, tasa de abandono en splash.
