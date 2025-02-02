import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationRepository
import com.yoku.guildmaster.service.OrganisationService
import com.yoku.guildmaster.util.MockEntityUtil
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class OrganisationServiceTest() {

    private val mockEntity: MockEntityUtil = MockEntityUtil()
    private val organisationRepository: OrganisationRepository = mockk()
    private val organisationService = OrganisationService(organisationRepository)


    @Test
    fun `getOrganisationByID should return organisation when valid UUID is provided`() {
        val uuid = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(uuid)

        every { organisationRepository.findById(uuid) } returns Optional.of(organisation)

        val result = organisationService.getOrganisationByID(uuid)

        assertNotNull(result)
        assertEquals(organisation, result)
        verify { organisationRepository.findById(uuid) }
    }

    @Test
    fun `getOrganisationByID should throw OrganisationNotFoundException when organisation is not found`() {
        val uuid = UUID.randomUUID()

        every { organisationRepository.findById(uuid) } returns Optional.empty()

        assertThrows(OrganisationNotFoundException::class.java) {
            organisationService.getOrganisationByID(uuid)
        }
    }

    @Test
    fun `getOrganisationByName should return organisation when name exists`() {
        val organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())

        every { organisationRepository.findByName("Test Org") } returns Optional.of(organisation)

        val result = organisationService.getOrganisationByName("Test Org")

        assertNotNull(result)
        assertEquals(organisation, result)
        verify { organisationRepository.findByName("Test Org") }
    }

    @Test
    fun `getOrganisationByName should throw OrganisationNotFoundException when organisation is not found`() {
        every { organisationRepository.findByName("Nonexistent Org") } returns Optional.empty()

        assertThrows(OrganisationNotFoundException::class.java) {
            organisationService.getOrganisationByName("Nonexistent Org")
        }
    }

    @Test
    fun `updateOrganisation should return updated organisation when valid`() {
        val userId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())

        every { organisationRepository.findById(organisation.id) } returns Optional.of(organisation)
        every { organisationRepository.save(organisation) } returns organisation

        val result = organisationService.updateOrganisation(userId, organisation)

        assertNotNull(result)
        assertEquals(organisation, result)
        verify { organisationRepository.findById(organisation.id) }
        verify { organisationRepository.save(organisation) }
    }

    @Test
    fun `updateOrganisation should throw OrganisationNotFoundException when organisation is not found`() {
        val userId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())

        every { organisationRepository.findById(organisation.id) } returns Optional.empty()

        assertThrows(OrganisationNotFoundException::class.java) {
            organisationService.updateOrganisation(userId, organisation)
        }
    }

    @Test
    fun `saveOrganisation should return saved organisation`() {
        val organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())

        every { organisationRepository.save(organisation) } returns organisation

        val result = organisationService.saveOrganisation(organisation)

        assertNotNull(result)
        assertEquals(organisation, result)
        verify { organisationRepository.save(organisation) }
    }

    @Test
    fun `deleteOrganisation should delete the organisation when valid ID is provided`() {
        val uuid = UUID.randomUUID()


        every { organisationRepository.deleteById(uuid) } just Runs

        organisationService.deleteOrganisation(uuid)

        verify { organisationRepository.deleteById(uuid) }
    }

}
