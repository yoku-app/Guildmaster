package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.util.MockEntityUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.*

@DataJpaTest
class OrganisationRepositoryTest(private val mockEntity: MockEntityUtil) {

    @Autowired
    private lateinit var organisationRepository: OrganisationRepository

    @Test
    fun `findByName should return organisation when it exists`() {
        val organisation: Organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())
        organisationRepository.save(organisation)

        val result = organisationRepository.findByName("Test Org")

        assertTrue(result.isPresent)
        assertEquals("Test Org", result.get().name)
    }

    @Test
    fun `findByName should return empty when organisation does not exist`() {
        val result = organisationRepository.findByName("Nonexistent Org")

        assertTrue(result.isEmpty)
    }
}
