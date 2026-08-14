# ChildCreationWizard

**Objetivo:** guiar al padre y al niño en la creación del primer perfil infantil, en 6 pasos (1 opcional).

**Usuario:** padre/tutor en primera configuración (o creando un perfil adicional).

**Contenido / Pasos documentados:**

1. **Intro parental** *(asistente-configuracion-padres, Paso 1 de 6)* — mascota Alphi presenta la misión ("empoderar a cada niño para que aprenda, crezca y brille"), 5 tarjetas de valor (Aprendizaje Personalizado, Aprende Jugando, Entorno Seguro y Confiable, Progreso Real, Participación Familiar), CTA "Continuar".
2. **Crear perfil del niño** *(Paso 2 de 6)* — preview de perfil en construcción (avatar + nombre + edad), input de nombre, chip selector de edad (3–7 años), date picker opcional, grid rápido de avatares, nota de privacidad, CTA "¡Comenzar su aventura!".
3. **Elegir avatar** *(Paso 3 de 6)* — preview de perfil, tabs de categoría (Animales/Exploradores/Fantasía), grid 4x2 de avatares con paginación por dots, CTA "¡Este soy yo!".
4. **Asignar institución** *(Paso 4 de 6, OPCIONAL)* — toggle "¿Tu hijo asiste a un colegio?" con chips Sí/No. Al seleccionar "Sí", se cargan instituciones activas via API. El padre selecciona institución y opcionalmente grado y sección. CTA "Continuar" / "Omitir".
5. **Elegir mascota** *(Paso 5 de 6)* — grid de 3 mascotas (Tito tortuga, Luna zorro, Drako dragón) con tags de personalidad, card de detalle de la mascota seleccionada con bio en bullets, input para nombrarla, CTA "¡Adoptar a [nombre]!".
6. **Bienvenida final** *(Paso 6 de 6, sin stepper visible)* — mascota Alphi celebrando con confetti, resumen "Este es tu equipo de aventura" (avatar del niño + mascota adoptada), recompensas iniciales (+150 Monedas, Nivel 1 Principiante). Al confirmar, se envía POST /tutors/children. Si se asignó institución → studentType INSTITUTIONAL + verificationStatus PENDING → dialog "Pendiente de verificación por el director". Si no → FREEMIUM + VERIFIED → navega a AdventureHome.

**Componentes:** `AvatarCards`, `PetCards`, `Inputs` (text, chip selector, date picker), `Buttons`, `RewardCards`, `Badges` (rank badge "Exploradora Curiosa").

**Estados:** paso activo resaltado en azul en el stepper, pasos futuros en gris, paso completado (⏳ pendiente confirmar marca visual).

**Navegación:** ← atrás entre pasos · "Saltar" disponible en la pantalla final · → `AdventureHome` o `ChildProfileSelector` tras completar (⏳ a confirmar).

**Eventos:** `wizard_step_viewed` (por paso), `profile_created`, `avatar_selected`, `pet_adopted`, `wizard_completed`, `wizard_skipped`.

**Analytics:** abandono por paso, tiempo total del wizard, mascota más elegida, avatar más elegido.
