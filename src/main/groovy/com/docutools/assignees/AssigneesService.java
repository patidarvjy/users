package com.docutools.assignees;

import com.docutools.contacts.ProjectContactRepository;
import com.docutools.roles.PermissionManager;
import com.docutools.services.core.resources.SortDirection;
import com.docutools.team.MembershipState;
import com.docutools.team.TeamMembership;
import com.docutools.team.TeamMembershipRepo;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.docutools.exceptions.ExceptionHelper.*;
@Service
@Transactional(readOnly = true)
public class AssigneesService {

    @Autowired
    private TeamMembershipRepo membershipsRepository;
    @Autowired
    private ProjectContactRepository contactRepository;

    @Autowired
    private PermissionManager permissionManager;
    @Autowired
    private SessionManager sessionManager;

    public List<Assignee> listAssignees(UUID projectId, UUID companyId, Instant since, AssigneeSort sort, SortDirection sortDirection, boolean includeRemoved, boolean excludeContacts) {
        requireToBeMember(projectId);
        Stream<Assignee> memberAssignees = (includeRemoved? membershipsRepository.findByProjectId(projectId) :
                membershipsRepository.findByProjectIdAndStateIsNot(projectId, MembershipState.Removed))
                .map(Assignee::new);
        Stream<Assignee> contactAssignees;
        if(!excludeContacts) {
            contactAssignees = contactRepository.findByProjectId(projectId)
                    .filter(c -> !c.isReplaced())
                    .map(Assignee::new);
        } else {
            contactAssignees = Stream.empty();
        }
        List<Assignee> assignees = Stream.concat(memberAssignees, contactAssignees)
            .filter(assignee -> companyId == null || companyId.equals(assignee.getCompanyId()))
            .filter(lastModifiedPredicate(since))
            .collect(Collectors.toList());

        // Add Current User when not in List
        DocutoolsUser me = sessionManager.getCurrentUser();
        if ((companyId == null || companyId.equals(me.getOrganisation().getId())) && assignees.stream().noneMatch(a -> a.getId().equals(me.getId()))) {
            Assignee assignee = new Assignee(me);
            if (since == null || assignee.getLastModified().isAfter(since)) {
                assignees.add(assignee);
            }
        }

        Comparator<Assignee> comparator;
        switch(sort){
            case Id:
                comparator = Comparator.comparing(Assignee::getId);
                break;
            case LastModified:
                comparator = Comparator.comparing(Assignee::getLastModified);
                break;
            case Name:
            default:
                comparator = Comparator.comparing(assignee -> assignee.getName().toLowerCase());
                break;
        }
        if(sortDirection.equals(SortDirection.DESC)){
            comparator = comparator.reversed();
        }

        return assignees.stream().sorted(comparator).collect(Collectors.toList());
    }

    private Predicate<Assignee> lastModifiedPredicate(Instant since) {
        if (since == null) return assignee -> true;
        return assignee -> assignee.getLastModified().isAfter(since);
    }

    private Predicate<AssigneeCompany> lastModifiedPredicateCompany(Instant since) {
        if (since == null) return assignee -> true;
        return assigneeCompany -> assigneeCompany.getLastModified().isAfter(since);
    }

    public List<AssigneeCompany> listCompanies(UUID projectId, Instant since, AssigneeSort sort, SortDirection sortDirection, boolean includeRemoved) {
        requireToBeMember(projectId);
        Stream<AssigneeCompany> memberCompanies = (includeRemoved? membershipsRepository.findByProjectId(projectId) :
                membershipsRepository.findByProjectIdAndStateIsNot(projectId, MembershipState.Removed))
                .map(teamMembership -> new AssigneeCompany(teamMembership.getUser(),teamMembership.getState()))
                .distinct();
        Stream<AssigneeCompany> contactCompanies = contactRepository.findByProjectId(projectId)
                .filter(c -> !c.isReplaced())
                .map(AssigneeCompany::new);
        List<AssigneeCompany> companies = Stream.concat(memberCompanies, contactCompanies)
            .filter(lastModifiedPredicateCompany(since))
            .collect(Collectors.toList());

        DocutoolsUser me = sessionManager.getCurrentUser();
        UUID companyId = me.getOrganisationName() != null ? me.getOrganisationName().getId() : me.getOrganisation().getId();
        if (companies.stream().noneMatch(c -> c.getId().equals(companyId))) {
            AssigneeCompany assigneeCompany = new AssigneeCompany(me);
            if (since == null || assigneeCompany.getLastModified().isAfter(since)) {
                companies.add(assigneeCompany);
            }
        }

        Comparator<AssigneeCompany> comparator;
        switch(sort){
            case Id:
                comparator = Comparator.comparing(AssigneeCompany::getId);
                break;
            case LastModified:
                comparator = Comparator.comparing(AssigneeCompany::getLastModified);
                break;
            case Name:
            default:
                comparator = (company1, company2) -> {
                    if(company1.getName() == null && company2.getName() != null){
                        return -1;
                    }
                    if(company1.getName() != null && company2.getName() == null){
                        return 1;
                    }
                    if(company1.getName() == null && company2.getName() == null){
                        return 0;
                    }
                    return company1.getName().compareToIgnoreCase(company2.getName());
                };
                break;
        }
        if(sortDirection.equals(SortDirection.DESC)){
            comparator = comparator.reversed();
        }

        return companies.stream().sorted(comparator).collect(Collectors.toList());
    }

    private void requireToBeMember(UUID projectId) {
        if(!permissionManager.isMember(projectId)) {
            throw newForbiddenError("User must be member in the project to list assignee!");
        }
    }

}
