package it.linksmt.cts2.plugin.sti.db;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByNameAndCodeSystemName;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;


public class ExtraSearchAssociationTest {
	

	
//	SELECT DISTINCT eva.* FROM map_set_version AS msv
//	INNER JOIN code_system_entity_version_association eva ON msv.versionid  =  eva.mapsetversionid			 
//	INNER JOIN code_system_entity_version ev 
//				ON ((ev.versionid = eva.codesystementityversionid1  ) 
//								OR  (ev.versionid = eva.codesystementityversionid2 )) 
//	INNER JOIN code_system_concept AS cpt ON cpt.codesystementityversionid = ev.versionid
//	LEFT JOIN code_system_concept_translation cptt ON cpt.codesystementityversionid = cptt.codesystementityversionid 
//	WHERE fullname='LOINC (2.34) - ATC (2014)'
//	AND (cpt.code LIKE '%Tibenzonio %' OR cpt.term LIKE '%Tibenzonio %' OR cpt.termabbrevation LIKE '%Tibenzonio %' OR cptt.term LIKE '%Tibenzonio %');		
	
	

	private static final String SQL_QUERY_SELECT_NATIVE = " SELECT DISTINCT eva.* "
			+ "	FROM map_set_version AS msv "
			+ "		INNER JOIN code_system_entity_version_association eva ON msv.versionid  =  eva.mapsetversionid "
			+ "		INNER JOIN code_system_entity_version ev "
			+ " 	ON ((ev.versionid = eva.codesystementityversionid1 ) "
			+ " 		OR  (ev.versionid = eva.codesystementityversionid2 ))  "
			+ " 	INNER JOIN code_system_concept AS cpt ON cpt.codesystementityversionid = ev.versionid "
			+ " 	LEFT JOIN code_system_concept_translation cptt ON cpt.codesystementityversionid = cptt.codesystementityversionid "
			+ " WHERE 1=1 "
			+ " 	AND fullname=:FULLANME "
			+ " 	AND (cpt.code LIKE :TEXT OR cpt.term LIKE :TEXT OR cpt.termabbrevation LIKE :TEXT OR cptt.term LIKE :TEXT) "
			+ "	LIMIT :NUM OFFSET :START ";
		
		
	private static final String SQL_QUERY_COUNT_NATIVE = SQL_QUERY_SELECT_NATIVE.replace("DISTINCT eva.*", "count(DISTINCT eva.id)").replace("	LIMIT :NUM OFFSET :START ", "");
		
	
	
	
	
	
	public static void main(final String[] args) throws ParseException {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);

		searchAssociation();


	}

	private static void searchAssociation() {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		Session session  = hibernateUtil.getSessionFactory().openSession();
		try {
		
			
			/*****	 INPUT ****/
			
			
			
			/*modo 1*/
//			String mapping = "LOINC (2.34) - ATC (2014)";
//			String mapping = "ICPC (1) - LOINC (2.58)";
			String mapping = "TMICD9 (1.100) - ICD9-CM (2007)";
//			String[] tmp = mapping.split(" ");
//			String codeSystemSrcName = tmp[0];
//			String codeSystemSrcVersion = tmp[1].replace("(","").replace(")","");
//			String codeSystemTrgName = tmp[3];;
//			String codeSystemTrgVersion = tmp[4].replace("(","").replace(")","");
			
			
			
//			String sourceOrTargetEntity = "Onda R";
			String sourceOrTargetEntity = "";
			Integer page = 0;
			Integer num = 20;
			/*****	 FINE INPUT ****/
			
			
			
			
			Integer start = page*num;
			
			
			SQLQuery queryCount = session.createSQLQuery(SQL_QUERY_COUNT_NATIVE);
			queryCount.setString("TEXT","%" + sourceOrTargetEntity + "%");
			queryCount.setString("FULLANME",mapping);
			
			Long numFound = ((BigInteger) queryCount.uniqueResult()).longValue();
			System.out.println("numFound::"+numFound);
			
			
			
			
			SQLQuery querySelect = session.createSQLQuery(SQL_QUERY_SELECT_NATIVE).addEntity(CodeSystemEntityVersionAssociation.class);
			querySelect.setString("TEXT","%" + sourceOrTargetEntity + "%");
			querySelect.setString("FULLANME",mapping);
			querySelect.setInteger("NUM",num);
			querySelect.setInteger("START",start);
			
			
			System.out.println(querySelect.getQueryString());
			
			
			List<CodeSystemEntityVersionAssociation> resList = querySelect.list();
			System.out.println("resList.size()::"+resList.size());
			for(CodeSystemEntityVersionAssociation row : resList){
				System.out.println(row.getId());
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
