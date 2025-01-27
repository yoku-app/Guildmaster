package com.yoku.guildmaster.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "user_profiles",
    indexes = [
        Index(name = "idx_profiles_email", columnList = "email"),
        Index(name = "idx_profiles_name", columnList = "display_name")
    ]
)
data class UserProfile(
    @Id @Column(name = "user_id", nullable = false) val userId: UUID,

    @Column(name = "phone") val phone: String? = null,

    @Column(name = "dob") val dob: LocalDateTime?,

    @Enumerated(EnumType.STRING) @Column(name = "main_focus") val focus: Focus?,

    @Column(name = "email", nullable = false) val email: String,

    @Column(name = "display_name", nullable = false) val displayName: String,

    @Column(name = "avatar_url") val avatarUrl: String? = null,

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    ) var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false) var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Embedded val onboardingCompletion: OnboardingCompletion? = null
) {

    @PrePersist
    fun onPrePersist() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }

    @Embeddable
    data class OnboardingCompletion(
        @Column(name = "respondent_onboarding_completion") val respondent: Date? = null,

        @Column(name = "creator_onboarding_completion") val creator: Date? = null,

        @Column(name = "core_onboarding_completion") val core: Date? = null
    )

    enum class Focus {
        RESPONDENT, CREATOR, HYBRID
    }
}
