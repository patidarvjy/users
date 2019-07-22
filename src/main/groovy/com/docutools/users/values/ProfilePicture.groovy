package com.docutools.users.values

import com.docutools.users.DocutoolsUser
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.Type

import javax.persistence.*
/**
 * Represents the profile picture of a {@link DocutoolsUser}.
 */
@Entity
@Table(name = "profile_pic")
@ApiModel(value = "Profile Picture Resource")
class ProfilePicture {

    @Id
    @Column(name = 'owner_id')
    @ApiModelProperty(value = "Id of the User")
    @Type(type = "pg-uuid") UUID id
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    DocutoolsUser owner
    @ApiModelProperty(value = "The content type of the profile picture")
    @Column(nullable = false) String contentType
    @ApiModelProperty(value = "The data of the profile picture")
    @Column(nullable = false) byte[] data
    @ApiModelProperty(value = "The data of the profile picture thumbnail")
    byte[] thumbnail

    def ProfilePicture() {
    }

    def void setOwner(DocutoolsUser owner) {
        id = owner.id
        this.owner = owner
    }

}
