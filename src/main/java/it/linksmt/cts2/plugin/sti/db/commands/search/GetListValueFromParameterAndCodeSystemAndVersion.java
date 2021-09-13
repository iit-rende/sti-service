package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class GetListValueFromParameterAndCodeSystemAndVersion  extends HibernateCommand {
	
//	SELECT distinct val.parametervalue 
//	FROM code_system c
//		INNER JOIN code_system_version v ON c.id = v.codesystemid
//		INNER JOIN code_system_version_entity_membership m ON v.versionid = m.codesystemversionid
//		INNER JOIN code_system_entity_version ev ON m.codesystementityid = ev.codesystementityid
//		INNER JOIN code_system_concept cpt ON ev.versionid = cpt.codesystementityversionid
//		LEFT JOIN code_system_concept_translation tr ON tr.codesystementityversionid = cpt.codesystementityversionid
//		INNER JOIN metadata_parameter p on c.id  = p.codesystemid
//		INNER JOIN code_system_metadata_value val on p.id = val.metadataparameterid and cpt.codesystementityversionid = val.codesystementityversionid
//	WHERE c.name='ICPC2' 
//		AND v.name = 'ICPC__7.0'  
//		AND p.paramname = 'INCLUSION' 
//	ORDER BY val.parametervalue;
	
	
	String SQL_QUERY_SELECT_FULL = " SELECT distinct val.parametervalue "
	+ " FROM code_system c "
	+ " 	INNER JOIN code_system_version v ON c.id = v.codesystemid "
	+ " 	INNER JOIN code_system_version_entity_membership m ON v.versionid = m.codesystemversionid "
	+ " 	INNER JOIN code_system_entity_version ev ON m.codesystementityid = ev.codesystementityid "
	+ " 	INNER JOIN code_system_concept cpt ON ev.versionid = cpt.codesystementityversionid "
	+ " 	LEFT JOIN code_system_concept_translation tr ON tr.codesystementityversionid = cpt.codesystementityversionid "
	+ " 	INNER JOIN metadata_parameter p on c.id  = p.codesystemid "
	+ " 	INNER JOIN code_system_metadata_value val on p.id = val.metadataparameterid and cpt.codesystementityversionid = val.codesystementityversionid "
	+ " WHERE c.name=:NAME  "
	+ " 	AND v.name = :VERSION   "
	+ " 	AND p.paramname = :PARAMNAME  "
	+ "		AND (cpt.languagecd is null or LOWER(cpt.languagecd) = LOWER(:LANG))	"
	+ " ORDER BY val.parametervalue; ";
	
	
	
	private String name;
	private String version;
	private String paramName;
	private String lang;
	
	public GetListValueFromParameterAndCodeSystemAndVersion(String name,String version,String paramName,String lang) {
		this.name = name;
		this.version = version;
		this.paramName = paramName;
		this.lang = lang;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		
		if (name==null || version==null || paramName==null || lang == null) {
			return null;
		}
		
		
		SQLQuery querySelect = session.createSQLQuery(SQL_QUERY_SELECT_FULL);
		querySelect.setString("NAME",name);
		querySelect.setString("VERSION",version);
		querySelect.setString("PARAMNAME",paramName);
		querySelect.setString("LANG",lang);
		
		List<String> resList = querySelect.list();

		return resList;
	}

}
