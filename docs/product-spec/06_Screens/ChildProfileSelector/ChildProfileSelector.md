# ChildProfileSelector

**Objetivo:** permitir al padre seleccionar (o crear) el perfil del niño que va a usar la app.

**Usuario:** padre/tutor con uno o más perfiles de hijos creados.

**Contenido:** header con saludo al tutor + botón "Configuración", mascota Alphi saludando, título "¿Quién está listo para aprender hoy?", mensaje de apoyo ("Cada niño tiene su propio viaje, sus logros y su historia"), grid de tarjetas de perfil (avatar + mascota + rank badge + nivel + XP bar + ranking + stats de logros/lecciones/racha), tarjeta punteada "Agregar nuevo perfil" con CTA "+ Crear nuevo niño", footer de confianza.

**Secciones:** Header · Hero narrativo · Grid de perfiles · CTA de creación · Footer.

**Componentes:** `Cards` (Profile Card, Action Card), `XPComponents` (Level Badge, XP bar), `Badges` (rank badge).

**Estados:** perfil sin seleccionar (default), menú contextual "..." por tarjeta (acciones ⏳ pendientes de definir, probablemente editar/eliminar perfil).

**Navegación:** → `AdventureHome` (al tocar un perfil) · → `ChildCreationWizard` (al tocar "Crear nuevo niño") · → `Settings` (vía ícono Configuración, ⏳ pendiente de captura).

**Eventos:** `profile_selected`, `add_profile_tap`, `settings_tap`.

**Analytics:** perfil más usado, frecuencia de creación de perfiles adicionales.
