# ParentRegistrationFlow

**Inicio:** tap en "Crear cuenta" desde ParentLogin, o primer acceso a la app.

**Pantallas involucradas:** ParentRegister → (validación) → Asistente de configuración para padres (intro) → ChildCreationWizard (pasos 1–5) → pantalla de bienvenida final.

**Decisiones:** ¿el usuario completa institución educativa? (opcional) · ¿acepta Términos y Condiciones? (bloqueante).

**Estados:** formulario incompleto, contraseñas no coinciden (⏳ sin mensaje visual de referencia), checkbox sin marcar.

**Resultado esperado:** cuenta de padre creada + al menos un perfil de niño creado + redirección a AdventureHome.

**Casos alternativos:** usuario ya tiene cuenta → redirige a ParentLogin vía link · usuario abandona en medio del wizard → "Saltar" en la pantalla final permite continuar sin ver el resumen completo de recompensas (⏳ confirmar si "Saltar" también permite omitir pasos intermedios).
