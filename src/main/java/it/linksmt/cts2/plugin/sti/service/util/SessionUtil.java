package it.linksmt.cts2.plugin.sti.service.util;

import java.util.ArrayList;

public final class SessionUtil {

	private SessionUtil() { }

	public static StiUserInfo getLoggedUser() {

		// TODO: introdurre meccanismo SSO con Liferay
		ArrayList<String> groups = new ArrayList<>();
		groups.add("Test");

		return new StiUserInfo("giorgio.test", groups);
	}

}
