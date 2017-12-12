package logicInterpreter.Tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.MultipleOutputsInInputException;
import logicInterpreter.Exceptions.NoInputFoundException;
import logicInterpreter.Exceptions.NoSuchTypeException;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.InputBean;
import logicInterpreter.Nodes.OutputBean;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class XMLparse {


	 private static boolean validateXML(File xsd, File xml) throws SAXException, IOException{
	      try {
	         SchemaFactory schemaFact =
	            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	            Schema schema = schemaFact.newSchema(xsd);
	            Validator v = schema.newValidator();
	            v.validate(new StreamSource(xml));
	      } catch (IOException e){
	         throw e;
	      }catch(SAXException e1){
	         throw e1;
	      }
			
	      return true;
		
	   }
	
	
	/*
	 * 
	 */
	
	//TODO: wyjątki
	public static BlockBean parseXMLBlock(File file) throws Exception{
		BlockBean block = new BlockBean();

		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc;
			doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			Node rootNode = doc.getFirstChild();
			Element rootElem = (Element) rootNode;
			block.setName(rootElem.getAttribute("name"));
			String type = rootElem.getAttribute("type");
			block.setType(type);
			
			if(type.equals("formula")){
				//typ: formula
			validateXML(new File("xmls/validateSchemas/FormulaBlockSchema.xsd"), file);
			NodeList inputsNode = doc.getElementsByTagName("inputs");
			int inputCount = doc.getElementsByTagName("input").getLength();
			Element inputs = (Element) inputsNode.item(0);

				for (int i = 0; i < inputCount; i++) {
					String inputName = inputs.getElementsByTagName("input").item(i).getTextContent();
					block.addInput(inputName);
				}
				NodeList outputsNode = doc.getElementsByTagName("outputs");
				int outputCount = doc.getElementsByTagName("output").getLength();
				Element outputs = (Element) outputsNode.item(0);

				for (int i = 0; i < outputCount; i++) {
					Element outputNode = (Element) outputs.getElementsByTagName("output").item(i);
					String outputName = "", outputFormula = "";

					try {
						outputName = outputNode.getTextContent();
					} catch (NullPointerException e) {
					}
					try {
						outputFormula = outputNode.getAttribute("function");
					} catch (NullPointerException e) {
					}

					block.addOutput(outputName, outputFormula);
				}
			}
			else if(type.equals("diagram")){
				validateXML(new File("xmls/validateSchemas/DiagramBlockSchema.xsd"), file);
				String source = file.getParent() + "/" + doc.getElementsByTagName("src").item(0).getTextContent();
				DiagramBean diagram = parseXMLDiagram(new File(source));
				block.setDiagram(diagram);
				for (int i = 0; i < diagram.getInputList().size(); i++) {
					DiagramInputBean input = diagram.getInput(i);
					String inputName = input.getName();
					block.addInput(inputName);
				}
				for (int i = 0; i < diagram.getOutputList().size(); i++) {
					DiagramOutputBean output = diagram.getOutput(i);
					String outputName = output.getName();
					block.addOutput(outputName, null);
				}
			}
			else{
				//nieznany typ
				throw new NoSuchTypeException(type);
			}

		} catch (Exception e) {
			throw e;
		}

		return block;
	}

	
	static List<String> antiRecurrencyList = new ArrayList<String>();
	//TODO: wyjątki
	public static DiagramBean parseXMLDiagram(File file) throws Exception{
		DiagramBean diagram = new DiagramBean();
		try {
			validateXML(new File("xmls/validateSchemas/DiagramSchema.xsd"), file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc;
			doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			Element rootElem = doc.getDocumentElement();
			diagram.setName(rootElem.getAttribute("name"));
			
			if(antiRecurrencyList.indexOf(file.getAbsolutePath()) != -1)
				throw new RecurrentLoopException();
			antiRecurrencyList.add(file.getAbsolutePath());
			if (!doc.getDocumentElement().getNodeName().equals("diagram"))
				return null;
			// tworzenie bloków
			Element blocks = (Element) doc.getElementsByTagName("blocks").item(0);
			if (blocks != null) {
				NodeList blockList = blocks.getElementsByTagName("block");
				for (int i = 0; i < blockList.getLength(); i++) {
					Node blockNode = blockList.item(i);
					if (blockNode.getNodeType() == Node.ELEMENT_NODE) {
						Element blockElement = (Element) blockNode;
						String src = blockElement.getElementsByTagName("src").item(0).getTextContent();
						BlockBean blockBean = parseXMLBlock(new File( file.getParent() + "/" +  src));
						blockBean.setName(blockElement.getElementsByTagName("id").item(0).getTextContent());
						diagram.addBlock(blockBean);
					}
				}
			}
			antiRecurrencyList.clear();
			// tworzenie wyjsc diagramu
			Element outputs = (Element) doc.getElementsByTagName("outputs").item(0);
			if (outputs != null) {
				NodeList outputsList = outputs.getElementsByTagName("output");
				for (int i = 0; i < outputsList.getLength(); i++) {
					Node outputNode = outputsList.item(i);
					if (outputNode.getNodeType() == Node.ELEMENT_NODE) {
						Element outputElement = (Element) outputNode;
						DiagramOutputBean outputBean = new DiagramOutputBean();
						outputBean.setName(outputElement.getTextContent());
						diagram.addOutput(outputBean);
					}
				}
			}

			Element inputs = (Element) doc.getElementsByTagName("inputs").item(0);
			if (inputs != null) {
				NodeList inputsList = inputs.getElementsByTagName("input");
				for (int i = 0; i < inputsList.getLength(); i++) {
					Node inputNode = inputsList.item(i);
					if (inputNode.getNodeType() == Node.ELEMENT_NODE) {
						Element inputElement = (Element) inputNode;
						DiagramInputBean inputBean = new DiagramInputBean();
						inputBean.setName(inputElement.getElementsByTagName("name").item(0).getTextContent());
						Element linksNode = (Element) inputElement.getElementsByTagName("links").item(0);
						if(linksNode != null){
								NodeList linksList = linksNode.getElementsByTagName("link");
	
							for (int j = 0; j < linksList.getLength(); j++) {
								Node linkNode = linksList.item(j);
								if (inputNode.getNodeType() == Node.ELEMENT_NODE) {
									Element linkElement = (Element) linkNode;
									String toStr = linkElement.getAttribute("to");
									String[] toSplit = toStr.split("[.]");
									if (toSplit[0].equals("outputs")) {
										DiagramOutputBean output = diagram.getOutput(toSplit[1]);
										inputBean.addLink(output);
									} else {
										BlockBean block = diagram.getBlock(toSplit[0]);
										InputBean toInput = block.getInput(toSplit[1]);
										inputBean.addLink(toInput);
									}
	
								}
							}
						}
						
						diagram.addInput(inputBean);
					}
				}
			}

			Element wires = (Element) doc.getElementsByTagName("wires").item(0);
			if (wires != null) {
				NodeList wireList = wires.getElementsByTagName("wire");
				for (int i = 0; i < wireList.getLength(); i++) {
					Node wireNode = wireList.item(i);
					if (wireNode.getNodeType() == Node.ELEMENT_NODE) {
						Element wireElement = (Element) wireNode;
						String fromText = wireElement.getElementsByTagName("from").item(0).getTextContent();
						String[] fromSplit = fromText.split("[.]");
						OutputBean fromNode;
						if (fromSplit[0].equals("inputs")) {
							DiagramInputBean input = diagram.getInput(fromSplit[1]);
							fromNode = input;
						} else {
							BlockBean block = diagram.getBlock(fromSplit[0]);
							fromNode = block.getOutput(fromSplit[1]);
						}
						String wireID = wireElement.getElementsByTagName("id").item(0).getTextContent();
						fromNode.getWire().setId(wireID);
						NodeList toNodes = wireElement.getElementsByTagName("to");
						for (int j = 0; j < toNodes.getLength(); j++) {
							Node toNode = toNodes.item(j);
							if (toNode.getNodeType() == Node.ELEMENT_NODE) {
								Element toElement = (Element) toNode;
								String toStr = toElement.getTextContent();
								String[] toSplit = toStr.split("[.]");
								if (toSplit[0].equals("outputs")) {
									DiagramOutputBean output = diagram.getOutput(toSplit[1]);
									if(output.getFrom() == null)
										fromNode.addLink(output);
									else
										throw new MultipleOutputsInInputException(diagram.getName(), output.toString());
								} else {
									BlockBean block = diagram.getBlock(toSplit[0]);
									InputBean toInput = block.getInput(toSplit[1]);
									if(toInput != null) {
										if(toInput.getFrom() == null)
											fromNode.addLink(toInput);
										else
											throw new MultipleOutputsInInputException(diagram.getName(), toInput.toString());
									}
									else {
										throw new NoInputFoundException(diagram.getName(), toSplit[0] + "." + toSplit[1]);
									}
									
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			throw e;
		}

		return diagram;
	}
}
