package com.docutools.mfa.sms;

import com.docutools.config.security.PasswordEncoder;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import com.docutools.users.values.VerificationStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

import static com.docutools.test.AlternativeFacts.email;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@AutoConfigureMockMvc
public class SMSBasedMultiFactorAuthTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrganisationRepo organisationRepository;
    @Autowired
    private UserRepo userRepository;

    @MockBean
    private SMSSender smsSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestRestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<String> smsMessageCaptor;
    @Captor
    private ArgumentCaptor<String> smsPhoneNumberCaptor;

    private Organisation testOrganisation;
    private DocutoolsUser testUser;
    private String planPasswordTest = "1q2w3e1q";
    private HttpHeaders headers;
    private HttpHeaders loginHeader;


    @Before
    public void setup() {

        ///
        // Maybe here has a better way to authenticate and get token

        // Creates user
        testOrganisation = organisationRepository.save(new Organisation());
        testUser = new DocutoolsUser(email(), testOrganisation);
        testUser.setPassword(passwordEncoder.hashPassword(planPasswordTest));
        testUser.setActive(true);
        testUser.setPhone("+551433220000");
        VerificationStatus verificationStatus = new VerificationStatus();
        verificationStatus.setVerified(true);
        testUser.setVerificationStatus(verificationStatus);
        userRepository.save(testUser);

        // Get token
        loginHeader = new HttpHeaders();
        loginHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        loginHeader.set("Authorization", "Basic YW5kcm9pZDpMJUUycHZuUQ==");
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", testUser.getEmail());
        body.add("password", planPasswordTest);
        body.add("grant_type", "password");
        HttpEntity<?> request = new HttpEntity<>(body, loginHeader);

        ResponseEntity<Map<String, String>> response = restTemplate
                .exchange("/oauth/token",
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<Map<String, String>>() {
                        });
        assert response.getStatusCodeValue() == 200;
        String token = response.getBody().get("access_token");

        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
        this.headers.set("Authorization", "Bearer " + token);
    }

    @Test
    public void shouldActivateAndAuthenticateSuccessful() throws Exception {
        // make sure 2fa and sms-fa DISABLED
        mvc.perform(get("/api/v2/twoFactor?email={email}", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.otp", Matchers.is(false)))
                .andExpect(jsonPath("$.sms", Matchers.is(false)));

        // request to activate sms-based mfa
        mvc.perform(post("/mfa-api/v1/sms")
                .headers(headers))
                .andExpect(status().isOk());


        // send verification code
        String verificationCode = getCodeFromSmsMessage();
        mvc.perform(post("/mfa-api/v1/sms/verification")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\": \"" + verificationCode + "\"}"))
                .andExpect(status().isOk());

        // make sure only sms-fa are ENABLED
        mvc.perform(get("/api/v2/twoFactor?email={email}", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.otp", Matchers.is(false)))
                .andExpect(jsonPath("$.sms", Matchers.is(true)));

        // request sms to authenticate
        mvc.perform(get("/mfa-api/v1/sms?email={email}", testUser.getEmail()))
                .andExpect(status().isOk());


        // authenticate with token got in sms
        String code = getCodeFromSmsMessage();
        mvc.perform(post("/oauth/token")
                .headers(loginHeader)
                .content(String.format("username=%s&password=%s&code=%s&grant_type=password", testUser.getEmail(), planPasswordTest, code)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldDeactivateSuccessful() throws Exception {
        // Active account
        shouldActivateAndAuthenticateSuccessful();

        // remove sms-based authentication
        mvc.perform(delete("/mfa-api/v1/sms")
                .headers(headers))
                .andExpect(status().isOk());

        // make sure sms-fa are DISABLED
        mvc.perform(get("/api/v2/twoFactor?email={email}", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.otp", Matchers.is(false)))
                .andExpect(jsonPath("$.sms", Matchers.is(false)));
    }

    private String getCodeFromSmsMessage() {
        verify(smsSender).sendSMSMessage(smsMessageCaptor.capture(), smsPhoneNumberCaptor.capture());
        String smsMessage = smsMessageCaptor.getValue();
        reset(smsSender);
        assertNotNull(smsMessage);
        return extractVerificationCode(smsMessage);
    }

    private String extractVerificationCode(String msg) {
        return msg.replaceAll("[A-Za-z\\s]", "");
    }

}
