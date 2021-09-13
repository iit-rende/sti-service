package it.linksmt.cts2.plugin.sti.db.hibernate;

import org.hibernate.Session;

import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiUserInfo;

public abstract class HibernateCommand {

	protected StiUserInfo userInfo = null;

	public StiUserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(final StiUserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public abstract void checkPermission(final Session session)
			throws  StiAuthorizationException, StiHibernateException;

	public abstract Object execute(final Session session)
			throws  StiAuthorizationException, StiHibernateException;
}
