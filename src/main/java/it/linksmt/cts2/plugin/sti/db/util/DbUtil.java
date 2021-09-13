package it.linksmt.cts2.plugin.sti.db.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.db.model.TempImportazione;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

/**
 * Utility for the {@link TempImportazione} table.
 *
 * @author Davide Pastore
 *
 */
public final class DbUtil {

	private static Logger log = Logger.getLogger(DbUtil.class);
	
	protected static String SOURCE_TABLE_URL = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_ADDRESS, "");
	protected static String SOURCE_TABLE_USER = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_USER, "");
	protected static String SOURCE_TABLE_PASS = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_PASS, "");

	private DbUtil() { }

	/**
	 * Add a record in the temp_importazione table.
	 *
	 * @param sourceTableDbUrl
	 *            The source table Db url.
	 * @param sourceDbUsername
	 *            The source db username.
	 * @param sourceDbPassword
	 *            The source db password.
	 * @param tempImportazione
	 *            The object to insert.
	 * @return Returns the id of the inserted element.
	 */
	public static Long create(final String sourceTableDbUrl,
			final String sourceDbUsername, final String sourceDbPassword,
			final TempImportazione tempImportazione)
		throws ImportException{

		Connection conn = null;
		PreparedStatement ps = null;
		Long codTempImportazione = -1l;

		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword);

			conn.setAutoCommit(false);
			ps = conn
					.prepareStatement(
							"INSERT INTO temp_importazione(start_time_stamp, name_code_system, version_code_system, effective_date, status, oid, description, status_message) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
			ps.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
			ps.setString(2, tempImportazione.getNameCodeSystem());
			ps.setString(3, tempImportazione.getVersionCodeSystem());
			ps.setDate(4, tempImportazione.getEffectiveDate());
			ps.setString(5, tempImportazione.getStatus());
			ps.setString(6, tempImportazione.getOid());
			ps.setString(7, tempImportazione.getDescription());
			ps.setString(8, tempImportazione.getStatusMessage());

			// execute insert SQL stetement
			int affectedRows = ps.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException(
						"Errore in fase di creazione di temp_importazione.");
			}

			try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					codTempImportazione = generatedKeys.getLong(1);
				} else {
					throw new SQLException(
							"Errore in fase di creazione di temp_importazione, non è stato possibile ottenere l'ID dell'elemento inserito.");
				}
			}

			conn.commit();
		}
		catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException sqlException) {
				log.error("Errore durante l'esecuzione della rollback.",
						sqlException);
			}

			throw new ImportException("Impossibile aggiornare lo stato di esecuzione"
					+ " delle procedure di importazione.", e);
		}
		finally {
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

		return codTempImportazione;
	}

	/**
	 * Read the last temp_importazione record from the temp_importazione table.
	 *
	 * @param sourceTableDbUrl
	 *            The source table Db url.
	 * @param sourceDbUsername
	 *            The source db username.
	 * @param sourceDbPassword
	 *            The source db password.
	 * @return Returns the last {@link TempImportazione} object.
	 */
	public static TempImportazione readLast(final String sourceTableDbUrl,
			final String sourceDbUsername, final String sourceDbPassword)
	throws ImportException {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		TempImportazione tempImportazione = null;

		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword);

			ps = conn.prepareStatement("SELECT * FROM temp_importazione "
					+ " ORDER BY start_time_stamp DESC LIMIT 1");

			// execute select SQL stetement
			rs = ps.executeQuery();

			while (rs.next()) {
				tempImportazione = new TempImportazione(rs);
			}
		}
		catch(Exception iex) {
			throw new ImportException("Impossibile leggere lo stato di esecuzione"
					+ " delle procedure di importazione.", iex);
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

		return tempImportazione;
	}

	/**
	 *
	 * @param sourceTableDbUrl
	 *            The source table Db url.
	 * @param sourceDbUsername
	 *            The source db username.
	 * @param sourceDbPassword
	 *            The source db password.
	 * @param codTempImportazione Cod temp importazione.
	 * @param status The status.
	 * @param statusMessage The status message.
	 * @return Returns true if the update is ok, false otherwise.
	 * @throws ImportException
	 */
	public static Boolean updateStatus(final String sourceTableDbUrl,
			final String sourceDbUsername, final String sourceDbPassword,
			final Long codTempImportazione, final String status, final String statusMessage)
		throws ImportException {

		Connection conn = null;
		PreparedStatement ps = null;
		Boolean result = false;

		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(sourceTableDbUrl,
					sourceDbUsername, sourceDbPassword);

			conn.setAutoCommit(false);
			ps = conn.prepareStatement(
							"UPDATE temp_importazione SET status = ?, status_message = ? WHERE cod_temp_importazione = ?",
							Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, status);
			ps.setString(2, statusMessage);
			ps.setLong(3, codTempImportazione);

			// execute update SQL stetement
			ps.executeUpdate();
			conn.commit();

			result = true;
		}
		catch (Exception e) {

			try {
				conn.rollback();
			}
			catch (SQLException sqlException) {
				log.error("Errore durante l'esecuzione della rollback.",
						sqlException);
			}

			throw new ImportException("Impossibile aggiornare lo stato di esecuzione"
					+ " delle procedure di importazione.", e);
		}
		finally {
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

		return result;
	}
	
	/**
	 * Update status workflow if it can.
	 * @param tempImportId
	 * @param status
	 * @param statusMessage
	 * @throws ImportException
	 */
	public static void updateStatusWorkflow(final long tempImportId,
			final String status, final String statusMessage)
		throws ImportException {
		
		TempImportazione last = DbUtil.readLast(SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS);
		if (last != null && last.getStatus().equals(ImportValues.ERROR)) {
			throw new ImportException("Lo stato dell'importazione è " + ImportValues.ERROR);
		}

		if (!DbUtil.updateStatus(SOURCE_TABLE_URL,
					SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
					tempImportId, status, statusMessage)) {
			throw new ImportException("Impossibile aggiornare lo stato della procedura di importazione");
		}
	}
}