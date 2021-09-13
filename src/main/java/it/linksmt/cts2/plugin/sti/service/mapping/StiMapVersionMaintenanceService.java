package it.linksmt.cts2.plugin.sti.service.mapping;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.mayo.cts2.framework.model.core.MapReference;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionMaintenanceService;
import it.linksmt.cts2.plugin.sti.db.commands.delete.DeleteMapSetVersion;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.exporter.mapping.ExportMapSet;
import it.linksmt.cts2.plugin.sti.importer.ChangeLogUtil;
import it.linksmt.cts2.plugin.sti.importer.mapset.ImportMapSetCsv;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiMapVersionMaintenanceService
	extends AbstractStiService
	implements MapVersionMaintenanceService {

	private static String CSV_BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");

	public static String EXPORT_BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_EXPORT_BASE_PATH, "");

	@Override
	public MapVersion createResource(final MapVersion resource) {

		try {
			String csvPath = StiServiceUtil.trimStr(resource.getSourceStatements());

			File csvData = new File(CSV_BASE_PATH + "/" + StiServiceUtil.trimStr(csvPath));
			if (!csvData.isFile()) {
				throw new RuntimeException("ERRORE - impossibile accedere al file: "
						+ csvData.getAbsolutePath());
			}
			
			String fromCsVers = resource.getFromCodeSystemVersion().getVersion().getContent();
			String toCsVers   = resource.getToCodeSystemVersion().getVersion().getContent();
			
			
			String mapVersionNameJson   = resource.getMapVersionName();
			String description = null;
			String organization = null;
			if(mapVersionNameJson!=null){
				try{
					Map<String, String> mapVersionName = new Gson().fromJson(mapVersionNameJson, new TypeToken<Map<String, String>>(){}.getType());
					description = mapVersionName.get("mappingDescription");
					organization = mapVersionName.get("mappingOrganization");
				}catch(Exception e){
					throw new RuntimeException("Impossibile eseguire l'importazone del Mapping generico.", e);
				}
				
			}
			

			MapSetVersion vers = ImportMapSetCsv.importNewVersion(fromCsVers, toCsVers, resource.getOfficialReleaseDate(), csvData, description, organization );

			// Esporta in formato JSON
			ExportMapSet.exportMapSetVersion(vers.getFullname(), vers.getReleaseDate(), csvData);

			// Copia il file in formato CSV per l'esportazione
			String fileName = StiServiceUtil.trimStr(vers.getFullname()).replace(".", "_").replace(" - ", "_")
					.replace("(", "").replace(")", "").replace(" ", "_").toUpperCase() + ".csv";

			File mapset_csv = new File(EXPORT_BASE_PATH + "/" + ExportController.MAPSET, fileName);
			Files.move(csvData, mapset_csv);

			MapReference mapRef = new MapReference();
			mapRef.setContent(vers.getFullname());

			resource.setVersionOf(mapRef);
			
			return resource;
		}
		catch(Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException)ex;
			}
			throw new RuntimeException("Impossibile eseguire l'importazone del Mapping generico.", ex);
		}
	}

	@Override
	public void deleteResource(final NameOrURI identifier, final String changeSetUri) {

		String name = identifier.getName();
		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			hibUtil.executeByUser(new DeleteMapSetVersion(name), SessionUtil.getLoggedUser());
		}
		catch(Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException)ex;
			}
			throw new RuntimeException("Impossibile eseguire l'eliminazione del Mapping generico.", ex);
		}
	}

	@Override
	public void updateChangeableMetadata(final NameOrURI identifier, final UpdateChangeableMetadataRequest request) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateResource(final MapVersion resource) {
		throw new UnsupportedOperationException();
	}
}
