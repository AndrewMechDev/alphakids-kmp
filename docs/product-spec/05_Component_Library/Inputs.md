# Inputs.md

**Propósito:** captura de datos en formularios (registro, login, perfil de niño).

**Variantes:**
- Text input — label arriba, ícono izquierdo, placeholder.
- Password input — ícono de ojo (mostrar/ocultar) + barra de fortaleza (Débil/Media/Fuerte).
- Select/Dropdown — ej. institución educativa (opcional).
- Date picker — ej. fecha de nacimiento (opcional).
- Chip selector (single-select pill group) — ej. edad: "3 años" / "4 años" / "5 años"...
- Checkbox — aceptación de Términos y Política de Privacidad (⏳ no listado como componente propio en el spec original; se sugiere formalizarlo aquí).

**Estados:** Empty/placeholder, Focused (borde Primary Blue), Filled, Error (⏳ pendiente, no observado), Disabled.

**Uso permitido:** un input por línea, label siempre visible (no usar placeholder como único label).

**Uso prohibido:** ocultar el label en estado focused.

**Navegación asociada:** ParentRegister, ParentLogin, ChildCreationWizard paso 1.
