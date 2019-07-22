package com.docutools.users;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v2/me/organisation/names")
public class OrganisationNameController {

    private static final Logger log = LoggerFactory.getLogger(OrganisationNameService.class);

    @Autowired
    private OrganisationNameService service;

    @ApiOperation(value = "Create Organisation Name", notes = "Required Attributes: none")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationName createName(@RequestBody OrganisationName name) {
        log.debug("POST /api/v2/me/organisation/names Body: {}", name);
        return service.createName(name);
    }

    @ApiOperation(value = "Change Organisation Name", notes = "Required Attributes: none")
    @PutMapping(path = "/{id}")
    public HttpEntity<OrganisationName> changeName(@PathVariable UUID id, @RequestBody OrganisationName newName) {
        log.debug("PUT /api/v2/me/organisation/names Body: {}", newName);
        return service.changeName(id, newName)
                .map(HttpEntity::new)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "List all Organisation Names")
    @GetMapping
    public List<OrganisationName> listNames() {
        log.debug("GET /api/v2/me/organisation/names");
        return service.listNames();
    }

    @ApiOperation(value = "Get Organisation Name")
    @GetMapping(path = "/{id}")
    public HttpEntity<OrganisationName> getName(@PathVariable UUID id) {
        log.debug("GET /api/v2/me/organisation/name/{}", id);
        return service.getName(id)
                .map(HttpEntity::new)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Remove Organisation Name")
    @DeleteMapping(path = "/{id}")
    public void removeName(@PathVariable UUID id) {
        log.debug("DELETE /api/v2/me/organisation/names/{}", id);
        service.deleteName(id);
    }

}
