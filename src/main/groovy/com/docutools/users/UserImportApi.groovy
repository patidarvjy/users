package com.docutools.users

import au.com.bytecode.opencsv.CSVReader
import com.docutools.config.api.BaseController
import com.docutools.exceptions.ErrorCodes
import com.docutools.exceptions.ExceptionHelper
import com.docutools.service.encoding.client.EncodingClient
import com.docutools.services.previews.resources.FileReference
import com.docutools.storage.FileType
import com.docutools.storage.StorageAccessKey
import com.docutools.storage.StorageEngine
import com.docutools.users.resources.UserDTO
import com.docutools.utils.FileUtils
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

import static com.docutools.exceptions.ErrorCodes.*
import static com.docutools.exceptions.ExceptionHelper.*

@RestController
@RequestMapping(value = '/api/v2/users/imports', produces = 'application/json')
class UserImportApi extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(UserImportApi)

    @Autowired
    private StorageEngine storageEngine
    @Autowired
    private UserFileImporter userFileImporter
    @Autowired
    private SessionManager sessionManager
    @Autowired
    private EncodingClient encodingClient
    @Autowired
    private Environment environment

    @ApiOperation(value = "Import File")
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping(value = '/files', consumes = 'multipart/form-data')
    ImportedFileDTO importFile(@RequestParam(name = "delimiter", defaultValue = ',') char delimiter,
                               @RequestParam(required = false) String language,
                               @RequestPart('file') MultipartFile file) {
        log.info('POST /api/v2/users/import/files?delimiter={}&language={} by {}', delimiter,language, userName)

        if (!FileUtils.isCsvFile(file.getContentType(), file.getOriginalFilename())) {
            throw newBadRequestError(INCORRECT_FILETYPE, "text/csv", file.getContentType())
        }

        def path = saveFile(file)
        def key = new StorageAccessKey(FileType.Temporary, UUID.randomUUID().toString())
        storageEngine.save(key, Files.newInputStream(path))


        //TODO remove the language after testing of new file encoding api
//        DocutoolsUser me = sessionManager.getCurrentUser()
//        if (StringUtils.isEmpty(language)) {
//            language = new Locale(me.settings?.language ?: "en").getDisplayLanguage()
//        }

        String fileEncoding
        try {
            String tempBucket = this.environment.getProperty("docutools.storage.s3.buckets.temporary", "docutools-dev-temporary");
            com.docutools.service.encoding.client.FileReference fileReference = new com.docutools.service.encoding.client.FileReference().withS3Bucket(tempBucket).withS3Key(key.getKey());
            fileEncoding = encodingClient.getEncoding(fileReference, SessionManager.getBearerToken());
        } catch (Exception e) {
            log.warn("Error in file detecting", e)
            fileEncoding = "UTF-8"
        }

        String[] columns = readColumns(path, delimiter, fileEncoding)
        boolean columnsContainLineBreaks = Arrays.stream(columns).anyMatch { it.contains("\n") || it.contains("\r") }
        if(columnsContainLineBreaks) {
            throw newBadRequestError(FILE_PARSING, "Cannot handle Line Breaks in Header Row")
        }
        CompletableFuture.runAsync {
            try {
                Files.delete(path)
            } catch (IOException e) {
                log.warn("Could not delete temporary file <$path>!", e)
            }
        }

        def importedFileDTO = new ImportedFileDTO(UUID.fromString(key.getKey()), file, ZonedDateTime.now().plusHours(2).toInstant(), columns, delimiter)
        importedFileDTO.setFileEncoding(fileEncoding)
        return importedFileDTO
    }

    @ApiOperation(value = "Import User from File")
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping(value = '/{storageId}', consumes = 'application/json')
    List<UserDTO> importUsers(@PathVariable UUID storageId,
                              @RequestParam(name = "delimiter", defaultValue = ',') char delimiter,
                              @RequestParam(name = "skipFirstRow", defaultValue = 'true') boolean skipFirstRow,
                              @RequestParam(name = "fileEncoding", defaultValue = 'UTF-8') String fileEncoding,
                              @RequestBody Map<String, UserFileImporter.Columns> columnsMap) {
        log.info('POST /api/v2/users/import/{}?delimiter={}&skipFirstRow={}&fileEncoding={} by {}', storageId, delimiter, skipFirstRow, fileEncoding, userName)
        log.debug('Columns Map: {}', columnsMap)

        // Load CSV File
        def key = new StorageAccessKey(FileType.Temporary, storageId.toString())
        if (!storageEngine.exists(key)) {
            throw newBadRequestError(FILE_NOT_FOUND, storageId.toString())
        }
        def is = storageEngine.openStreamTo(key)

        // Import Users
        def users = userFileImporter.importUsers(is, columnsMap, delimiter, skipFirstRow,fileEncoding)
        // Delete CSV File on Success
        CompletableFuture.runAsync {
            storageEngine.delete(key)
        }
        .handle { result, e ->
            if (e) {
                log.warn("Error when trying to remove User Import CSV with ID <$storageId>!", e)
            }
        }
        // Return DTOs
        return users.collect { new UserDTO(it) }
    }

    private Path saveFile(MultipartFile file) {
        def path = Paths.get(System.getProperty("java.io.tmpdir") + '/' + UUID.randomUUID())
        file.transferTo(path.toFile())
        return path
    }

    private String[] readColumns(Path path, char delimiter, String fileEncoding) {
        path.toFile().withReader fileEncoding, {
            def csvReader = new CSVReader(it, delimiter)
            return csvReader.readNext()
        }
    }

}
