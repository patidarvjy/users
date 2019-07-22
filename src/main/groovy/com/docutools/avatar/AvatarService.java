package com.docutools.avatar;

import com.docutools.contacts.ProjectContact;
import com.docutools.contacts.ProjectContactRepository;
import com.docutools.exceptions.ErrorCodes;
import com.docutools.roles.PermissionManager;
import com.docutools.roles.Privilege;
import com.docutools.storage.FileType;
import com.docutools.storage.StorageAccessKey;
import com.docutools.storage.StorageEngine;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.SessionManager;
import com.docutools.users.UserRepo;
import com.docutools.users.values.ChecksumAlgorithm;
import com.docutools.users.values.ProfilePicture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static com.docutools.exceptions.ExceptionHelper.newBadRequestError;
import static com.docutools.exceptions.ExceptionHelper.newForbiddenError;
import static com.docutools.exceptions.ExceptionHelper.newInternalServerError;
/**
 * Service for storing and accessing User Avatars.
 *
 * <b>Notes for Development:</b>
 * Currently much of the code in this class handles old accounts where avatars where stored as Blobs in the database.
 * Eventually the goal is to remove this and only do the storing and removing in this class, while the access should be
 * done directly using S3 links to AWS S3, <i>maybe</i> still leaving the access method here for testing with FileSystem
 * as StorageEngine implementation.
 *
 * @author amp
 * @since 1.0.0
 */
@Service
public class AvatarService {

    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    public static final int THUMBNAIL_WIDTH = 264;

    @Autowired
    private StorageEngine storageEngine;
    @Autowired
    private UserRepo usersRepository;
    @Autowired
    private ProjectContactRepository contactRepository;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private PermissionManager permissionManager;

    public void writeAvatarToResponse(HttpServletResponse response) {
        writeAvatarToResponse(sessionManager.getCurrentUser().getId(), response);
    }

