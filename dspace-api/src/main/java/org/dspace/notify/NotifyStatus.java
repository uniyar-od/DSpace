package org.dspace.notify;

import org.dspace.core.I18nUtil;

public enum NotifyStatus {

	PENDING_REVIEW, ONGOING, REVIEWED, PENDING_ENDORSEMENT, ENDORSED;

	@Override
	public String toString() {
		switch (this) {
		case PENDING_REVIEW:
			return I18nUtil.getMessage("coar.notify.pending-review.label");
		case ONGOING:
			return I18nUtil.getMessage("coar.notify.ongoing.label");
		case REVIEWED:
			return I18nUtil.getMessage("coar.notify.reviewed.label");
		case PENDING_ENDORSEMENT:
			return I18nUtil.getMessage("coar.notify.pending-endorsement.label");
		case ENDORSED:
			return I18nUtil.getMessage("coar.notify.endorsed.label");
		default:
			return "UNKNOWN STATUS";
		}
	}
	
	public static NotifyStatus getEnumFromString(String status) {
		for(NotifyStatus enumStatus: NotifyStatus.values()) {
			if(enumStatus.toString().equals(status))
				return enumStatus;
		}
		return null;
	}
}
