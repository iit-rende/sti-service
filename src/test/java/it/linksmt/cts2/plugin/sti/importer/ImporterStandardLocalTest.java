package it.linksmt.cts2.plugin.sti.importer;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.ImportStandardLocal;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

public class ImporterStandardLocalTest {
	
	
	//
	//CODE_SYSTEM_OID: 1
	//CODE_SYSTEM_DESCRIPTION: we
	//STANDARD_LOCAL_CSV_FILE_IT: /codesystem/standardlocal/Esempio10Righe.it.csvdd199eb7-a7f0-4dfb-98e5-f641e3c17dbb
	//STANDARD_LOCAL_CSV_FILE_EN: /codesystem/standardlocal/Esempio10Righe.en.csvb101b3d1-f041-4523-a752-cad58eef8169
	//DOMAIN: 1
	//ORGANIZATION: 1
	//TYPE: STANDARD_NATIONAL
	//SUBTYPE: nomenclature
	//TYPE_MAPPING: {"descrizione":"description","codice":"code","componente":"java.lang.String","campo_numerico":"java.lang.String","versione":"java.lang.String","proprieta":"java.lang.String","campo_data":"java.lang.String"}
	//CODIFICATION_MAPPING: {}
	//VERSION_DESCRIPTION: we
	//NAME: T
	
	
	/*	query per debug	
	SELECT distinct c.name,v.name,cpt.code,cpt.term,tr.term as term_translate,p.paramname,p.languagecd,val.parametervalue 
		FROM code_system c
			INNER JOIN code_system_version v ON c.id = v.codesystemid
			INNER JOIN code_system_version_entity_membership m ON v.versionid = m.codesystemversionid
			INNER JOIN code_system_entity_version ev ON m.codesystementityid = ev.codesystementityid
			INNER JOIN code_system_concept cpt ON ev.versionid = cpt.codesystementityversionid
			LEFT JOIN code_system_concept_translation tr ON tr.codesystementityversionid = cpt.codesystementityversionid
			INNER JOIN metadata_parameter p on c.id  = p.codesystemid
			INNER JOIN code_system_metadata_value val on p.id = val.metadataparameterid and cpt.codesystementityversionid = val.codesystementityversionid
	WHERE c.name='MATT' AND v.name = 'MATT01' --and cpt.code = 'CODICE00'
		ORDER BY p.languagecd,p.paramname;
	 * */
	
	
	
//	private static Logger log = Logger.getLogger(ImporterStandardLocalTest.class);

	
	private static int testVersion = 1;
	
	
	private static String standardLocalName = "MATT";
	private static String csDescription = "Descrizione";
	private static String csVersionName = "MT000"+testVersion;
	private static String versionDescription = csVersionName;
	private static String oid = "10";
	private static Date releaseDate = new Date();
	private static String domain = "salute";
	private static String organization = "test";
	private static String type = CodeSystemType.STANDARD_NATIONAL.getKey();
	private static String subType = "nomenclature";
	
//	private static String type = CodeSystemType.VALUE_SET.getKey();
//	private static String subType = null;
	
//	private static HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
	
	/******** TEST 1*****************/
	private static String fileNameIt="Esempio10Righe_v"+testVersion+".it.csv";
	private static String fileNameEn ="Esempio10Righe_v"+testVersion+".en.csv";
	private static String basePath = "E:/Lavoro/PROGETTI_DOC/STI (Sistema Terminologico Integrato)/CSV ESEMPIO/nuovo codesystem/";
	private static File csvDataIt = new File(basePath+"esempi bilingua/base/"+fileNameIt);
	private static File csvDataEn = new File(basePath+"esempi bilingua/base/"+fileNameEn);
	

	
	private static LinkedHashMap<String,String> parameterTypeMapIt = new LinkedHashMap<String,String>(0);
	static{
		parameterTypeMapIt.put("codice", "code");
		parameterTypeMapIt.put("descrizione", "description");
		parameterTypeMapIt.put("componente", "java.lang.String");
		parameterTypeMapIt.put("proprieta", "java.lang.String");
		parameterTypeMapIt.put("campo_numerico", "java.lang.Double");
		parameterTypeMapIt.put("campo_data", "java.util.Date");
	}
	
