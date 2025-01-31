package com.yoku.guildmaster.util

import com.yoku.guildmaster.entity.lookups.Industry
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.user.UserProfile
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
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
            dob = ZonedDateTime.now().minusYears(20),
        )
    }

    fun generateMockIndustry(id: UUID): Industry{
        return Industry(
            id = id,
            name = "Test Industry",
            description = "Description"
        )
    }

}