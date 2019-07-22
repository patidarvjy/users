package com.docutools.users.values

import com.docutools.utils.Alpha2Languages
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.jboss.aerogear.security.otp.api.Base32

import javax.persistence.Column
import javax.persistence.Embeddable
import java.time.ZoneId

import static com.docutools.exceptions.ExceptionHelper.newInputValidationError;

/**
 * User specific settings.
 */
@Embeddable
@ApiModel(value = "User Setting Resource")
class UserSettings {

    @ApiModelProperty(value = "Timezone of the User")
    ZoneId timeZone
    @Column(length = 2, nullable = false)
    @ApiModelProperty(value = "Language of the User")
    private String language = 'en'
    @ApiModelProperty(value = "Whether the user has 2FA enabled")
    private boolean twoFactorAuthEnabled = false
    @ApiModelProperty(value = "Whether the user has SMS FA Enabled")
    private boolean smsFactorAuthEnabled = false
    @Column(name = 'two_fa_secret')
    @ApiModelProperty(value = "The 2FA Secret String")
    private String twoFASecret
    @ApiModelProperty(value = "Whether the user is an admin")
    boolean admin = true
    @ApiModelProperty(value = "Whether the user is the project creator")
    boolean projectCreator
    @ApiModelProperty(value = "When true, native apps need to store all photos in the OS specific photo stores")
    boolean savePhotosOnDevice
    @ApiModelProperty(value = "When true, convert audio media to text and save as description")
    boolean transcribeAudios

    def UserSettings() {
    }

    /**
     * Sets the user's preferred language.
     *
     * @param language the ISO 639-1 two-letter language code
     * (see <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">here</a>).
     * @throws com.docutools.apierrors.ApiException when the specified language is null or not a valid code.
     */
    def void setLanguage(String language) {
        if(language == null)
            throw newInputValidationError('Cannot set null for preferred language.')
        if(!Alpha2Languages.instance.isValid(language))
            throw newInputValidationError("Expecting a valid two-letter language code, not <${language}>!")
        this.language = language
    }

    /**
     * Gets the user's preferred language.
     *
     * @return two-letter language code (like {@code en} or {@code de}).
     */
    def String getLanguage() {
        this.language
    }

    def boolean getTwoFactorAuthEnabled() {
        twoFactorAuthEnabled
    }

    def void enableTwoFactorAuth() {
        twoFactorAuthEnabled = true
        generateTwoFASecretIfNecessary()
    }

    def void generateTwoFASecretIfNecessary() {
        if (twoFASecret == null) {
            twoFASecret = Base32.random()
        }
    }

    def void disbaleTwoFactorAuth() {
        twoFactorAuthEnabled = false
        removeTwoFASecret()
    }

    private void removeTwoFASecret() {
        if (smsFactorAuthEnabled || twoFactorAuthEnabled) {
            return
        }
        twoFASecret = null
    }

    def String getTwoFASecret() {
        twoFASecret
    }

    def void enableSMSFactorAuth() {
        smsFactorAuthEnabled = true
    }

    def void disableSMSFactorAuth() {
        smsFactorAuthEnabled = false
        removeTwoFASecret()
    }

    def boolean getSmsFactorAuthEnabled() {
        smsFactorAuthEnabled
    }

}
