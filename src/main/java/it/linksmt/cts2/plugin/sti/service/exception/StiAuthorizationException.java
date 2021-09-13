package it.linksmt.cts2.plugin.sti.service.exception;

@SuppressWarnings("serial")
public class StiAuthorizationException extends Exception {

	public StiAuthorizationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public StiAuthorizationException(final String message) {
		super(message);
	}

}
