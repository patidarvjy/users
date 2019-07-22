package com.docutools.users

import com.docutools.emails.EmailTemplateType
import com.docutools.emails.MailServer
import com.docutools.users.values.UserSettings
import com.docutools.users.values.UserType
import com.xlson.groovycsv.CsvParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert

import static com.docutools.exceptions.ErrorCodes.*
import static com.docutools.exceptions.ExceptionHelper.*


/**
 * Imports {@link DocutoolsUser}s form CSV files.
 */
@Transactional
@Service
class UserFileImporter {

    private static final Logger log = LoggerFactory.getLogger(UserFileImporter)

    /**
     * All known columns for importing a user.
     */
    static enum Columns {
        FirstName,
        LastName,
        Name,
        Phone,
        Fax,
        JobTitle,
        Department,
        InternalId,
        Email,
        Username,
        Street,
        Zip,
        City,
        CompanyName,
        UserType,
        /**
         * By default all users are {@code active=true}, but when a column mapped to it is present a user is only active
         * when the column value is not empty or whitespace only.
         */
        Active,
        /**
         * Indicates that this column shall be ignored by the import.
         */
        Ignore
    }

    @Autowired
    private UserRepo userRepo
    @Autowired
    private SessionManager sessionManager
    @Autowired
    private MailServer mailServer
    @Autowired
    private OrganisationNameService organisationNameService

    /**
     * Imports a list of users from a CSV file to the current users organisation.
     *
     * @param path the path to the CSV file.
     * @param columnMap mapping the columns in a CSV file to attributes in {@link DocutoolsUser} entity.
     * @return the list of created {@link DocutoolsUser}s.
     * @throws IllegalArgumentException if any of the passed parameters is {@code null} or the given file does not exist.
     */
    List<DocutoolsUser> importUsers(InputStream is, Map<String, Columns> columnMap, char delimiter,
                                    boolean skipFirstRow, String fileEncoding) {
        try {
            Assert.notNull(is)
            Assert.notNull(columnMap)

            def columns = columnMap.keySet()
                    .findAll { columnMap.get(it) != Columns.Ignore }

            if (!columnMap.values().contains(Columns.Email)){
                throw newBadRequestError(MISSING_REQUIRED_VALUE, "Email Column")
            }

            List<DocutoolsUser> newUsers = []
            def currentUser = sessionManager.currentUser
            def organisation = currentUser.organisation

            is.withReader fileEncoding,{
                def parserSettings = new HashMap<String, Character>()
                parserSettings.put("separator", delimiter)
                def csv = CsvParser.parseCsv(parserSettings, it)

                if (!skipFirstRow) {
                    def valuesInFirstRow = new HashMap<String, String>()
                    columns.each {
                        valuesInFirstRow.put(it, it)
                    }
                    DocutoolsUser newUser = parseUser(columns, valuesInFirstRow, columnMap, organisation, currentUser)
                    newUsers.add newUser
                }

                for (line in csv) {
                    DocutoolsUser newUser = parseUser(columns, line, columnMap, organisation, currentUser)
                    newUsers.add newUser
                }
            }

            log.debug("Parsed <${newUsers.size()}> users from CSV file.")

            def conflicts = newUsers
                .findAll {userRepo.findByUsernameIgnoreCase(it.username).isPresent()}
                .collect {it.username}
            if(conflicts) {
                throw newConflictError("Some of the given usernames")
            }

            // Save new Users to Database
            userRepo.saveAll newUsers
            // Send Invitation Email on all Active Users
            def invitationsSent = newUsers.findAll { it.active }
                    .each {
                        if (it.type == UserType.SAML) {
                            it.verificationStatus.verificationRequired = false
                            it.verificationStatus.verified = true
                            userRepo.save(it)
                        } else {
                            mailServer.sendEmail(EmailTemplateType.Invitation,
                                    [inviter: currentUser.name, organisation_name: currentUser.getCompanyName()], it)
                        }
                    }
                    .size()
            log.debug("Sent <$invitationsSent> email invitations!")

            return newUsers
        } catch (IOException e) {
            log.error("Received an IOException when importing users CSV.", e)
            throw newInternalServerError('Received IOException when importing CSV file.', e)
        } catch(ArrayIndexOutOfBoundsException e) {
            log.warn("Received a ArrayIndexOutOfBoundsException when importing users CSV.", e)
            throw newBadRequestError(INVALID_RESOURCE, "CSV File")
        } catch (MissingPropertyException e) {
            log.warn("Received a MissingPropertyException when importing users CSV.", e)
            throw newBadRequestError(MISSING_REQUIRED_VALUE, String.format("%s in the users csv", e.property))
        }
    }

    private DocutoolsUser parseUser(Set<String> columns, Object line, Map<String, Columns> columnMap, Organisation organisation, DocutoolsUser currentUser) {
        def newUser = new DocutoolsUser(organisation: organisation,
                settings: new UserSettings(admin: false, projectCreator: false, language: currentUser.settings.language))

        columns.each {
            def value = line[it] as String
            if (value == null)
                return
            value = value.trim()

            def mapping = columnMap[it]
            if (!mapping) {
                log.debug("Warning: Column <$it> has no mapping!")
                return
            }

            switch (mapping) {
                case Columns.FirstName:
                    newUser.name.firstName = value
                    break
                case Columns.LastName:
                    newUser.name.lastName = value
                    break
                case Columns.Name:
                    if (value.contains(' ')) {
                        newUser.name.firstName = value.substring(0, value.indexOf(' '))
                        newUser.name.lastName = value.substring(value.indexOf(' ') + 1, value.length())
                    } else {
                        newUser.name.firstName = value
                        log.debug("Could not split up name <$value> by whitespace, set it as first name.")
                    }
                    break
                case Columns.Phone:
                    newUser.phone = value
                    break
                case Columns.Fax:
                    newUser.fax = value
                    break
                case Columns.JobTitle:
                    newUser.jobTitle = value
                    break
                case Columns.Department:
                    newUser.department = value
                    break
                case Columns.InternalId:
                    newUser.internalId = value
                    break
                case Columns.Email:
                    newUser.username = value
                    break
                case Columns.Username:
                    newUser.username = value
                    break
                case Columns.Street:
                    newUser.street = value
                    break
                case Columns.Zip:
                    newUser.zip = value
                    break
                case Columns.City:
                    newUser.city = value
                    break
                case Columns.Active:
                    newUser.active = !value.empty
                    break
                case Columns.CompanyName:
                    if (!value.empty && !value.equalsIgnoreCase(organisation.name)) {
                        newUser.organisationName = organisationNameService.loadOrCreate(organisation, value)
                    }
                    break
                case Columns.UserType:
                    if (!value.isEmpty() && value.equalsIgnoreCase('SAML')) {
                        newUser.setType(UserType.SAML)
                    }
                    break
                default:
                    log.debug("Unknown Columns Mapping for value <$value>: <$mapping>!")
                    break
            }
        }
        newUser
    }


}
