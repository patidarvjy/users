package com.docutools.chargebee;

import com.chargebee.Environment;
import com.chargebee.models.Event;
import com.chargebee.org.json.JSONObject;
import com.docutools.apierrors.ApiException;
import com.docutools.subscriptions.PaymentPlan;
import com.docutools.subscriptions.SubscriptionType;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
class ChargebeeTest {

    @Autowired
    private ChargebeeService chargebeeService;
    @Autowired
    private OrganisationRepo organisationRepo;

    @MockBean
    private SessionManager mockSessionManager;
    @MockBean
    private ChargebeeApiClient apiClient;

    private final UUID ORGANISATION_WITH_CB = UUID.fromString("766dd8ef-ef16-4d2c-a330-c678537d0a6b");
    private final UUID ORGANISATION_WITHOUT_CB = UUID.fromString("84675b2b-305d-41bf-b8e8-8efb6074536d");
    private final String PLAN_ID = "multi-tool-monthly";

    @BeforeEach
    public void setup() {
        Organisation org = new Organisation();
        org.setId(ORGANISATION_WITH_CB);
        org.setHasChargebeeAccount(false);
        organisationRepo.save(org);

        Organisation org2 = new Organisation();
        org2.setId(ORGANISATION_WITHOUT_CB);
        org2.setHasChargebeeAccount(false);
        organisationRepo.save(org2);
    }

    @AfterEach
    public void tearDown() {
        organisationRepo.deleteAll();
        try {
            com.chargebee.models.Customer.delete(String.valueOf(ORGANISATION_WITHOUT_CB)).request();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void getCheckoutForExistingPlan() throws Exception {
        //Arrange
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITHOUT_CB);
        DocutoolsUser docutoolsUser = new DocutoolsUser();
        docutoolsUser.setOrganisation(organisation);
        Mockito.when(mockSessionManager.getCurrentUser()).thenReturn(docutoolsUser);
        Mockito.when(apiClient.getPlanCheckoutForOrganisation(PLAN_ID, organisation))
                .thenReturn(new JSONObject("{\"checkout_page\":\"fake\"}"));

        //Act
        Optional<JSONObject> checkoutPage = chargebeeService.getCheckoutPage(PLAN_ID);

        //Verify
        assertTrue(checkoutPage.isPresent(), "did not get checkout page");
    }

    @Test
    public void getCheckoutForNotExistingPlan() throws Exception {
        //Arrange
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);
        DocutoolsUser docutoolsUser = new DocutoolsUser();
        docutoolsUser.setOrganisation(organisation);
        Mockito.when(mockSessionManager.getCurrentUser()).thenReturn(docutoolsUser);
        Mockito.when(apiClient.getPlanCheckoutForOrganisation("lollolo", organisation))
                .thenThrow(Exception.class);


        //Act
        assertThrows(ApiException.class, () -> chargebeeService.getCheckoutPage("lollolo"));
    }

    @Test
    public void getCheckoutForNull() {
        assertThrows(IllegalArgumentException.class, () -> chargebeeService.getCheckoutPage(null), "checkout for null did not throw");
    }

    @Test
    public void getPortalForRegisteredOrganisation() throws Exception {
        //Arrange
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);
        DocutoolsUser docutoolsUser = new DocutoolsUser();
        docutoolsUser.setOrganisation(organisation);
        Mockito.when(mockSessionManager.getCurrentUser()).thenReturn(docutoolsUser);
        Mockito.when(apiClient.getPortalForOrganisation(organisation))
                .thenReturn(new JSONObject("{\"portal_session\":\"fake\"}"));

        //Act
        Optional<JSONObject> portal = chargebeeService.getPortal();

