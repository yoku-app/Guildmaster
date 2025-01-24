package com.yoku.guildmaster.util

import com.yoku.guildmaster.entity.organisation.Organisation
import org.springframework.stereotype.Component
import java.util.*

@Component
class MockEntityUtil {

    fun generateMockOrganisation(id: UUID): Organisation {
        return Organisation(
            id = id,
            industry = null,
            creatorId = UUID.randomUUID(),
            name = "Test Org",
            description = "Description",
            email = "email@test.com",
            memberCount = 100,
            avatarURL = "url",
            publicStatus = true)
    }

}