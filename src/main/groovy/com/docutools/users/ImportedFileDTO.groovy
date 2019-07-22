package com.docutools.users

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.web.multipart.MultipartFile

import java.time.Instant

@ApiModel(value = "Imported File Resource")
class ImportedFileDTO {

    @ApiModelProperty(value = "Id of the Imported File")
    UUID id
    @ApiModelProperty(value = "MimeType of the imported File")
    String mimeType
    @ApiModelProperty(value = "Size of the Imported File")
    long sizeInBytes
    @ApiModelProperty(value = "Timestamp until when the file is saved")
    Instant savedUntil
    @ApiModelProperty(value = "List of columns")
    String[] columns
    @ApiModelProperty(value = "The delimiter used")
    Character delimiter
    @ApiModelProperty(value = "FileEncoding of the File")
    String fileEncoding

    ImportedFileDTO() {
    }

    ImportedFileDTO(UUID storageId, MultipartFile original, Instant savedUntil, String[] columns, Character delimiter) {
        this.id = storageId
        this.mimeType = original.contentType
        this.sizeInBytes = original.size
        this.savedUntil = savedUntil
        this.columns = columns
        this.delimiter = delimiter
    }

}
