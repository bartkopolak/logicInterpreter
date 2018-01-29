package logicInterpreter.Tools;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.management.modelmbean.XMLParseException;
import javax.swing.JFileChooser;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.DefaultFileFilter;
import logicInterpreter.DiagramEditor.editor.GraphEditor;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.MultipleOutputsInInputException;
import logicInterpreter.Exceptions.NoInputFoundException;
import logicInterpreter.Exceptions.NoSuchTypeException;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.GNDNode;
import logicInterpreter.Nodes.InputBean;
import logicInterpreter.Nodes.OutputBean;
import logicInterpreter.Nodes.VCCNode;
import logicInterpreter.Nodes.Wire;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class DiagFileUtils {


	 private static boolean validateXML(File xsd, Object xml) throws SAXException, IOException{
	      try {
	         SchemaFactory schemaFact =
	            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	            Schema schema = schemaFact.newSchema(xsd);
	            Validator v = schema.newValidator();
	            if(xml instanceof File)
	            	v.validate(new StreamSource((File)xml));
	            else if(xml instanceof InputStream)
	            	v.validate(new StreamSource((InputStream)xml));
	      } catch (IOException e){
	         throw e;
	      }catch(SAXException e1){
	         throw e1;
	      }
			
	      return true;
		
	   }
	
	public static String getType(File file) {
		try {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc;
		doc = dBuilder.parse(file);
		doc.getDocumentElement().normalize();
		Node rootNode = doc.getFirstChild();
		Element rootElem = (Element) rootNode;
		return rootElem.getTagName();
		}
		catch(IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	 
	public static Document getDocument(Object src) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc;
		if(src instanceof File)
			doc = dBuilder.parse((File)src);
		else if (src instanceof InputStream)
			doc = dBuilder.parse((InputStream)src);
		else
			doc = null;
		return doc;
	}
	
	/*
	 * 
	 */
	//TODO: wyjątki
	public static BlockBean parseXMLBlock(Object file, String templateBlockPath) throws Exception{
		BlockBean block = new BlockBean();

		try {

			Document doc;
			
			doc = getDocument(file);
			doc.getDocumentElement().normalize();
			Node rootNode = doc.getFirstChild();
			Element rootElem = (Element) rootNode;
			block.setName(rootElem.getAttribute("name"));
			String type = rootElem.getAttribute("type");
			block.setType(type);
			String defaultB = rootElem.getAttribute("default");
			if(defaultB.equals("true")) block.setDefault(true);
			if(file instanceof File)
				block.setFile(new File(templateBlockPath));
			
			if(type.equals("formula")){
				//typ: formula
			validateXML(new File("xmls/validateSchemas/FormulaBlockSchema.xsd"), file);
			NodeList inputsNode = doc.getElementsByTagName("inputs");
			int inputCount = doc.getElementsByTagName("input").getLength();
			Element inputs = (Element) inputsNode.item(0);

				for (int i = 0; i < inputCount; i++) {
					Element input = (Element) inputs.getElementsByTagName("input").item(i);
					String inputName = input.getTextContent();
					String inputPos = input.getAttribute("position");
					block.addInput(inputName, inputPos);
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
				String source;
				if(file instanceof File)
					source = ((File)file).getParent() + "/" + doc.getElementsByTagName("src").item(0).getTextContent();
				else
					source = "temp/" + doc.getElementsByTagName("src").item(0).getTextContent(); 
				DiagramBean diagram = parseXMLDiagram(new File(source));
				block.setDiagram(diagram);
				for (int i = 0; i < diagram.getInputList().size(); i++) {
					DiagramInputBean input = diagram.getInput(i);
					String inputName = input.getName();
					block.addInput(inputName, input.getPosition());
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
			System.out.println("Nie udało się załadować bloku! " + templateBlockPath );
			//e.printStackTrace(System.out);
			throw e;
		}

		return block;
	}

	private static String[] splitOnFirstDot(String in) {
		String[] split = in.split("[.]");
		String[] outsplit = new String[2];
		outsplit[0] = split[0];
		
		StringBuffer sb = new StringBuffer();
		for(int i= 1; i<split.length; i++) {
			sb.append(split[i]);
		}
		outsplit[1] = sb.toString();
		
		return outsplit;
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
						BlockBean blockBeanTemplate = parseXMLBlock(new File( file.getParent() + "/" +  src), "");
						BlockBean blockBean = new BlockBean(blockBeanTemplate);
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
						String nodeSpecType = inputElement.getAttribute("type");
						DiagramInputBean inputBean;
						if(nodeSpecType.equals("VCC"))
							inputBean = new VCCNode();
						else if(nodeSpecType.equals("GND"))
							inputBean = new GNDNode();
						else
							inputBean = new DiagramInputBean();
						inputBean.setName(inputElement.getElementsByTagName("name").item(0).getTextContent());
						String position = inputElement.getAttribute("position");
						if(!position.equals("")) 
							inputBean.setPosition(position);
						Element linksNode = (Element) inputElement.getElementsByTagName("links").item(0);
						if(linksNode != null){
								NodeList linksList = linksNode.getElementsByTagName("link");
	
							for (int j = 0; j < linksList.getLength(); j++) {
								Node linkNode = linksList.item(j);
								if (inputNode.getNodeType() == Node.ELEMENT_NODE) {
									Element linkElement = (Element) linkNode;
									String toStr = linkElement.getAttribute("to");
									String[] toSplit = splitOnFirstDot(toStr);
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
						String[] fromSplit = splitOnFirstDot(fromText);
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
								String[] toSplit = splitOnFirstDot(toStr);
								if (toSplit[0].equals("outputs")) {
									DiagramOutputBean output = diagram.getOutput(toSplit[1]);
									if(output != null) {
										if(output.getFrom() == null)
											fromNode.addLink(output);
									}
								} else {
									BlockBean block = diagram.getBlock(toSplit[0]);
									InputBean toInput = block.getInput(toSplit[1]);
									if(toInput != null) {
										if(toInput.getFrom() == null)
											fromNode.addLink(toInput);
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
	/**
	 * Zapisuje diagram w postaci XML.<br>
	 * UWAGA: diagram będzie odczytywał bloki z folderu blocks, nazwa pliku bloku to *nazwabloku*.blok
	 * @param diagram
	 * @param targetFile
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException 
	 */
	public static String createDiagramXML(DiagramBean diagram) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("diagram");
		rootElement.setAttribute("name", diagram.getName());
		doc.appendChild(rootElement);
		//blocks
		{
			Element blocks = doc.createElement("blocks");
			ArrayList<String> fileList = new ArrayList<String>();
			for (int i =0;i<diagram.getBlocksList().size();i++) {
				BlockBean blockBean = diagram.getBlock(i);
				if(fileList.contains(blockBean.getFile().getAbsolutePath()));
				Element block = doc.createElement("block");
				
				Element id = doc.createElement("id");
				id.setTextContent(blockBean.getName());
				Element src = doc.createElement("src");
				src.setTextContent(blockBean.getTemplateBlock().getName() + ".xml");
				block.appendChild(id);
				block.appendChild(src);
				blocks.appendChild(block);
			}
			rootElement.appendChild(blocks);
		}
		//outputs
		{
			Element outputs = doc.createElement("outputs");
			for (int i =0;i<diagram.getOutputList().size();i++) {
				
				Element output = doc.createElement("output");
				DiagramOutputBean b = diagram.getOutput(i);
				output.setTextContent(b.getName());
				outputs.appendChild(output);
			}
			rootElement.appendChild(outputs);
		}
		//inputs
		{
			Element inputs = doc.createElement("inputs");
			for (int i =0;i<diagram.getInputList().size();i++) {
				DiagramInputBean b = diagram.getInput(i);
				Element input = doc.createElement("input");
				if(b instanceof VCCNode)
					input.setAttribute("type", "VCC");
				else if(b instanceof GNDNode)
					input.setAttribute("type", "GND");
				Element id = doc.createElement("name");
				id.setTextContent(b.getName());
				
				Element links = doc.createElement("links");
				List<InputBean> toList = b.getWire().getToList();
				for(int j=0;j<toList.size();j++) {
					InputBean toInput = toList.get(j);
					Element link = doc.createElement("link");
					link.setAttribute("to", toInput.toString());
					links.appendChild(link);
				}
				
				input.appendChild(id);
				input.appendChild(links);
				
				inputs.appendChild(input);
			}
			rootElement.appendChild(inputs);
		}
		//wires
		{
			Element wires = doc.createElement("wires");
			int count = 0;
			for(int i=0; i<diagram.getBlocksList().size(); i++) {
				BlockBean block = diagram.getBlock(i);
				for(int j=0; j<block.getOutputList().size(); j++) {
					BlockOutputBean outputNode = block.getOutput(j);
					if(outputNode.getWire().getToList().isEmpty()) continue;
					Element wire = doc.createElement("wire");
					Element id = doc.createElement("id");
					id.setTextContent("w"+String.valueOf(count));
					count++;
					wire.appendChild(id);
					Element from = doc.createElement("from");
					from.setTextContent(outputNode.toString());
					wire.appendChild(from);
					for(int k=0; k<outputNode.getWire().getToList().size(); k++) {
						Element to = doc.createElement("to");
						to.setTextContent(outputNode.getWire().getToList().get(k).toString());
						wire.appendChild(to);
					}
					wires.appendChild(wire);
				}
			}
			rootElement.appendChild(wires);
		}
		
		StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
	}
	
	public static String createBlockXML(BlockBean block) throws ParserConfigurationException, TransformerException{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("block");
			rootElement.setAttribute("name", block.getName());
			if(block.isDefault()) rootElement.setAttribute("default", "true");
			if(block.getType().equals("formula")) {
				rootElement.setAttribute("type", "formula");
				doc.appendChild(rootElement);
				
				Element inputs = doc.createElement("inputs");
				for (int i =0;i<block.getInputList().size();i++) {
					Element input = doc.createElement("input");
					input.setTextContent(block.getInput(i).getName());
					inputs.appendChild(input);
				}
				Element outputs = doc.createElement("outputs");
				for (int i =0;i<block.getOutputList().size();i++) {
					
					Element output = doc.createElement("output");
					BlockOutputBean b = block.getOutput(i);
					output.setTextContent(b.getName());
					
					output.setAttribute("function", b.getFormula());
					outputs.appendChild(output);
				}
				rootElement.appendChild(inputs);
				rootElement.appendChild(outputs);
			}
			else if(block.getType().equals("diagram")) {
				rootElement.setAttribute("type", "diagram");
				doc.appendChild(rootElement);
				
				Element src = doc.createElement("src");
				src.setTextContent(block.getName() + ".diagram.xml");
				rootElement.appendChild(src);
			}
			
			
			StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();

	}

	
	private static String createDiagramBlockXML(String blockname) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("block");
		rootElement.setAttribute("name", blockname);
		
		rootElement.setAttribute("type", "diagram");
		doc.appendChild(rootElement);
		
		Element src = doc.createElement("src");
		src.setTextContent("diagram.main.xml");
		rootElement.appendChild(src);
		
		StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();	
	}
	/**
	 * Tworzy plik bloku w 
	 * @param block - obiekt bloku, wykorzystywany gdy typ bloku to blok oparty na tabeli prawdy (może być null)
	 * @param editor -
	 * @param name
	 * @param stream
	 * @throws IOException 
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 * @throws MultipleOutputsInInputException 
	 */
	public static void createTemplateBlockFile(BlockBean block, GraphEditor editor, String name, OutputStream stream) throws IOException, ParserConfigurationException, TransformerException, MultipleOutputsInInputException {
			ZipOutputStream zipout = new ZipOutputStream(stream);
			BlockBean blo = null;
			
			
			if(block == null && editor != null)
			{		
				ZipEntry blockEntry = new ZipEntry("blocks/block.main.xml");
				zipout.putNextEntry(blockEntry);
				byte[] data = createDiagramBlockXML(name).getBytes();
				zipout.write(data);
				zipout.closeEntry();
				
				createDiagramFile(editor, null, zipout);
			}
			else if (block != null){
				blo = block;
				boolean isDiagram = (block.getDiagram() != null) ? true : false;
				ZipEntry blockEntry = new ZipEntry("blocks/block.main.xml");
				zipout.putNextEntry(blockEntry);
				byte[] data = (isDiagram) ? createDiagramBlockXML(blo.getName()).getBytes() : createBlockXML(blo).getBytes();
				zipout.write(data);
				zipout.closeEntry();
				if(isDiagram) {
					createDiagramFile(null, block.getDiagram(), zipout);
				}
			}
			
			

			zipout.close();
		
	}
	
	public static BlockBean readTemplateBlockFile(String filename) throws Exception {
		BlockBean out = null;
		ZipFile zip = new ZipFile(filename);
		String tempPath = System.getProperty("java.io.tmpdir");
		File tempFolder = new File(tempPath +"/logicInterpretBlockTemp/");
		if(tempFolder.exists()) deleteDirectory(tempFolder);

		if(!tempFolder.mkdirs()) {
			File currFile = new File(System.getProperty("user.dir"));
			tempFolder = new File(currFile.getAbsolutePath() +"/logicInterpretDiagLoadTemp/");
		}
		
		File blocksFolder = new File(tempFolder.getAbsolutePath() + "/blocks/");
		blocksFolder.mkdirs();
		
 		unzip(zip, tempFolder);
		
		File blockXMLFile = new File(tempFolder.getAbsolutePath() + "/blocks/block.main.xml");
		
		out = parseXMLBlock(blockXMLFile, filename);
		//out.setFile(new File(filename));
		deleteDirectory(tempFolder);
		zip.close();
		return out;
	}
		
	/**
	 * Tworzy archiwum przechowujące wszystkie niezbędne pliki do stworzenia diagramu
	 * @param editor - obiekt edytora diagramów
	 * @param diagram - obiekt diagramu np, z bloku, używany do 
	 * @param stream - strumień do zapisu (jesli plik diagramu zapisywany jext wewnątrz archiwum)
	 * @return
	 * @throws IOException
	 * @throws MultipleOutputsInInputException 
	 */
	public static boolean createDiagramFile(GraphEditor editor, DiagramBean diag, OutputStream stream) throws IOException, MultipleOutputsInInputException {
		try {
			{
				mxCodec codec = new mxCodec();
				DiagramBean diagram;
				ZipOutputStream zipout;
				
				if(stream instanceof ZipOutputStream)
					zipout = (ZipOutputStream) stream;
					
				else
					zipout = new ZipOutputStream(stream);
				
				if(editor != null) {
					editor.createDiagram();
					String xml = mxXmlUtils.getXml(codec.encode(editor.getGraphComponent().getGraph().getModel()));
					ZipEntry editorSchemaEntry = new ZipEntry("editorSchema.xml");
					zipout.putNextEntry(editorSchemaEntry);
					{
						byte[] data = xml.getBytes();
						zipout.write(data);
						zipout.closeEntry();
					}
					diagram = editor.createDiagram();
				}
				else{
					diagram = diag;
				}
				
				{
					ZipEntry diagramEntry = new ZipEntry("blocks/diagram.main.xml");
					zipout.putNextEntry(diagramEntry);
					byte[] data = DiagFileUtils.createDiagramXML(diagram).getBytes();
					zipout.write(data);
					zipout.closeEntry();
				}
				//templateBlocks
				ArrayList<BlockBean> templateBlocks = diagram.getAllTemplateBlocks(null, true);
				zipout.putNextEntry(new ZipEntry("blocks/"));
				for(int i=0; i<templateBlocks.size(); i++) {
					BlockBean block = templateBlocks.get(i);
					ZipEntry blockEntry = new ZipEntry("blocks/"+block.getName()+".xml");
					zipout.putNextEntry(blockEntry);
					byte[] data = DiagFileUtils.createBlockXML(block).getBytes();
					zipout.write(data);
					zipout.closeEntry();
					if(block.getDiagram() != null) {
						DiagramBean blockDiagram = block.getDiagram();
						ZipEntry diagramEntry = new ZipEntry("blocks/"+block.getName()+".diagram.xml");
						zipout.putNextEntry(diagramEntry);
						byte[] diagdata = DiagFileUtils.createDiagramXML(blockDiagram).getBytes();
						zipout.write(diagdata);
						zipout.closeEntry();
					}
					
				}
				System.out.println("zapisano");
				zipout.close();
			}
			return true;
		}
		catch(IOException | ParserConfigurationException | TransformerException e) {
			return false;
		}
	}
	
	private static boolean deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}
	
	private static void unzip(ZipFile zip, File targetDir) throws IOException {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while(entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File f = new File(targetDir.getAbsolutePath() + "/" + entry.getName());
			if(!f.isDirectory()) {
				FileOutputStream fos = new FileOutputStream(f);
				InputStream is = zip.getInputStream(entry);
				byte[] buffer = new byte[is.available()];
				zip.getInputStream(entry).read(buffer);
	       		fos.write(buffer);
	       		fos.close();
			}
			
            }
	}
	
	public static void readDiagramFile(GraphEditor editor, String filename) throws Exception {
		DiagramBean diagram = null;
		ZipFile zip = new ZipFile(filename);
		String tempPath = System.getProperty("java.io.tmpdir");
		
		
		File currFile = new File(filename);
		File tempFolder = new File(tempPath +"/logicInterpretDiagLoadTemp/");
		if(tempFolder.exists()) deleteDirectory(tempFolder);
		
		if(!tempFolder.mkdirs()) tempFolder = new File(currFile.getAbsolutePath() +"/logicInterpretDiagLoadTemp/");

		File blocksFolder = new File(tempFolder.getAbsolutePath() + "/blocks/");
		blocksFolder.mkdirs();
		
		unzip(zip, tempFolder);
		
		//editorSchema

		File editorSchemaFile = new File(tempFolder.getAbsolutePath() + "/editorSchema.xml");
		if(editorSchemaFile.exists() && editor != null) {
			Document document = mxXmlUtils.parseXml(mxUtils.readFile(editorSchemaFile.getAbsolutePath()));
	
			mxCodec codec = new mxCodec(document);
			codec.decode(
					document.getDocumentElement(),
					editor.getGraphComponent().getGraph().getModel());
			editor.setCurrentFile(currFile);
			editor.setModified(false);
			editor.getUndoManager().clear();
			editor.getGraphComponent().zoomAndCenter();
			editor.setDiagramName(currFile.getName().replaceFirst("[.][^.]+$", ""));
		}
		//diagram
		File mainDiagram = new File(tempFolder.getAbsolutePath() + "/blocks/diagram.main.xml");
        diagram = DiagFileUtils.parseXMLDiagram(mainDiagram);
        {
	        ArrayList<mxCell> blockCells = editor.getCellsOfType(BlockBean.class);
	        for(int i=0; i<blockCells.size(); i++) {
	        	mxCell cell = blockCells.get(i);
	        	BlockBean cb = (BlockBean) cell.getValue();
	        	String cbName = cb.getName();
	        	for(int j=0; j<diagram.getBlocksList().size(); j++) {
	        		BlockBean db = diagram.getBlock(i);
	        		if(db.getName().equals(cbName)) {
	        			cell.setValue(db);
	        			for(int k=0; k<cell.getChildCount(); k++) {
	        				mxCell pin = (mxCell) cell.getChildAt(k);
	        				if(pin.getValue() instanceof BlockInputBean) pin.setValue( db.getInput( ((BlockInputBean)pin.getValue()).getName()) );
	        				else if (pin.getValue() instanceof BlockOutputBean) pin.setValue(db.getOutput(((BlockOutputBean)pin.getValue()).getName()));
	        			}
	        			break;
	        		}
	        	}
	        }
        }

        ArrayList<mxCell> inputCells = editor.getCellsOfType(DiagramInputBean.class);
        for (int i=0;i<inputCells.size(); i++) {
        	mxCell cell = inputCells.get(i);
        	if(cell.getChildCount() == 0) continue;
        	DiagramInputBean cb = (DiagramInputBean)cell.getValue();
        	String cbName = cb.getName();
        	for(int j=0; j<diagram.getInputList().size(); j++) {
        		Object db = diagram.getInput(j);
        		String dbName = ((DiagramInputBean) db).getName();
        		if(dbName.equals(cbName)) {
        			if(cb instanceof VCCNode)
        				cell.setValue(new VCCNode());
        			else if(cb instanceof GNDNode)
        				cell.setValue(new GNDNode());
        			else
        				cell.setValue(db);
        		}
        	}
        }
        
        ArrayList<mxCell> outputCells = editor.getCellsOfType(DiagramOutputBean.class);
        for (int i=0;i<outputCells.size(); i++) {
        	mxCell cell = outputCells.get(i);
        	if(cell.getChildCount() == 0) continue;
        	DiagramOutputBean cb = (DiagramOutputBean) cell.getValue();
        	String cbName = cb.getName();
        	for(int j=0; j<diagram.getOutputList().size(); j++) {
        		DiagramOutputBean db = diagram.getOutput(j);
        		String dbName = db.getName();
        		if(dbName.equals(cbName)) {
        			cell.setValue(db);
        			
        		}
        	}
        }
        deleteDirectory(tempFolder);
        //System.out.println("OK");
        zip.close();
		}
	
	
	public String createVHDL(DiagramBean diagram) {
		StringBuffer sb = new StringBuffer();
		
		
		
		return sb.toString();
	}
	
	
/*	
	public static void main(String[] args) {
		File xmlFolder = new File("xmls/");
		if(xmlFolder.exists() && xmlFolder.isDirectory()) {
			File[] list = xmlFolder.listFiles();
			for(int i=0; i<list.length; i++) {
				try {
				if(!list[i].isFile() && !list[i].getAbsolutePath().endsWith(".tmpb")) continue;
				
					BlockBean block;
						block = DiagFileUtils.parseXMLBlock(list[i]);
						
						File f = new File("xmls/"+block.getName()+".tmpb");
						DiagFileUtils.createTemplateBlockFile(block, null, null, new FileOutputStream(f));
					
					
				
			}
				catch (Exception e) {}
			}

	}
			
*/		
}

