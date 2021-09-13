package it.linksmt.cts2.plugin.sti.importer.atc_aic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportAtc;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.exporter.atc_aic.ExportAtc;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ImportAtcData {

	private static Logger log = Logger.getLogger(ImportAtcData.class);

	private ImportAtcData() { }

	public static void importNewVersion(
			final String sourceTableDbUrl,
			final String sourceDbUsername,
			final String sourceDbPassword,
			final HibernateUtil destHibernateUtil,
			final String csVersionName, final String csVersionDescription,
			final Date effectiveDate, final String oid,
			final long tempImportFk) throws Exception {

		Map<String, JsonObject> atcMap = null;
		try {
			atcMap = readTempAtc(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword,
					tempImportFk);
		}
		catch(Exception ex) {
			log.error("Errore durante la lettura dei dati temporanei.", ex);
			throw new ImportException("Errore durante la lettura dei dati temporanei.");
		}

		destHibernateUtil.executeBySystem(new ImportAtc(
        		csVersionName, csVersionDescription,
        		oid, effectiveDate, atcMap));

		// Esporta in formato Json
		ExportAtc.exportNewVersion(csVersionName, atcMap);
	}


	private static Map<String, JsonObject> readTempAtc(
			final String sourceTableDbUrl,
			final String sourceDbUsername,
			final String sourceDbPassword,
			final long tempImportFk) throws Exception {

		Map<String, JsonObject> retVal = new HashMap<String, JsonObject>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		int countTot = 0;

		try {
			Class.forName("org.postgresql.Driver");

			conn = DriverManager.getConnection(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword);

			ps = conn.prepareStatement("SELECT * FROM temp_atc "
					+ " WHERE codice_atc is not null AND fk_tempimportazione = "
					+ String.valueOf(tempImportFk));

			rs = ps.executeQuery();

			while (rs.next()) {
				String codeATC = StiServiceUtil.cleanValCsv(rs.getString("codice_atc"));
				if (StiServiceUtil.isNull(codeATC)) {
					throw new ImportException("Impossibile importare la riga con id: " + rs.getString("cod_temp_atc"));
				}

				JsonObject valAtc = new JsonObject();
				valAtc.addProperty(AtcAicFields.ATC_CODICE, codeATC);

				valAtc.addProperty(AtcAicFields.ATC_DENOMINAZIONE,StiServiceUtil.cleanValCsv(rs.getString("descrizionecategoria_principioattivo")));

				valAtc.addProperty(AtcAicFields.ATC_GRUPPO_ANATOMICO,StiServiceUtil.cleanValCsv(rs.getString("pl_gruppoanatomicoprincipale")));

				String[] l = new String[5];
				l[0] = StiServiceUtil.cleanValCsv(rs.getString("primo_livello"));
				l[1] = StiServiceUtil.cleanValCsv(rs.getString("secondo_livello"));
				l[2] = StiServiceUtil.cleanValCsv(rs.getString("terzo_livello"));
				l[3] = StiServiceUtil.cleanValCsv(rs.getString("quarto_livello"));
				l[4] = StiServiceUtil.cleanValCsv(rs.getString("quinto_livello"));

				String superClass = "";
				for (int i = 1; i < l.length; i++) {
					if (!StiServiceUtil.isNull(l[i])) {
						superClass += StiServiceUtil.trimStr(l[i-1]);
					}
					else {
						break;
					}
				}

				if (!StiServiceUtil.isNull(superClass)) {
					valAtc.addProperty(AtcAicFields.ATC_SUBCLASS_OF, superClass);
				}

				retVal.put(codeATC, valAtc);
				countTot++;
			}

			log.info("Codici ATC totali: " + countTot);
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

		Map<String, JsonObject> atcMap = readTempAtc(sourceTableDbUrl, sourceDbUsername, sourceDbPassword, tempImportFk);

		ExportAtc.exportNewVersion(csVersionName, atcMap);
	}
}
