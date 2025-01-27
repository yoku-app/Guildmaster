package com.yoku.guildmaster.exceptions

class OrganisationNotFoundException(message: String): RuntimeException(message)
class InvalidOrganisationPermissionException(message: String): RuntimeException(message)
class MemberNotFoundException(message: String): RuntimeException(message)
class InvitationNotFoundException(message: String): RuntimeException(message)