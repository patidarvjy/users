package com.docutools.contacts;

import com.docutools.users.messages.LanguagesLoader;
import com.docutools.exceptions.ErrorCodes;
import com.docutools.exceptions.ExceptionHelper;
import com.docutools.roles.PermissionManager;
import com.docutools.roles.Privilege;
import com.docutools.service.encoding.client.EncodingClient;
import com.docutools.service.encoding.client.FileReference;
import com.docutools.storage.FileType;
import com.docutools.storage.StorageAccessKey;
import com.docutools.storage.StorageEngine;
import com.docutools.users.ImportedFileDTO;
import com.docutools.users.SessionManager;
import com.docutools.users.UserManager;
import com.docutools.users.values.CsvHeaderColumn;
import com.docutools.utils.FileUtils;
import com.docutools.vcard.vCardGenerator;
import ezvcard.VCardVersion;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.docutools.exceptions.ExceptionHelper.*;

@Service
@Transactional
public class ProjectContactService {

    private static final Logger log = LoggerFactory.getLogger(ProjectContactService.class);

    @Autowired
    private ProjectContactRepository contactRepository;

    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private UserManager userManager;
    @Autowired
    private StorageEngine storageEngine;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private EncodingClient encodingClient;
    @Autowired
    private Environment environment;
    @Autowired
    private LanguagesLoader languagesLoader;

    public ProjectContact create(ProjectContact contact) {
        Assert.notNull(contact, "contact is required - must not be NULL!");
        if (contact.getProjectId() == null) {
            throw ExceptionHelper.newMissingRequiredValueError("projectId");
        }
        if (!permissionManager.hasPrivileges(contact.getProjectId(), Privilege.ManageTeam)) {
            throw ExceptionHelper.newUnprivilegedError("ManageTeam");
        }
        if (StringUtils.isEmpty(contact.getEmail()) && StringUtils.isEmpty(contact.getCompanyName()) &&
            StringUtils.isEmpty(contact.getFirstName()) && StringUtils.isEmpty(contact.getLastName())) {
            throw ExceptionHelper.newMissingRequiredValueError("Email or Company Name or First Name or Last Name");
        }
        return contactRepository.save(contact).withPermissions(permissionManager);
    }

