## Tablas completas — Descripción de cada campo

---

### CAPA 1 — Autenticación

---

#### `users`
**Propósito:** Única fuente de autenticación para todos los tipos de usuario del sistema (institucionales y tutores).

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único generado automáticamente |
| `email` | VARCHAR(255) | Correo electrónico, credencial de acceso. UNIQUE |
| `password_hash` | VARCHAR(255) | Contraseña encriptada con bcrypt. Nunca texto plano |
| `name` | VARCHAR(255) | Nombre de display temporal hasta implementar `user_profiles` |
| `account_type` | ENUM | `INSTITUTIONAL` = web (coordinator/teacher), `TUTOR` = app móvil |
| `is_active` | BOOLEAN | FALSE bloquea el acceso sin eliminar el registro |
| `last_login_at` | TIMESTAMP | Último inicio de sesión. Útil para detectar cuentas inactivas |
| `created_at` | TIMESTAMP | Fecha de creación del registro |
| `updated_at` | TIMESTAMP | Última modificación del registro |

---

#### `user_preferences`
**Propósito:** Configuración de comportamiento del usuario. Separada de `users` porque son datos de preferencia, no de autenticación, y escalarán con más campos en el futuro (modo lotus, tema visual, etc.).

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `user_id` | CUID | FK → users.id. UNIQUE, relación 1:1 estricta |
| `locale` | VARCHAR(10) | Idioma preferido en estándar BCP 47. ej: `es`, `es-PE`, `en-US` |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

### CAPA 2 — RBAC

---

#### `roles`
**Propósito:** Catálogo de roles del sistema web institucional. Solo 3 valores reales: `SUPERADMIN`, `COORDINATOR`, `TEACHER`. El tutor no tiene rol aquí, se maneja por `account_type`.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `name` | VARCHAR(50) | Nombre del rol. UNIQUE. ej: `SUPERADMIN`, `COORDINATOR`, `TEACHER` |
| `description` | TEXT | Descripción legible del rol para documentación interna |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `permissions`
**Propósito:** Catálogo atómico de permisos con patrón `recurso:accion`. Permite control granular de qué puede hacer cada rol.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `name` | VARCHAR(100) | Nombre del permiso. UNIQUE. ej: `students:create`, `words:read` |
| `description` | TEXT | Descripción del permiso para documentación |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `role_permissions`
**Propósito:** Pivot N:M entre roles y permisos. Un rol tiene muchos permisos, un permiso puede pertenecer a muchos roles.

| Campo | Tipo | Descripción |
|---|---|---|
| `role_id` | CUID | FK → roles.id. Parte de PK compuesta |
| `permission_id` | CUID | FK → permissions.id. Parte de PK compuesta |

---

#### `user_roles`
**Propósito:** Pivot N:M entre usuarios y roles. Incluye trazabilidad de quién asignó el rol y cuándo.

| Campo | Tipo | Descripción |
|---|---|---|
| `user_id` | CUID | FK → users.id. Parte de PK compuesta |
| `role_id` | CUID | FK → roles.id. Parte de PK compuesta |
| `assigned_at` | TIMESTAMP | Cuándo se asignó el rol |
| `assigned_by` | CUID | FK → users.id. Quién realizó la asignación. NULLABLE si es el sistema |

---

### CAPA 3 — Estructura Institucional

---

