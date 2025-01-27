package com.yoku.guildmaster.util

import com.yoku.guildmaster.entity.lookups.Industry
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.user.UserProfile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class MockEntityUtil {
    fun generateMockOrganisation(id: UUID): Organisation {
        return Organisation(
            id = id,
            industry = generateMockIndustry(UUID.randomUUID()),
            creator = generateMockUserProfile(UUID.randomUUID()),
            name = "Test Org",
            description = "Description",
            email = "email@test.com",
            memberCount = 100,
            avatarURL = "url",
            publicStatus = true)
    }

    fun generateMockUserProfile(id: UUID): UserProfile {
        return UserProfile(
            userId = id,
            displayName = "Test User",
            email = "email@email.com",
            focus = UserProfile.Focus.CREATOR,
            dob = LocalDateTime.now().minusYears(20),
        )
    }

    fun generateMockIndustry(id: UUID): Industry{
        return Industry(
            id = id,
            name = "Test Industry",
            description = "Description"
        )
    }

    fun generateMockOrganisationInvite(organisation: Organisation, user: UserProfile): OrganisationInvite {
        return OrganisationInvite(
            id = UUID.randomUUID(),
            userId = user.userId,
            organisation = organisation,
            email = user.email,
            token = generateInviteCode(),
            inviteStatus = OrganisationInvite.InviteStatus.PENDING
        )
    }

    fun generateMockOrganisationMember(organisation: Organisation, user: UserProfile): OrganisationMember {
        val mockOrganisationMemberKey: OrganisationMember.OrganisationMemberKey = OrganisationMember.OrganisationMemberKey(
            organisationId = organisation.id,
            userId = user.userId
        )

        return OrganisationMember(
            id = mockOrganisationMemberKey,
            user = user

        )
    }

    private fun generateInviteCode(): String{
        return UUID.randomUUID().toString().substring(0, 12)
    }

}