# ParentLogin

**Objetivo:** autenticar a un padre/tutor existente.

**Usuario:** padre/tutor con cuenta.

**Contenido:** mascota Alphi saludando, título "¡Bienvenido de nuevo!", subtítulo, badge "Acceso seguro y protegido", formulario (correo, contraseña con toggle de visibilidad), link "¿Olvidaste tu contraseña?", CTA "Continuar", link "Crear cuenta", bloque de confianza ("Tu hijo está en buenas manos").

**Secciones:** Header narrativo · Card de formulario (Layer 3, Hero Shadow) · Footer de confianza.

**Componentes:** `Inputs` (text, password), `Buttons` (Primary CTA, Text Link), `Headers`.

**Estados:** Default, error de validación (⏳ pendiente, no observado), loading en submit (⏳ pendiente).

**Navegación:** ← atrás (a Splash u origen previo) · → `ChildProfileSelector` (login exitoso) · → `ParentRegister` (vía "Crear cuenta") · → recuperación de contraseña (⏳ flujo no documentado, no incluido en el set).

**Eventos:** `login_attempt`, `login_success`, `login_failure`, `forgot_password_tap`.

**Analytics:** tasa de conversión de login, tiempo en pantalla.
