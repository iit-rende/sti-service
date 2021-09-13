package it.linksmt.cts2.plugin.sti.service.util;

import java.util.List;

public class StiUserInfo {

	private List<String> groupIds = null;
	private String identityID = null;

	public StiUserInfo(final String identityID, final List<String> groupIds) {
		this.groupIds = groupIds;
		this.identityID = identityID;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public String getIdentityID() {
		return identityID;
	}

	public boolean isUserInGroup(final String checkGroup) {

		if ((groupIds == null) || StiServiceUtil.isNull(checkGroup)) {
			return false;
		}

		for (String sGroup : groupIds) {
			if (StiServiceUtil.trimStr(sGroup).equalsIgnoreCase(StiServiceUtil.trimStr(checkGroup))) {
				return true;
			}
		}

		return false;
	}

	public boolean isAdministrator() {
		// TODO: gestire nella corretta maniera
		return  true;
	}
}
