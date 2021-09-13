package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class GetCodeSystemLanguages extends HibernateCommand {
	
	
	private String name = null;
	private String versionName = null;
	
	private static final String SQL_QUERY_SELECT_NATIVE = ""
		+ " SELECT distinct LOWER(p.languagecd) "
		+ " 			FROM code_system c "
		+ " 				INNER JOIN code_system_version v ON c.id = v.codesystemid "
		+ " 				INNER JOIN code_system_version_entity_membership m ON v.versionid = m.codesystemversionid "
		+ " 				INNER JOIN code_system_entity_version ev ON m.codesystementityid = ev.codesystementityid "
		+ " 				INNER JOIN code_system_concept cpt ON ev.versionid = cpt.codesystementityversionid "
		+ " 				LEFT JOIN code_system_concept_translation tr ON tr.codesystementityversionid = cpt.codesystementityversionid "
		+ " 						INNER JOIN metadata_parameter p on c.id  = p.codesystemid "
		+ " 						INNER JOIN code_system_metadata_value val on p.id = val.metadataparameterid and cpt.codesystementityversionid = val.codesystementityversionid "
		+ " 				WHERE c.name=:NAME AND v.name = :VERSION_NAME  AND p.languagecd IS NOT NULL AND p.languagecd <> 'LOC' ";

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}
	
	
	

	public GetCodeSystemLanguages(String name, String versionName) {
		this.name = name;
		this.versionName = versionName;
	}




	@Override
	public List<String> execute(Session session) throws StiAuthorizationException, StiHibernateException {

		
		SQLQuery querySelect = session.createSQLQuery(SQL_QUERY_SELECT_NATIVE);
		querySelect.setString("NAME", name);
		querySelect.setString("VERSION_NAME",versionName);
		
		List<String> resList = querySelect.list();
		
		return resList;
	}

}
