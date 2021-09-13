package it.linksmt.cts2.plugin.sti.db.commands.insert;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConceptAlt;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gson.JsonObject;

public class CreateStandardLocal extends HibernateCommand {

	private static Logger log = Logger.getLogger(CreateStandardLocal.class);

	// private static HashSet<String> SKIP_FIELDS = new HashSet<String>();
	// static {
	// SKIP_FIELDS.add(StandardLocalFields.LOCAL_CODE.trim().toLowerCase());
	// SKIP_FIELDS.add(StandardLocalFields.LOCAL_DESCRIPTION.trim().toLowerCase());
	// }

	private String csName;
	private String csVersionName;
	private String csVersionDescription;
	private String oid;
	private Date effectiveDate;

	private LinkedHashMap<String, JsonObject> csvMapPrimary = null;
	private LinkedHashMap<String, JsonObject> csvMapTranslate = null;

	private static int numInsert = 0;
	private static Session session = null;

	private String fieldCodePrimary;
	private String fieldDescriptionPrimary;
	
	private String fieldCodeTranslate;
	private String fieldDescriptionTranslate;

	private static List<String> insertedCsConceptCodes = new ArrayList<String>();
	private static HashMap<String, Long> insertedEntities = new HashMap<String, Long>();
	
	private boolean isClassification;
	private String languagePrimary;
	private String languageTranslate;
	
	
	public CreateStandardLocal(String csName, String csVersionName,
			String csVersionDescription, String oid, Date effectiveDate, LinkedHashMap<String, JsonObject> csvMapPrimary, LinkedHashMap<String, JsonObject> csvMapTranslate, 
			String fieldCodePrimary, String fieldDescriptionPrimary,String fieldCodeTranslate, String fieldDescriptionTranslate, 
			boolean isClassification,String languagePrimary,String languageTranslate) {		
		super();
		this.csName = csName;
		this.csVersionName = csVersionName;
		this.csVersionDescription = csVersionDescription;
		this.oid = oid;
		this.effectiveDate = effectiveDate;
		this.csvMapPrimary = csvMapPrimary;
		this.csvMapTranslate = csvMapTranslate;

		this.fieldCodePrimary = fieldCodePrimary;
		this.fieldDescriptionPrimary = fieldDescriptionPrimary;
		this.fieldCodeTranslate = fieldCodeTranslate;
		this.fieldDescriptionTranslate = fieldDescriptionTranslate;
		this.isClassification = isClassification;
		this.languagePrimary = languagePrimary;
		this.languageTranslate = languageTranslate;
	}

