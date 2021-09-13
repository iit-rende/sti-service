package it.linksmt.cts2.plugin.sti.importer;

@SuppressWarnings("serial")
public class ImportException extends Exception {

	public ImportException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ImportException(final String message) {
		super(message);
	}

}
