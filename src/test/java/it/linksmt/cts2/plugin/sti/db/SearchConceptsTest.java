package it.linksmt.cts2.plugin.sti.db;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetListValueFromParameterAndCodeSystemAndVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetParentConcept;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.text.ParseException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class SearchConceptsTest {
	
	
	public static void main(final String[] args) throws ParseException {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);
		
		select();
		//select1();
	}
	
	private static void select() {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		
		try {
			List<String> valori =  (List<String>) hibernateUtil.executeBySystem(new GetListValueFromParameterAndCodeSystemAndVersion("ICPC2","ICPC__7.0","INCLUSION","EN"));
			for (String string : valori) {
				System.out.println(string);
			}
		} catch (StiHibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StiAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	private static void select1() {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();

		try {
			
			
//			CodeSystemConcept concept =  (CodeSystemConcept) hibernateUtil.executeBySystem(new GetCodeSystemConcept("100-8",6));
			CodeSystemConcept concept =  (CodeSystemConcept) hibernateUtil.executeBySystem(new GetCodeSystemConcept("Cefoperazone",6));
			
			String result = getSubClassOf(hibernateUtil, concept, null);
			
			System.out.println(result);
		} catch (StiHibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StiAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static String getSubClassOf(final HibernateUtil hibernateUtil, final CodeSystemConcept concept,final String SUBCLASS_OF) throws StiAuthorizationException, StiHibernateException {
		String result = SUBCLASS_OF;
		if(concept != null){
			if(result!=null){
				result=concept.getCode()+"_"+result;
			}
			else{
				result=concept.getCode();
			}
			
			CodeSystemConcept parent = (CodeSystemConcept) hibernateUtil.executeBySystem(new GetParentConcept(concept));
			if(parent != null){
				result = getSubClassOf(hibernateUtil, parent, result);
			}
			else{
				if(result!=null && result.endsWith("_")){
					result=result.substring(0,result.length()-1);
				}
			}
		}
		return result;
	}

	
}
