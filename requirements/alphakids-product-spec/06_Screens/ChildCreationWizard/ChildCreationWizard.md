# ChildCreationWizard

**Objetivo:** guiar al padre y al niño en la creación del primer perfil infantil, en 5 pasos.

**Usuario:** padre/tutor en primera configuración (o creando un perfil adicional).

**Contenido / Pasos documentados:**

1. **Intro parental** *(asistente-configuracion-padres)* — mascota Alphi presenta la misión ("empoderar a cada niño para que aprenda, crezca y brille"), 5 tarjetas de valor (Aprendizaje Personalizado, Aprende Jugando, Entorno Seguro y Confiable, Progreso Real, Participación Familiar), CTA "Continuar".
2. **Crear perfil del niño** *(marcado "Paso 1 de 5" en el mockup — ver nota de inconsistencia)* — preview de perfil en construcción (avatar + nombre + edad), input de nombre, chip selector de edad (3–7 años), date picker opcional, grid rápido de avatares, nota de privacidad, CTA "¡Comenzar su aventura!".
3. **Elegir avatar** *(marcado "Paso 2 de 5")* — preview de perfil, tabs de categoría (Animales/Exploradores/Fantasía), grid 4x2 de avatares con paginación por dots, CTA "¡Este soy yo!".
4. **Elegir mascota** *(marcado "Paso 3 de 5")* — grid de 3 mascotas (Tito tortuga, Luna zorro, Drako dragón) con tags de personalidad, card de detalle de la mascota seleccionada con bio en bullets, input para nombrarla, CTA "¡Adoptar a [nombre]!".
5. **Bienvenida final** *(sin stepper visible, pantalla de celebración)* — mascota Alphi celebrando con confetti, resumen "Este es tu equipo de aventura" (avatar del niño + mascota adoptada), recompensas iniciales (+150 Monedas, Nivel 1 Principiante) junto a cofre abierto, 4 íconos de confianza (Entorno seguro, Aprende jugando, Progreso constante, Diversión asegurada), CTA "¡Comenzar mi aventura!", opción "Saltar" arriba a la derecha.

> ⚠️ **Inconsistencia detectada:** la pantalla "Asistente de configuración para padres" usa stepper "Paso 1 de 5" con contenido puramente informativo (misión de la app), mientras que "Creemos el perfil de tu hijo" también muestra "Paso 1 de 5" pero con captura de datos. Esto sugiere dos steppers distintos (uno parental de bienvenida + uno de creación de perfil) o un error de numeración en los mockups. **Recomendación:** definir explícitamente si son 2 wizards separados (Parent Onboarding de 1 paso + Child Creation Wizard de 5 pasos) o renumerar a un único flujo de 6 pasos antes de pasar a desarrollo.

**Componentes:** `AvatarCards`, `PetCards`, `Inputs` (text, chip selector, date picker), `Buttons`, `RewardCards`, `Badges` (rank badge "Exploradora Curiosa").

**Estados:** paso activo resaltado en azul en el stepper, pasos futuros en gris, paso completado (⏳ pendiente confirmar marca visual).

**Navegación:** ← atrás entre pasos · "Saltar" disponible en la pantalla final · → `AdventureHome` o `ChildProfileSelector` tras completar (⏳ a confirmar).

**Eventos:** `wizard_step_viewed` (por paso), `profile_created`, `avatar_selected`, `pet_adopted`, `wizard_completed`, `wizard_skipped`.

**Analytics:** abandono por paso, tiempo total del wizard, mascota más elegida, avatar más elegido.
