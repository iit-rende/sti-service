package it.linksmt.cts2.plugin.sti.importer.icd9cm;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportIcd9Cm;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.exporter.icd9cm.ExportIcd9Map;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ImportOwl {

	private ImportOwl() {
	}

	private static Logger log = Logger.getLogger(ImportOwl.class);

	public static void importNewVersion(final HibernateUtil hibernateUtil, final File icd9cmEngOwl, final File icd9cmItaOwl, final String csVersionName, final String csVersionDescription,
			final Date effectiveDate, final String oid) throws Exception {

		Map<String, JsonObject> icd9Map = readOwlIta(icd9cmItaOwl);
		Map<String, JsonObject> icd9EngMap = readOwlEng(icd9cmEngOwl);

		mergeMaps(icd9Map, icd9EngMap);

		hibernateUtil.executeBySystem(new ImportIcd9Cm(csVersionName, csVersionDescription, oid, effectiveDate, icd9Map));

		// Esporta in formato json e csv
		ExportIcd9Map.exportNewVersion(csVersionName, icd9Map);
	}

	public static String getCode(final JsonObject icd9Data) throws ImportException {

		String code = null;
		if (icd9Data.get(Icd9CmFields.ICD9_CM_CODE) != null) {
			code = StiServiceUtil.trimStr(icd9Data.getAsJsonPrimitive(Icd9CmFields.ICD9_CM_CODE).getAsString());
		} else if (icd9Data.get(Icd9CmFields.ICD9_CM_CODE_RANGE) != null) {
			code = StiServiceUtil.trimStr(icd9Data.getAsJsonPrimitive(Icd9CmFields.ICD9_CM_CODE_RANGE).getAsString());
		}

		if (StiServiceUtil.isNull(code)) {
			log.error("Errore: codice identificativo del termine non specificato: " + icd9Data.toString());
			throw new ImportException("Codice identificativo del termine non specificato.");
		}

		return code;
	}

	private static void mergeMaps(final Map<String, JsonObject> icd9Map, final Map<String, JsonObject> icd9EngMap) throws ImportException {

		Iterator<Entry<String, JsonObject>> mapIt = icd9Map.entrySet().iterator();
		while (mapIt.hasNext()) {
			Map.Entry<String, JsonObject> entry = mapIt.next();

			JsonObject valJson = entry.getValue();
			String icd9Code = getCode(valJson);

			JsonObject translationEn = icd9EngMap.get(icd9Code);

			// Fix per i Code_Range
			if (icd9Code.indexOf("-") > 0) {
				if (translationEn == null) {
					translationEn = icd9EngMap.get(icd9Code + ".99");
				}
				if (translationEn == null) {
					translationEn = icd9EngMap.get(icd9Code + ".9");
				}
			}

			if (translationEn == null) {
				log.warn("Impossibile leggere la traduzione " + "in inglese per il codice: " + icd9Code);
				continue;
			}

			if (translationEn.get(Icd9CmFields.ICD9_CM_NAME_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_NAME_en, translationEn.get(Icd9CmFields.ICD9_CM_NAME_en));
			}
			if (translationEn.get(Icd9CmFields.ICD9_CM_DESCRIPTION_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_DESCRIPTION_en, translationEn.get(Icd9CmFields.ICD9_CM_DESCRIPTION_en));
			}
			if (translationEn.get(Icd9CmFields.ICD9_CM_OTHER_DESCR_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_OTHER_DESCR_en, translationEn.get(Icd9CmFields.ICD9_CM_OTHER_DESCR_en));
			}
			if (translationEn.get(Icd9CmFields.ICD9_CM_NOTE_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_NOTE_en, translationEn.get(Icd9CmFields.ICD9_CM_NOTE_en));
			}
			if (translationEn.get(Icd9CmFields.ICD9_CM_INCLUDE_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_INCLUDE_en, translationEn.get(Icd9CmFields.ICD9_CM_INCLUDE_en));
			}
			if (translationEn.get(Icd9CmFields.ICD9_CM_ESCLUDE_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_ESCLUDE_en, translationEn.get(Icd9CmFields.ICD9_CM_ESCLUDE_en));
			}
			if (translationEn.get(Icd9CmFields.ICD9_CM_USE_ADD_CODE_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_USE_ADD_CODE_en, translationEn.get(Icd9CmFields.ICD9_CM_USE_ADD_CODE_en));
			}
			if (translationEn.get(Icd9CmFields.ICD9_CM_CODIFY_FIRST_en) != null) {
				valJson.add(Icd9CmFields.ICD9_CM_CODIFY_FIRST_en, translationEn.get(Icd9CmFields.ICD9_CM_CODIFY_FIRST_en));
			}
		}
	}

	private static Map<String, JsonObject> readOwlEng(final File icd9EngMap) throws Exception {

		FileInputStream owlIn = null;
		try {
			owlIn = new FileInputStream(icd9EngMap);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(owlIn);
			Element root = doc.getDocumentElement();

			root.normalize();
			log.info("Root element :" + root.getNodeName());

			NodeList conceptsParent = root.getElementsByTagName("concepts");
			if (conceptsParent.getLength() != 1) {
				throw new ImportException("Definizione non corretta per il nodo \"concepts\". " + "Numero di nodi rilevato: " + conceptsParent.getLength());
			}

			int cntEntry = 0;
			NodeList allNodes = conceptsParent.item(0).getChildNodes();

			Map<String, JsonObject> icd9Map = new HashMap<String, JsonObject>();
			for (int i = 0; i < allNodes.getLength(); i++) {
				Node owlNode = allNodes.item(i);
				if (owlNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				String nodeName = StiServiceUtil.trimStr(owlNode.getNodeName()).toLowerCase();

				if (nodeName.equalsIgnoreCase("lgCon:concept")) {
					cntEntry++;
				} 
				else {
					throw new ImportException("Elemento non riconosciuto: " + nodeName);
				}

				JsonObject valDoc = new JsonObject();

				// Code o Code-Range ICD-9
				String icd9id = StiServiceUtil.trimStr(owlNode.getAttributes().getNamedItem("id").getNodeValue());

				if (StiServiceUtil.isNull(icd9id)) {
					throw new ImportException("Identificativo non presente: " + owlNode.getTextContent());
				}

				valDoc.addProperty(Icd9CmFields.ICD9_CM_ID, icd9id);

				JsonArray arrDescr = new JsonArray();
				JsonArray arrAltraDescr = new JsonArray();
				JsonArray arrNote = new JsonArray();

				JsonArray arrInclude = new JsonArray();
				JsonArray arrEsclude = new JsonArray();

				NodeList icd9CodeInfo = owlNode.getChildNodes();
				for (int j = 0; j < icd9CodeInfo.getLength(); j++) {

					Node infoNode = icd9CodeInfo.item(j);
					if (infoNode.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}

					String infoName = StiServiceUtil.trimStr(infoNode.getNodeName()).toLowerCase();

					if (infoName.equalsIgnoreCase("lgCon:conceptProperty")) {

						if (infoNode.getAttributes().getNamedItem("propertyId") != null) {

							String propertyId = StiServiceUtil.trimStr(infoNode.getAttributes().getNamedItem("propertyId").getNodeValue());

							String propertyDs = StiServiceUtil.trimStr(infoNode.getAttributes().getNamedItem("propertyName").getNodeValue());

							if (propertyId.equalsIgnoreCase("P-1") && propertyDs.equalsIgnoreCase("ICE")) {
								arrAltraDescr.add(new JsonPrimitive(StiServiceUtil.trimStr(infoNode.getTextContent())));
							} 
							else if (propertyDs.equalsIgnoreCase("ICN")) {
								String nodeCont = StiServiceUtil.trimStr(infoNode.getTextContent());

								if (nodeCont.toLowerCase().startsWith("code first")) {
									valDoc.addProperty(Icd9CmFields.ICD9_CM_CODIFY_FIRST_en, StiServiceUtil.trimStr(infoNode.getTextContent()));
								} else {
									arrNote.add(new JsonPrimitive(nodeCont));
								}
							} 
							else if (propertyDs.equalsIgnoreCase("SOS")) {
								String nodeCont = StiServiceUtil.trimStr(infoNode.getTextContent());

								if (nodeCont.toLowerCase().startsWith("includes")) {
									arrInclude.add(new JsonPrimitive(nodeCont));
								} 
								else if (nodeCont.toLowerCase().startsWith("excludes")) {
									arrEsclude.add(new JsonPrimitive(nodeCont));
								} 
								else {
									log.warn("Impossibile mappare un nodo \"SOS\" - " + icd9id + ": " + nodeCont);
								}
							} 
							else if (propertyDs.equalsIgnoreCase("ICA")) {
								valDoc.addProperty(Icd9CmFields.ICD9_CM_USE_ADD_CODE_en, StiServiceUtil.trimStr(infoNode.getTextContent()));
							}
						}
					} 
					else if (infoName.equalsIgnoreCase("lgCommon:entityDescription")) {
						valDoc.addProperty(Icd9CmFields.ICD9_CM_NAME_en, StiServiceUtil.trimStr(infoNode.getTextContent()));
					}
					else if (infoName.equalsIgnoreCase("lgCon:presentation")) {
						if (infoNode.getAttributes().getNamedItem("propertyId") != null) {

							String propertyId = StiServiceUtil.trimStr(infoNode.getAttributes().getNamedItem("propertyId").getNodeValue());

							String propertyDs = StiServiceUtil.trimStr(infoNode.getAttributes().getNamedItem("propertyName").getNodeValue());

							if (propertyId.equalsIgnoreCase("T-1") && propertyDs.equalsIgnoreCase("textualPresentation")) {
								arrDescr.add(new JsonPrimitive(StiServiceUtil.trimStr(infoNode.getTextContent())));
							}
						}
					}
				}

				valDoc.add(Icd9CmFields.ICD9_CM_DESCRIPTION_en, arrDescr);
				valDoc.add(Icd9CmFields.ICD9_CM_OTHER_DESCR_en, arrAltraDescr);
				valDoc.add(Icd9CmFields.ICD9_CM_NOTE_en, arrNote);
				valDoc.add(Icd9CmFields.ICD9_CM_INCLUDE_en, arrInclude);
				valDoc.add(Icd9CmFields.ICD9_CM_ESCLUDE_en, arrEsclude);

				icd9Map.put(icd9id, valDoc);
			}

			log.info("Entries ICD-9 CM totali (inglese): " + cntEntry);
			return icd9Map;
		} finally {
			try {
				owlIn.close();
			} catch (Exception ie) {
				log.error("Errore durante la chiusura del file.", ie);
			}
		}
	}

	private static Map<String, JsonObject> readOwlIta(final File icd9cmItaOwl) throws Exception {

		FileInputStream owlIn = null;
		try {
			owlIn = new FileInputStream(icd9cmItaOwl);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(owlIn);
			Element root = doc.getDocumentElement();

			root.normalize();
			log.info("Root element :" + root.getNodeName());

			int cntEntry = 0;
			NodeList allNodes = root.getChildNodes();

			Map<String, JsonObject> icd9Map = new HashMap<String, JsonObject>();
			for (int i = 0; i < allNodes.getLength(); i++) {

				Node owlNode = allNodes.item(i);
				if (owlNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				String nodeName = StiServiceUtil.trimStr(owlNode.getNodeName()).toLowerCase();

				if (nodeName.equalsIgnoreCase("owl:ontology") || nodeName.equalsIgnoreCase("owl:annotationproperty") || nodeName.equalsIgnoreCase("owl:objectproperty")) {
					continue;
				}

				if (nodeName.equalsIgnoreCase("owl:class")) {
					cntEntry++;
				} else {
					throw new ImportException("Elemento non riconosciuto: " + nodeName);
				}

				JsonObject valDoc = new JsonObject();

				// Idendificativo ICD-9
				String icd9id = null;
				try {
					icd9id = owlNode.getAttributes().getNamedItem("rdf:about").getNodeValue();
					icd9id = StiServiceUtil.trimStr(icd9id.substring(icd9id.indexOf("#") + 1));

					if (StiServiceUtil.isNull(icd9id)) {
						throw new ImportException("Identificativo non presente: " + owlNode.getAttributes().getNamedItem("rdf:about").getNodeValue());
					}
				} catch (Exception ex) {
					throw new ImportException("Impossibile leggere l'identificativo dell'elemento ICD-9.", ex);
				}

				valDoc.addProperty(Icd9CmFields.ICD9_CM_ID, icd9id);

				JsonArray arrDescr = new JsonArray();
				JsonArray arrAltraDescr = new JsonArray();
				JsonArray arrNote = new JsonArray();

				JsonArray arrInclude = new JsonArray();
				JsonArray arrEsclude = new JsonArray();

				NodeList icd9CodeInfo = owlNode.getChildNodes();
				for (int j = 0; j < icd9CodeInfo.getLength(); j++) {

					Node infoNode = icd9CodeInfo.item(j);
					if (infoNode.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}

					String infoName = StiServiceUtil.trimStr(infoNode.getNodeName()).toLowerCase();

					if (infoName.equalsIgnoreCase("rdfs:subClassOf")) {
						String superClass = null;
						try {
							superClass = infoNode.getAttributes().getNamedItem("rdf:resource").getNodeValue();
							superClass = StiServiceUtil.trimStr(superClass.substring(superClass.indexOf("#") + 1));
						} catch (Exception ex) {
						}

						if (StiServiceUtil.isNull(superClass)) {
							continue;
						}

						valDoc.addProperty(Icd9CmFields.ICD9_CM_SUBCLASS_OF, superClass);
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:code") || infoName.equalsIgnoreCase("code")) {
						valDoc.addProperty(Icd9CmFields.ICD9_CM_CODE, StiServiceUtil.trimStr(infoNode.getTextContent()));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:code_range") || infoName.equalsIgnoreCase("code_range")) {
						valDoc.addProperty(Icd9CmFields.ICD9_CM_CODE_RANGE, StiServiceUtil.trimStr(infoNode.getTextContent()));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:description_it") || infoName.equalsIgnoreCase("description_it")) {
						arrDescr.add(new JsonPrimitive(StiServiceUtil.trimStr(infoNode.getTextContent())));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:name_it") || infoName.equalsIgnoreCase("name_it")) {
						valDoc.addProperty(Icd9CmFields.ICD9_CM_NAME_it, StiServiceUtil.trimStr(infoNode.getTextContent()));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:type") || infoName.equalsIgnoreCase("type")) {
						valDoc.addProperty(Icd9CmFields.ICD9_CM_TYPE, StiServiceUtil.trimStr(infoNode.getTextContent()));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:note_it") || infoName.equalsIgnoreCase("note_it")) {
						arrNote.add(new JsonPrimitive(StiServiceUtil.trimStr(infoNode.getTextContent())));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:other_descr_it") || infoName.equalsIgnoreCase("other_descr_it")) {
						arrAltraDescr.add(new JsonPrimitive(StiServiceUtil.trimStr(infoNode.getTextContent())));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:excludes_it") || infoName.equalsIgnoreCase("excludes_it")) {
						arrEsclude.add(new JsonPrimitive(StiServiceUtil.trimStr(infoNode.getTextContent())));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:includes_it") || infoName.equalsIgnoreCase("includes_it")) {
						arrInclude.add(new JsonPrimitive(StiServiceUtil.trimStr(infoNode.getTextContent())));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:codify_first_it") || infoName.equalsIgnoreCase("codify_first_it")) {
						valDoc.addProperty(Icd9CmFields.ICD9_CM_CODIFY_FIRST_it, StiServiceUtil.trimStr(infoNode.getTextContent()));
					} 
					else if (infoName.equalsIgnoreCase("ICD9CM-2007-ita:use_add_code_it") || infoName.equalsIgnoreCase("use_add_code_it")) {
						valDoc.addProperty(Icd9CmFields.ICD9_CM_USE_ADD_CODE_it, StiServiceUtil.trimStr(infoNode.getTextContent()));
					}
				}

				valDoc.add(Icd9CmFields.ICD9_CM_DESCRIPTION_it, arrDescr);
				valDoc.add(Icd9CmFields.ICD9_CM_OTHER_DESCR_it, arrAltraDescr);
				valDoc.add(Icd9CmFields.ICD9_CM_NOTE_it, arrNote);
				valDoc.add(Icd9CmFields.ICD9_CM_INCLUDE_it, arrInclude);
				valDoc.add(Icd9CmFields.ICD9_CM_ESCLUDE_it, arrEsclude);

				icd9Map.put(icd9id, valDoc);
			}

			log.info("Entries ICD-9 CM totali (italiano): " + cntEntry);
			return icd9Map;
		} finally {
			try {
				owlIn.close();
			} catch (Exception ie) {
				log.error("Errore durante la chiusura del file.", ie);
			}
		}
	}

	public static void testExportVersion(final File icd9cmEngOwl, final File icd9cmItaOwl, final String csVersionName) throws Exception {

		Map<String, JsonObject> icd9Map = readOwlIta(icd9cmItaOwl);
		Map<String, JsonObject> icd9EngMap = readOwlEng(icd9cmEngOwl);

		mergeMaps(icd9Map, icd9EngMap);

		ExportIcd9Map.exportNewVersion(csVersionName, icd9Map);
	}
}