        //Verify
        assertTrue(portal.isPresent(), "portal not present");
    }

    @Test
    public void getPortalForNotRegisteredOrganisation() throws Exception {
        //Arrange
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITHOUT_CB);
        DocutoolsUser docutoolsUser = new DocutoolsUser();
        docutoolsUser.setOrganisation(organisation);
        Mockito.when(mockSessionManager.getCurrentUser()).thenReturn(docutoolsUser);
        Mockito.when(apiClient.getPortalForOrganisation(organisation))
                .thenThrow(Exception.class);

        //Act
        assertThrows(ApiException.class, () -> chargebeeService.getPortal());
    }

    @Test
    public void getValidPortalNoSiteNameNoApiKey() {
        //Arrange
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);
        DocutoolsUser docutoolsUser = new DocutoolsUser();
        docutoolsUser.setOrganisation(organisation);
        Mockito.when(mockSessionManager.getCurrentUser()).thenReturn(docutoolsUser);
        Environment.configure("", "");

        //Act
        assertThrows(ApiException.class, () -> chargebeeService.getPortal());

    }

    @Test
    public void notSetWithoutRegisterd() {
        //Arrange
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);

        //Verify
        assertFalse(organisation.getHasChargebeeAccount());
    }

    @Test
    public void chargebeeCustomerCreated() throws Exception {
        //Arrange
        String eventId = "ev_Hr5514pR9BCPBL13bg";
        String customerCreatedEvent = "{\"id\": \"" + eventId + "\",}";

        Mockito.when(apiClient.getEventById(eventId))
                .thenReturn(new Event("{" +
                        "\"event_type\": \"customer_created\"," +
                        "\"content\": {" +
                        "\"customer\": {" +
                        "\"id\": \"" + ORGANISATION_WITHOUT_CB + "\"," +
                        "}" +
                        "}" +
                        "}"));

        //Act
        chargebeeService.processEvent(customerCreatedEvent);

        //Verify
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITHOUT_CB);
        assertTrue(organisation.getHasChargebeeAccount());
    }

    @Test
    public void chargebeeCustomerDeleted() throws Exception {
        //Arrange
        String eventId = "ev_Izy9T8TR9Nm1xzeH";
        String customerDeletedEvent = "{\"id\": \"" + eventId + "\",}";

        Mockito.when(apiClient.getEventById(eventId))
                .thenReturn(new Event(new JSONObject("{" +
                        "\"event_type\": \"customer_deleted\"," +
                        "\"content\": {" +
                        "\"customer\": {" +
                        "\"id\": \"" + ORGANISATION_WITH_CB + "\"," +
                        "}" +
                        "}" +
                        "}")));

        //Act
        chargebeeService.processEvent(customerDeletedEvent);

        //Verify
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);
        assertFalse(organisation.getHasChargebeeAccount());
    }

    @Test
    public void chargebeeCustomerDeletedAndSubscriptionRemoved() throws Exception {
        //Arrange
        String eventId = "ev_Izy9T8TR9Nm1xzeH";
        String customerDeletedEvent = "{\"id\": \"" + eventId + "\",}";

        Mockito.when(apiClient.getEventById(eventId))
                .thenReturn(new Event(new JSONObject("{" +
                        "\"event_type\": \"customer_deleted\"," +
                        "\"content\": {" +
                        "\"customer\": {" +
                        "\"id\": \"" + ORGANISATION_WITH_CB + "\"," +
                        "}" +
                        "}" +
                        "}")));

        //Act
        chargebeeService.processEvent(customerDeletedEvent);

        //Verify
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);
        assertFalse(organisation.getHasChargebeeAccount());
        assertEquals(SubscriptionType.Test, organisation.getSubscription().getType());
    }

    @Test
    public void subscriptionCreated() throws Exception {
        //Arrange
        String eventId = "ev_Hr5514pR9BCPCF13bl";
        String subscriptionCreatedEvent = "{\"id\": \"" + eventId + "\",}";

        Mockito.when(apiClient.getEventById(eventId))
                .thenReturn(new Event(
                        new JSONObject("{" +
                                "\"event_type\": \"subscription_created\"," +
                                "\"content\": {" +
                                "\"customer\": {" +
                                "\"id\": \"" + ORGANISATION_WITH_CB + "\"," +
                                "}," +
                                "\"subscription\": {" +
                                "\"plan_id\": \"" + "pocket-tool-monthly" + "\"," +
                                "}" +
                                "}" +
                                "}")));

        //Act
        chargebeeService.processEvent(subscriptionCreatedEvent);

        //Verify
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);
        assertEquals(SubscriptionType.Pocket, organisation.getSubscription().getType());
        assertEquals(PaymentPlan.Monthly, organisation.getSubscription().getPaymentPlan());
    }

    @Test
    public void subscriptionDeleted() throws Exception {
        //Arrange
        String eventId = "ev_3Nl7oGgQqGGSBv75";

        String subscriptionDeletedEvent = "{\"id\": \"" + eventId + "\",}";

        Mockito.when(apiClient.getEventById(eventId))
                .thenReturn(new Event(
                        new JSONObject("{" +
                                "\"event_type\": \"subscription_deleted\"," +
                                "\"content\": {" +
                                "\"customer\": {" +
                                "\"id\": \"" + ORGANISATION_WITH_CB + "\"," +
                                "}" +
                                "}" +
                                "}")));

        //Act
        chargebeeService.processEvent(subscriptionDeletedEvent);

        //Verify
        Organisation organisation = organisationRepo.getOne(ORGANISATION_WITH_CB);
        assertEquals(SubscriptionType.Test, organisation.getSubscription().getType());
    }
}
