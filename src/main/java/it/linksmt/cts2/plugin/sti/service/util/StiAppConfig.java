package it.linksmt.cts2.plugin.sti.service.util;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;


public final class StiAppConfig {

	// NON rimuovere: gestione della classe come se fosse un Singleton
	private StiAppConfig() { }

	private static Logger log = Logger.getLogger(StiAppConfig.class);

	// Posizione file di configurazione
	public static final	String CONFIG_FILE_LOCATION = "STI_CTS2_CONFIG";


	private static java.util.Properties _props = null;
	static {
		try {

//			 Caricamento properties file customizzabile
			String confPath = cleanEnvVar(System.getenv(CONFIG_FILE_LOCATION));
			log.info("*********************Path di configurazione " + confPath);
			if (confPath.trim().length() < 1) {
				confPath = cleanEnvVar(System.getProperty(CONFIG_FILE_LOCATION));
			}

			InputStream inputStream = new FileInputStream(confPath);

			_props = new java.util.Properties();
			_props.load(inputStream);
		}
		catch (Exception ex) {
			_props = null;
			log.error("Errore nella lettura della configurazione dell'applicazione. " + ex.getMessage(),ex);
		}
	}

	public static String getProperty(final String name) {
		return getProperty(name, "");
	}

	public static String getProperty(final String name, final String defaultValue) {

		String retVal = null;
		if ((_props != null) && (name != null)) {
			retVal = _props.getProperty(name.trim(), defaultValue);
		}

		return retVal;
	}


	private static String cleanEnvVar(final String original) {

		if (original == null) {
			return "";
		}

		return original.replace("\n", "").replace("\r", "").replace("\"", "").trim();
	}
}
