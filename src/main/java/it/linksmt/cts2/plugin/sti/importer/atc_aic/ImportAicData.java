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

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportAic;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.exporter.atc_aic.ExportAic;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ImportAicData {

	private static Logger log = Logger.getLogger(ImportAicData.class);

	private ImportAicData() { }

	public static void importNewVersion(
			final String sourceTableDbUrl,
			final String sourceDbUsername,
			final String sourceDbPassword,
			final HibernateUtil destHibernateUtil,
			final String csVersionName, final String csVersionDescription,
			final Date effectiveDate, final String oid,
			final long tempImportFk) throws Exception {

		Map<String, JsonObject> aicMap = null;
		try {
			aicMap = readTempAic(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword,
					tempImportFk);
		}
		catch(Exception ex) {
			log.error("Errore durante la lettura dei dati temporanei.", ex);
			throw new ImportException("Errore durante la lettura dei dati temporanei.");
		}

		destHibernateUtil.executeBySystem(new ImportAic(
        		csVersionName, csVersionDescription,
        		oid, effectiveDate, aicMap));

		// Esporta in formato json
		ExportAic.exportNewVersion(csVersionName, aicMap);
	}

	private static Map<String, JsonObject> readTempAic(
			final String sourceTableDbUrl, final String sourceDbUsername,
			final String sourceDbPassword, final long tempImportFk)
		throws Exception {

		Map<String, JsonObject> retVal = new HashMap<String, JsonObject>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		int countTot = 0;

		try {
			Class.forName("org.postgresql.Driver");

			conn = DriverManager.getConnection(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword);

			ps = conn.prepareStatement("SELECT * FROM temp_aic "
					+ " WHERE codice_aic is not null AND fk_tempimportazione = "
					+ String.valueOf(tempImportFk));

			rs = ps.executeQuery();

			while (rs.next()) {
				String codeAIC = StiServiceUtil.cleanValCsv(rs.getString("codice_aic"));
				if (StiServiceUtil.isNull(codeAIC)) {
					throw new ImportException("Impossibile importare la riga con id: " + rs.getString("cod_temp_aic_id_seq"));
				}

				JsonObject valAic = new JsonObject();
				valAic.addProperty(AtcAicFields.AIC_CODICE, codeAIC);

				valAic.addProperty(AtcAicFields.AIC_DENOMINAZIONE,
						StiServiceUtil.cleanValCsv(rs.getString("denominazione")));

				valAic.addProperty(AtcAicFields.AIC_DITTA,
						StiServiceUtil.cleanValCsv(rs.getString("ditta")));
				valAic.addProperty(AtcAicFields.AIC_CONFEZIONE,
						StiServiceUtil.cleanValCsv(rs.getString("confezione")));
				valAic.addProperty(AtcAicFields.AIC_TIPO_FARMACO,
						StiServiceUtil.cleanValCsv(rs.getString("tipo_farmaco")));
				valAic.addProperty(AtcAicFields.AIC_PRINCIPIO_ATTIVO,
						StiServiceUtil.cleanValCsv(rs.getString("principio_attivo")));

				valAic.addProperty(AtcAicFields.AIC_CLASSE,
						StiServiceUtil.cleanValCsv(rs.getString("classe")));
				valAic.addProperty(AtcAicFields.AIC_CODICE_GRUPPO_EQ,
						StiServiceUtil.cleanValCsv(rs.getString("codice_gruppo_equivalenza")));
				valAic.addProperty(AtcAicFields.AIC_DESCR_GRUPPO_EQ,
						StiServiceUtil.cleanValCsv(rs.getString("descrizione_gruppo_equivalenza")));

				valAic.addProperty(AtcAicFields.AIC_PREZZO_EX_FACTORY,
						StiServiceUtil.cleanValCsv(rs.getString("prezzo_ex_factory")));
				valAic.addProperty(AtcAicFields.AIC_PREZZO_AL_PUBBLICO,
						StiServiceUtil.cleanValCsv(rs.getString("prezzo_al_pubblico")));
				valAic.addProperty(AtcAicFields.AIC_PREZZO_MASSIMO_CESSIONE,
						StiServiceUtil.cleanValCsv(rs.getString("prezzo_massimo_di_cessione")));

				valAic.addProperty(AtcAicFields.AIC_IN_LISTA_TRASPARENZA_AIFA,
						String.valueOf(!StiServiceUtil.isNull(StiServiceUtil.cleanValCsv(
								rs.getString("x_in_lista_trasparenza_aifa")))));
				valAic.addProperty(AtcAicFields.AIC_IN_LISTA_REGIONE,
						StiServiceUtil.cleanValCsv(rs.getString("solo_in_lista_regione")));

				valAic.addProperty(AtcAicFields.AIC_METRI_CUBI_OSSIGENO,
						StiServiceUtil.cleanValCsv(rs.getString("metri_cubi_ossigeno")));
				valAic.addProperty(AtcAicFields.AIC_UNITA_POSOLOGICA,
						StiServiceUtil.cleanValCsv(rs.getString("unita_posologica")));
				valAic.addProperty(AtcAicFields.AIC_PREZZO_UNITA_POSOLOGICA,
						StiServiceUtil.cleanValCsv(rs.getString("prezzo_unita_posologica")));

				valAic.addProperty(AtcAicFields.AIC_NOTA,
						StiServiceUtil.cleanValCsv(rs.getString("nota")));

				// Workaroud per consentire l'Export del JSON
				PreparedStatement ps2 = null;
				ResultSet rs2 = null;
				try {

					ps2 = conn.prepareStatement("SELECT codice_aic, codice_atc FROM temp_mapping_atc_aic "
							+ " WHERE codice_atc is not null AND codice_aic = ? "
							+ " AND fk_tempimportazione = ? ");

					ps2.setString(1, String.valueOf(codeAIC));
					ps2.setLong(2, tempImportFk);

					String value = "";
					rs2 = ps2.executeQuery();
					if (rs2.next()) {
						value = StiServiceUtil.cleanValCsv(rs2.getString("codice_atc"));
					}

					if (!StiServiceUtil.isNull(value)) {
						valAic.addProperty("EXPORT_ATC", value);
					}
				}
				catch(Exception jex) {
					log.error("Errore durante l'esportazione del codice ATC - Json.", jex);
				}
				finally {
					if (rs2 != null) {
						try {
							rs2.close();
						} catch (Exception e) {
							log.error("Errore chiusura resultset.", e);
						}
					}
					if (ps2 != null) {
						try {
							ps2.close();
						} catch (Exception e) {
							log.error("Errore chiusura statement.", e);
						}
					}
				}

				retVal.put(codeAIC, valAic);
				countTot++;
			}

			log.info("Codici AIC totali: " + countTot);
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

		Map<String, JsonObject> aicMap = readTempAic(
				sourceTableDbUrl, sourceDbUsername, sourceDbPassword, tempImportFk);

		ExportAic.exportNewVersion(csVersionName, aicMap);
	}
}
