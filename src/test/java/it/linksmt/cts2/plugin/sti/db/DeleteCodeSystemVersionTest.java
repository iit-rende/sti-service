package it.linksmt.cts2.plugin.sti.db;

import it.linksmt.cts2.plugin.sti.db.commands.delete.DeleteCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.text.ParseException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class DeleteCodeSystemVersionTest {
	public static void main(final String[] args) throws ParseException {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);
		// LogManager.getLogger("").setLevel(Level.WARN);

		deleteCodeySystemByIdVerion();


	}

	private static void deleteCodeySystemByIdVerion() {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();

		try {
			Long csVersion = 281L;
			
			hibernateUtil.executeBySystem(new DeleteCodeSystemVersionById(csVersion));

			
		} catch (StiHibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StiAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
