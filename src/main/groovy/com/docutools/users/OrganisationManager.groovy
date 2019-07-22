package com.docutools.users

import com.docutools.users.resources.OrganisationDTO
import com.docutools.users.resources.UserDTO
import com.docutools.users.values.VatNumber
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.stream.Collectors

/**
 * Business logic for managing {@link Organisation}s.
 */
@Service
@Transactional
class OrganisationManager {

    @Autowired OrganisationRepo organisationRepo

    /**
     * Updates an organisations name, vat number and/or country code. Only updates the specified fields which are not
     * {@code null} in the {@link OrganisationDTO} instance.
     *
     * @param organisation organisation to update.
     * @param update values for update.
     * @return updated entity.
     */
    Organisation updateOrganisation(Organisation organisation, OrganisationDTO update) {
        if(update.name) organisation.name = update.name
        if(update.vat?.number) organisation.vat = new VatNumber(number: update.vat.number)
        if(update.cc) organisation.cc = update.cc
        if (update.idpLink) {
            organisation.idpLink = update.idpLink
        }
        if(update.noLicenseMessages) {
            update.noLicenseMessages.keySet().each {
                organisation.noLicenseMessages.put(it, update.noLicenseMessages.get(it))
            }
            def deletedKeys = organisation.noLicenseMessages.keySet().findAll {!update.noLicenseMessages.containsKey(it)}
            deletedKeys.each {
                organisation.noLicenseMessages.remove(it)
            }
        }

        organisationRepo.save(organisation)
    }

    /**
     * Lists all users with the organisation administrator role in the organisation with the specified id.
     *
     * @param organisationId id of the organisation
     * @return all admins in the organisation.
     * @throw com.docutools.apierrors.ApiException when the {@link Organisation} does not exist.
     */
    @Transactional(readOnly = true)
    List<UserDTO> listOrganisationsAdmins(UUID organisationId) {
        organisationRepo.findOrganisationAdministrators(organisationId)
            .map {new UserDTO(it)}
            .collect(Collectors.toList())
    }

}
