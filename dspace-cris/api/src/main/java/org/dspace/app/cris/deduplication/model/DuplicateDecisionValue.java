package org.dspace.app.cris.deduplication.model;

public enum DuplicateDecisionValue {

	REJECT("reject"), VERIFY("verify");

    private String text;

    DuplicateDecisionValue(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    public static DuplicateDecisionValue fromString(String text) {
        if (text == null) {
            return null;
        }
        for (DuplicateDecisionValue b : DuplicateDecisionValue.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No visibility enum with text " + text + " found");
    }
}