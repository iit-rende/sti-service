package it.linksmt.cts2.plugin.sti.db.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiUserInfo;

public final class HibernateUtil {

	private HibernateUtil() { }

	private static Logger log = Logger.getLogger(HibernateUtil.class);

	private SessionFactory sessionFactory;
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static HibernateUtil create(
			final String connectionUrl,
			final String connectionUsername,
			final String connectionPassword,
			final String configurationResource,
			final boolean enableDebug) throws StiHibernateException {

		try {

			Configuration hibConf = new Configuration();
	    	hibConf.configure(configurationResource);

	    	hibConf.setProperty("hibernate.connection.url", connectionUrl);
	        hibConf.setProperty("hibernate.connection.username", connectionUsername);
	        hibConf.setProperty("hibernate.connection.password", connectionPassword);
	        // hibConf.setProperty("hibernate.default_schema", "");

	        if (enableDebug) {
	        	hibConf.setProperty("hibernate.show_sql", "true");
	        }

	        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(
	        		hibConf.getProperties()).buildServiceRegistry();

	        SessionFactory sessFact = hibConf.buildSessionFactory(serviceRegistry);
	        if (sessFact != null) {

	        	HibernateUtil product = new HibernateUtil();
		        product.sessionFactory = sessFact;

				return product;
	        }

	        throw new StiHibernateException("Session Factory non inizializzata.");
		}
		catch (Throwable ex) {
            throw new StiHibernateException(ex.getMessage(), ex);
        }
	}

	public Object executeByUser(final HibernateCommand command,
			final StiUserInfo userInfo) throws
			StiHibernateException, StiAuthorizationException {

		if ( userInfo == null ) {
			throw new StiAuthorizationException("Occorre effettuare "
					+ "l'autenticazione per acceddere alla risorsa.");
		}

		return execute(command, userInfo);
	}

	public Object executeBySystem(final HibernateCommand command) throws
			StiHibernateException, StiAuthorizationException {

		return execute(command, null);
	}

	private Object execute(final HibernateCommand command,
			final StiUserInfo userInfo) throws
				StiHibernateException, StiAuthorizationException {

		if (command == null) {
			return null;
		}

		// String logStatus = "OK";
		Object result = null;

		Session session = null;
		Transaction tx = null;

		try
		{
			if (sessionFactory == null) {
				throw new StiHibernateException("Session Factory non inizializzata. " +
						"Occorre prima di tutto invocare il metodo \"create()\".");
			}

			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			command.setUserInfo(userInfo);
			if ( (userInfo != null) && (!userInfo.isAdministrator())) {
				command.checkPermission(session);
			}

			result = command.execute(session);

			if (session.isOpen()) {
				session.clear();
	            tx.commit();
			}
        }
        catch (Exception e) {

        	// logStatus = e.getClass().getName() + ": " + e.getMessage();
        	log.error("Errore durante l'esecuzione della query.", e);

            try {
            	if (tx != null) {
            		tx.rollback();
            	}
            }
            catch (Exception te) { }

            try {
            	if (session != null	) {
            		session.clear();
            	}
            }
            catch (Exception te) { }

            if (e instanceof StiAuthorizationException) {
            	throw (StiAuthorizationException)e;
            }

            if (e instanceof StiHibernateException) {
            	throw (StiHibernateException)e;
            }

            throw new StiHibernateException(e.getMessage(), e);
        }
        finally {

        	if ( (session != null) && (session.isOpen()) ) {
        		session.close();
        	}
        }

		return result;
	}
}
