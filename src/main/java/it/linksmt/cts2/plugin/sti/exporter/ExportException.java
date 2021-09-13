package it.linksmt.cts2.plugin.sti.exporter;

@SuppressWarnings("serial")
public class ExportException extends Exception {

	public ExportException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ExportException(final String message) {
		super(message);
	}

}