	@Override
	public void checkPermission(Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public Object execute(Session originalSession) throws StiAuthorizationException, StiHibernateException {

		CodeSystemVersion newVers = ImportCsUtil.createCsVersion(originalSession, csName, csVersionName, csVersionDescription, oid, effectiveDate, languagePrimary, languageTranslate);

		numInsert = 0;
		session = originalSession;

		insertedCsConceptCodes = new ArrayList<String>();

		// INSERT ENTITY
		String[] keyArr = csvMapPrimary.keySet().toArray(new String[0]);
		for (String key : keyArr) {
			try {
				JsonObject entObjPrimary = csvMapPrimary.get(key);
				JsonObject entObjTranslate = csvMapTranslate.get(key);
				insertLocalEntity(newVers, entObjPrimary, entObjTranslate);
			} catch (ImportException ex) {
				throw new StiHibernateException("Errore durante la scrittura sul DB.", ex);
			}
		}
		

		// Dopo qualche altra istruzione.......
		ImportCsUtil.setCurrentCodeSystemVersion(session, newVers);

		// Chiudo l'ultima transazione
		session.clear();
		session.getTransaction().commit();
		session.close();

		log.info("Importazione LOCALE terminata. Totale: " + String.valueOf(numInsert));
		return newVers;
	}
	
	
	private CodeSystemEntityVersion insertLocalEntity(CodeSystemVersion csVers, JsonObject dataPrimary,JsonObject dataTranslate) throws StiHibernateException, StiAuthorizationException, ImportException {
		// log.info("Inserting " + data.toString());

//		String codePrimary = StiServiceUtil.trimStr(dataPrimary.getAsJsonPrimitive(this.fieldCodePrimary.toUpperCase()).getAsString());
//		String termPrimary = StiServiceUtil.trimStr(dataPrimary.getAsJsonPrimitive(this.fieldDescriptionPrimary.toUpperCase()).getAsString());
//		
//		String termTranslate = null;
//		if(this.fieldDescriptionTranslate!=null){
//			termTranslate = StiServiceUtil.trimStr(dataTranslate.getAsJsonPrimitive(this.fieldDescriptionTranslate.toUpperCase()).getAsString());
//		}
		
		String codePrimary = StiServiceUtil.trimStr(dataPrimary.getAsJsonPrimitive(this.fieldCodePrimary).getAsString());
		String termPrimary = StiServiceUtil.trimStr(dataPrimary.getAsJsonPrimitive(this.fieldDescriptionPrimary).getAsString());
		
		String termTranslate = null;
		if(this.fieldDescriptionTranslate!=null){
			termTranslate = StiServiceUtil.trimStr(dataTranslate.getAsJsonPrimitive(this.fieldDescriptionTranslate).getAsString());
		}


		// Verifico se la entry risulta inserita e sollevo una eccezione
		// CodeSystemConcept existConc = new GetCodeSystemConcept(
		// code, csVers.getVersionId().longValue()).execute(session);
		// if (existConc != null) {
		// throw new ImportException("Il codice loinc risulta già presente: " + code);
		// }

		if (insertedCsConceptCodes.contains(codePrimary)) {
			throw new ImportException("Il codice loinc risulta già presente: " + codePrimary);
		}

		HashSet<String> SKIP_FIELDS = new HashSet<String>();
		SKIP_FIELDS.add(this.fieldCodePrimary.trim().toLowerCase());
		SKIP_FIELDS.add(this.fieldDescriptionPrimary.trim().toLowerCase());
		if(this.fieldCodeTranslate!=null){
			SKIP_FIELDS.add(this.fieldCodeTranslate.trim().toLowerCase());
		}
		if(this.fieldDescriptionTranslate!=null){
			SKIP_FIELDS.add(this.fieldDescriptionTranslate.trim().toLowerCase());
		}
		
		
		//TODO
		CodeSystemConcept superClass = null;
		if(isClassification ){
			List<String> codes = new ArrayList<String>(csvMapPrimary.keySet());
			for(int i = codes.size() - 1; i>=0; i--){
				String current = codes.get(i);
				if( !codePrimary.equals(current) && codePrimary.contains(current) ) {
					log.info(codePrimary + " is children of " + current );
					superClass = new GetCodeSystemConceptAlt(current, insertedEntities.get(current)).execute(session);
					
					break;
				}
			}
		}
		
		CodeSystemEntityVersion csEntityVers = ImportCsUtil.insertEntity(session, csVers, codePrimary, termPrimary, termTranslate, null, null, null, 
				effectiveDate, true, superClass, SKIP_FIELDS, dataPrimary, dataTranslate, languagePrimary, languageTranslate);


		insertedEntities.put(codePrimary, csEntityVers.getVersionId());
		insertedCsConceptCodes.add(codePrimary);
		
		numInsert++;
		if ((numInsert % ImportCsUtil.CHUNK_SIZE_IMPORT) == 0) {
			// Chiudo la transazione per il chunk
			SessionFactory sessFactory = session.getSessionFactory();

			session.clear();
			session.getTransaction().commit();
			session.close();

			// Apro una nuova transazione per il nuovo chunk
			session = sessFactory.openSession();
			session.beginTransaction();
		}

		// Log avanzamento
		if ((numInsert % 100) == 0) {
			log.info("Numero elementi elaborati: " + numInsert + " at " + new Date());
		}

		return (CodeSystemEntityVersion) session.get(CodeSystemEntityVersion.class, csEntityVers.getVersionId().longValue());
	}

}
