package it.linksmt.cts2.plugin.sti.importer;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersions;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class ChunkExtractor {

	private static Logger log = Logger.getLogger(ChunkExtractor.class);

	public static final String AT_END_FIELD = "AT_END";
	public static final String DOCS_ARRAY_FIELD = "DOCS_ARRAY";

	private String codeSystemName = null;

	private long chunkVersionId = -1;
	private int  curVersIdx = -1;
	private int  chunkStartCurr =  0;

	private List<CodeSystemVersion> csVersList = null;

	private static final String CHUNK_SQL_QUERY = "select conc.* from code_system_version_entity_membership as memb"
			+ " inner join code_system_entity as ent on memb.codesystementityid = ent.id"
			+ " inner join code_system_entity_version as entVers on ent.currentversionid = entVers.versionId"
			+ " inner join code_system_concept as conc on entVers.versionId = conc.codesystementityversionid"
			+ " where memb.codesystemversionid={#__CHUNK_VERSIONID#}"
			+ " and entVers.statusvisibility=1 and entVers.statusdeactivated=0"
			+ " order by conc.code";

	public ChunkExtractor(final String codeSystemName) {
		this.codeSystemName = codeSystemName;
	}

	public JsonObject nextChunk() throws StiHibernateException, StiAuthorizationException {

		JsonObject retVal = new JsonObject();

		HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
		if (csVersList == null) {
			log.info("Cominicio il processo di indicizzazione per " + codeSystemName);

			csVersList = (List<CodeSystemVersion>) hibUtil.executeBySystem(
					new GetCodeSystemVersions(codeSystemName, StiConstants.STATUS_CODES.ACTIVE));

			if ((csVersList == null) || (csVersList.size() == 0)) {
				retVal.addProperty(AT_END_FIELD, true);
				return retVal;
			}

			curVersIdx = 0;
			chunkVersionId = csVersList.get(curVersIdx).getVersionId().longValue();

			log.info("Indicizzo la versione " + csVersList.get(curVersIdx).getName());
		}

		Session session = null;
		try {
			session = hibUtil.getSessionFactory().openSession();

			List<CodeSystemConcept> codeConcept = session.createSQLQuery(CHUNK_SQL_QUERY
						.replace("{#__CHUNK_VERSIONID#}", String.valueOf(chunkVersionId)))
					.addEntity(CodeSystemConcept.class).setFirstResult(chunkStartCurr)
					.setMaxResults(ImportCsUtil.CHUNK_SIZE_INDEX).list();

			JsonArray chunkDocs = new JsonArray();
			for (int i = 0; i < codeConcept.size(); i++) {
				CodeSystemConcept curConc = codeConcept.get(i);

				chunkDocs.add(IndexDocumentBuilder.createByCodeSystemFields(session, codeSystemName, curConc));
			}

			retVal.add(DOCS_ARRAY_FIELD, chunkDocs);
			if (codeConcept.size() < ImportCsUtil.CHUNK_SIZE_INDEX) {
				if (curVersIdx < csVersList.size()) {
					log.info("Termine indicizzazione versione " + csVersList.get(curVersIdx).getName());
				}
				else {
					log.info("Termine indicizzazione versione " + csVersList.get(curVersIdx-1).getName());
				}

				curVersIdx++;
				if (curVersIdx < csVersList.size()) {
					retVal.addProperty(AT_END_FIELD, false);

					chunkVersionId = csVersList.get(curVersIdx).getVersionId().longValue();
					chunkStartCurr = 0;

					log.info("Indicizzo la versione " + csVersList.get(curVersIdx).getName());
				}
				else {
					retVal.addProperty(AT_END_FIELD, true);
					log.info("Indicizzazione terminata per " + codeSystemName);
				}
			}
			else {
				retVal.addProperty(AT_END_FIELD, false);
				chunkStartCurr += ImportCsUtil.CHUNK_SIZE_INDEX;
			}

			return retVal;
		}
	    catch (Exception e) {
	    	try {
            	if (session != null	) {
            		session.clear();
            	}
            }
	    	catch(Exception ex) { }

	    	throw new StiHibernateException(e.getMessage(), e);
	    }
        finally {

        	if ( (session != null) && (session.isOpen()) ) {
        		session.close();
        	}
        }
	}
}
