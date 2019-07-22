package com.docutools.roles

import com.docutools.config.api.BaseController
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = '/api/v2/me', produces = 'application/json')
class PrivilegeApi extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(PrivilegeApi)

    @Autowired
    private PermissionManager permissionManager

    @ApiOperation(value = "Check Privilege of the Current User")
    @GetMapping(path = "/checkPrivilege")
    PrivilegeCheckDTO checkPrivilege(@RequestParam UUID projectId,
                                     @RequestParam(required = false, defaultValue = 'false') boolean any,
                                     @RequestParam Privilege[] privileges) {
        log.info('GET /api/v2/me/checkPrivilege?projectId={}&privileges={}&any={} by {}', projectId, privileges, any, userName)

        if(!privileges)
            privileges = []
        permissionManager.checkPrivilege(projectId, privileges?.toList(), any)
    }

    @ApiOperation(value = "Check if the User is a member of a Project")
    @GetMapping(path = "/checkMember")
    boolean isMember(@RequestParam(required = true) UUID projectId) {
        log.debug('GET /api/v2/me/checkMember?projectId={} by {}', projectId, userName)
        permissionManager.isMember(projectId)
    }

}
