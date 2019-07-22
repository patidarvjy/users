package com.docutools.roles;

public enum RoleType {
    PowerUser(10),
    Assistant(5),
    Viewer(0),
    ExternalVisitor(1),
    ExternalCommentator(1),
    SubContractor(1),
    Custom(10);

    private int order;

    RoleType(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
