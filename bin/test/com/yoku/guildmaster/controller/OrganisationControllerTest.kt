package com.yoku.guildmaster.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.service.OrganisationService
import com.yoku.guildmaster.utils.MockEntityUtil
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(OrganisationController::class)
class OrganisationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var organisationService: OrganisationService

    private val mockEntity = MockEntityUtil()

    @Test
    fun `getOrganisation should return organisation when it exists`() {
        val orgId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(orgId)

        `when`(organisationService.getOrganisationByID(orgId)).thenReturn(organisation)

        mockMvc.perform(get("/api/v1/organisations/{id}", orgId))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(orgId.toString()))
    }

    @Test
    fun `createOrganisation should create and return new organisation`() {
        val organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())

        `when`(organisationService.saveOrganisation(organisation)).thenReturn(organisation)

        mockMvc.perform(post("/api/v1/organisations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organisation)))
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(organisation.name))
    }

    @Test
    fun `updateOrganisation should update and return organisation`() {
        val orgId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(orgId)

        `when`(organisationService.updateOrganisation(userId, organisation)).thenReturn(organisation)

        mockMvc.perform(put("/api/v1/organisations/{id}", orgId)
            .header("X-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organisation)))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(orgId.toString()))
    }

    @Test
    fun `deleteOrganisation should return no content`() {
        val orgId = UUID.randomUUID()

        mockMvc.perform(delete("/api/v1/organisations/{id}", orgId))
            .andExpect(status().isNoContent)
    }
}