package it.linksmt.cts2.plugin.sti.db;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConceptTranslation;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

@SuppressWarnings("unchecked")
public class AddTranslationConceptTest {

	private static Logger log = Logger.getLogger(AddTranslationConceptTest.class);

//	SELECT cpt.*,tr.term FROM code_system c
//		INNER JOIN code_system_version v ON c.id = v.codesystemid
//		INNER JOIN code_system_version_entity_membership m ON v.versionid = m.codesystemversionid
//		INNER JOIN code_system_entity_version ev ON m.codesystementityid = ev.codesystementityid
//		INNER JOIN code_system_concept cpt ON ev.versionid = cpt.codesystementityversionid
//		LEFT JOIN code_system_concept_translation tr ON tr.codesystementityversionid = cpt.codesystementityversionid
//	WHERE c.name='TST' AND v.name = 'T2';

	private static String SQL_QUERY_SELECT_NATIVE = ""
			+ "	SELECT cpt.* FROM code_system c " 
			+ "			INNER JOIN code_system_version v ON c.id = v.codesystemid "
			+ " 		INNER JOIN code_system_version_entity_membership m ON v.versionid = m.codesystemversionid " 
			+ "			INNER JOIN code_system_entity_version ev ON m.codesystementityid = ev.codesystementityid "
			+ "			INNER JOIN code_system_concept cpt ON ev.versionid = cpt.codesystementityversionid "
			+ "			LEFT JOIN code_system_concept_translation tr ON tr.codesystementityversionid = cpt.codesystementityversionid"
			+ " WHERE c.name=:CS_NAME AND v.name=:VERSION_NAME";

	// private static String csName = "LOINC";
	// private static String csName = "2.58";
	
	private static String csName = "TST";
	private static String versionName = "T1";
//	private static String lang = StiConstants.LANG_IT;
	private static String lang = StiConstants.LANG_EN;
	
	
	public static void main(final String[] args) throws StiHibernateException, StiAuthorizationException {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);
		// LogManager.getLogger("").setLevel(Level.WARN);

		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		Session session = hibernateUtil.getSessionFactory().openSession();

		SQLQuery querySelect = session.createSQLQuery(SQL_QUERY_SELECT_NATIVE).addEntity(CodeSystemConcept.class);
		querySelect.setString("CS_NAME", csName);
		querySelect.setString("VERSION_NAME", versionName);
		System.out.println(querySelect.getQueryString());

		/*concept recuperati in base al codesystem passato ai queli aggiugere la traduzione*/
		
		List<CodeSystemConcept> conceptListSrc = querySelect.list();
		List<CodeSystemConcept> conceptListTrg = getMockConcept(conceptListSrc,lang);
		
		

		testInsert(session, conceptListSrc);
		testSelect(session);
		testDelete(session, conceptListSrc);

	}

	private static List<CodeSystemConcept> getMockConcept(List<CodeSystemConcept> conceptListSrc,String lang) {
		List<CodeSystemConcept> conceptTraslate =  new ArrayList<CodeSystemConcept>(0);
		for (CodeSystemConcept codeSystemConcept : conceptListSrc) {
			codeSystemConcept.setTerm(codeSystemConcept.getTerm()+"_"+lang);
			conceptTraslate.add(codeSystemConcept);
		}
		return null;
	}

	private static void testInsert(Session session, List<CodeSystemConcept> conceptListSrc) {
		System.out.println("conceptListSrc.size()::" + conceptListSrc.size());
		for (CodeSystemConcept csConc : conceptListSrc) {

			String lang = StiConstants.LANG_EN;
			String term = csConc.getTerm() + "_" + lang;

			System.out.println(" -code:" + csConc.getCode() + " -term:" + csConc.getTerm() + " -description:" + csConc.getDescription());
			addTraslation(session, csConc, term, null, lang);

			Set<CodeSystemConceptTranslation> traslations = csConc.getCodeSystemConceptTranslations();
			for (CodeSystemConceptTranslation i : traslations)
				System.out.println(" - TRADUZIONE:: " + i.getTerm());
		}
	}

	private static void testSelect(Session session) {
		SQLQuery querySelect = session.createSQLQuery(SQL_QUERY_SELECT_NATIVE).addEntity(CodeSystemConcept.class);
		querySelect.setString("CS_NAME", csName);
		querySelect.setString("VERSION_NAME", versionName);
		System.out.println(querySelect.getQueryString());

		/*concept recuperati in base al codesystem passato ai queli aggiugere la traduzione*/
		List<CodeSystemConcept> conceptList = querySelect.list();
		
		for (CodeSystemConcept row : conceptList) {
			Set<CodeSystemConceptTranslation> traslations = row.getCodeSystemConceptTranslations();
			for (CodeSystemConceptTranslation i : traslations)
				System.out.println(" -CODE:" + row.getCode() + " -TERM-1::" + row.getTerm() + " -DESC-1::" + row.getDescription() + " -TERM-2::" + i.getTerm() + " -DESC-2::" + i.getDescription());
		}
	}

	private static void testDelete(Session session, List<CodeSystemConcept> conceptListSrc) {
		for (CodeSystemConcept csConc : conceptListSrc) {
			Transaction transaction = session.beginTransaction();
			Set<CodeSystemConceptTranslation> traslations = csConc.getCodeSystemConceptTranslations();

			csConc.setCodeSystemConceptTranslations(null);
			session.save(csConc);

			for (CodeSystemConceptTranslation i : traslations) {
				session.delete(i);
				System.out.println(" - TRADUZIONE:: " + i.getTerm() + " DELETED");
			}

			session.flush();
			transaction.commit();
		}
	}

	/*
	 * Aggiunge una traduzione ad un code_system_concept
	 */
	public static void addTraslation(Session session, CodeSystemConcept csConc, String term, String description, String lang) {
		CodeSystemConceptTranslation concTr = new CodeSystemConceptTranslation();
		concTr.setCodeSystemConcept(csConc);

		concTr.setLanguageCd(lang.toUpperCase());
		concTr.setTerm(term);
		concTr.setTermAbbrevation(null);

		concTr.setDescription(description);
		csConc.setMeaning(null);
		csConc.setHints(null);

		Transaction transaction = session.beginTransaction();
		session.save(concTr);
		session.flush();
		session.refresh(concTr);
		transaction.commit();

	}

}
