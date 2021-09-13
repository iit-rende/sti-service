package it.linksmt.cts2.plugin.sti.importer;

@SuppressWarnings("serial")
public class IndexException extends Exception {

	public IndexException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public IndexException(final String message) {
		super(message);
	}

}