#### `institutions`
**Propósito:** Representa cada centro educativo registrado en la plataforma. Creadas exclusivamente por SUPERADMIN.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `name` | VARCHAR(150) | Nombre oficial de la institución |
| `slug` | VARCHAR(150) | Versión URL-friendly del nombre. UNIQUE. ej: `colegio-san-jose` |
| `ruc` | VARCHAR(20) | Número de identificación fiscal. UNIQUE. NULLABLE |
| `address` | TEXT | Dirección física de la institución |
| `phone` | VARCHAR(20) | Teléfono de contacto. NULLABLE |
| `logo_url` | TEXT | URL del logo almacenado en storage. NULLABLE |
| `is_active` | BOOLEAN | FALSE desactiva la institución sin eliminarla |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `institution_members`
**Propósito:** Pivot N:M entre usuarios e instituciones. Define qué usuarios pertenecen a qué institución y en qué rol. UNIQUE en `(user_id, institution_id)` garantiza que un usuario no se duplica en la misma institución.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | PK surrogate para referencias externas como `sections.teacher_id` |
| `institution_id` | CUID | FK → institutions.id |
| `user_id` | CUID | FK → users.id |
| `role_id` | CUID | FK → roles.id. Rol del usuario dentro de esta institución |
| `joined_at` | TIMESTAMP | Fecha en que se incorporó a la institución |
| `left_at` | TIMESTAMP | Fecha de salida. NULLABLE. Permite historial sin borrar |
| `created_at` | TIMESTAMP | Fecha de creación del registro |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `grades`
**Propósito:** Grados académicos dentro de una institución. Para nivel inicial: "3 años", "4 años", "5 años". Los rangos de edad permiten validar que el estudiante tenga la edad correcta para el grado.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `institution_id` | CUID | FK → institutions.id. Cada grado pertenece a una institución |
| `name` | VARCHAR(50) | Nombre del grado. ej: `3 años`, `Kínder`, `Pre-Kínder` |
| `age_range_min` | SMALLINT | Edad mínima en años para este grado. ej: 3 |
| `age_range_max` | SMALLINT | Edad máxima en años para este grado. ej: 4 |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `sections`
**Propósito:** Secciones dentro de un grado. ej: Grado "3 años" → Secciones "A", "B", "Estrellitas". Cada sección tiene exactamente un profesor asignado via `teacher_id`.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `grade_id` | CUID | FK → grades.id. La sección pertenece a un grado |
| `teacher_id` | CUID | FK → institution_members.id. Profesor asignado. UNIQUE garantiza que un profesor no cubre dos secciones |
| `name` | VARCHAR(10) | Nombre de la sección. ej: `A`, `B`, `Estrellitas` |
| `capacity` | SMALLINT | Cupo máximo de estudiantes. DEFAULT 25 |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

### CAPA 4 — Estudiantes

---

#### `students`
**Propósito:** Perfil del niño o estudiante. Cubre dos tipos: institucional (registrado por coordinator, vinculado a sección) y freemium (registrado por tutor desde la app móvil, sin institución). Datos sensibles de menores, acceso restringido.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `institution_id` | CUID | FK → institutions.id. NULL si es freemium. Desnormalización controlada para queries de seguridad |
| `section_id` | CUID | FK → sections.id. NULL si es freemium. Sección fija por año escolar |
| `enrollment_code` | VARCHAR(50) | Código de matrícula generado por el sistema. UNIQUE. NULL si es freemium |
| `first_name` | VARCHAR(100) | Nombre del niño |
| `last_name` | VARCHAR(100) | Apellido del niño |
| `birth_date` | DATE | Fecha de nacimiento. Valida edad vs grado |
| `gender` | ENUM | `MALE`, `FEMALE`, `OTHER`. NULLABLE |
| `photo_url` | TEXT | URL de la foto del niño. Solo si `has_photo_consent = TRUE` |
| `has_photo_consent` | BOOLEAN | Consentimiento explícito del tutor para almacenar foto. DEFAULT FALSE |
| `student_type` | ENUM | `INSTITUTIONAL` = registrado por coordinator, `FREEMIUM` = registrado por tutor desde app |
| `is_active` | BOOLEAN | FALSE retira al estudiante sin borrar su historial |
| `registered_by` | CUID | FK → users.id. Coordinator si institucional, tutor si freemium |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

### CAPA 5 — Palabras

---

