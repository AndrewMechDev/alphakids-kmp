# ParentRegister

**Objetivo:** crear una nueva cuenta de padre/tutor.

**Usuario:** padre/tutor nuevo.

**Contenido:** mascota Alphi saludando con letras decorativas (A, B), título "¡Comencemos juntos!", badge "Seguro, confiable y protegido", formulario (nombre completo, correo, teléfono, contraseña con medidor de fortaleza, confirmar contraseña, institución educativa opcional), checkbox de aceptación de Términos y Política de Privacidad, CTA "Crear mi cuenta", link "Iniciar sesión".

**Secciones:** Header narrativo · Card de formulario extenso (Layer 3).

**Componentes:** `Inputs` (text, email, phone, password con strength meter, select institución, checkbox), `Buttons`.

**Estados:** Fortaleza de contraseña (Débil/Media/Fuerte — observado "Media" con barra parcial amarilla), checkbox sin marcar (⏳ confirmar si bloquea el CTA), error de validación (⏳ pendiente).

**Navegación:** ← atrás · → Onboarding parental / `ChildCreationWizard` (tras registro exitoso) · → `ParentLogin` (vía "Iniciar sesión").

**Eventos:** `register_attempt`, `register_success`, `terms_accepted`, `institution_selected`.

**Analytics:** tasa de completitud de formulario, campo con mayor abandono.
