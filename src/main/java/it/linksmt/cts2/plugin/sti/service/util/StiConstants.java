package it.linksmt.cts2.plugin.sti.service.util;


public interface StiConstants {

	String LOCAL_LANGUAGE_CD = "LOC";
	String LOCAL_VALUE_SEPARATOR = "_#_V#_: ";

	String ALL_ENTITIES_FILTER 		= "ALL";
	String ENTITY_CHILDREN_FILTER 	= "SUBCLASS_OF";

	String TAXONOMY_FORWARD_NAME = "Sottoclasse di";
	String TAXONOMY_REVERSE_NAME = "Superclasse di";

	String AIC_ATC_FORWARD_NAME = "Equivalente ATC";
	String AIC_ATC_REVERSE_NAME = "Equivalente AIC";

	String GENERIC_FORWARD_NAME = "Correlato a";
	String GENERIC_REVERSE_NAME = "Correlato a";
	
	String LANG_IT = "IT";
	String LANG_EN = "EN";
	
	String NAME_VERSION_SEPARATOR = "__";
	
	

	/*
	 *   COSTANTI PER LE TABELLE (MODELLO CONCETTUALE CTS-2)
	 */
	enum STATUS_CODES {
		INACTIVE(0), ACTIVE(1), DELETED(2);
		private int code;

		private STATUS_CODES(final int c) {
			code = c;
		}

		public int getCode() {
			return code;
		}

		public static String getAsString(final int c) {
			if (c == 0) {
				return INACTIVE.name();
			}
			else if (c == 1) {
				return ACTIVE.name();
			}
			else if (c == 1) {
				return DELETED.name();
			}

			return null;
		}
	}

	enum ASSOCIATION_KIND {
		ONTOLOGY(1), TAXONOMY(2), CROSS_MAPPING(3), LINK(4);
		private int code;

		private ASSOCIATION_KIND(final int c) {
			code = c;
		}

		public int getCode() {
			return code;
		}
	}

	enum STATUS_WORKFLOW_CODES {
		ACTIVE(1), DEPRECATED(2), DISCOURAGED(3), TRIAL(4);
		private int code;

		private STATUS_WORKFLOW_CODES(final int c) {
			code = c;
		}

		public int getCode() {
			return code;
		}
	}
}
