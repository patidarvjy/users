package com.docutools.avatar;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v2")
public class AvatarController {

    private static final Logger log = LoggerFactory.getLogger(AvatarController.class);

    @Autowired
    private AvatarService avatarService;

    @ApiOperation(value = "Download my Avatar")
    @GetMapping(path = "/me/avatar")
    public void downloadMyAvatar(HttpServletResponse response, HttpServletRequest request) {
        log.debug("GET /api/v2/me/avatar");
        avatarService.writeThumbnailToResponse(response, request);
    }

    @ApiOperation(value = "Download my Avatar in original size")
    @GetMapping(path = "/me/avatar/original")
    public void downloadMyAvatarInOriginalSize(HttpServletResponse response) {
        log.debug("GET /api/v2/me/avatar/original");
        avatarService.writeAvatarToResponse(response);
    }

    @ApiOperation(value = "Download User Avatar")
    @GetMapping(path = "/users/{id}/avatar")
    public void downloadUsersAvatar(@PathVariable UUID id, HttpServletResponse response, HttpServletRequest request) {
        log.debug("GET /api/v2/users/{}/avatar", id);
        avatarService.writeThumbnailToResponse(id, response, request);
    }

    @ApiOperation(value = "Download User Avatar in original size")
    @GetMapping(path = "/users/{id}/avatar/original")
    public void downloadUsersAvatarInOriginalSize(@PathVariable UUID id, HttpServletResponse response) {
        log.debug("GET /api/v2/users/{}/avatar/original", id);
        avatarService.writeAvatarToResponse(id, response);
    }

    @ApiOperation(value = "Change my Avatar")
    @PutMapping(path = "/me/avatar")
    public void changeMyAvatar(@RequestPart("avatar")MultipartFile avatar) {
        log.debug("PUT /api/v2/me/avatar avatar: {} ({} {} Bytes)", avatar.getOriginalFilename(), avatar.getContentType(), avatar.getSize());
        avatarService.uploadAvatar(avatar);
    }

    @ApiOperation(value = "Change User Avatar")
    @PutMapping(path = "/users/{id}/avatar")
    public void changeUsersAvatar(@PathVariable UUID id, @RequestPart("avatar") MultipartFile avatar) {
        log.debug("PUT /api/v2/users/{}/avatar avatar: {} ({} {} Bytes)", id, avatar.getOriginalFilename(), avatar.getContentType(), avatar.getSize());
        avatarService.uploadAvatar(id, avatar);
    }

    @ApiOperation(value = "Delete my Avatar")
    @DeleteMapping(path = "/me/avatar")
    public void deleteMyAvatar() {
        log.debug("DELETE /api/v2/me/avatar");
        avatarService.removeAvatar();
    }

    @ApiOperation(value = "Delete User Avatar")
    @DeleteMapping(path = "/users/{id}/avatar")
    public void deleteUsersAvatar(@PathVariable UUID id) {
        log.debug("DELETE /api/v2/users/{}/avatar", id);
        avatarService.removeAvatar(id);
    }

}
