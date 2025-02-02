package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.user.UserProfile
import java.util.UUID

data class OrgMemberDTO(
    val id: UUID,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
){
    constructor(user: UserProfile):this(
        id = user.userId,
        displayName = user.displayName,
        email = user.email,
        avatarUrl = user.avatarUrl
    )
}