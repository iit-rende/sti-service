package it.linksmt.cts2.plugin.sti.service.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;

public final class StiServiceUtil {

	public static String SOLR_URL = StiAppConfig.getProperty(StiServiceConfiguration.CTS2_STI_SOLR_ADDRESS, "");
	
	public static String SERVER_URL = StiAppConfig.getProperty(StiServiceConfiguration.CTS2_STI_SERVER_ADDRESS, "");

	private StiServiceUtil() { }

	public static final String EXTERNAL_ENTITY_NAME_SEPERATOR = ":";

	public static Pattern NUMERIC_PATTERN = Pattern.compile("[0-9]+");

	//pattern source: <a href="http://regxlib.com/REDetails.aspx?regexp_id=26" target="_blank" rel="nofollow">http://regxlib.com/REDetails.aspx?regexp_id=26</a>
	public static final Pattern EMAIL_PATTERN = Pattern.compile(
		      "([_A-Za-z0-9-]+)(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})");


	public static String buildCsIndexPath(final String csName) {
		if(csName.equals(StandardLocalFields.STANDARD_LOACL_CODE_SYSTEM_INDEX_SUFFIX_NAME) || csName.equals(ValueSetFields.VALUESET_INDEX_SUFFIX_NAME)){
			return SOLR_URL + "/sti_" + csName.toLowerCase();
		}
		else{
			return SOLR_URL + "/sti_" + StiServiceUtil.cleanStr(csName).toLowerCase();
		}
	}

	public static boolean isNull(final String aString) {

		if ((aString == null) || (aString.trim().length() == 0)) {
			return true;
		}

		return false;
	}

	public static String trimStr(final String original) {

		String retVal = "";
		if (original != null) {
			retVal = original.trim();
		}

		return retVal;
	}

	public static String cleanStr(final String source) {

		String retVal = trimStr(source);

		// ASCII
		retVal = Normalizer.normalize(retVal, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		retVal = pattern.matcher(retVal).replaceAll("");

		// AlphaAndDigits
		retVal = retVal.replaceAll("[^a-zA-Z0-9]+", "");

		return retVal;
	}

	public static String getByUriXpath(final String uriPath, final String uri) {
		String expressionString =
				 "[" + uriPath + "='" + uri + "']";

		return expressionString;
	}

	public static String paramNameToUpperCaseAndClean(String n) {
		if(n!=null){
			n = n.trim().toUpperCase().replaceAll("\\s","_").replaceAll("\\(","").replaceAll("\\)","").replaceAll("\"","");
		}	
		return n; 
	  }

	public static String getExternalEntityName(final ScopedEntityName name) {
		return name.getNamespace() + EXTERNAL_ENTITY_NAME_SEPERATOR + name.getName();
	}
	
//	public static String getExternalEntityName(final Object name) {
//		return "";
//	}
	
	public static String cleanValCsv(String valCsv) {
		if (valCsv == null) {
			return null;
		}

		valCsv = valCsv.trim();
		while(valCsv.startsWith("\"")) {
			valCsv = valCsv.substring(1);
		}
		while(valCsv.endsWith(";")) {
			valCsv = valCsv.substring(0, valCsv.length()-1);
		}

		while(valCsv.endsWith("\"")) {
			valCsv = valCsv.substring(0, valCsv.length()-1);
		}

		if (StiServiceUtil.isNull(valCsv)) {
			return null;
		}

		return valCsv.trim();
	}
	
	
	public static String makeMappingName(String title) {
		if(title!=null && title.indexOf(StiConstants.NAME_VERSION_SEPARATOR)!=-1){
			String[] tmp = title.split(" - ");
			if(tmp.length==2){
				String[] src = tmp[0].split("\\(");
				String[] trg = tmp[1].split("\\(");

				String csSrc = src[0];
				String csTrg = trg[0];
				String versionSrc = src[1];
				String versionTrg = trg[1];
				
				if(versionSrc.indexOf(StiConstants.NAME_VERSION_SEPARATOR)!=-1){
					String[] versionSrcTmp = src[1].split(StiConstants.NAME_VERSION_SEPARATOR);
					versionSrc = versionSrcTmp[1];
				}
				
				if(versionTrg.indexOf(StiConstants.NAME_VERSION_SEPARATOR)!=-1){
					String[] versionTrgTmp = trg[1].split(StiConstants.NAME_VERSION_SEPARATOR);
					versionTrg = versionTrgTmp[1];
				}

				versionSrc = versionSrc.replace(")","");
				versionTrg = versionTrg.replace(")","");
				
				title = csSrc +" ("+versionSrc+")"+" - "+csTrg+" ("+versionTrg+")";
			}
		}
		return title;
	}
}

