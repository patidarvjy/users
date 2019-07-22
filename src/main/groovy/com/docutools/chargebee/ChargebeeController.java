package com.docutools.chargebee;

import com.chargebee.org.json.JSONObject;
import com.docutools.users.UserManager;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/chargebee-api/v2")
@PreAuthorize("hasAuthority('owner')")
public class ChargebeeController {

    private static final Logger log = LoggerFactory.getLogger(ChargebeeController.class);
    private static final HttpStatus OK_TO_PREVENT_RETRIES = HttpStatus.OK;

    private final ChargebeeService chargebeeService;

    @Value("${docutools.chargebee.enabled:false}")
    private boolean chargebeeEnabled;


    @Autowired
    public ChargebeeController(ChargebeeService chargebeeService) {
        this.chargebeeService = chargebeeService;
    }

    @ApiOperation("Get link for checkout page (for not registered users)")
    @PostMapping(path = "/checkout/{planId}")
    public HttpEntity<String> getCheckoutPage(@PathVariable String planId) {
        log.info("POST /chargebee-api/v2/checkout/{}", planId);

        if (chargebeeEnabled) {
            return chargebeeService.getCheckoutPage(planId)
                    .map(JSONObject::toString)
                    .map(HttpEntity::new)
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @ApiOperation("Get link for portal (for registered users)")
    @PostMapping(path = "/portal")
    public HttpEntity<String> getPortal() {
        log.info("POST /chargebee-api/v2/portal by {}", UserManager.getCurrentActor());

        if (chargebeeEnabled) {
            return chargebeeService.getPortal()
                    .map(JSONObject::toString)
                    .map(HttpEntity::new)
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @ApiOperation(value = "Chargebee callback", notes = "Callback used by chargebee to manage organisations and their subscriptions")
    @PostMapping(path = "/event")
    @PreAuthorize("true")
    public HttpEntity<Boolean> chargbeeCallback(@RequestBody String jsonEvent) {
        log.debug("POST /chargebee-api/v2/event/ Body: {}", jsonEvent);
        if (chargebeeEnabled) {
            return chargebeeService.processEvent(jsonEvent)
                    .map(HttpEntity::new)
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        } else {
            return new ResponseEntity<>(OK_TO_PREVENT_RETRIES);
        }
    }
}