	private static LinkedHashMap<String,String> parameterTypeMapEn = new LinkedHashMap<String,String>(0);
	static{
		parameterTypeMapEn.put("code", "code");
		parameterTypeMapEn.put("description", "description");
		parameterTypeMapEn.put("component", "java.lang.String");
		parameterTypeMapEn.put("property", "java.lang.String");
		parameterTypeMapEn.put("numeric_field", "java.lang.Double");
		parameterTypeMapEn.put("date_field", "java.util.Date");
	}
//	static{
//		parameterTypeMapEn.put("description", "description");
//		parameterTypeMapEn.put("code", "code");
//		parameterTypeMapEn.put("component", "java.lang.Double");
//		parameterTypeMapEn.put("numeric_field", "java.lang.Double");
//		parameterTypeMapEn.put("property", "java.lang.String");
//	}
	
	
	/******** TEST 2*****************/

	
//	private static LinkedHashMap<String,String> parameterTypeMapIt = new LinkedHashMap<String,String>(0);
//	static{
//		parameterTypeMapIt.put("Codice LOINC", "code");
//		parameterTypeMapIt.put("Descrizione", "description");
//		parameterTypeMapIt.put("Designazione (originale)", "java.lang.String");
//	}
//	private static File csvDataIt = new File("E:/Lavoro/PROGETTI_DOC/STI (Sistema Terminologico Integrato)/CSV ESEMPIO/nuovo codesystem/nomi strani/SPECIALITÀ DI LABORATORIO_LOINC.csv");

	
	
	
	
	private static HashMap<String, String> codificationMap = new HashMap<String,String>(0);



	public static void main(final String[] args) {

		// Logging configuration for Test
//		BasicConfigurator.configure();
//		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
//		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
//		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
//		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
//		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);

	
		try {
			String csvCollaudo = "E:/Lavoro/PROGETTI_DOC/STI (Sistema Terminologico Integrato)/CSV ESEMPIO/CSV collaudo/CND_2011 - SMALL.csv";
			File csvFilePrimary = new File(csvCollaudo);
			
			@SuppressWarnings("resource")
			CSVReader readerCsvPrimary = new CSVReader(new InputStreamReader(new FileInputStream(csvFilePrimary),   "ISO-8859-1"), ';', CSVParser.DEFAULT_QUOTE_CHARACTER);
			List<String[]> recordsPrimary = readerCsvPrimary.readAll();
			
			int row =0;
			for (String[] values : recordsPrimary) {
				row++;
				for (String v : values){
					if(v!=null && !"".equals(v.trim())){
						System.out.print("row::"+row+" values.size::"+values.length);
						System.out.print("mamt::"+v);
						System.out.println("\n###########################");
					}
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		
//		try{
//			Session session = hibUtil.getSessionFactory().openSession();
//			CodeSystemVersion srcVers = new GetCodeSystemLastVersion("MATT").execute(session);
//			CodeSystemVersion trgVers = new GetCodeSystemLastVersion("SEN").execute(session);
//			new CreateMapSetVersion(srcVers, trgVers, new Date(),"Descrizione","Organizzazione").execute(session);
//		}
//		catch(Exception e){
//			
//		}


		
//		/*Import ItaEng*/
//		importNewVersionTest(csvDataIt,csvDataEn,parameterTypeMapIt,fileNameIt,fileNameEn);
//		
//		/*Import Ita*/
//		importNewVersionTest(csvDataIt,null,parameterTypeMapIt,fileNameIt,null);
//		
//		/*Import Eng*/
//		importNewVersionTest(null,csvDataEn,parameterTypeMapEn,null,fileNameEn);
		

	}

//	private static void importNewVersionTest(File csvDataIt,File csvDataEn,LinkedHashMap<String,String> parameterTypeMap, String fileNameIt,String fileNameEn) {
//		try {
//			ImportStandardLocal.importNewVersion(hibUtil, csvDataIt, csvDataEn, standardLocalName, csVersionName, csDescription, releaseDate, oid, versionDescription, domain, organization, type, subType,"N",null,fileNameIt, fileNameEn, parameterTypeMap, codificationMap);
//		}
//		catch(Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	
}
