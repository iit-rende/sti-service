package it.linksmt.cts2.plugin.sti.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.util.spring.AbstractSpringServiceProvider;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

@Component("stiServiceProvider")
public class StiServiceProvider extends AbstractSpringServiceProvider {

	private static Logger log = Logger.getLogger(StiServiceProvider.class);

	private static HibernateUtil hibernateUtil;
	static {
		try {
			hibernateUtil = HibernateUtil.create(
					StiAppConfig.getProperty(StiServiceConfiguration.DB_STI_SERVER_ADDRESS),
					StiAppConfig.getProperty(StiServiceConfiguration.DB_STI_USERNAME),
					StiAppConfig.getProperty(StiServiceConfiguration.DB_STI_PASSWORD),
					StiServiceConfiguration.HIBERNANTE_CONFIGURATION_RESOURCE, false);
		}
		catch (StiHibernateException e) {
			log.error("Errore di accesso al database!!!", e);
		}
	}

	public static HibernateUtil getHibernateUtil() {
		return hibernateUtil;
	}
}