    public void copyContacts(UUID from, UUID to, List<UUID> contactsList, boolean allContacts) {
        Assert.notNull(from, "from is required - must not be NULL!");
        Assert.notNull(to, "to is required - must not be NULL!");
        if (!permissionManager.hasPrivileges(to, Privilege.ManageTeam)) {
            throw ExceptionHelper.newUnprivilegedError("ManageTeam");
        }
        if (!permissionManager.hasPrivileges(from, Privilege.ViewTeam)) {
            throw ExceptionHelper.newUnprivilegedError("ViewTeam");
        }
        if (allContacts) {
            contactRepository.findByProjectId(from)
                    .filter(projectContact -> contactsList == null || contactsList.isEmpty() || !contactsList.contains(projectContact.getId()))
                    .filter(c -> !c.isReplaced())
                    .map(original -> new ProjectContact(to, original))
                    .forEach(contactRepository::save);
        } else {
            if (contactsList != null && !contactsList.isEmpty()) {
                contactRepository.findAllById(contactsList).stream()
                        .filter(projectContact -> !projectContact.isReplaced())
                        .map(original -> new ProjectContact(to, original))
                        .forEach(contactRepository::save);
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<ProjectContact> get(UUID contactId) {
        Assert.notNull(contactId, "contactId is required - must not be NULL!");
        return contactRepository.findById(contactId)
            .map(contact -> contact.withPermissions(permissionManager))
            .filter(ProjectContact::canView);
    }

    public Optional<ProjectContact> update(ProjectContact update) {
        Assert.notNull(update, "update is required - must not be NULL!");
        Assert.notNull(update.getId(), "update ID is required  - must not be NULL!");
        return get(update.getId())
            .filter(ProjectContact::canEdit)
            .map(contact -> {
                if (update.getEmail() != null)
                    contact.setEmail(update.getEmail());
                if (update.getCompanyName() != null)
                    contact.setCompanyName(update.getCompanyName());
                if (update.getFirstName() != null)
                    contact.setFirstName(update.getFirstName());
                if (update.getLastName() != null)
                    contact.setLastName(update.getLastName());
                if (update.getPhone() != null)
                    contact.setPhone(update.getPhone());
                if (update.getFax() != null)
                    contact.setFax(update.getFax());
                if (update.getJobTitle() != null)
                    contact.setJobTitle(update.getJobTitle());
                if (update.getDepartment() != null)
                    contact.setDepartment(update.getDepartment());
                if (update.getInternalId() != null)
                    contact.setInternalId(update.getInternalId());
                if (update.getStreet() != null)
                    contact.setStreet(update.getStreet());
                if (update.getZip() != null)
                    contact.setZip(update.getZip());
                if (update.getCity() != null)
                    contact.setCity(update.getCity());
                if (update.getCountryCode() != null)
                    contact.setCountryCode(update.getCountryCode());
                return contactRepository.save(contact);
            });
    }

    public void delete(UUID contactId) {
        Assert.notNull(contactId, "contactId is required - must not be NULL!");
        get(contactId)
            .filter(ProjectContact::canEdit)
            .ifPresent(contactRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<ProjectContact> list(UUID projectId, String search) {
        Assert.notNull(projectId, "projectId is required - must not be NULL!");
        return contactRepository.searchInProject(projectId, search)
            .map(contact -> contact.withPermissions(permissionManager))
            .filter(ProjectContact::canView)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProjectContact> list(UUID projectId, String search, int page, int size) {
        Assert.notNull(projectId, "projectId is required - must not be NULL!");
        List<ProjectContact> projectContacts = contactRepository.searchInProject(projectId, search, PageRequest.of(page, size))
                .map(contact -> contact.withPermissions(permissionManager))
                .filter(ProjectContact::canView)
                .collect(Collectors.toList());
        return new PageImpl<>(projectContacts);
    }

    public String exportVCard(UUID contactId) {
        Assert.notNull(contactId, "contactId is required - must not be NULL!");

        ProjectContact contact = get(contactId)
            .filter(ProjectContact::canView)
            .orElseThrow(() -> newForbiddenError("You are not allowed to view or export this contact!"));

        if (contact.isReplaced()) {
            return userManager.exportVCard(contact.getReplacedBy());
        }

        return vCardGenerator.generateVCard(VCardVersion.V3_0, contact);
    }

    public String exportProjectContacts(UUID projectId) {
        List<String> contacts = contactRepository.findByProjectId(projectId)
                .map(contact -> contact.withPermissions(permissionManager))
                .filter(ProjectContact::canView)
                .filter(projectContact -> !projectContact.isReplaced())
                .map(ProjectContact::toCommaSeparated).collect(Collectors.toList());

        return getLocalisedCsvHeader(sessionManager.getCurrentUser().getSettings().getLanguage()) + "\n" + String.join("\n", contacts);
    }

    public String getLocalisedCsvHeader(String lang) {
        List<String> columns = Arrays.stream(CsvHeaderColumn.values())
                .map(csvHeaderColumn -> languagesLoader.getLocalisedLabel(csvHeaderColumn, lang))
                .collect(Collectors.toList());

        String csvHeader = columns.get(0);
        for(int i=1; i < columns.size(); i++) {
            csvHeader = csvHeader + "," + columns.get(i);
        }
        return csvHeader;
    }

    public List<ProjectContact> importContacts(ProjectContactImport projectContactImport) {
        Assert.notNull(projectContactImport, "Project contact import body- must not be NULL!");
        Map<String, Columns> columnsMap = projectContactImport.getColumnsMap();
        Assert.notEmpty(projectContactImport.getColumnsMap(), "Column mapping is required for contact import - must not be NULL or Empty!");
        if (!permissionManager.hasPrivileges(projectContactImport.getProjectId(), Privilege.ManageTeam)) {
            throw ExceptionHelper.newForbiddenError( "ManageTeam");
        }
        StorageAccessKey key = new StorageAccessKey(FileType.Temporary, projectContactImport.getFileId().toString());
        if (!storageEngine.exists(key)) {
            throw ExceptionHelper.newBadRequestError(ErrorCodes.FILE_NOT_FOUND, projectContactImport.getFileId().toString());
        }

        List<String> columns = projectContactImport.getColumnsMap().keySet().stream().filter(s -> columnsMap.get(s) != Columns.Ignore).collect(Collectors.toList());

        if (!(columnsMap.containsValue(Columns.Email) || columnsMap.containsValue(Columns.Username) || columnsMap.containsValue(Columns.CompanyName) ||
            columnsMap.containsValue(Columns.FirstName) || columnsMap.containsValue(Columns.LastName) || columnsMap.containsValue(Columns.Name))) {
            throw ExceptionHelper.newMissingRequiredValueError("Email or Company Name or User Name or First Name or Last Name");
        }
        List<ProjectContact> newContacts = new ArrayList<>();

        // Load CSV File
        Path path = null;
        try {
            path = Files.createTempFile(UUID.randomUUID().toString(), "");
            storageEngine.copyTo(key, path);

            try (Reader reader = new InputStreamReader(new FileInputStream(path.toFile()), projectContactImport.getFileEncoding())) {

                Set<String> csvHeaders = this.getCsvHeaders(path, projectContactImport.getDelimiter(),
                    projectContactImport.getFileEncoding());

                CSVFormat csvFormat = CSVFormat.newFormat(projectContactImport.getDelimiter());
                CSVParser parser = new CSVParser(reader, csvFormat.withQuote('"').withSkipHeaderRecord()
                        .withHeader(csvHeaders.toArray(new String[]{})));

                Iterator<CSVRecord> iterator = parser.iterator();

                iterator.forEachRemaining(record ->
                        fetchContactFromCsvRecord(projectContactImport.getProjectId(), columnsMap, columns, record)
                                .ifPresent(newContacts::add));

                List<ProjectContact> projectContacts = contactRepository.saveAll(newContacts);
                // Delete CSV File on Success
                storageEngine.delete(key);

                return projectContacts.stream()
                        .map(contact -> contact.withPermissions(permissionManager))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw newInternalServerError("Reading project contact csv : ", e);
        } finally {
            FileUtils.deleteTempFile(path);
        }
    }

    private Optional<ProjectContact> fetchContactFromCsvRecord(UUID projectId, Map<String, Columns> columnsMap, List<String> columns, CSVRecord record) {
        ProjectContact contact = new ProjectContact();
        contact.setProjectId(projectId);

        columns.forEach(columnHeader -> {
            String value = record.get(columnHeader);
            if (value == null)
                return;
            value = value.trim();

            Columns mapping = columnsMap.get(columnHeader);
            if (mapping == null) {
                log.debug(String.format("Warning: Column <%s> has no mapping!", columnHeader));
                return;
            }

            switch (mapping) {
                case FirstName:
                    contact.setFirstName(value);
                    break;
                case LastName:
                    contact.setLastName(value);
                    break;
                case Name:
                    if (value.contains(" ")) {
                        contact.setFirstName(value.substring(0, value.indexOf(' ')));
                        contact.setLastName(value.substring(value.indexOf(' ') + 1, value.length()));
                    } else {
                        contact.setFirstName(value);
                        log.debug(String.format("Could not split up name <%s> by whitespace, set it as first name.", value));
                    }
                    break;
                case Phone:
                    contact.setPhone(value);
                    break;
                case Fax:
                    contact.setFax(value);
                    break;
                case JobTitle:
                    contact.setJobTitle(value);
                    break;
                case Department:
                    contact.setDepartment(value);
                    break;
                case InternalId:
                    contact.setInternalId(value);
                    break;
                case CompanyName:
                    contact.setCompanyName(value);
                    break;
                case Email:
                case Username:
                    contact.setEmail(value);
                    break;
                case Street:
                    contact.setStreet(value);
                    break;
                case Zip:
                    contact.setZip(value);
                    break;
                case City:
                    contact.setCity(value);
                    break;
                case CountryCode:
                    contact.setCountryCode(value);
                    break;
                default:
                    log.debug(String.format("Unknown Columns Mapping for value <%s>: <%s>!", value, mapping));
            }
        });
        if (StringUtils.isEmpty(contact.getEmail()) && StringUtils.isEmpty(contact.getCompanyName()) &&
                StringUtils.isEmpty(contact.getFirstName()) && StringUtils.isEmpty(contact.getLastName())) {
            return Optional.empty();
        }
        return Optional.of(contact);
    }

    public ImportedFileDTO saveContactFile(MultipartFile file, char delimiter, UUID projectId, String language) {
        if (projectId == null) {
            throw newMissingRequiredValueError("ProjectId");
        }
        if (!permissionManager.hasPrivileges(projectId, Privilege.ManageTeam)) {
            throw newForbiddenError("You need the ManageTeam Privilege to update team members.");
        }
        Path path = saveFile(file);
        StorageAccessKey key = new StorageAccessKey(FileType.Temporary, UUID.randomUUID().toString());
        storageEngine.save(key, path);

        //TODO remove the language after testing of new file encoding api
//        DocutoolsUser me = sessionManager.getCurrentUser();
//        if (StringUtils.isEmpty(language)) {
//            language = new Locale(me.getSettings().getLanguage() != null ? me.getSettings().getLanguage() : "en").getDisplayLanguage();
//        }

        String fileEncoding;
        try {
            String tempBucket = this.environment.getProperty("docutools.storage.s3.buckets.temporary", "docutools-dev-temporary");
            FileReference fileReference = new FileReference().withS3Bucket(tempBucket).withS3Key(key.getKey());
            fileEncoding = encodingClient.getEncoding(fileReference, SessionManager.getBearerToken());
        } catch (Exception e) {
            log.warn("Error in file detecting", e);
            fileEncoding = "UTF-8";
        }
        String[] columns = readColumns(path, delimiter, fileEncoding);
        boolean columnsContainLineBreaks = Arrays.stream(columns)
                .anyMatch(c -> c.contains("\n") || c.contains("\r"));
        if (columnsContainLineBreaks) {
            throw ExceptionHelper.newBadRequestError(ErrorCodes.FILE_PARSING, "Cannot heandle Line Breaks in Header Row.");
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn(String.format("Could not delete temporary file <%s>!", path), e);
        }
        FileUtils.deleteTempFile(path);
        ImportedFileDTO importedFileDTO = new ImportedFileDTO(UUID.fromString(key.getKey()), file, Instant.now().plusSeconds(60 * 60 * 2L), columns, delimiter);
        importedFileDTO.setFileEncoding(fileEncoding);
        return importedFileDTO;
    }

    private Path saveFile(MultipartFile file) {
        try {
            Path path = Files.createTempFile(UUID.randomUUID().toString(), "");
            file.transferTo(path.toFile());
            return path;

        } catch (IOException e) {
            throw newInternalServerError("Project contact import: not able to  save file", e);
        }
    }

    private String[] readColumns(Path path, char delimiter, String fileEncoding) {
        try (Reader reader = new InputStreamReader(new FileInputStream(path.toFile()), fileEncoding)) {
            CSVFormat csvFormat = CSVFormat.newFormat(delimiter);
            CSVParser parser = new CSVParser(reader, csvFormat.withQuote('"'));

            Iterator<CSVRecord> iterator = parser.iterator();
            if (!iterator.hasNext()) {
                return new String[]{};
            }
            List<String> columns = new ArrayList<>();
            iterator.next().iterator().forEachRemaining(columns::add);
            return columns.toArray(new String[]{});
        } catch (IOException e) {
            throw newInternalServerError("Reading project contact csv : ", e);
        }
    }

    private Set<String> getCsvHeaders(Path filePath, char delimiter, String fileEncoding) {
        try (Reader reader = new InputStreamReader(new FileInputStream(filePath.toFile()), fileEncoding)) {
            CSVFormat csvFormat = CSVFormat.newFormat(delimiter);
            try (CSVParser parse = CSVParser.parse(reader, csvFormat.withQuote('"'))) {
                CSVRecord next = parse.iterator().next();
                Set<String> parsedColumns = new LinkedHashSet<>();
                next.iterator().forEachRemaining(parsedColumns::add);
                return parsedColumns;
            }
        } catch (IOException e) {
            throw newInternalServerError("Parsing Headers", e);
        }
    }
}
