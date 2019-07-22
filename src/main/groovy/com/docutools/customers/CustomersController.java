package com.docutools.customers;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.docutools.exceptions.ExceptionHelper.newInternalServerError;

@RestController
@RequestMapping(path = "/api/v2")
@PreAuthorize("hasAuthority('sustain_user')")
public class CustomersController {

    private static final Logger log = LoggerFactory.getLogger(CustomersController.class);

    @Autowired
    private CustomerService customerService;

    @ApiOperation(value = "Get Customer")
    @GetMapping(path = "/customers/{id}")
    public HttpEntity<Customer> getCustomer(@PathVariable UUID id) {
        log.debug("GET /api/v2/customers/{}", id);
        return customerService.getCustomer(id)
                .map(HttpEntity::new)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Update Customer", notes = "Required Attributes: organisationId")
    @PatchMapping(path = "/customers/{id}")
    public HttpEntity<Customer> updateCustomer(@PathVariable UUID id, @RequestBody CustomerUpdate customerUpdate) {
        log.debug("PATCH /api/v2/customers/{} Body: {}", id, customerUpdate);
        customerUpdate.setOrganisationId(id);
        return customerService.update(customerUpdate)
                .map(HttpEntity::new)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get Customers Paged")
    @GetMapping(path = "/customers")
    public Page<Customer> getCustomersPaged(@RequestParam(required = false, defaultValue = "0") int page,
                                            @RequestParam(required = false, defaultValue = "25") int size,
                                            @RequestParam(required = false, defaultValue = "name") String sortBy,
                                            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
                                            @RequestParam(required = false, defaultValue = "") String search) {
        log.debug("GET /api/v2/customers?page={}&size={}&sortBy={}&sortDirection={}&search={}",
                page, size, sortBy, sortDirection, search);
        return customerService.getCustomersPaged(search, PageRequest.of(page, size, sortDirection, sortBy));
    }

    @ApiOperation(value = "Get all Customers")
    @GetMapping(path = "/customers/all")
    public List<Customer> getAllCustomers(@RequestParam(required = false, defaultValue = "name") String sortBy,
                                          @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
                                          @RequestParam(required = false, defaultValue = "") String search) {
        log.debug("GET /api/v2/customers?sortBy={}&sortDirection={}&search={}",
                sortBy, sortDirection, search);
        return customerService.getAllCustomers(search, Sort.by(sortDirection, sortBy));
    }

    @ApiOperation(value = "Export All Customers as CSV")
    @GetMapping(path = "/customers/all/csv")
    public void exportAllCustomersAsCsv(@RequestParam(required = false, defaultValue = "name") String sortBy,
                                        @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
                                        @RequestParam(required = false, defaultValue = "") String search,
                                        HttpServletResponse response) {
        log.debug("GET /api/v2/customers/all/csv?sortBy={}&sortDirection={}&search={}", sortBy, sortDirection, search);
        try {
            response.setContentType("text/csv");
            customerService.writeAllCustomersAsCsv(response.getOutputStream(), search, sortBy, sortDirection);
        } catch (IOException e) {
            throw newInternalServerError("IOException when writing all Customers to CSV Stream!", e);
        }
    }

    @ApiOperation(value = "List Employees")
    @GetMapping(path = "/customers/{id}/employees")
    public List<CustomerUser> listEmployees(@PathVariable UUID id) {
        log.debug("GET /api/v2/customers/{}/employees", id);
        return customerService.listEmployees(id);
    }

    @ApiOperation(value = "Invite User")
    @PostMapping(path = "/customers/{id}/employees")
    public CustomerUser inviteUser(@PathVariable UUID id, @RequestBody NewCustomerUser body) {
        log.debug("POST /api/v2/customers/{}/employees Body: {}", id, body);
        return customerService.inviteUser(id, body);
    }

    @ApiOperation(value = "List Customer Accounts")
    @GetMapping(path = "/customers/{id}/accounts")
    public List<CustomerAccount> listAccounts(@PathVariable UUID id) {
        log.debug("GET /api/v2/customers/{}/accounts", id);
        return customerService.getAllCustomerAccounts(id);
    }

    @ApiOperation(value = "List Unassigned User")
    @GetMapping(path = "/customers/{id}/unassignedUsers")
    public List<AccountHolder> listUnassignedUsers(@PathVariable UUID id) {
        log.debug("GET /api/v2/customers/{}/unassignedUsers", id);
        return customerService.getAllUnassignedUsers(id);
    }

    @ApiOperation(value = "List all users")
    @GetMapping(path = "/customers/{id}/users")
    public Page<AccountHolder> listUsers(@RequestParam(required = false, defaultValue = "0") int page,
                                         @RequestParam(required = false, defaultValue = "25") int size,
                                         @RequestParam(required = false, defaultValue = "name") String sortBy,
                                         @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
                                         @PathVariable UUID id) {
        log.debug("GET /api/v2/customers/{}/users?page={}&size={}&sortBy={}&sortDirection={}", id, page, size, sortBy, sortDirection);
        return customerService.getUsersPaged(id, PageRequest.of(page, size, sortDirection, sortBy));
    }

    @ApiOperation(value = "Add Account")
    @PostMapping(path = "/customers/{id}/accounts")
    public HttpEntity<CustomerAccount> addAccount(@PathVariable UUID id) {
        log.debug("POST /api/v2/customers/{}/accounts", id);
        return customerService.addAccount(id)
                .map(HttpEntity::new)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Delete Account")
    @DeleteMapping(path = "/accounts/{id}")
    public HttpEntity<CustomerAccount> deleteAccount(@PathVariable UUID id) {
        log.debug("DELETE /api/v2/accounts/{}", id);
        return customerService.deleteAccount(id)
                .map(HttpEntity::new)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Assign Account")
    @PutMapping(path = "/accounts/{id}/holder")
    public HttpEntity<CustomerAccount> assingAccount(@PathVariable UUID id, @RequestBody AccountHolder holder) {
        log.debug("PUT /api/v2/accounts/{}/holder Body: {}", id, holder);
        return customerService.assignAccount(id, holder)
                .map(HttpEntity::new)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Unassign Account")
    @DeleteMapping(path = "/accounts/{id}/holder")
    public HttpEntity<CustomerAccount> unassignAccount(@PathVariable UUID id) {
        log.debug("DELETE /api/v2/accounts/{}/holder", id);
        return customerService.unassignAccount(id)
                .map(HttpEntity::new)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
