package it.linksmt.cts2.plugin.sti.service.exception;

@SuppressWarnings("serial")
public class StiQueryServiceException extends RuntimeException {

	public StiQueryServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public StiQueryServiceException(final String message) {
		super(message);
	}

}
