package it.linksmt.cts2.plugin.sti.transformer;

import java.util.Map;

/**
 * The Kitchen model that will contains all the data to pass to the Kitchen
 * launcher.
 * 
 * @author Davide Pastore
 *
 */
public class KitchenModel {
	private String file;
	private Map<String, String> params;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

}
