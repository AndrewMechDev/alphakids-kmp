# ChildSelectionFlow

**Inicio:** ParentLogin exitoso, o regreso desde Settings/logout de un perfil de niño.

**Pantallas involucradas:** ChildProfileSelector → AdventureHome.

**Decisiones:** ¿cuántos perfiles existen? (0 → fuerza ChildCreationFlow; 1+ → permite selección directa).

**Estados:** ninguno seleccionado (default), perfil resaltado al tap antes de la transición.

**Resultado esperado:** AdventureHome cargado con los datos del perfil seleccionado (XP, mascota, racha, etc.).

**Casos alternativos:** padre edita/elimina un perfil vía el menú "..." (acciones no documentadas visualmente — ⏳ pendiente).
