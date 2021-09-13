package it.linksmt.cts2.plugin.sti.importer.atc_aic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportMappingAicAtc;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ImportMappingData {

	private static Logger log = Logger.getLogger(ImportMappingData.class);

	public static void importMapping(
		final String sourceTableDbUrl,
		final String sourceDbUsername,
		final String sourceDbPassword,
		final HibernateUtil destHibernateUtil,
		final String aicVersionName,
		final String atcVersionName,
		final String solrUrl,
		final long tempImportFk) throws Exception {

		Map<String, String> aicAtcMap = null;

		try {
			aicAtcMap = readMappingAtcAic(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword,
					tempImportFk);
		}
		catch(Exception ex) {
			log.error("Errore durante la lettura dei dati temporanei.", ex);
			throw new ImportException("Errore durante la lettura dei dati temporanei.");
		}

		SolrIndexerUtil.rollback(solrUrl);

		destHibernateUtil.executeBySystem(new ImportMappingAicAtc(
				aicVersionName, atcVersionName, solrUrl, aicAtcMap));

		SolrIndexerUtil.optimize(solrUrl);
	}


	private static Map<String, String> readMappingAtcAic(
			final String sourceTableDbUrl,
			final String sourceDbUsername,
			final String sourceDbPassword,
			final long tempImportFk)
		throws Exception {

		Map<String, String> retVal = new HashMap<String, String>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		int countTot = 0;

		try {
			Class.forName("org.postgresql.Driver");

			conn = DriverManager.getConnection(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword);

			ps = conn.prepareStatement("SELECT * FROM temp_mapping_atc_aic "
					+ " WHERE codice_atc is not null AND fk_tempimportazione = "
					+ String.valueOf(tempImportFk));

			rs = ps.executeQuery();

			while (rs.next()) {

				String codeAIC = StiServiceUtil.cleanValCsv(rs.getString("codice_aic"));
				String codeATC = StiServiceUtil.cleanValCsv(rs.getString("codice_atc"));

				if (StiServiceUtil.isNull(codeAIC) || StiServiceUtil.isNull(codeATC)) {
					log.error("Impossibile importare la riga con id: " +
							rs.getString("cod_temp_mapping_atc_aic"));
					continue;
				}

				retVal.put(codeAIC, codeATC);
				countTot++;
			}

			log.info("Numero mapping importati: " + countTot);
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

}