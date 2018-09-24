package org.dspace.app.cris.deduplication.model;

public enum DuplicateDecisionType {
	
	WORKSPACE("WORKSPACE"), WORKFLOW("WORKFLOW"), ADMIN("ADMIN");

    private String text;

    DuplicateDecisionType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    public static DuplicateDecisionType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (DuplicateDecisionType b : DuplicateDecisionType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No Decision enum with type " + text + " found");
    }
}