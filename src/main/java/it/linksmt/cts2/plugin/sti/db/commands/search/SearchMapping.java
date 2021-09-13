package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;

import edu.mayo.cts2.framework.model.association.Association;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.dtos.OutputDto;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class SearchMapping extends HibernateCommand {

	private static Logger log = Logger.getLogger(SearchMapping.class);

	
	private AbstractStiService service = null;
	private String sourceOrTargetEntity = null;
	private String mapping = null;
	private Integer page = 0;
	private Integer num = 10;
	

//	SELECT DISTINCT  
//	( SELECT DISTINCT eva2.id 
//		FROM code_system_entity_version_association AS eva2 
//		WHERE 1=1 
//		and eva2.codesystementityversionid1 = eva.codesystementityversionid1 
//		and eva2.codesystementityversionid2 = eva.codesystementityversionid2 
//		and eva2.leftid = eva.leftid 
//		and eva2.forwardname = eva.forwardname 
//		and eva2.reversename = eva.reversename 
//		and eva2.associationKind = eva.associationkind 
//		and eva2.status = eva.status 
//		and eva2.mapsetversionid = eva.mapsetversionid 
//	limit 1) as id, 
//	eva.codesystementityversionid1, 
//	eva.codesystementityversionid2, 
//	eva.leftid, 
//	eva.forwardname, 
//	eva.reversename, 
//	eva.associationkind, 
//	eva.status, 
//	eva.mapsetversionid, 
//	( SELECT DISTINCT eva2.statusdate 
//		FROM code_system_entity_version_association AS eva2 
//		WHERE 1=1 
//		and eva2.codesystementityversionid1 = eva.codesystementityversionid1 
//		and eva2.codesystementityversionid2 = eva.codesystementityversionid2 
//		and eva2.leftid = eva.leftid 
//		and eva2.forwardname = eva.forwardname 
//		and eva2.reversename = eva.reversename 
//		and eva2.associationKind = eva.associationkind 
//		and eva2.status = eva.status 
//		and eva2.mapsetversionid = eva.mapsetversionid 
//	limit 1) as statusdate, 
//	( SELECT DISTINCT eva2.inserttimestamp 
//		FROM code_system_entity_version_association AS eva2 
//		WHERE 1=1 
//		and eva2.codesystementityversionid1 = eva.codesystementityversionid1 
//		and eva2.codesystementityversionid2 = eva.codesystementityversionid2 
//		and eva2.leftid = eva.leftid 
//		and eva2.forwardname = eva.forwardname 
//		and eva2.reversename = eva.reversename 
//		and eva2.associationKind = eva.associationkind 
//		and eva2.status = eva.status 
//		and eva2.mapsetversionid = eva.mapsetversionid 
//	limit 1) as inserttimestamp 
//FROM map_set_version AS msv 
//	INNER JOIN code_system_entity_version_association eva ON msv.versionid  =  eva.mapsetversionid 
//	INNER JOIN code_system_entity_version ev 
//  	ON ((ev.versionid = eva.codesystementityversionid1 ) 
//  		OR  (ev.versionid = eva.codesystementityversionid2 ))  
//  	INNER JOIN code_system_concept AS cpt ON cpt.codesystementityversionid = ev.versionid 
//  	LEFT JOIN code_system_concept_translation cptt ON cpt.codesystementityversionid = cptt.codesystementityversionid 
//  WHERE 1=1  
// 	AND  LOWER(msv.fullname) LIKE LOWER('Catalogo_unico_delle_prestazioni_-_Umbria (CatalogounicodelleprestazioniUmbria__2018) - Nomenclatore_Tariffario_Regionale_Umbria (NomenclatoreTariffarioRegionaleUmbria__2018)')
//	AND (
//	LOWER(cpt.code) LIKE LOWER('%toracica%iniezione%') 					OR (LOWER(cpt.code) LIKE LOWER('%toracica%') AND LOWER(cpt.code) LIKE LOWER('%iniezione%'))
//	OR LOWER(cpt.term) LIKE LOWER('%toracica%iniezione%')	 			OR (LOWER(cpt.term) LIKE LOWER('%toracica%') AND LOWER(cpt.term) LIKE LOWER('%iniezione%'))
//	OR LOWER(cpt.termabbrevation) LIKE LOWER('%toracica%iniezione%') 	OR (LOWER(cpt.termabbrevation) LIKE LOWER('%toracica%') AND LOWER(cpt.termabbrevation) LIKE LOWER('%iniezione%'))
//	OR LOWER(cptt.term) LIKE LOWER('%toracica%iniezione%') 				OR (LOWER(cptt.term) LIKE LOWER('%toracica%') AND LOWER(cptt.term) LIKE LOWER('%iniezione%'))
//	)
//	
//	
//	
//	
//LOWER(cpt.code) LIKE LOWER('%iniezione%toracica%') 
//			OR LOWER(cpt.term) LIKE LOWER('%iniezione%toracica%') 
//			OR LOWER(cpt.termabbrevation) LIKE LOWER('%iniezione%toracica%') 
//			OR LOWER(cptt.term) LIKE LOWER('%iniezione%toracica%')	
//	
	

	 
	private static final String SQL_QUERY_SELECT_NATIVE = "SELECT DISTINCT  "
				+ "	( SELECT DISTINCT eva2.id "
					+ "	FROM code_system_entity_version_association AS eva2 "
					+ "	WHERE 1=1 "
					+ "	and eva2.codesystementityversionid1 = eva.codesystementityversionid1 "
					+ "	and eva2.codesystementityversionid2 = eva.codesystementityversionid2 "
					+ "	and eva2.leftid = eva.leftid "
					+ "	and eva2.forwardname = eva.forwardname "
					+ "	and eva2.reversename = eva.reversename "
					+ "	and eva2.associationKind = eva.associationkind "
					+ "	and eva2.status = eva.status "
					+ "	and eva2.mapsetversionid = eva.mapsetversionid "
				+ "	limit 1) as id, "
				+ "	eva.codesystementityversionid1, "
				+ "	eva.codesystementityversionid2, "
				+ "	eva.leftid, "
				+ "	eva.forwardname, "
				+ "	eva.reversename, "
				+ "	eva.associationkind, "
				+ " eva.status, "
				+ "	eva.mapsetversionid, "
				+ "	( SELECT DISTINCT eva2.statusdate "
					+ "	FROM code_system_entity_version_association AS eva2 "
					+ "	WHERE 1=1 "
					+ "	and eva2.codesystementityversionid1 = eva.codesystementityversionid1 "
					+ "	and eva2.codesystementityversionid2 = eva.codesystementityversionid2 "
					+ "	and eva2.leftid = eva.leftid "
					+ "	and eva2.forwardname = eva.forwardname "
					+ "	and eva2.reversename = eva.reversename "
					+ "	and eva2.associationKind = eva.associationkind "
					+ "	and eva2.status = eva.status "
					+ "	and eva2.mapsetversionid = eva.mapsetversionid "
				+ "	limit 1) as statusdate, "
				+ "	( SELECT DISTINCT eva2.inserttimestamp "
					+ "	FROM code_system_entity_version_association AS eva2 "
					+ "	WHERE 1=1 "
					+ "	and eva2.codesystementityversionid1 = eva.codesystementityversionid1 "
					+ "	and eva2.codesystementityversionid2 = eva.codesystementityversionid2 "
					+ "	and eva2.leftid = eva.leftid "
					+ "	and eva2.forwardname = eva.forwardname "
					+ "	and eva2.reversename = eva.reversename "
					+ "	and eva2.associationKind = eva.associationkind "
					+ "	and eva2.status = eva.status "
					+ "	and eva2.mapsetversionid = eva.mapsetversionid "
				+ "	limit 1) as inserttimestamp "
				+ "	FROM map_set_version AS msv "
				+ "		INNER JOIN code_system_entity_version_association eva ON msv.versionid  =  eva.mapsetversionid "
				+ "		INNER JOIN code_system_entity_version ev "
				+ " 	ON ((ev.versionid = eva.codesystementityversionid1 ) "
				+ " 		OR  (ev.versionid = eva.codesystementityversionid2 ))  "
				+ " 	INNER JOIN code_system_concept AS cpt ON cpt.codesystementityversionid = ev.versionid "
				+ " 	LEFT JOIN code_system_concept_translation cptt ON cpt.codesystementityversionid = cptt.codesystementityversionid "
				+ " WHERE 1=1 "
				+ " 	AND  LOWER(msv.fullname) LIKE LOWER(:FULLANME) ";



	public SearchMapping(String sourceOrTargetEntity,String mapping, Integer page,Integer num,final AbstractStiService service) {

		this.sourceOrTargetEntity = sourceOrTargetEntity;
		this.mapping = mapping;
		this.service = service;
		
		if(page!=null){
			this.page = page;
		}
		if(num!=null && num>0){
			this.num = num;
		}
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public OutputDto execute(final Session session) throws StiAuthorizationException, StiHibernateException {
		Integer start = page*num;
		//sourceOrTargetEntity = sourceOrTargetEntity.replaceAll("\\s+", "%");
		
		String terms[] = sourceOrTargetEntity.split("\\s+");
		String queryCode = " LOWER(cpt.code) LIKE LOWER(_TEXT_) ";
		String queryTerm = " LOWER(cpt.term) LIKE LOWER(_TEXT_) ";
		String queryTermabbrevation = " LOWER(cpt.termabbrevation) LIKE LOWER(_TEXT_)";
		String queryTermTraslate = " LOWER(cptt.term) LIKE LOWER(_TEXT_) ";
		
		String subQuery ="";
		if(terms.length>0){
			
			//cpt.code
			String subQueryCode = makeTermQuery(terms, queryCode);
			if(!"".equals(subQueryCode) && subQueryCode.length()>0){
				subQuery=subQuery+ " ("+subQueryCode+")";
			}
			
			//cpt.term
			String subQueryTerm = makeTermQuery(terms, queryTerm);
			if(!"".equals(subQueryTerm) && subQueryTerm.length()>0){
				subQuery=subQuery+ " OR ("+subQueryTerm+")";
			}
			
			//cpt.termabbrevation
			String subQueryTermAbr = makeTermQuery(terms, queryTermabbrevation);
			if(!"".equals(subQueryTermAbr) && subQueryTermAbr.length()>0){
				subQuery=subQuery+ " OR ("+subQueryTermAbr+")";
			}
			
			//cptt.term
			String subQueryTermTranslate = makeTermQuery(terms, queryTermTraslate);
			if(!"".equals(subQueryTermTranslate) && subQueryTermTranslate.length()>0){
				subQuery=subQuery+ " OR ("+subQueryTermTranslate+")";
			}
		}
		
		if(!"".equals(subQuery) && subQuery.length()>0){
			subQuery = " AND  ("+subQuery+")";
		}
		
		String SQL_QUERY_SELECT_FULL = SQL_QUERY_SELECT_NATIVE + subQuery +  " LIMIT :NUM OFFSET :START ";
		//log.debug("\n\n\n\n SQL_QUERY_SELECT_FULL::\n\n"+SQL_QUERY_SELECT_FULL+"\n\n\n");
		
		sourceOrTargetEntity = sourceOrTargetEntity.replaceAll("[\\s+]+","%");
		mapping = mapping.replaceAll("\\s+", "%");
		
		String SQL_QUERY_COUNT_NATIVE = "select count(id) FROM ("+SQL_QUERY_SELECT_FULL.replace("LIMIT :NUM OFFSET :START", "")+") as a ";
		SQLQuery queryCount = session.createSQLQuery(SQL_QUERY_COUNT_NATIVE);
		queryCount.setString("FULLANME",mapping);
		
		Long numFound = ((BigInteger) queryCount.uniqueResult()).longValue();
		
		
		SQLQuery querySelect = session.createSQLQuery(SQL_QUERY_SELECT_FULL).addEntity(CodeSystemEntityVersionAssociation.class);
		querySelect.setString("FULLANME",mapping);
		querySelect.setInteger("NUM",num);
		querySelect.setInteger("START",start);
		
		
		List<CodeSystemEntityVersionAssociation> resList = querySelect.list();
		
		List<Association> retVal = new ArrayList<Association>(0);
		for (int i = 0; i < resList.size(); i++) {
			Association association = DbTransformUtil.entityVersionAssociationToAssociation(session, resList.get(i), service);
			retVal.add(association);
		}
		
		OutputDto outputDto = new OutputDto();
		outputDto.setNumFound(numFound);
		outputDto.setEntry(retVal);
		return outputDto;
	}

	private String makeTermQuery(String[] terms, String queryTpl) {
		String subQuery ="";
		for (String term : terms) {
			if(term!=null && !"".equals(term)){
				subQuery = subQuery + queryTpl.replace("_TEXT_", "'%"+term+"%'")+" AND ";
			}
		}
		if(subQuery!=null && !"".equals(subQuery)){
			subQuery = subQuery.substring(0,subQuery.length()-4);
		}
		return subQuery;
	}
	
}
