package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.dtos.MapSetVersionDto;
import it.linksmt.cts2.plugin.sti.dtos.OutputDto;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Session;

public class GetMapSetVersionAlt extends HibernateCommand {


	public GetMapSetVersionAlt() {

	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public OutputDto execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		List<MapSetVersion> chkList = session.createCriteria(MapSetVersion.class).list();
		List<MapSetVersionDto> mapSetVersionDtoList = new ArrayList<MapSetVersionDto>(0);
		for (MapSetVersion mapSetVersion : chkList) {
			MapSetVersionDto mapSetVersionDto = new MapSetVersionDto();
			try {
				BeanUtils.copyProperties(mapSetVersionDto, mapSetVersion);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String fullname = mapSetVersion.getFullname();
			String csSrcName = parseFullName(fullname,"src");
			String csTgrName = parseFullName(fullname,"trg");
			
			List<CodeSystem> codeSystemsListSrc = new GetCodeSystemByName(csSrcName).execute(session);
			List<CodeSystem> codeSystemsListTrg = new GetCodeSystemByName(csTgrName).execute(session);
			CodeSystem csSrc = null;
			CodeSystem csTrg = null;
			
			if(codeSystemsListSrc!=null && codeSystemsListSrc.size()>0){
				csSrc = codeSystemsListSrc.get(0);
			}
			
			if(codeSystemsListTrg!=null && codeSystemsListTrg.size()>0){
				csTrg = codeSystemsListTrg.get(0);
			}
			
			String domainSrc = getDomainByCs(session, csSrc);
			
			String domainTrg = getDomainByCs(session, csTrg);

			mapSetVersionDto.setDomainSrc(domainSrc);
			mapSetVersionDto.setDomainTrg(domainTrg);
			mapSetVersionDto.setCsSrc(csSrcName);
			mapSetVersionDto.setCsTrg(csTgrName);
			mapSetVersionDtoList.add(mapSetVersionDto);
		}
		
		
		OutputDto outputDto = new OutputDto();
		outputDto.setNumFound(Long.valueOf(chkList.size()));
		outputDto.setEntry(mapSetVersionDtoList);

		return outputDto;
	}

	private String getDomainByCs(final Session session, CodeSystem cs) throws StiAuthorizationException, StiHibernateException {
		String domain = "";
		if(cs!=null){
			if(cs.getCodeSystemType().equals(CodeSystemType.STANDARD_NATIONAL_STATIC.getKey())){
				domain="salute";
			}
			else{
				ExtraMetadataParameter extraMetadataParameter = new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.DOMAIN).execute(session);
				if(extraMetadataParameter!=null && extraMetadataParameter.getParamValue()!=null){
					domain = extraMetadataParameter.getParamValue();
				}
			}
		}
		return domain;
	}
	
	private static String parseFullName(String fullname,String typeElement) {
		int idx = -1;
		if(typeElement.equals("src")){
			idx=0;
		}
		else if(typeElement.equals("trg")){
			idx=1;
		}
		String tmp1[] = fullname.split("\\) - ");
		String name = "";
		if(tmp1[0].indexOf("(")!=-1){
			String tmp2[] = tmp1[idx].split("\\(");
			name = tmp2[0].trim();
		}
		return name;
	}
}
