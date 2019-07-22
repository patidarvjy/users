package com.docutools.users;

import com.docutools.users.resources.OrganisationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.docutools.exceptions.ErrorCodes.*;
import static com.docutools.exceptions.ExceptionHelper.*;

@Service
@Transactional
public class OrganisationNameService {

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private OrganisationRepo organisationRepository;
    @Autowired
    private OrganisationNameRepository nameRepository;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrganisationManager organisationManager;

    public OrganisationName createName(OrganisationName name) {
        try {
            DocutoolsUser user = sessionManager.getCurrentUser();
            Organisation organisation = user.getOrganisation();
            name.setOrganisation(organisation);
            organisation.getNames().add(name);
            organisationRepository.saveAndFlush(organisation);
            return name;
        } catch (DataIntegrityViolationException e) {
            throw newConflictError(String.format("Name '%s' already exists.", name));
        }
    }

    public Optional<OrganisationName> changeName(UUID id, OrganisationName newName) {
        DocutoolsUser currentUser = sessionManager.getCurrentUser();
        Organisation organisation = currentUser.getOrganisation();
        if (organisation.getId().equals(id)) {
            if (currentUser.isAdmin()) {
                OrganisationDTO organisationDTO = new OrganisationDTO();
                organisationDTO.setName(newName.getName());
                Organisation updatedOrganization =
                    organisationManager.updateOrganisation(organisation, organisationDTO);
                OrganisationName updatedName = new OrganisationName(updatedOrganization.getName(),
                    updatedOrganization, updatedOrganization.getId());
                updatedName.setPermissions(Collections.emptyList());
                return Optional.of(updatedName);
            } else {
                throw newForbiddenError("Only Admin can change organization's real name!");
            }
        }
        try {
            return getName(id)
                    .map(name -> {
                        name.setName(newName.getName());
                        return nameRepository.saveAndFlush(name);
                    });
        } catch (DataIntegrityViolationException e) {
            throw newConflictError(String.format("Name '%s' already exists.", newName.getName()));
        }
    }

    @Transactional(readOnly = true)
    public List<OrganisationName> listNames() {
        Organisation organisation = sessionManager.getCurrentUser().getOrganisation();
        ArrayList<OrganisationName> names = new ArrayList<>(organisation
            .getNames());
        OrganisationName organisationName = new OrganisationName(organisation.getName(),
            organisation, organisation.getId());
        organisationName.setPermissions(Collections.emptyList());
        names.add(organisationName);
        return names;
    }

    @Transactional(readOnly = true)
    public Optional<OrganisationName> getName(UUID id) {
        return listNames()
                .stream()
                .filter(name -> name.getId().equals(id))
                .findFirst();
    }

    public Optional<OrganisationName> deleteName(UUID id) {
        Organisation organisation = sessionManager.getCurrentUser().getOrganisation();
        if (organisation.getId().equals(id)) {
            throw newForbiddenError("Cannot delete OrganisationName, It is organization's real name!");
        }
        return getName(id)
                .map(name -> {
                    if(userRepo.countByOrganisationName(name) > 0) {
                        throw newBadRequestError(RESOURCE_IN_USE, "Organisation Name");
                    }
                    organisation.removeName(name.getName());
                    organisationRepository.save(organisation);
                    return name;
                });
    }

    public OrganisationName loadOrCreate(Organisation organisation, String name) {
        return nameRepository.findOneByOrganisationIdAndNameIgnoreCase(organisation.getId(), name).orElseGet(() ->
            createName(new OrganisationName(name, organisation)));
    }

}
