package com.docutools.scheduler.jobs;

import org.quartz.Job;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class DocutoolsJob implements Job {

    private String name;
    private UUID id;

    public DocutoolsJob(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    public DocutoolsJob() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getClass().toString() + "{" +
            "name='" + name + '\'' +
            ", id=" + id +
            '}';
    }

    public Map<String, String> getPropertiesMap() {
        HashMap<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("name", this.getName());
        propertiesMap.put("id", this.getId().toString());
        return propertiesMap;
    }

}