    /**
     * Loads the Avatar and writes it to the response {@link java.io.OutputStream}.
     * When no avatar specified will give 204 status code.
     *
     * @param id user's ID
     * @param response {@link HttpServletResponse}
     */
    public void writeAvatarToResponse(UUID id, HttpServletResponse response) {
        StorageAccessKey key = toKey(id);
        if(storageEngine.exists(key)) {
            response.setContentType("image/jpeg");
            response.setContentLengthLong(storageEngine.size(key));
            try(InputStream in = storageEngine.openStreamTo(key)) {
                StreamUtils.copy(in, response.getOutputStream());
                return;
            } catch (IOException e) {
                throw newInternalServerError("Could not read avatar!", e);
            }
        }
        if(usersRepository.existsById(id)) {
            DocutoolsUser user = usersRepository.getOne(id);
            ProfilePicture avatar = user.getAvatar();
            if(avatar != null && avatar.getData() != null) {
                byte[] data = avatar.getData();
                if(!"image/jpeg".equals(avatar.getContentType())) {
                    try(ByteArrayInputStream in = new ByteArrayInputStream(data)) {
                        BufferedImage image = ImageIO.read(in);
                        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                            ImageIO.write(image, "jpeg", out);
                            data = out.toByteArray();
                        }
                    } catch (IOException e) {
                        throw newInternalServerError("Error when converting to JPEG!", e);
                    }
                }
                storageEngine.save(key, data);
                log.debug("Moved Avatar from Database to StorageEngine for {}.", id);
                if(avatar.getThumbnail() != null) {
                    StorageAccessKey tKey = toThumbnailKey(id);
                    storageEngine.save(tKey, avatar.getThumbnail());
                }
                user.setAvatar(null);
                usersRepository.save(user);
                writeAvatarToResponse(id, response);
                return;
            }
        }
        response.setStatus(204);
    }

    public void writeThumbnailToResponse(HttpServletResponse response, HttpServletRequest request) {
        writeThumbnailToResponse(sessionManager.getCurrentUser().getId(), response,request);
    }

    public Optional<byte[]> getAvatarData(UUID id){
        StorageAccessKey key = toKey(id);
        if(storageEngine.exists(key)) {
            return Optional.ofNullable(storageEngine.download(key));
        }
        if(usersRepository.existsById(id)) {
            DocutoolsUser user = usersRepository.getOne(id);
            ProfilePicture avatar = user.getAvatar();
            if(avatar != null && avatar.getData() != null) {
                return Optional.ofNullable(avatar.getData());
            }
        }
        return Optional.empty();
    }

    /**
     * Loads the Avatar's thumbnail and writes it to the response {@link java.io.OutputStream}.
     * When no avatar specified will give 204 status code.
     *
     * @param id user's ID
     * @param response {@link HttpServletResponse}
     */
    public void writeThumbnailToResponse(UUID id, HttpServletResponse response, HttpServletRequest request) {
        StorageAccessKey key = toThumbnailKey(id);
        DocutoolsUser user = sessionManager.getCurrentUser();
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (!StringUtils.isEmpty(ifNoneMatch) && ifNoneMatch.equals(user.getAvatarThumbnailChecksum())) {
            response.setHeader(HttpHeaders.ETAG, user.getAvatarThumbnailChecksum());
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            return;
        }
        if(storageEngine.exists(key)) {
            if (StringUtils.isEmpty(user.getAvatarThumbnailChecksum())) {
                generateThumbnailChecksum(user.getId(), key);
            }
            response.setHeader(HttpHeaders.ETAG, user.getAvatarThumbnailChecksum());
            response.setContentType("image/jpeg");
            response.setContentLengthLong(storageEngine.size(key));
            try(InputStream in = storageEngine.openStreamTo(key)) {
                StreamUtils.copy(in, response.getOutputStream());
                return;
            } catch (IOException e) {
                throw newInternalServerError("Could not read avatar thumbnail!", e);
            }
        }
        StorageAccessKey oKey = toKey(id);
        if(storageEngine.exists(oKey)) {
            try(InputStream in = storageEngine.openStreamTo(oKey)) {
                BufferedImage image = ImageIO.read(in);
                image = createThumbnail(image, true);
                save(key, image);
                writeThumbnailToResponse(id, response, request);
                return;
            } catch (IOException e) {
                throw newInternalServerError("Error while generating thumbnail!", e);
            }
        }
        response.setStatus(204);
    }

    public void uploadAvatar(MultipartFile file) {
        uploadAvatar(sessionManager.getCurrentUser().getId(), file);
    }

    /**
     * Uploads a new images as avatar.
     *
     * @param id user or contact's ID
     * @param file the file
     */
    public void uploadAvatar(UUID id, MultipartFile file) {
        canModifyAvatar(id);
        try(ByteArrayInputStream in = new ByteArrayInputStream(file.getBytes())) {
            BufferedImage image = ImageIO.read(in);
            boolean isJpeg = MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(file.getContentType());
            BufferedImage thumbnail = createThumbnail(image, isJpeg);
            save(toKey(id), isJpeg ? image : toJpg(image));
            log.debug("Uploaded new Avatar for {}.", id);
            StorageAccessKey thumbnailKey = toThumbnailKey(id);
            save(thumbnailKey, thumbnail);
            generateThumbnailChecksum(id, thumbnailKey);
            log.debug("Uploaded Avatar Thumbnail for {}.", id);
        } catch (IOException e) {
            throw newInternalServerError("Error while storing Avatar!", e);
        }
    }

    private void generateThumbnailChecksum(UUID id, StorageAccessKey thumbnailKey) {
        if(usersRepository.existsById(id)){
            DocutoolsUser user = usersRepository.getOne(id);
            String checksum = storageEngine.md5AsHex(thumbnailKey);
            user.setAvatarThumbnailChecksum(checksum);
            user.setChecksumAlgorithm(ChecksumAlgorithm.MD5);
            usersRepository.save(user);
        }
    }

    public void removeAvatar() {
        removeAvatar(sessionManager.getCurrentUser().getId());
    }

    /**
     * Deletes the avatar for this user or contact.
     *
     * @param id user or contact's ID
     */
    public void removeAvatar(UUID id) {
        canModifyAvatar(id);
        StorageAccessKey key = toKey(id);
        if(storageEngine.exists(key)) {
            storageEngine.delete(key);
            log.debug("Deleted Avatar for {}!", id);
        } else {
            log.debug("Did not delete Avatar for {}, doesn't exist.", id);
        }
        StorageAccessKey thumbnailKey = toThumbnailKey(id);
        if(storageEngine.exists(thumbnailKey)) {
            storageEngine.delete(thumbnailKey);
            if(usersRepository.existsById(id)){
                DocutoolsUser user = usersRepository.getOne(id);
                user.setAvatarThumbnailChecksum("");
                user.setChecksumAlgorithm(ChecksumAlgorithm.NONE);
                usersRepository.save(user);
            }
            log.debug("Deleted Avatar Thumbnail for {}.", id);
        } else {
            log.debug("Did not delete Avatar Thumbnail for {}, doesn't exist.", id);
        }
    }

    private StorageAccessKey toKey(UUID id) {
        return new StorageAccessKey(FileType.Misc, String.format("users/avatars/%s.jpg", id));
    }

    private StorageAccessKey toThumbnailKey(UUID id) {
        return new StorageAccessKey(FileType.Misc, String.format("users/avatars/thumbnails/%s.jpg", id));
    }

    @NotNull
    private BufferedImage createThumbnail(BufferedImage image, boolean isJpeg) {
        int width = image.getWidth();
        if(width > THUMBNAIL_WIDTH) {
            int scaledHeight = (int)(((double)image.getHeight())/((double)image.getWidth())*THUMBNAIL_WIDTH);
            Image thumbnail = image.getScaledInstance(THUMBNAIL_WIDTH, scaledHeight, Image.SCALE_SMOOTH);
            image = new BufferedImage(THUMBNAIL_WIDTH, scaledHeight, isJpeg ? image.getType() : BufferedImage.TYPE_INT_RGB);
            image.createGraphics().drawImage(thumbnail, 0, 0, Color.WHITE, null);
        }
        return image;
    }

    private void save(StorageAccessKey key, BufferedImage image) throws IOException {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", out);
            storageEngine.save(key, out.toByteArray());
        }
    }

    /**
     * Convert to Jpeg by removing alpha channel
     * @param image
     * @return
     */
    private BufferedImage toJpg(BufferedImage image) {
        BufferedImage jpegImage = new BufferedImage(image.getWidth(),
            image.getHeight(), BufferedImage.TYPE_INT_RGB);
        jpegImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
        return jpegImage;
    }

    /**
     * Checks whether the current user can modify the avatar.
     *
     * @param id ID of the user/avatar.
     */
    private void canModifyAvatar(UUID id) {
        if(usersRepository.existsById(id)) {
            DocutoolsUser user = usersRepository.getOne(id);
            DocutoolsUser currentUser = sessionManager.getCurrentUser();
            if(!((currentUser.getOrganisation().getId().equals(user.getOrganisation().getId()) && currentUser.isAdmin()) || user.getId().equals(currentUser.getId()))) {
                throw newForbiddenError("Users can only update their own avatar!");
            }
        } else if(contactRepository.existsById(id)) {
            ProjectContact contact = contactRepository.getOne(id);
            if(!permissionManager.hasPrivileges(contact.getProjectId(), Privilege.ManageTeam)) {
                throw newForbiddenError("User has not the privilege to change contacts in this project!");
            }
        } else {
            throw newBadRequestError(ErrorCodes.MODIFY_AVATAR_FOR_UNKNOWN_USER, id.toString());
        }
    }

}