#### `words`
**Propósito:** Catálogo global de palabras compartido por toda la plataforma. Ninguna institución ni profesor posee las palabras, las seleccionan y asignan. Incluye soporte multiidioma y niveles de dificultad numéricos y etiquetados.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `text` | VARCHAR(100) | La palabra en sí. ej: `gato`, `casa`, `árbol` |
| `locale` | VARCHAR(10) | Idioma de la palabra en BCP 47. ej: `es`, `en`. Independiente del locale del usuario |
| `difficulty_level` | SMALLINT | Nivel numérico de dificultad del 1 al 5. Permite filtrado y ordenamiento |
| `difficulty_label` | VARCHAR(50) | Etiqueta legible del nivel. ej: `INICIAL`, `BÁSICO`, `INTERMEDIO`, `AVANZADO`, `EXPERTO` |
| `image_url` | TEXT | URL de imagen de apoyo visual para el niño. NULLABLE |
| `audio_url` | TEXT | URL de audio con la pronunciación. NULLABLE |
| `is_active` | BOOLEAN | FALSE desactiva la palabra sin eliminarla del catálogo |
| `created_by` | CUID | FK → users.id. NULLABLE si fue cargada por el sistema o superadmin |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `word_assignments`
**Propósito:** Registro de palabras que el profesor envía desde la web hacia la app móvil. Puede ser para toda una sección o para un estudiante específico. El CHECK constraint garantiza exclusión mutua: `section_id` y `student_id` no pueden ser ambos NULL ni ambos tener valor simultáneamente.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `word_id` | CUID | FK → words.id. La palabra asignada |
| `assigned_by` | CUID | FK → users.id. El profesor que realiza la asignación |
| `section_id` | CUID | FK → sections.id. NULLABLE. Asignación a toda la sección |
| `student_id` | CUID | FK → students.id. NULLABLE. Asignación a estudiante específico |
| `status` | ENUM | `PENDING` recién creada, `ACTIVE` visible en app, `COMPLETED` finalizada, `CANCELLED` anulada por profesor, `EXPIRED` venció sin completarse |
| `scheduled_at` | TIMESTAMP | Cuándo debe aparecer en la app. NULLABLE para disponibilidad inmediata |
| `expires_at` | TIMESTAMP | Cuándo deja de estar disponible. NULLABLE sin vencimiento |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

### CAPA 6 — Auditoría

---

#### `audit_logs`
**Propósito:** Registro inmutable de toda acción relevante en el sistema. Solo INSERT, nunca UPDATE ni DELETE. Acceso exclusivo de SUPERADMIN. Cubre cambios en datos, accesos y eventos críticos.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `table_name` | VARCHAR(100) | Nombre de la tabla afectada. ej: `students`, `word_assignments` |
| `record_id` | CUID | ID del registro modificado en la tabla afectada |
| `action` | ENUM | `INSERT`, `UPDATE`, `DELETE`, `LOGIN` |
| `old_values` | JSONB | Estado completo del registro antes del cambio. NULL en INSERT |
| `new_values` | JSONB | Estado completo del registro después del cambio. NULL en DELETE |
| `performed_by` | CUID | FK → users.id. NULLABLE si es proceso automático del sistema |
| `ip_address` | INET | Dirección IP de origen de la acción. NULLABLE |
| `performed_at` | TIMESTAMP | Fecha y hora exacta de la acción |

---

### CAPA 7 — Tutores

---

#### `tutor_profiles`
**Propósito:** Datos específicos del tutor o padre registrado desde la app móvil. Sigue el mismo patrón que `user_profiles`: la autenticación vive en `users`, los datos de perfil viven aquí. Relación 1:1 estricta.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `user_id` | CUID | FK → users.id. UNIQUE. Relación 1:1 con users donde `account_type = TUTOR` |
| `phone` | VARCHAR(20) | Teléfono de contacto del tutor. NULLABLE |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `tutor_students`
**Propósito:** Pivot N:M entre tutores y estudiantes. Un tutor puede tener varios hijos (perfil tipo Netflix). Un estudiante puede tener más de un tutor (padre y madre). El campo `relationship` define el vínculo y `is_primary` identifica el contacto principal.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | PK surrogate |
| `tutor_profile_id` | CUID | FK → tutor_profiles.id |
| `student_id` | CUID | FK → students.id |
| `relationship` | ENUM | `FATHER`, `MOTHER`, `GUARDIAN`. Tipo de vínculo familiar |
| `is_primary` | BOOLEAN | TRUE identifica al tutor principal de contacto. DEFAULT FALSE |
| `created_at` | TIMESTAMP | Fecha de creación del vínculo |

