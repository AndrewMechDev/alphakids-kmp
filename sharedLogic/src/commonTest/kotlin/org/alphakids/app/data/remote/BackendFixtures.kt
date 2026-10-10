package org.alphakids.app.data.remote

/**
 * JSON payloads shaped exactly like the alphakids-api responses (Prisma models
 * serialized by NestJS, including fields the client ignores such as
 * `createdAt`/`updatedAt`).
 */
object BackendFixtures {

    const val PET_CATALOG_CAT = """
        {"id":"cat_1","name":"Gatito Básico","species":"Cat","imageUrl":null,"coinPrice":100,
         "minLevelRequired":1,"isActive":true,
         "createdAt":"2026-10-01T00:00:00.000Z","updatedAt":"2026-10-01T00:00:00.000Z"}
    """

    const val PET_CATALOG_OWL = """
        {"id":"cat_3","name":"Búho Sabio","species":"Owl","imageUrl":null,"coinPrice":500,
         "minLevelRequired":2,"isActive":true,
         "createdAt":"2026-10-01T00:00:00.000Z","updatedAt":"2026-10-01T00:00:00.000Z"}
    """

    const val ACCESSORY_APPLE = """
        {"id":"acc_1","name":"Manzana","coinPrice":10,"hungerRestore":20,"happinessRestore":5,
         "compatibleSpecies":"*","isActive":true,
         "createdAt":"2026-10-01T00:00:00.000Z","updatedAt":"2026-10-01T00:00:00.000Z"}
    """

    /** GET /students/:studentId/pets — StudentPet with `include: { petCatalog: true }`. */
    const val STUDENT_PETS = """
        [{"id":"pet_1","studentId":"stu_1","petCatalogId":"cat_1","customName":"Michi",
          "hungerLevel":40,"happinessLevel":50,"isActive":true,"lastFedAt":null,
          "createdAt":"2026-10-02T00:00:00.000Z","updatedAt":"2026-10-02T00:00:00.000Z",
          "petCatalog":$PET_CATALOG_CAT}]
    """

    /** POST /students/:studentId/pets/:petId/feed — bare StudentPet, no petCatalog. */
    const val FED_PET = """
        {"id":"pet_1","studentId":"stu_1","petCatalogId":"cat_1","customName":"Michi",
         "hungerLevel":60,"happinessLevel":55,"isActive":true,
         "lastFedAt":"2026-10-09T10:00:00.000Z",
         "createdAt":"2026-10-02T00:00:00.000Z","updatedAt":"2026-10-09T10:00:00.000Z"}
    """

    /** POST /students/:studentId/store/pets/buy — new StudentPet with petCatalog. */
    const val BOUGHT_PET = """
        {"id":"pet_2","studentId":"stu_1","petCatalogId":"cat_1","customName":null,
         "hungerLevel":100,"happinessLevel":100,"isActive":true,"lastFedAt":null,
         "createdAt":"2026-10-09T10:00:00.000Z","updatedAt":"2026-10-09T10:00:00.000Z",
         "petCatalog":$PET_CATALOG_CAT}
    """

    /** POST /students/:studentId/store/accessories/buy — StudentInventory row with accessoryCatalog. */
    const val BOUGHT_ACCESSORY = """
        {"id":"inv_1","studentId":"stu_1","accessoryCatalogId":"acc_1","quantity":3,
         "createdAt":"2026-10-09T10:00:00.000Z","updatedAt":"2026-10-09T10:00:00.000Z",
         "accessoryCatalog":$ACCESSORY_APPLE}
    """

    /** GET /students/:id/inventory — mapped `{ id, quantity, accessory }` rows. */
    const val INVENTORY = """
        [{"id":"inv_1","quantity":2,"accessory":$ACCESSORY_APPLE}]
    """

    /** NestJS BadRequestException body. */
    fun badRequest(message: String) =
        """{"statusCode":400,"message":"$message","error":"Bad Request"}"""
}
