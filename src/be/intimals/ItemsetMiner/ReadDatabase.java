package be.intimals.ItemsetMiner;

import be.intimals.ItemsetMiner.structure.NodeFreqT;
import be.intimals.ItemsetMiner.util.Variables;
import be.intimals.ItemsetMiner.util.XmlFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
/*
create tree data from ASTs
 */
//import java.io.File;


public class ReadDatabase {

    private int top;
    private int id;
    private ArrayList<Integer> sr;
    private ArrayList<Integer> sibling;

    private List<String> labels = new LinkedList<>();

    //////////////////////////////

    //read white labels from given file
    public Map<String,Set<String> > readWhiteLabel(String path){
        Map<String,Set<String> > _whiteLabels = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if( ! line.isEmpty() && line.charAt(0) != '#' ) {
                    String[] str_tmp = line.split(" ");
                    String ASTNode = str_tmp[0];
                    Set<String> children = new HashSet<>();
                    for(int i=1; i<str_tmp.length; ++i){
                        children.add(str_tmp[i]);
                    }
                    _whiteLabels.put(ASTNode,children);
                }
            }
        }catch (IOException e) {System.out.println("Error: reading white list "+e);}
        return _whiteLabels;
    }


    //count number children of a node
    public int countNBChildren(Node node){
        int nbChildren = 0;
        NodeList list = node.getChildNodes();
        for(int j = 0; j<list.getLength(); ++j) {
            if ( list.item(j).getNodeType() != Node.TEXT_NODE && list.item(j).getNodeType() == Node.ELEMENT_NODE ){
                ++nbChildren;
            }
        }
        return nbChildren;
    }

    //count total number of nodes of a tree
    public int countNBNodes(Node root) {
        NodeList childrenNodes = root.getChildNodes();
        int nbChildren = countNBChildren(root);
        int c = nbChildren; //node.getChildNodes().getLength();
        int result = c;

        if(childrenNodes.getLength() > 1){
            for (int i=0; i< childrenNodes.getLength(); i++) {
                if (childrenNodes.item(i).getNodeType() != Node.TEXT_NODE) {
                    result += countNBNodes(root.getChildNodes().item(i));
                }
            }
        }else {
            result++;
        }
        return result;
    }

    //read tree by breadth first traversal
    private void readTreeDepthFirst(Node node , ArrayList <NodeFreqT> trans, Map<String,Set<String>> whiteLabels, Map <Integer, String> indexLabel ) {
        try {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // add this node label into trans
                trans.get(id).setNodeLabel(node.getNodeName());
                //System.out.print(node.getNodeName());

                //update labelIndex for internal labels
                if(indexLabel.isEmpty() && labels.isEmpty()) {
                    trans.get(id).setNode_label_int(0);
                    indexLabel.put(0, node.getNodeName());
                    labels.add(node.getNodeName());
                }
                else{
                    if(!labels.contains(node.getNodeName())) {
                        trans.get(id).setNode_label_int(indexLabel.size());
                        indexLabel.put(indexLabel.size(), node.getNodeName());
                        labels.add(node.getNodeName());
                    }else{
                        trans.get(id).setNode_label_int(labels.indexOf(node.getNodeName()));
                    }
                }

                //keep positions to calculate relationships: parent - child - sibling
                sr.add(id);
                ++id;
                if (node.hasChildNodes()) {
                    //get list of children
                    //NodeList nodeList = node.getChildNodes();
                    //if node is a parent of a leaf node
                    if (node.getChildNodes().getLength() == 1) {
                        String leafLabel = "*" + node.getTextContent().replace(",",Variables.uniChar).trim();
                        //add leaf node label
                        trans.get(id).setNodeLabel(leafLabel);
                        //update labelIndex for leaf labels
                        if(!labels.contains(leafLabel)) {
                            trans.get(id).setNode_label_int(indexLabel.size()*(-1));
                            indexLabel.put(indexLabel.size()*(-1), leafLabel);
                            labels.add(leafLabel);
                        }else {
                            trans.get(id).setNode_label_int(labels.indexOf(leafLabel)*(-1));
                        }

                        //System.out.println("node "+trans.elementAt(id).getNodeLabel());
                        sr.add(id);
                        ++id;
                        //////close a node and calculate parent, child, sibling
                        top = sr.size() - 1;
                        int child = sr.get(top);
                        int parent = sr.get(top - 1);
                        trans.get(child).setNodeParent(parent);
                        if (trans.get(parent).getNodeChild() == -1)
                            trans.get(parent).setNodeChild(child);
                        if (sibling.get(parent) != -1)
                            trans.get(sibling.get(parent)).setNodeSibling(child);
                        sibling.set(parent,child);
                        sr.remove(top);
                        ///////////////
                    } else {//internal node
                        NodeList nodeList = node.getChildNodes();
                        if(whiteLabels.containsKey(node.getNodeName())){
                            //System.out.println(node.getNodeName());
                            Set<String> temp = whiteLabels.get(node.getNodeName());
                            for(int i=0; i<nodeList.getLength(); ++i)
                                if(nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                    if(temp.contains(nodeList.item(i).getNodeName())) {
                                        //System.out.println(nodeList.item(i).getNodeName());
                                        readTreeDepthFirst(nodeList.item(i), trans, whiteLabels, indexLabel);
                                    }
                                }
                        }else{
                            for (int i = 0; i < nodeList.getLength(); ++i) {
                                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                    readTreeDepthFirst(nodeList.item(i), trans, whiteLabels, indexLabel);
                                }
                            }
                        }
                    }
                }
                //close a node and calculate parent, child, sibling
                //System.out.println(" )");
                top = sr.size() - 1;
                if (top < 1) return;
                int child = sr.get(top);
                int parent = sr.get(top - 1);
                trans.get(child).setNodeParent(parent);
                if (trans.get(parent).getNodeChild() == -1)
                    trans.get(parent).setNodeChild(child);
                if (sibling.get(parent) != -1)
                    trans.get(sibling.get(parent)).setNodeSibling(child);
                sibling.set(parent,child);
                sr.remove(top);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //create transaction from ASTs in multiple folders
    public ArrayList <ArrayList<NodeFreqT>> readAST(File rootDirectory, Map<String,Set<String>> whiteLabels, Map <Integer, String> indexLabel) {

        ArrayList < ArrayList<NodeFreqT> > database = new ArrayList < ArrayList<NodeFreqT> >();
        ArrayList<String> files = new ArrayList<>();
        populateFileList(rootDirectory,files);
        //ArrayList<File> files = new ArrayList<File>();
        //populateFileList(rootDirectory,files);
        Collections.sort(files);
        System.out.print("Reading " + files.size() +" files ");
        XmlFormatter formatter = new XmlFormatter();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            //for (File fi : files) {
            for (String fi : files) {
                //format XML file before create tree
                String inFileTemp = rootDirectory+"/temp.xml";
                Files.deleteIfExists(Paths.get(inFileTemp));
                formatter.format(fi,inFileTemp);

                //create tree
                File fXmlFile = new File(inFileTemp);
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                doc.getDocumentElement().normalize();

                //get total number of nodes
                int size = countNBNodes(doc.getDocumentElement())+1;

                id = 0;
                top = 0;
                sr = new ArrayList<>();
                sibling = new ArrayList<>(size);
                ArrayList<NodeFreqT> trans = new ArrayList<NodeFreqT>(size);

                for (int i = 0; i < size; ++i) {
                    NodeFreqT nodeTemp = new NodeFreqT(-1,-1,-1,"0",true);
                    trans.add(nodeTemp);
                    sibling.add(-1);
                }
                //create tree
                readTreeDepthFirst(doc.getDocumentElement(), trans, whiteLabels, indexLabel);
                //add tree to database
                database.add(trans);
                //delete temporary input file
                Files.deleteIfExists(Paths.get(inFileTemp));
                System.out.print(".");
            }
            System.out.println(" reading ended.");
        } catch (Exception e) {
            System.out.println(" read error.");
            e.printStackTrace();
            System.exit(-1);
        }
        return database;
    }

    //collect full name of files in the directory
    private void populateFileList(File directory, ArrayList<String> list){
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        ArrayList<String> fullNames = new ArrayList<>();
        for(int i=0; i<files.length; ++i)
            fullNames.add(files[i].getAbsolutePath());
        list.addAll(fullNames);
        File[] directories = directory.listFiles(File::isDirectory);
        for (File dir : directories) populateFileList(dir,list);
    }

    public static void printTransaction(ArrayList < ArrayList<NodeFreqT> > trans){
        for(int i=0; i<trans.size(); ++i){
            for(int j=0; j<trans.get(i).size(); ++j)
                System.out.print((trans.get(i).get(j).getNodeLabel())+"-"+trans.get(i).get(j).getNode_label_int()+" , ");
            System.out.println();
        }
    }

}