---

### CAPA 8 — Progreso y Gamificación

---

#### `ranks`
**Propósito:** Catálogo global e inmutable de los rangos del sistema. Define los 4 rangos con sus umbrales de estrellas. El backend asciende al estudiante automáticamente cuando `total_stars` supera `min_stars` del siguiente rango.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `name` | VARCHAR(100) | Nombre del rango. ej: `Semillita`, `Hoja Verde`, `Pequeño Sabio`, `Guardián del Bosque` |
| `slug` | VARCHAR(50) | Versión técnica del nombre. UNIQUE. ej: `semillita`, `guardian_del_bosque` |
| `icon` | VARCHAR(100) | Emoji o nombre del asset visual. ej: `🌱`, `🍃`, `🦉`, `🦊` |
| `min_stars` | INT | Estrellas mínimas para alcanzar este rango. ej: 0, 10, 20, 30 |
| `max_stars` | INT | Estrellas máximas del rango. NULL en el último rango |
| `order` | SMALLINT | Posición del rango en la jerarquía. UNIQUE. 1 = menor, 4 = mayor |
| `created_at` | TIMESTAMP | Fecha de creación |

---

#### `student_progress`
**Propósito:** Estado global del estudiante en la plataforma. Es el "perfil de juego" del niño. Se crea automáticamente al registrar un estudiante y se actualiza cada vez que completa una sesión de juego. Es la tabla más consultada desde la app móvil.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `student_id` | CUID | FK → students.id. UNIQUE. Relación 1:1 estricta |
| `coins_balance` | INT | Saldo actual de monedas disponibles para gastar. DEFAULT 0 |
| `total_stars` | INT | Total histórico acumulado de estrellas. Nunca decrementa. DEFAULT 0 |
| `current_rank_id` | CUID | FK → ranks.id. Rango actual. NULLABLE hasta ganar la primera estrella |
| `words_completed` | INT | Total de palabras completadas con acierto históricamente. DEFAULT 0 |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_at` | TIMESTAMP | Última modificación |

---

#### `trophies`
**Propósito:** Catálogo global de trofeos desbloqueables. Define los hitos del sistema: Primera Estrella, Copa Bronce, Copa Plata, Copa Oro, Maestro de Palabras. El backend los otorga automáticamente cuando se cumple la condición.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `name` | VARCHAR(100) | Nombre del trofeo. UNIQUE. ej: `Primera Estrella`, `Copa Bronce` |
| `description` | TEXT | Descripción motivacional para el niño. NULLABLE |
| `icon` | VARCHAR(100) | Emoji o nombre del asset. ej: `⭐`, `🥉`, `🥈`, `🥇` |
| `condition_type` | ENUM | `STARS_REACHED` por estrellas, `WORDS_COMPLETED` por palabras, `STREAK_DAYS` por días seguidos |
| `condition_value` | INT | Valor de la condición. ej: 1 estrella, 10 estrellas, 50 palabras |
| `created_at` | TIMESTAMP | Fecha de creación |

---

#### `student_trophies`
**Propósito:** Pivot N:M entre estudiantes y trofeos. Registra exactamente qué trofeos ha desbloqueado cada estudiante y cuándo. PK compuesta garantiza que un estudiante no recibe el mismo trofeo dos veces.

| Campo | Tipo | Descripción |
|---|---|---|
| `student_id` | CUID | FK → students.id. Parte de PK compuesta |
| `trophy_id` | CUID | FK → trophies.id. Parte de PK compuesta |
| `unlocked_at` | TIMESTAMP | Fecha y hora exacta en que se desbloqueó el trofeo |

---

#### `student_dictionary`
**Propósito:** Cofre de palabras personal del estudiante. Solo almacena palabras que el niño completó exitosamente. El campo `first_letter` permite agrupar por letra del abecedario sin recalcular en cada consulta. UNIQUE en `(student_id, word_id)` evita duplicados.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `student_id` | CUID | FK → students.id |
| `word_id` | CUID | FK → words.id. La palabra dominada |
| `first_letter` | CHAR(1) | Primera letra de la palabra en mayúscula. ej: `G` para "gato". Desnormalización controlada para agrupación alfabética eficiente |
| `mastered_at` | TIMESTAMP | Fecha en que el estudiante dominó la palabra por primera vez |

---

#### `game_session_summaries`
**Propósito:** Resumen estructurado de cada sesión de juego en PostgreSQL. Permite al docente consultar reportes con JOINs rápidos sin ir a MongoDB. El campo `mongo_session_id` es el puente hacia el documento de métricas detalladas en MongoDB donde viven los datos específicos de cada tipo de juego.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `student_id` | CUID | FK → students.id |
| `word_assignment_id` | CUID | FK → word_assignments.id. NULLABLE si el estudiante freemium juega sin asignación |
| `word_id` | CUID | FK → words.id. La palabra jugada en esta sesión |
| `game_type` | ENUM | `OCR_SCAN` escaneo de letras, `SPEECH_SPELL` deletreo por voz |
| `mongo_session_id` | VARCHAR(24) | ObjectId del documento en MongoDB con métricas detalladas del juego |
| `attempts` | SMALLINT | Total de intentos realizados en la sesión |
| `coins_earned` | SMALLINT | Monedas ganadas. Se suman a `student_progress.coins_balance` |
| `stars_earned` | SMALLINT | Estrellas ganadas 0 a 3. Se suman a `student_progress.total_stars` |
| `duration_seconds` | SMALLINT | Duración total de la sesión en segundos |
| `status` | ENUM | `COMPLETED` terminó con éxito, `FAILED` agotó intentos, `ABANDONED` salió sin terminar |
| `played_at` | TIMESTAMP | Fecha y hora de la sesión |

---

### CAPA 9 — Mascotas y Tienda

---

#### `pet_catalog`
**Propósito:** Catálogo global de mascotas disponibles para comprar. Las mascotas se desbloquean por nivel: nivel 0 ofrece 3 mascotas comunes para elegir 1, nivel 10 desbloquea la siguiente, etc. La rareza aumenta con el nivel requerido.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `name` | VARCHAR(100) | Nombre de la mascota en el catálogo. ej: `Firulais`, `Dragoncito Rex` |
| `species` | VARCHAR(50) | Especie de la mascota. ej: `DOG`, `CAT`, `DRAGON`, `BUNNY` |
| `description` | TEXT | Descripción para mostrar al niño antes de comprar. NULLABLE |
| `image_url` | TEXT | URL del asset visual de la mascota. NULLABLE |
| `coin_price` | INT | Precio en monedas para adquirirla |
| `min_level_required` | INT | Nivel mínimo del estudiante para desbloquear esta mascota. DEFAULT 0 |
| `rarity` | ENUM | `COMMON` nivel 0, `RARE` nivel 10, `EPIC` nivel 20, `LEGENDARY` nivel 30+ |
| `is_active` | BOOLEAN | FALSE oculta la mascota del catálogo sin eliminarla |
| `created_at` | TIMESTAMP | Fecha de creación |

---

#### `student_pets`
**Propósito:** Mascotas que posee cada estudiante. Cada fila es una mascota viva con nombre personalizado y estados propios de hambre y felicidad. Un estudiante puede tener varias mascotas según su nivel, cada una con su propio perfil de estado.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `student_id` | CUID | FK → students.id |
| `pet_catalog_id` | CUID | FK → pet_catalog.id. Qué tipo de mascota es |
| `custom_name` | VARCHAR(50) | Nombre que le puso el niño a su mascota específica |
| `hunger_level` | SMALLINT | Nivel de hambre de 0 a 100. 0 = muerta de hambre, 100 = satisfecha. Decrementa con el tiempo |
| `happiness_level` | SMALLINT | Nivel de felicidad de 0 a 100. 0 = muy triste, 100 = muy feliz. Decrementa con el tiempo |
| `is_active` | BOOLEAN | TRUE = mascota viva y activa |
| `acquired_at` | TIMESTAMP | Fecha en que el niño compró esta mascota |
| `last_fed_at` | TIMESTAMP | Última vez que fue alimentada. NULLABLE si nunca fue alimentada |
| `created_at` | TIMESTAMP | Fecha de creación del registro |
| `updated_at` | TIMESTAMP | Última modificación de estados |

---

#### `accessory_catalog`
**Propósito:** Catálogo global de accesorios y alimentos disponibles en la tienda. Cada accesorio define cuánto restaura de hambre o felicidad y con qué especies es compatible. El campo `compatible_species` evita que se compre comida de perro para un dragón.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `name` | VARCHAR(100) | Nombre del accesorio. ej: `Galleta`, `Croquetas`, `Hueso`, `Pelota` |
| `description` | TEXT | Descripción para mostrar en tienda. NULLABLE |
| `image_url` | TEXT | URL del asset visual. NULLABLE |
| `coin_price` | INT | Precio en monedas |
| `accessory_type` | ENUM | `FOOD` alimenta, `TOY` aumenta felicidad, `DECORATION` estética sin efecto |
| `compatible_species` | TEXT | Especies compatibles separadas por coma. ej: `DOG,CAT`. Desnormalización controlada para evitar tabla pivot de baja utilidad |
| `hunger_restore` | SMALLINT | Puntos de hambre que restaura al usarlo. DEFAULT 0 |
| `happiness_restore` | SMALLINT | Puntos de felicidad que restaura al usarlo. DEFAULT 0 |
| `is_active` | BOOLEAN | FALSE oculta el accesorio de la tienda |
| `created_at` | TIMESTAMP | Fecha de creación |

---

#### `student_inventory`
**Propósito:** Inventario de accesorios que posee el estudiante. UNIQUE en `(student_id, accessory_catalog_id)` garantiza una sola fila por tipo de accesorio, la cantidad varía. Cuando `quantity` llega a 0 el ítem desaparece del inventario.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `student_id` | CUID | FK → students.id |
| `accessory_catalog_id` | CUID | FK → accessory_catalog.id |
| `quantity` | SMALLINT | Cantidad disponible de este accesorio. Decrementa al usar |
| `acquired_at` | TIMESTAMP | Primera vez que obtuvo este tipo de accesorio |
| `updated_at` | TIMESTAMP | Última vez que cambió la cantidad |

---

#### `store_transactions`
**Propósito:** Ledger inmutable de todas las transacciones de monedas del estudiante. Solo INSERT, nunca UPDATE ni DELETE. Permite reconstruir el historial completo de ganancias y gastos. `balance_after` evita recalcular el saldo sumando toda la tabla.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | CUID | Identificador único |
| `student_id` | CUID | FK → students.id |
| `transaction_type` | ENUM | `EARN_GAME` monedas ganadas jugando, `SPEND_PET` gasto en mascota, `SPEND_ACCESSORY` gasto en accesorio |
| `amount` | INT | Monto de la transacción. Positivo = ingreso, Negativo = gasto |
| `balance_after` | INT | Saldo del estudiante después de esta transacción. Desnormalización controlada para consultas de saldo histórico |
| `reference_id` | CUID | NULLABLE. ID de la sesión de juego o compra relacionada con esta transacción |
| `reference_type` | VARCHAR(50) | NULLABLE. Tipo del registro referenciado. ej: `game_session`, `pet_purchase` |
| `created_at` | TIMESTAMP | Fecha y hora de la transacción |

---

