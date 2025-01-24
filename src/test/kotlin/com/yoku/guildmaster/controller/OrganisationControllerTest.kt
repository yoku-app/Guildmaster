package com.yoku.guildmaster.controller

import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationRepository
import com.yoku.guildmaster.service.OrganisationService
import com.yoku.guildmaster.util.MockEntityUtil
import com.yoku.guildmaster.util.UUIDUtil
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import java.util.UUID
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@WebMvcTest(OrganisationController::class)
class OrganisationControllerTest(private val mockEntity: MockEntityUtil) {

    private val uuidUtil: UUIDUtil = mockk()
    private val organisationRepository: OrganisationRepository = mockk()
    private val organisationService = OrganisationService(organisationRepository,uuidUtil)

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `getOrganisationById should return 200 OK when organisation is found`() {
        val uuid = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(uuid)

        every { organisationService.getOrganisationByID(uuid.toString()) } returns organisation

        mockMvc.perform(get("/api/organisation/id/{id}", uuid.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Test Org"))
    }

    @Test
    fun `getOrganisationById should return 404 when organisation is not found`() {
        val uuid = UUID.randomUUID()

        every { organisationService.getOrganisationByID(uuid.toString()) } throws OrganisationNotFoundException("Organisation not found")

        mockMvc.perform(get("/api/organisation/id/{id}", uuid.toString()))
            .andExpect(status().isNotFound)
    }
}
