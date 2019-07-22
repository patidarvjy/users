package com.docutools.contacts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public class ProjectContactImport {
    private UUID fileId;
    private UUID projectId;
    private char delimiter = ',';
    private Map<String, Columns> columnsMap;
    private String fileEncoding;

    public ProjectContactImport() {
    }

    @JsonCreator
    public ProjectContactImport(@JsonProperty(required = true, value = "fileId") UUID fileId,
                                @JsonProperty(required = true, value = "projectId") UUID projectId,
                                @JsonProperty(value = "delimiter", defaultValue = ",") char delimiter,
                                @JsonProperty(required = true, value = "columnsMap") Map<String, Columns> columnsMap,
                                @JsonProperty(value = "fileEncoding",defaultValue = "UTF-8") String fileEncoding) {
        this.fileId = fileId;
        this.projectId = projectId;
        this.delimiter = delimiter;
        this.columnsMap = columnsMap;
        this.fileEncoding = fileEncoding;
        if(fileEncoding==null){
            this.fileEncoding = "UTF-8";
        }
    }


    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public Map<String, Columns> getColumnsMap() {
        return columnsMap;
    }

    public void setColumnsMap(Map<String, Columns> columnsMap) {
        this.columnsMap = columnsMap;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }
}
