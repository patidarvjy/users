package com.docutools.contacts;

import com.docutools.exceptions.ExceptionHelper;
import com.docutools.exceptions.ErrorCodes;
import com.docutools.users.ImportedFileDTO;
import com.docutools.utils.FileUtils;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v2/contacts", produces = "application/json")
public class ProjectContactController {

    private static final Logger log = LoggerFactory.getLogger(ProjectContactController.class);

    @Autowired
    private ProjectContactService contactManager;

    @ApiOperation(value = "Create Contacts")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectContact create(@RequestBody ProjectContact contact) {
        log.debug("POST /api/v2/contacts Body: {}", contact);
        return contactManager.create(contact);
    }

    @ApiOperation(value = "Copy Contacts")
    @PutMapping
    public void copyContacts(@RequestParam UUID from,
                             @RequestParam UUID to,
                             @RequestBody(required = false) List<UUID> contactsList,
                             @RequestParam(name = "contacts", required = false, defaultValue = "true") boolean contacts) {
        log.debug("PUT /api/v2/contacts Body: [from={}, to={}]", from, to);
        contactManager.copyContacts(from, to, contactsList, contacts);
    }

    @ApiOperation(value = "Get Contacts")
    @GetMapping(path = "/{id}")
    public HttpEntity<ProjectContact> get(@PathVariable UUID id) {
        log.debug("GET /api/v2/contacts/{}", id);
        return contactManager.get(id)
            .map(HttpEntity::new)
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Update Contacts")
    @PatchMapping(path = "/{id}", consumes = "application/json")
    public HttpEntity<ProjectContact> update(@PathVariable UUID id, @RequestBody ProjectContact contact) {
        log.debug("PATCH /api/v2/contacts/{} Body: {}", id, contact);
        contact.setId(id);
        return contactManager.update(contact)
            .map(HttpEntity::new)
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Delete Contacts")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable UUID id) {
        log.debug("DELETE /api/v2/contacts/{}", id);
        contactManager.delete(id);
    }

    @ApiOperation(value = "List Contacts")
    @GetMapping
    public List<ProjectContact> list(@RequestParam UUID projectId, @RequestParam(required = false, defaultValue = "") String search) {
        log.debug("GET /api/v2/contacts?projectId={}&search={}", projectId, search);
        return contactManager.list(projectId, search);
    }

    @ApiOperation(value = "List Contacts (paged)")
    @GetMapping(path = "/paged")
    public Page<ProjectContact> listContacts(@RequestParam UUID projectId,
                                                  @RequestParam(required = false, defaultValue = "") String search,
                                                  @RequestParam(required = false, defaultValue = "0") int page,
                                                  @RequestParam(required = false, defaultValue = "10") int size) {
        log.debug("GET /api/v2/contacts/paged?projectId={UUID}&search={String}&page=0&size=10");
        return contactManager.list(projectId, search, page, size);
    }

    @ApiOperation(value = "Download Contact as VCard")
    @GetMapping(value = "/{id}/vcard", produces = "text/x-vcard")
    public String downloadVCard(@PathVariable UUID id) {
        log.info("GET /api/v2/contacts/{}/vcard", id);

        return contactManager.exportVCard(id);
    }

    @ApiOperation(value = "Download Contacts as CSV")
    @GetMapping(value = "/{projectId}/csv", produces = "text/csv")
    public String downloadProjectContactsCSV(@PathVariable UUID projectId, HttpServletResponse response) {
        log.info("GET /api/v2/contacts/{}/csv", projectId);
        response.setHeader("Content-disposition", "attachment;filename=contacts.csv");
        response.setContentType("text/csv");
        return contactManager.exportProjectContacts(projectId);
    }

    @ApiOperation(value = "Upload Import Contact File")
    @PostMapping(value = "/import/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportedFileDTO uploadContactImportFile(@RequestParam(defaultValue = ",") char delimiter,
                                                   @RequestParam UUID projectId,
                                                   @RequestParam(required = false) String language,
                                                   @RequestPart("file") MultipartFile file) {
        log.info("POST /api/v2/contacts/import/file?projectId={}&delimiter={}&language={}", projectId, delimiter,language);

        if (!FileUtils.isCsvFile(file.getContentType(), file.getOriginalFilename())) {
            throw ExceptionHelper.newBadRequestError(ErrorCodes.INCORRECT_FILETYPE, "<text/csv>", file.getContentType());
        }
        return contactManager.saveContactFile(file, delimiter, projectId,language);
    }

    @ApiOperation(value = "Import Contact File into Project")
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ProjectContact> importProjectContacts(@RequestBody ProjectContactImport projectContactImport) {
        log.info("POST /api/v2/contacts/import body {}", projectContactImport);
        return contactManager.importContacts(projectContactImport);
    }


}
