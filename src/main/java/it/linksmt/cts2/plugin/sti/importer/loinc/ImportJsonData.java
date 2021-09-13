package it.linksmt.cts2.plugin.sti.importer.loinc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportLoinc;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.exporter.loinc.ExportLoinc;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ImportJsonData {

	private static Logger log = Logger.getLogger(ImportJsonData.class);

	private ImportJsonData() { }

	public static void importNewVersion(
			final String sourceTableDbUrl,
			final String sourceDbUsername,
			final String sourceDbPassword,
			final HibernateUtil destHibernateUtil,
			final String csVersionName, final String csVersionDescription,
			final Date effectiveDate, final String oid,
			final long tempImportFk) throws Exception {

		Map<String, JsonObject> loincMap = null;
		try {
			loincMap = readTempLoinc(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword,
					csVersionName, tempImportFk);
		}
		catch(Exception ex) {
			log.error("Errore durante la lettura dei dati temporanei.", ex);
			throw new ImportException("Errore durante la lettura dei dati temporanei.");
		}

		destHibernateUtil.executeBySystem(new ImportLoinc(
        		csVersionName, csVersionDescription,
        		oid, effectiveDate, loincMap));

		ExportLoinc.exportNewVersion(csVersionName, loincMap);
	}


	private static Map<String, JsonObject> readTempLoinc(
			final String sourceTableDbUrl,
			final String sourceDbUsername,
			final String sourceDbPassword,
			final String loincVersion,
			final long tempImportFk) throws Exception {

		Map<String, JsonObject> retVal = new HashMap<String, JsonObject>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			Class.forName("org.postgresql.Driver");

			conn = DriverManager.getConnection(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword);

			int countTot = 0;
			int countIt = 0;

			String enableEtl = StiServiceUtil.trimStr(StiAppConfig.getProperty(
					StiServiceConfiguration.ETL_EXECUTION_LOINC_ENABLE, "")).toLowerCase();

			String tmpTable = "temp_loinc_" + loincVersion.replace(".", "_");
			if ("true".equals(enableEtl) || "yes".equals(enableEtl) || "si".equals(enableEtl)) {
				tmpTable = "temp_loinc";
			}

			ps = conn.prepareStatement("SELECT * FROM " + tmpTable
					+ " WHERE loincnumen is not null AND fk_tempimportazione = "
					+ String .valueOf(tempImportFk));

			rs = ps.executeQuery();

			while (rs.next()) {
				String loincNum = StiServiceUtil.cleanValCsv(rs.getString("loincnumen"));
				if (StiServiceUtil.isNull(loincNum)) {
					// TODO: far saltare una eccezione!!!
					log.warn("Impossibile importare la riga con id: " + rs.getString("codtemploinc"));
					// throw new ImportException("Codice Loinc nullo- Id: " + rs.getLong("codtemploinc"));
				}

				JsonObject valEn = new JsonObject();
				valEn.addProperty(LoincFields.LOINC_NUM, loincNum);

				valEn.addProperty(LoincFields.COMPONENT_EN, StiServiceUtil.cleanValCsv(rs.getString("componenten")));
				valEn.addProperty(LoincFields.PROPERTY_EN, StiServiceUtil.cleanValCsv(rs.getString("propertyen")));
				valEn.addProperty(LoincFields.TIME_ASPECT_EN, StiServiceUtil.cleanValCsv(rs.getString("timeaspcten")));

				valEn.addProperty(LoincFields.SYSTEM_EN, StiServiceUtil.cleanValCsv(rs.getString("systemen")));
				valEn.addProperty(LoincFields.SCALE_TYP_EN, StiServiceUtil.cleanValCsv(rs.getString("scaletypen")));

				valEn.addProperty(LoincFields.METHOD_TYP_EN, StiServiceUtil.cleanValCsv(rs.getString("methodtypen")));
				valEn.addProperty(LoincFields.CLASS_EN, StiServiceUtil.cleanValCsv(rs.getString("classen")));

				valEn.addProperty(LoincFields.SHORTNAME_EN, StiServiceUtil.cleanValCsv(rs.getString("shortnameen")));
				valEn.addProperty(LoincFields.LONG_COMMON_NAME_EN, StiServiceUtil.cleanValCsv(rs.getString("longcommonnameen")));

				String relatedNames2 = StiServiceUtil.cleanValCsv(rs.getString("relatednames2en"));
				if ((relatedNames2 != null) && (relatedNames2.trim().length() > 0) ) {

					String[] strArr = relatedNames2.split(";");
					JsonArray relArr = new JsonArray();

					for (int i = 0; i < strArr.length; i++) {
						JsonPrimitive element = new JsonPrimitive(strArr[i].trim());
						relArr.add(element);
					}
					valEn.add(LoincFields.RELATEDNAMES2_EN,  relArr);
				}

				valEn.addProperty(LoincFields.CHNG_TYPE, StiServiceUtil.cleanValCsv(rs.getString("chngtypeen")));
				valEn.addProperty(LoincFields.STATUS, StiServiceUtil.cleanValCsv(rs.getString("statusen")));
				valEn.addProperty(LoincFields.VERSION_LAST_CHANGED, StiServiceUtil.cleanValCsv(rs.getString("versionalastchangeden")));

				valEn.addProperty(LoincFields.DEFINITION_DESCRIPTION, StiServiceUtil.cleanValCsv(rs.getString("definitiondescriptionen")));
				valEn.addProperty(LoincFields.CONSUMER_NAME, StiServiceUtil.cleanValCsv(rs.getString("consumername")));

				valEn.addProperty(LoincFields.CLASS_TYPE, StiServiceUtil.cleanValCsv(rs.getString("classtypeen")));
				valEn.addProperty(LoincFields.UNITS_REQUIRED, StiServiceUtil.cleanValCsv(rs.getString("unitsrequireden")));
				valEn.addProperty(LoincFields.SUBMITTED_UNITS, StiServiceUtil.cleanValCsv(rs.getString("submittedunitsen")));

				valEn.addProperty(LoincFields.ORDER_OBS, StiServiceUtil.cleanValCsv(rs.getString("orderobsen")));
				valEn.addProperty(LoincFields.UNITS_AND_RANGE, StiServiceUtil.cleanValCsv(rs.getString("unitsandrangeen")));
				valEn.addProperty(LoincFields.EXAMPLE_UNITS, StiServiceUtil.cleanValCsv(rs.getString("exampleunitsen")));

				valEn.addProperty(LoincFields.STATUS_REASON, StiServiceUtil.cleanValCsv(rs.getString("statusreasonen")));
				valEn.addProperty(LoincFields.STATUS_TEXT, StiServiceUtil.cleanValCsv(rs.getString("statustexten")));
				valEn.addProperty(LoincFields.CHANGE_REASON_PUBLIC, StiServiceUtil.cleanValCsv(rs.getString("changereasonpublicen")));

				valEn.addProperty(LoincFields.MAP_TO, StiServiceUtil.cleanValCsv(rs.getString("mapto")));
				valEn.addProperty(LoincFields.MAP_TO_COMMENT, StiServiceUtil.cleanValCsv(rs.getString("comment")));

				if (!StiServiceUtil.isNull(StiServiceUtil.cleanValCsv(rs.getString("loincnum")))) {

					valEn.addProperty(LoincFields.COMPONENT_IT, StiServiceUtil.cleanValCsv(rs.getString("component")));
					valEn.addProperty(LoincFields.PROPERTY_IT, StiServiceUtil.cleanValCsv(rs.getString("property")));
					valEn.addProperty(LoincFields.TIME_ASPECT_IT, StiServiceUtil.cleanValCsv(rs.getString("timeaspect")));

					valEn.addProperty(LoincFields.SYSTEM_IT, StiServiceUtil.cleanValCsv(rs.getString("system")));
					valEn.addProperty(LoincFields.SCALE_TYP_IT, StiServiceUtil.cleanValCsv(rs.getString("scaletyp")));

					valEn.addProperty(LoincFields.METHOD_TYP_IT, StiServiceUtil.cleanValCsv(rs.getString("methodtyp")));
					valEn.addProperty(LoincFields.CLASS_IT, StiServiceUtil.cleanValCsv(rs.getString("class")));

					valEn.addProperty(LoincFields.SHORTNAME_IT, StiServiceUtil.cleanValCsv(rs.getString("shortname")));
					valEn.addProperty(LoincFields.LONG_COMMON_NAME_IT, StiServiceUtil.cleanValCsv(rs.getString("longcommonname")));

					String relatedNames2it = StiServiceUtil.cleanValCsv(rs.getString("relatednames2"));
					if ((relatedNames2it != null) && (relatedNames2it.trim().length() > 0) ) {

						String[] strArr = relatedNames2it.split(";");
						JsonArray relArr = new JsonArray();

						for (int i = 0; i < strArr.length; i++) {
							JsonPrimitive element = new JsonPrimitive(strArr[i].trim());
							relArr.add(element);
						}
						valEn.add(LoincFields.RELATEDNAMES2_IT,  relArr);
					}

					countIt++;
				}

				retVal.put(loincNum, valEn);
				countTot++;
			}

			log.info("Codici LOINC presenti solo nel set di dati per l'italiano: " + countIt);
            log.info("Codici LOINC totali: " + countTot);
		}
		finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					log.error("Errore chiusura resultset.", e);
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					log.error("Errore chiusura statement.", e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					log.error("Errore chiusura connessione.", e);
				}
			}
		}

		return retVal;
	}

	public static void testExportVersion(
			final String sourceTableDbUrl,
			final String sourceDbUsername,
			final String sourceDbPassword,
			final String csVersionName,
			final long tempImportFk) throws Exception {

		Map<String, JsonObject> loincMap = readTempLoinc(
				sourceTableDbUrl, sourceDbUsername, sourceDbPassword,
				csVersionName, tempImportFk);

		ExportLoinc.exportNewVersion(csVersionName, loincMap);
	}
}
