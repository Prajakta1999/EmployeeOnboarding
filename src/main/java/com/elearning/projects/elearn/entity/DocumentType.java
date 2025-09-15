package com.elearning.projects.elearn.entity;

public enum DocumentType {
    ID_PROOF("ID Proof", true),
    PAN_AADHAR("PAN/Aadhar", false),
    BANK_DETAILS("Bank Account Details", false),
    OFFER_LETTER("Offer Letter Acceptance", true);
    
    private final String displayName;
    private final boolean mandatory;
    
    DocumentType(String displayName, boolean mandatory) {
        this.displayName = displayName;
        this.mandatory = mandatory;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
}
