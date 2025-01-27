package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.organisation.OrgMemberDTO
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationMember.OrganisationMemberKey
import com.yoku.guildmaster.entity.user.UserProfile
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvalidOrganisationPermissionException
import com.yoku.guildmaster.repository.OrganisationMemberRepository
import com.yoku.guildmaster.utils.MockEntityUtil
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class MemberServiceTest {
    private val mockEntity: MockEntityUtil = MockEntityUtil()
    private val organisationService: OrganisationService = mockk()
    private val organisationMemberRepository: OrganisationMemberRepository = mockk()
    
    private val memberService = MemberService(organisationService, organisationMemberRepository)

    @Test
    fun `fetchOrganisationMembers should return list of members when organisation exists`() {
        val orgId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(orgId)
        val user1 = UserProfile(UUID.randomUUID(), "user1@example.com", "User One")
        val user2 = UserProfile(UUID.randomUUID(), "user2@example.com", "User Two")
        val member1 = OrganisationMember(organisation, user1)
        val member2 = OrganisationMember(organisation, user2)
        
        organisation.members = mutableSetOf(member1, member2)

        every { organisationService.getOrganisationByID(orgId) } returns organisation

        val result = memberService.fetchOrganisationMembers(orgId)

        assertNotNull(result)
        assertEquals(2, result.size)
        assertTrue(result.any { it.userId == user1.userId })
        assertTrue(result.any { it.userId == user2.userId })
        verify { organisationService.getOrganisationByID(orgId) }
    }

    @Test
    fun `removeMemberFromOrganisation should remove member when valid request`() {
        val orgId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val requesterUserId = userId // Self-removal case
        val organisation = mockEntity.generateMockOrganisation(orgId)
        val user = UserProfile(userId, "user@example.com", "Test User")
        val member = OrganisationMember(organisation, user)
        
        organisation.members = mutableSetOf(member)

        every { organisationService.getOrganisationByID(orgId) } returns organisation
        every { organisationMemberRepository.deleteById(OrganisationMemberKey(orgId, userId)) } just Runs

        memberService.removeMemberFromOrganisation(orgId, userId, requesterUserId)

        verify { organisationMemberRepository.deleteById(OrganisationMemberKey(orgId, userId)) }
    }

    @Test
    fun `removeMemberFromOrganisation should throw exception when removing organisation creator`() {
        val orgId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val requesterUserId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(orgId)
        val creator = UserProfile(creatorId, "creator@example.com", "Creator")
        val member = OrganisationMember(organisation, creator)
        
        organisation.creator = creator
        organisation.members = mutableSetOf(member)

        every { organisationService.getOrganisationByID(orgId) } returns organisation

        assertThrows<InvalidOrganisationPermissionException> {
            memberService.removeMemberFromOrganisation(orgId, creatorId, requesterUserId)
        }
    }

    @Test
    fun `addMemberToOrganisation should add new member successfully`() {
        val organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())
        val user = UserProfile(UUID.randomUUID(), "user@example.com", "Test User")
        val invite = OrganisationInvite(
            organisation = organisation,
            email = user.email,
            token = "test-token"
        )
        val expectedMember = OrganisationMember(organisation, user)

        every { organisationMemberRepository.save(any()) } returns expectedMember

        val result = memberService.addMemberToOrganisation(invite, user)

        assertNotNull(result)
        assertEquals(user, result.user)
        assertEquals(organisation, result.organisation)
        verify { organisationMemberRepository.save(any()) }
    }
}