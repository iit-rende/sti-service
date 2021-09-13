package it.linksmt.cts2.plugin.sti.db;

import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetExtraMetadataParameterByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetExtraMetadataParameterValueByCs;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.enums.MetadataParameterType;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.text.ParseException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class ExtraMetadataParameterTest {
	
	
	private static final String CODESYSTEM = "Cha";
	private static final String PARAMETER = "parametro1";
	private static final String VALORE = "valore1";
	
	public static void main(final String[] args) throws ParseException {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);
		// LogManager.getLogger("").setLevel(Level.WARN);

		//insertExtraParameter();
		getExtraParameter();


	}

	private static void insertExtraParameter() {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();

		try {
			List<CodeSystem> css = (List<CodeSystem>) hibernateUtil.executeBySystem(new GetCodeSystemByName(CODESYSTEM));
			for (CodeSystem codeSystem : css) {
				System.out.println(codeSystem.getName());

				ExtraMetadataParameter emp = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(PARAMETER, codeSystem, MetadataParameterType.STRING.getKey(), null, VALORE));
				System.out.println(emp.getId() + " - " + emp.getParamName());
			}
		} catch (StiHibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StiAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getExtraParameter() {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();

		try {

			List<ExtraMetadataParameter> emp1 = (List<ExtraMetadataParameter>) hibernateUtil.executeBySystem(new GetExtraMetadataParameterByName(PARAMETER));
			if (emp1.size() > 0) {
				System.out.println(emp1.get(0).getId() + " - " + emp1.get(0).getParamName());
			}
			
			
			List<CodeSystem> css = (List<CodeSystem>) hibernateUtil.executeBySystem(new GetCodeSystemByName(CODESYSTEM));
			for (CodeSystem codeSystem : css) {
				System.out.println(codeSystem.getName());

				List<ExtraMetadataParameter> emp2 = (List<ExtraMetadataParameter>) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCs(codeSystem.getId()));
				if (emp2.size() > 0) {
					System.out.println(emp2.get(0).getId() + " - " + emp2.get(0).getParamName());
				}
			}
			

		} catch (StiHibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StiAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	

}
