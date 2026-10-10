package org.alphakids.app.data.repository

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.alphakids.app.data.remote.RecordedRequests
import org.alphakids.app.data.remote.assertCancellationPropagates
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataError
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.CreateChildRequest
import org.alphakids.app.parent.domain.model.PlanType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Contract tests for the tutor endpoints used by [ParentRepositoryImpl]. */
class ParentRepositoryContractTest {

    @Test
    fun getChildren_decodes_the_tutors_children() = runTest {
        val recorded = RecordedRequests()
        val repo = ParentRepositoryImpl(mockApi(recorded) { respondJson("[$STUDENT]") })

        val child = repo.getChildren().getOrThrow().single()

        assertEquals("/tutors/children", recorded.last.url.encodedPath)
        assertEquals("Ana Pérez", child.name)
        assertEquals("VERIFIED", child.verificationStatus)
    }

    @Test
    fun a_parent_without_children_is_a_success_with_an_empty_list() = runTest {
        val repo = ParentRepositoryImpl(mockApi { respondJson("[]") })

        assertEquals(emptyList(), repo.getChildren().getOrThrow())
    }

    @Test
    fun getChildren_failures_carry_the_reason_instead_of_an_empty_list() = runTest {
        assertEquals(
            DataError.Server(500),
            ParentRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) }).getChildren().dataError,
        )
        assertEquals(
            DataError.Network,
            ParentRepositoryImpl(mockApi { throw IOException("offline") }).getChildren().dataError,
        )
        assertEquals(
            DataError.Timeout,
            ParentRepositoryImpl(mockApi { throw HttpRequestTimeoutException("https://api.test", 30_000) })
                .getChildren().dataError,
        )
    }

    @Test
    fun createChild_returns_the_created_profile() = runTest {
        val recorded = RecordedRequests()
        val repo = ParentRepositoryImpl(mockApi(recorded) { respondJson(STUDENT, HttpStatusCode.Created) })

        val created = repo.createChild(CreateChildRequest("Ana", "Pérez")).getOrThrow()

        assertEquals(HttpMethod.Post, recorded.last.method)
        assertEquals("stu_1", created.id)
        assertEquals("FREEMIUM", created.studentType)
    }

    @Test
    fun createChild_rejected_by_validation_is_a_validation_error() = runTest {
        val repo = ParentRepositoryImpl(mockApi {
            respondJson(
                """{"statusCode":400,"message":"Validation failed","timestamp":"2026-10-10T00:00:00.000Z","path":"/tutors/children"}""",
                HttpStatusCode.BadRequest,
            )
        })

        assertEquals(DataError.Validation("Validation failed"), repo.createChild(CreateChildRequest("", "")).dataError)
    }

    @Test
    fun deleteChild_succeeds_on_2xx_and_reports_a_missing_child() = runTest {
        val recorded = RecordedRequests()
        val ok = ParentRepositoryImpl(mockApi(recorded) { respondJson("{}") })

        assertTrue(ok.deleteChild("stu_1").isSuccess)
        assertEquals(HttpMethod.Delete, recorded.last.method)
        assertEquals("/students/stu_1", recorded.last.url.encodedPath)
        assertEquals(
            DataError.NotFound,
            ParentRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.NotFound) }).deleteChild("stu_1").dataError,
        )
    }

    @Test
    fun getChildStats_and_institutions_map_failures() = runTest {
        val down = ParentRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.ServiceUnavailable) })

        assertEquals(DataError.Server(503), down.getChildStats("stu_1").dataError)
        assertEquals(DataError.Server(503), down.getPublicInstitutions().dataError)
    }

    @Test
    fun operations_without_a_backend_endpoint_say_so() = runTest {
        val repo = ParentRepositoryImpl(mockApi { error("no request expected") })

        assertEquals(DataError.Unsupported, repo.getRecentActivity().dataError)
        assertEquals(DataError.Unsupported, repo.getFAQs().dataError)
        assertEquals(DataError.Unsupported, repo.submitContactForm(ContactForm("Ana", "ana@correo.com", "Hola")).dataError)
        assertEquals(PlanType.FREE, repo.getSubscription().getOrThrow().planType)
    }

    @Test
    fun cancellation_propagates_from_every_backed_call() = runTest {
        assertCancellationPropagates { ParentRepositoryImpl(it).getChildren() }
        assertCancellationPropagates { ParentRepositoryImpl(it).createChild(CreateChildRequest("Ana", "Pérez")) }
        assertCancellationPropagates { ParentRepositoryImpl(it).deleteChild("stu_1") }
        assertCancellationPropagates { ParentRepositoryImpl(it).getChildStats("stu_1") }
        assertCancellationPropagates { ParentRepositoryImpl(it).getPublicInstitutions() }
    }

    private companion object {
        const val STUDENT = """
            {"id":"stu_1","firstName":"Ana","lastName":"Pérez","avatarUrl":null,"isActive":true,
             "verificationStatus":"VERIFIED","studentType":"FREEMIUM","registeredById":"tut_1",
             "institutionId":null,"sectionId":null,
             "createdAt":"2026-10-01T00:00:00.000Z","updatedAt":"2026-10-01T00:00:00.000Z"}
        """
    }
}
