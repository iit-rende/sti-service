package it.linksmt.cts2.plugin.sti.db;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;

import java.text.ParseException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class DeleteCodeSystemTest {
	
	
	
	private static final String CALL_FUNCTION_REMOVE_CODE_SYSTEM = " SELECT * FROM remove_code_system(:id) ";
	
	public static void main(final String[] args) throws ParseException {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);

		deleteCodeySystemById();


	}

	private static void deleteCodeySystemById() {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		Session session  = hibernateUtil.getSessionFactory().openSession();
		try {
			session.beginTransaction();
			
			SQLQuery querySelect = session.createSQLQuery(CALL_FUNCTION_REMOVE_CODE_SYSTEM);
			querySelect.setLong("id",272);
			System.out.println(querySelect.getQueryString());
			querySelect.uniqueResult();

			session.clear();
			session.getTransaction().commit();
			session.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
