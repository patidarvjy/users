package com.docutools.emails;

public enum EmailTemplateType {
    Register,
    Invitation,
    TokenExpired,
    ForgotPassword,
    ChangeEmail,
    InviteToProject,
    EndTestPhase, // Deprecated
    EndTestPhase3Days,
    EndTestPhase10Days,
    PersonalNotificationAfter48Hours
}
