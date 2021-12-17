package org.dspace.notify;

import java.util.Arrays;
import java.util.Comparator;

import org.dspace.core.I18nUtil;
import org.dspace.ldn.LDNMetadataFields;

public enum NotifyStatus {

	PENDING_REVIEW(1), ONGOING(2), REVIEWED(3), PENDING_ENDORSEMENT(4), ENDORSED(5);

	private int order;

	private NotifyStatus(int order) {
		this.order = order;
	}

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

	public String getQualifierForNotifyStatus() {
		switch (this) {
		case PENDING_REVIEW:
			return LDNMetadataFields.REQUEST;
		case ONGOING:
			return LDNMetadataFields.EXAMINATION;
		case REVIEWED:
			return LDNMetadataFields.REVIEW;
		case PENDING_ENDORSEMENT:
			return LDNMetadataFields.REQUEST;
		case ENDORSED:
			return LDNMetadataFields.ENDORSMENT;
		default:
			return "";
		}
	}

	public static NotifyStatus getEnumFromString(String status) {
		for (NotifyStatus enumStatus : NotifyStatus.values()) {
			if (enumStatus.toString().equals(status))
				return enumStatus;
		}
		return null;
	}

	public static NotifyStatus[] getOrderedValues() {
		NotifyStatus[] notifyStatuses = NotifyStatus.values();
		Arrays.sort(notifyStatuses, new Comparator<NotifyStatus>() {

			@Override
			public int compare(NotifyStatus arg0, NotifyStatus arg1) {
				if (arg0.order < arg1.order)
					return -1;
				else if (arg0.order > arg1.order)
					return 1;
				else
					return 0;
			}

		});
		return notifyStatuses;
	}

}
