package it.linksmt.cts2.plugin.sti.exporter;

import java.io.File;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ExportUtil {

	public static String BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_EXPORT_BASE_PATH, "");

	private ExportUtil() {}

	public static void addJsonString(final JsonObject jsonObj, final String name, final String value) {
		if ((!StiServiceUtil.isNull(name)) && (!StiServiceUtil.isNull(value))) {
			jsonObj.addProperty(name, StiServiceUtil.trimStr(value));
		}
	}

	public static String getAsString(final JsonObject jsonObj, final String name) {
		if ( (jsonObj.get(name) != null) && (! (jsonObj.get(name) instanceof JsonNull))) {
			return StiServiceUtil.trimStr(jsonObj.getAsJsonPrimitive(name).getAsString());
		}
		return "";
	}

	public static String jsonArrayToString(final JsonObject jsonObj, final String name, final String separator) {
		String retVal = "";

		if (jsonObj.get(name) != null) {
			JsonArray arr = jsonObj.getAsJsonArray(name);
			for (int i = 0; i < arr.size(); i++) {
				if (retVal.length() > 0) {
					retVal += separator;
				}
				retVal += arr.get(i).getAsString();
			}
		}

		return retVal;
	}

	public static File getFileExport(
		final String folder,
		final String codeSystemName, final String codeSystemVersion,
		final String variant, final String fileExtension) {

		String fileName = StiServiceUtil.trimStr(codeSystemName).toUpperCase() + "_"
				+ StiServiceUtil.trimStr(codeSystemVersion).replace(".", "_").toUpperCase();

		if (!StiServiceUtil.isNull(variant)) {
			fileName += "_" + StiServiceUtil.trimStr(variant).toLowerCase();
		}
		
		fileName += fileExtension;
		return new File(BASE_PATH + "/" + folder, fileName);
	}
}
