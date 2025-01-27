package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.user.UserProfile
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvitationNotFoundException
import com.yoku.guildmaster.repository.OrganisationInviteRepository
import com.yoku.guildmaster.utils.MockEntityUtil
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class InvitationServiceTest {
    private val mockEntity: MockEntityUtil = MockEntityUtil()
    private val organisationInviteRepository: OrganisationInviteRepository = mockk()
    private val organisationService: OrganisationService = mockk()
    private val httpService: HttpService = mockk()
    private val memberService: MemberService = mockk()
    
    private val invitationService = InvitationService(
        organisationInviteRepository,
        organisationService,
        httpService,
        memberService
    )

    @Test
    fun `createInvitation should create new invitation when valid data is provided`() {
        val orgId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(orgId)
        val email = "test@example.com"
        val userId = UUID.randomUUID()

        every { organisationService.getOrganisationByID(orgId) } returns organisation
        every { organisationInviteRepository.findByOrganisationAndEmailAndInviteStatus(
            organisation,
            email,
            OrganisationInvite.InviteStatus.PENDING
        ) } returns Optional.empty()
        every { organisationInviteRepository.save(any()) } answers { firstArg() }

        val result = invitationService.createInvitation(orgId, email, userId)

        assertNotNull(result)
        assertEquals(email, result.email)
        assertEquals(userId, result.userId)
        assertEquals(organisation, result.organisation)
        assertEquals(OrganisationInvite.InviteStatus.PENDING, result.inviteStatus)
        verify { organisationInviteRepository.save(any()) }
    }

    @Test
    fun `createInvitation should throw InvalidArgumentException when active invitation exists`() {
        val orgId = UUID.randomUUID()
        val organisation = mockEntity.generateMockOrganisation(orgId)
        val email = "test@example.com"
        val existingInvite = OrganisationInvite(
            organisation = organisation,
            email = email
        )

        every { organisationService.getOrganisationByID(orgId) } returns organisation
        every { organisationInviteRepository.findByOrganisationAndEmailAndInviteStatus(
            organisation,
            email,
            OrganisationInvite.InviteStatus.PENDING
        ) } returns Optional.of(existingInvite)

        assertThrows<InvalidArgumentException> {
            invitationService.createInvitation(orgId, email, null)
        }
    }

    @Test
    fun `handleInvitationAccept should accept valid invitation and create organisation member`() {
        val token = "valid-token"
        val userEmail = "test@example.com"
        val organisation = mockEntity.generateMockOrganisation(UUID.randomUUID())
        val invite = OrganisationInvite(
            organisation = organisation,
            email = userEmail,
            token = token
        )
        val userProfile = UserProfile(UUID.randomUUID(), userEmail, "Test User")
        val member = OrganisationMember(organisation, userProfile)

        every { organisationInviteRepository.findByTokenAndInviteStatus(
            token,
            OrganisationInvite.InviteStatus.PENDING
        ) } returns Optional.of(invite)
        every { httpService.get(any<Request.Builder>(), UserProfile::class.java) } returns userProfile
        every { organisationInviteRepository.save(any()) } returns invite
        every { memberService.addMemberToOrganisation(invite, userProfile) } returns member

        val result = invitationService.handleInvitationAccept(token, userEmail)

        assertNotNull(result)
        assertEquals(userProfile, result.user)
        assertEquals(organisation, result.organisation)
        verify { organisationInviteRepository.save(invite) }
        verify { memberService.addMemberToOrganisation(invite, userProfile) }
    }

    @Test
    fun `handleInvitationAccept should throw InvitationNotFoundException for invalid token`() {
        val token = "invalid-token"
        val userEmail = "test@example.com"

        every { organisationInviteRepository.findByTokenAndInviteStatus(
            token,
            OrganisationInvite.InviteStatus.PENDING
        ) } returns Optional.empty()

        assertThrows<InvitationNotFoundException> {
            invitationService.handleInvitationAccept(token, userEmail)
        }
    }
}