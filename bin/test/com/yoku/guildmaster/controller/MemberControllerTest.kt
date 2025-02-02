package com.yoku.guildmaster.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.yoku.guildmaster.entity.organisation.OrgMemberDTO
import com.yoku.guildmaster.entity.user.UserProfile
import com.yoku.guildmaster.service.MemberService
import com.yoku.guildmaster.util.MockEntityUtil
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(MemberController::class)
class MemberControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var memberService: MemberService

    private val mockEntity = MockEntityUtil()

    @Test
    fun `getOrganisationMembers should return list of members`() {
        val orgId = UUID.randomUUID()
        val user1 = UserProfile(UUID.randomUUID(), "user1@example.com", "User One")
        val user2 = UserProfile(UUID.randomUUID(), "user2@example.com", "User Two")
        val members = listOf(
            OrgMemberDTO(user1),
            OrgMemberDTO(user2)
        )

        `when`(memberService.fetchOrganisationMembers(orgId)).thenReturn(members)

        mockMvc.perform(get("/api/v1/organisations/{id}/members", orgId))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].email").value("user1@example.com"))
            .andExpect(jsonPath("$[1].email").value("user2@example.com"))
    }

    @Test
    fun `removeMember should return no content when successful`() {
        val orgId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val requesterUserId = UUID.randomUUID()

        mockMvc.perform(delete("/api/v1/organisations/{orgId}/members/{userId}", orgId, userId)
            .header("X-User-ID", requesterUserId.toString()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `removeMember should return bad request when user header is missing`() {
        val orgId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        mockMvc.perform(delete("/api/v1/organisations/{orgId}/members/{userId}", orgId, userId))
            .andExpect(status().isBadRequest)
    }
}