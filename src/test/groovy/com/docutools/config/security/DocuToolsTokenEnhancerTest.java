package com.docutools.config.security;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.values.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("Token Enhancer")
class DocuToolsTokenEnhancerTest {

    @ParameterizedTest(name = "run #{index} with [{arguments}]")
    @CsvSource({"anton, oellerer, test@test.com, testOrg"})
    void additionalInfoGetsAdded(String firstName, String lastName, String email, String organisationName) {
        //Before
        Organisation organisation = new Organisation();
        organisation.setName(organisationName);
        DocutoolsUser user = new DocutoolsUser("user", organisation);
        PersonName personName = new PersonName();
        personName.setFirstName(firstName);
        personName.setLastName(lastName);
        user.setName(personName);
        user.setEmail(email);

        //When
        Map<String, Object> additionalInfo = DocuToolsTokenEnhancer.createAdditionalInfo(user);

        //Then
        assertTrue(additionalInfo.containsKey("id"));
        assertTrue(additionalInfo.containsKey("organizationId"));
        assertTrue(additionalInfo.containsKey("organizationName"));
        assertTrue(additionalInfo.containsKey("email"));
        assertTrue(additionalInfo.containsKey("firstName"));
        assertTrue(additionalInfo.containsKey("lastName"));

        assertEquals(organisationName, additionalInfo.get("organizationName"));
        assertEquals(firstName, additionalInfo.get("firstName"));
        assertEquals(lastName, additionalInfo.get("lastName"));
        assertEquals(email, additionalInfo.get("email"));
    }

    @ParameterizedTest(name = "run #{index} with [{arguments}]")
    @CsvSource({"anton, oellerer, test@test.com"})
    void additionalInfoGetsAddedNoOrgName(String firstName, String lastName, String email) {
        //Before
        Organisation organisation = new Organisation();
        DocutoolsUser user = new DocutoolsUser("user", organisation);
        PersonName personName = new PersonName();
        personName.setFirstName(firstName);
        personName.setLastName(lastName);
        user.setName(personName);
        user.setEmail(email);

        //When
        Map<String, Object> additionalInfo = DocuToolsTokenEnhancer.createAdditionalInfo(user);

        //Then
        assertTrue(additionalInfo.containsKey("id"));
        assertTrue(additionalInfo.containsKey("organizationId"));
        assertTrue(additionalInfo.containsKey("organizationName"));
        assertTrue(additionalInfo.containsKey("email"));
        assertTrue(additionalInfo.containsKey("firstName"));
        assertTrue(additionalInfo.containsKey("lastName"));

        assertEquals("", additionalInfo.get("organizationName"));
        assertEquals(firstName, additionalInfo.get("firstName"));
        assertEquals(lastName, additionalInfo.get("lastName"));
        assertEquals(email, additionalInfo.get("email"));
    }
}
