package it.linksmt.cts2.plugin.sti.transformer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

/**
 * Launch the kitchen command.
 *
 * @author Davide Pastore
 *
 */
public class KitchenLauncher {

	private static Logger log = Logger.getLogger(KitchenLauncher.class);

	private KitchenModel kitchenModel;

	private String kitchenExecutablePath = StiAppConfig.getProperty(StiServiceConfiguration.KITCHEN_EXECUTABLE_PATH, "");

	/**
	 * Create the {@link KitchenLauncher} instance by the given parameter.
	 *
	 * @param kitchenModel
	 *            The {@link KitchenModel} from which retrieve the parameters.
	 */
	public KitchenLauncher(final KitchenModel kitchenModel) {
		this.setKitchenModel(kitchenModel);
	}

	/**
	 * Execute the Kitchen command.
	 */
	public void execute() {
		String command = generateCommand();

		//Execute the kitchen command
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				log.info(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate the command to launch.
	 *
	 * @return Returns the {@link String} that contains the command to launch
	 */
	private String generateCommand() {
		//Build by the given path
		StringBuilder command = new StringBuilder(kitchenExecutablePath);

		//Add file
		command.append(" -file:" + kitchenModel.getFile() + " ");

		//Add additional parameters
		Map<String, String> params = kitchenModel.getParams();
		for (Map.Entry<String, String> param : params.entrySet()) {
			command.append("-param:" + param.getKey() + "=" + param.getValue() + " ");
		}

		//Returns the generated command
		log.info("Esecuzione etl: " + command.toString());
		return command.toString();
	}

	public KitchenModel getKitchenModel() {
		return kitchenModel;
	}

	public void setKitchenModel(final KitchenModel kitchenModel) {
		this.kitchenModel = kitchenModel;
	}

}
