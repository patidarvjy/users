package com.docutools.chargebee;

public enum ChargebeePlan {
    POCKET_TOOL_MONTHLY("pocket-tool-monthly"),
    COMBO_TOOL_MONTHLY("combo-tool-monthly"),
    MULTI_TOOL_MONTHLY("multi-tool-monthly"),
    POCKET_TOOL_YEARLY("pocket-tools-yearly"),
    COMBO_TOOL_YEARLY("combo-tools-yearly"),
    MULTI_TOOL_YEARLY("multi-tool-user-yearly"),
    ;

    private final String id;

    ChargebeePlan(String id) {
        this.id = id;
    }

    public static ChargebeePlan getEnum(String id) {
        for (ChargebeePlan chargebeePlan : ChargebeePlan.values()) {
            if (chargebeePlan.id.equals(id)) {
                return chargebeePlan;
            }
        }
        //TODO Create correct default licence (test)?
        return POCKET_TOOL_MONTHLY;
    }
}
