package com.docutools.test

import com.docutools.users.Organisation
import com.docutools.users.OrganisationRepo
import com.docutools.users.UserRepo
import com.docutools.config.security.PasswordEncoder
import com.docutools.users.values.UserType
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

import static com.docutools.test.AlternativeFacts.*

@Service
class TestUserHelper {

    def restTemplate = new RestTemplate()

    @Autowired UserRepo userRepo
    @Autowired PasswordEncoder pwEncoder
    @Autowired OrganisationRepo orgRepo

    int port = 0

    TestUserHelper(UserRepo userRepo, PasswordEncoder pwEncoder, OrganisationRepo orgRepo) {
        this.userRepo = userRepo
        this.pwEncoder = pwEncoder
        this.orgRepo = orgRepo
    }

    DocutoolsTestUser newOwner() {
        newTestUser(null, true, true, true)
    }

    DocutoolsTestUser newAdmin(Organisation organisation = null) {
        newTestUser(organisation, true, false, false)
    }

    DocutoolsTestUser newProjectCreator(Organisation organisation = null) {
        newTestUser(organisation, false, true, false)
    }

    def DocutoolsTestUser newTestUser(Organisation organisation = null,
                                      boolean admin = false,
                                      boolean projectCreator = false,
                                      boolean owner = false) {
        if(organisation == null) {
            organisation = orgRepo.save new Organisation(name: organisationName(), cc: cc())
        }
        def newTestUser = new DocutoolsTestUser(randomPassword(), pwEncoder, organisation)
        newTestUser.settings.admin = admin
        newTestUser.settings.projectCreator = projectCreator
        newTestUser.jobTitle = "Manager"
        userRepo.save newTestUser
        if(owner) {
            organisation.owner = newTestUser
            orgRepo.save organisation
        }
        Optional<DocutoolsTestUser> result = userRepo.findById(newTestUser.id)
        return result.get()
    }

    DocutoolsTestUser newSustainUser() {
        def user = newTestUser()
        user.username = randomString() + '@docu-tools.com'
        save(user)
    }

    DocutoolsTestUser save(DocutoolsTestUser user) {
        userRepo.save user
    }

    def String login(DocutoolsTestUser testUser, int localServerPort = port) {
        requestJWT(testUser, localServerPort)["access_token"]
    }

    def Map requestJWT(DocutoolsTestUser testUser, int localServerPort) {
        def headers = new HttpHeaders()
        headers.set 'Authorization', "Basic ${getBasicAuthHeader('tester', 'secret')}"
        LinkedMultiValueMap<String, Object> body = ["username": [testUser.username], "password": [testUser.clearTextPassword], "grant_type": ["password"]]
        def requestEntity = new HttpEntity(body, headers)
        def result = restTemplate.exchange("http://localhost:${localServerPort}/oauth/token", HttpMethod.POST, requestEntity, Map)
        result.getBody()
    }



    def static getBasicAuthHeader(username, password) {
        new String(Base64.encodeBase64("${username}:${password}".bytes))
    }

    DocutoolsTestUser newSAMLUser() {
        def user = newTestUser()
        user.username = randomString() + '@docu-tools.com'
        user.type = UserType.SAML
        save(user)
    }

}
