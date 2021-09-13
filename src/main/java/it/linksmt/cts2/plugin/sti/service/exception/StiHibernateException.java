package it.linksmt.cts2.plugin.sti.service.exception;

@SuppressWarnings("serial")
public class StiHibernateException extends Exception {

	public StiHibernateException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public StiHibernateException(final String message) {
		super(message);
	}

}
