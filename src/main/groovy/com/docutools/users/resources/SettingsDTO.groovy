package com.docutools.users.resources

import com.docutools.users.values.UserSettings
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import java.time.ZoneId

/**
 * Resource representation of {@link UserSettings}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "User Setting Resource")
class SettingsDTO {

    @ApiModelProperty(value = "Language of the User")
    String language
    @ApiModelProperty(value = "Timezone of the User")
    ZoneId timeZone
    @ApiModelProperty(value = "Whether 2FA is enabled for the USer")
    Boolean twoFactorAuthEnabled
    @ApiModelProperty(value = "Whether the User is an admin")
    Boolean admin
    @ApiModelProperty(value = "Whether the User is a Project Creator")
    Boolean projectCreator
    @ApiModelProperty(value = "The maestro Url of the User")
    String maestroUrl
    @ApiModelProperty(value = "When true, native apps need to store all photos in the OS specific photo stores")
    Boolean savePhotosOnDevice
    @ApiModelProperty(value = "When true, convert audio media to text and save as description")
    Boolean transcribeAudios

    SettingsDTO(UserSettings entity) {
        language = entity.language
        timeZone = entity.timeZone
        twoFactorAuthEnabled = entity.twoFactorAuthEnabled
        admin = entity.admin
        projectCreator = entity.projectCreator
        savePhotosOnDevice = entity.savePhotosOnDevice
        transcribeAudios = entity.transcribeAudios
    }

    SettingsDTO() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SettingsDTO that = (SettingsDTO) o

        if (admin != that.admin) return false
        if (language != that.language) return false
        if (projectCreator != that.projectCreator) return false
        if (timeZone != that.timeZone) return false
        if (savePhotosOnDevice != that.savePhotosOnDevice) return false
        if (transcribeAudios != that.transcribeAudios) return false
        if (twoFactorAuthEnabled != that.twoFactorAuthEnabled) return false

        return true
    }

    int hashCode() {
        int result
        result = (language != null ? language.hashCode() : 0)
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0)
        result = 31 * result + (twoFactorAuthEnabled != null ? twoFactorAuthEnabled.hashCode() : 0)
        result = 31 * result + (admin != null ? admin.hashCode() : 0)
        result = 31 * result + (savePhotosOnDevice != null ? savePhotosOnDevice.hashCode() : 0)
        result = 31 * result + (transcribeAudios != null ? transcribeAudios.hashCode() : 0)
        result = 31 * result + (projectCreator != null ? projectCreator.hashCode() : 0)
        return result
    }
}
