package com.docutools.assignees;

import com.docutools.services.core.resources.SortDirection;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.docutools.utils.DateUtils.parseDateTime;

@RestController
@RequestMapping(path = "/api/v2", produces = "application/json")
public class AssigneesController {

    private static final Logger log = LoggerFactory.getLogger(AssigneesController.class);

    @Autowired
    private AssigneesService assigneesService;

    @ApiOperation(value = "List all Assignees")
    @GetMapping(path = "/projects/{projectId}/assignees/all")
    public List<Assignee> allAssignees(@PathVariable UUID projectId,
                                       @RequestParam(required = false) UUID company,
                                       @RequestParam(required = false) String since,
                                       @RequestParam(required = false, defaultValue = "Name") AssigneeSort sort,
                                       @RequestParam(required = false, defaultValue = "ASC") SortDirection sortDirection,
                                       @RequestParam(required = false, defaultValue = "false") boolean includeRemoved,
                                       @RequestParam(required = false, defaultValue = "false") boolean excludeContacts) {
        log.debug("GET /api/v2/projects/{}/assignees/all?company={}&since={}&includeRemoved={}", projectId, company, since, includeRemoved);
        return assigneesService.listAssignees(projectId, company, parseDateTime(since), sort, sortDirection, includeRemoved, excludeContacts);
    }

    @ApiOperation(value = "List all Assignee Companies")
    @GetMapping(path = "/projects/{projectId}/companies/all")
    public List<AssigneeCompany> allCompanies(@PathVariable UUID projectId,
                                              @RequestParam(required = false) UUID craft,
                                              @RequestParam(required = false) String since,
                                              @RequestParam(required = false, defaultValue = "Name") AssigneeSort sort,
                                              @RequestParam(required = false, defaultValue = "ASC") SortDirection sortDirection,
                                              @RequestParam(required = false, defaultValue = "false") boolean includeRemoved) {
        log.debug("GET /api/v2/projects/{}/companies/all?craft={}&since={}", projectId, craft, since);
        return assigneesService.listCompanies(projectId, parseDateTime(since), sort, sortDirection, includeRemoved);
    }

    @ApiOperation(value = "List all Crafts (Not in Use)")
    @GetMapping(path = "/projects/{projectId}/crafts/all")
    public List<String> allCrafts(@PathVariable UUID projectId) {
        log.debug("GET /api/v2/projects/{}/crafts/all", projectId);
        return Collections.emptyList();
    }

}
