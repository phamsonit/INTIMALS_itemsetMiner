package be.intimals.ItemsetMiner;

import be.intimals.ItemsetMiner.config.Config;
import be.intimals.ItemsetMiner.structure.NodeFreqT;
import be.intimals.ItemsetMiner.structure.Projected;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPMax;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoEclat;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;


import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ItemsetMiner {

    public static final String uniChar = "\u00a5";

    private  Config basicConf;

    private ArrayList<ArrayList<NodeFreqT>> treeDatabase = new ArrayList<>();
    private Map<Integer, String> labelsIndex = new LinkedHashMap<>();
    private Map<String, Set<String>> whiteLabels = new HashMap<>();
    private Map<String,String> xmlCharacters = new HashMap<>();


    private List<List<Integer>> transactionDatabase = new LinkedList<>();
    private Map<Integer,String> pathsIndex = new HashMap<>();

    public ItemsetMiner(Config _conf) {
        basicConf = _conf;
    }

    //create sequence transactionDatabase
    private void createSequenceTransactionDatabase(String output){
        try{
            //create output file
            FileWriter out = new FileWriter(output);

            //create treeDatabase transactionDatabase
            List<String> itemIndex = new LinkedList<>();

            for(int i=0; i<treeDatabase.size(); ++i){
                List<Integer> trans = new LinkedList<>();
                for(int j=0; j<treeDatabase.get(i).size(); ++j){
                    //if this is a leaf node
                    if(treeDatabase.get(i).get(j).getNode_label_int() < 0) {
                        //find path from leaf to root
                        String item = String.valueOf(treeDatabase.get(i).get(j).getNode_label_int());
                        NodeFreqT currentNode = treeDatabase.get(i).get(j);
                        while(currentNode.getNodeParent() != -1){
                            int parentPos = currentNode.getNodeParent();
                            currentNode = treeDatabase.get(i).get(parentPos);
                            item = item + "," + String.valueOf(currentNode.getNode_label_int());
                        }
                        //add path to itemIndex
                        if(!itemIndex.contains(item)) {
                            itemIndex.add(item);
                        }
                        //add path id to transaction
                        //if(!trans.contains(itemIndex.indexOf(item)))
                            trans.add(itemIndex.indexOf(item));
                    }

                }
                //add transaction to transaction database
                transactionDatabase.add(trans);
            }

            //write header information: if using sequence pattern mining in spmf software
            //for(int i=0; i< itemIndex.size(); ++i){
                //out.write("@ITEM"+uniChar+i+uniChar+toStringLabels(itemIndex.get(i))+"\n");
                //out.write("@ITEM"+"="+i+"="+toStringLabels(itemIndex.get(i))+"\n");
            //}
            //out.write("@ITEM=-1=|\n");

            //write sequence transaction to file
            for(List<Integer> list : transactionDatabase){
                for(int i = 0; i< list.size(); ++i) {
                    out.write(String.valueOf(list.get(i)));
                    if(i != list.size()-1)
                        out.write(" ");
//                    else
//                        out.write(" -2");
                }
                out.write("\n");
            }
            //close file
            out.flush();
            out.close();
        }catch (Exception e){
            System.out.println("error create sequence transaction database "+e);
        }
    }

    //create sequence transactionDatabase with timestamps
    private void createSequenceTimeTransactionDatabase(String output){
        try{
            //create output file
            FileWriter out = new FileWriter(output);
            int maxSize = 0;
            //create transactionDatabase
            List<String> itemIndex = new LinkedList<>();

            for(int i=0; i<treeDatabase.size(); ++i){
                List<Integer> trans = new LinkedList<>();
                for(int j=0; j<treeDatabase.get(i).size(); ++j){
                    //if this is a leaf node
                    if(treeDatabase.get(i).get(j).getNode_label_int() < 0) {
                        //find path from leaf to root
                        String itemInt = String.valueOf(treeDatabase.get(i).get(j).getNode_label_int());
                        String itemStr = treeDatabase.get(i).get(j).getNodeLabel();
                        NodeFreqT currentNode = treeDatabase.get(i).get(j);
                        while(currentNode.getNodeParent() != -1){
                            int parentPos = currentNode.getNodeParent();
                            currentNode = treeDatabase.get(i).get(parentPos);
                            itemInt = itemInt + "," + String.valueOf(currentNode.getNode_label_int());
                            itemStr = itemStr + "," + currentNode.getNodeLabel();
                        }
                        //add path to itemIndex
                        if(!itemIndex.contains(itemInt)) {
                            itemIndex.add(itemInt);
                            pathsIndex.put(itemIndex.size()-1, itemStr);
                        }
                        //add path id to transaction
                        //if(!trans.contains(itemIndex.indexOf(item)))
                        trans.add(itemIndex.indexOf(itemInt));
                    }
                }
                //add transaction to transactionDatabase
                if(maxSize < trans.size()) maxSize = trans.size();
                transactionDatabase.add(trans);
            }

            //write sequence timestamp to file
            for(int sid = 0; sid<transactionDatabase.size(); ++sid){
                for(int i = 0; i< transactionDatabase.get(sid).size(); ++i) {
                    String temp = String.valueOf(sid+1) + " " + String.valueOf(i+1) + " 1 " + String.valueOf(transactionDatabase.get(sid).get(i)+1);
                    out.write(String.valueOf(temp)+"\n");
                }
            }
            System.out.println(maxSize);
            //close file
            out.flush();
            out.close();
        }catch (Exception e){
            System.out.println("error create sequence with timestamp transaction Database "+e);
        }
    }

    //create transactionDatabase
    private void createTransactionDatabase(String output){
        try{
            //create output file
            FileWriter out = new FileWriter(output);

            //create transactionDatabase
            List<String> itemIndex = new LinkedList<>();
            
            for(int i=0; i<treeDatabase.size(); ++i){
                List<Integer> trans = new LinkedList<>();
                for(int j=0; j<treeDatabase.get(i).size(); ++j){
                    //if this is a leaf node
                    if(treeDatabase.get(i).get(j).getNode_label_int() < 0) {
                        //find path from leaf to root
                        String item = String.valueOf(treeDatabase.get(i).get(j).getNode_label_int());
                        String itemStr = treeDatabase.get(i).get(j).getNodeLabel();
                        NodeFreqT currentNode = treeDatabase.get(i).get(j);
                        while(currentNode.getNodeParent() != -1){
                            int parentPos = currentNode.getNodeParent();
                            currentNode = treeDatabase.get(i).get(parentPos);
                            item = item + "," + String.valueOf(currentNode.getNode_label_int());
                            itemStr = itemStr + "," + currentNode.getNodeLabel();
                        }
                        //add path to itemIndex
                        if(!itemIndex.contains(item)) {
                            itemIndex.add(item);
                            pathsIndex.put(itemIndex.size()-1, itemStr);
                        }
                        //add path id to transaction
                        if(!trans.contains(itemIndex.indexOf(item)))
                            trans.add(itemIndex.indexOf(item));
                    }
                }
                //add transaction to transactionDatabase
                transactionDatabase.add(trans);
            }

            //write header information to file
            //out.write("@CONVERTED_FROM_TEXT\n");
            for(int i=0; i< itemIndex.size(); ++i){
                out.write("@ITEM"+uniChar+i+uniChar+toStringLabels(itemIndex.get(i))+"\n");
            }
            //write treeDatabase transactionDatabase to file
            for(List<Integer> list : transactionDatabase){
                Collections.sort(list);
                for(int i = 0; i< list.size(); ++i) {
                    out.write(String.valueOf(list.get(i)));
                    if(i != list.size()-1)
                        out.write(" ");
                }
                out.write("\n");
            }
            //close file
            out.flush();
            out.close();

        }catch (Exception e){
            System.out.println("error create transactionDatabase "+e);
        }
    }

    //convert item from Integer to String representation
    private String toStringLabels(String inputItemset){
        String stringLabels = "";
        String[] items = inputItemset.split(",");
        stringLabels += labelsIndex.get(Integer.valueOf(items[items.length-1]));
        for(int j=items.length-2; j>0; --j)
            stringLabels += "," + labelsIndex.get(Integer.valueOf(items[j]));
        stringLabels += ","+labelsIndex.get(Integer.valueOf(items[0])).substring(1,labelsIndex.get(Integer.valueOf(items[0])).length());
        return stringLabels;
    }

    //print itemsets found by krimp
    private void printKrimpItemset(String inputFile, String outputFile, String encodedFile, int minsize){
        try{

            FileWriter out = new FileWriter(outputFile);

            //read Krimp encoded data
            Map<Integer,Integer> encodedItems = new HashMap<>();
            BufferedReader readerEncode = new BufferedReader(new FileReader(encodedFile));
            String line;
            while( (line = readerEncode.readLine()) != null){
                if(!line.isEmpty()){
                    String[] tempStr = line.split(" ");
                    if(tempStr[0].contains("=>")){
                        String[] items = tempStr[0].split("=>");
                        encodedItems.put(Integer.valueOf(items[0]),Integer.valueOf(items[1].trim()));
                    }
                }
            }
            //read output itemsets from Krimp
            BufferedReader readerItem = new BufferedReader(new FileReader(inputFile));
            while((line = readerItem.readLine()) != null){
                if(!line.isEmpty()){
                    //System.out.println(line);
                    String[] itemsets = line.split(" ");
                    if(itemsets.length > minsize){
                        //extract support
                        String[] fooo = itemsets[itemsets.length-1].split(",");
                        String supp = "#SUB: "+fooo[1].substring(0,fooo[1].length()-1);
                        //find tids of the itemset
                        String tids = " #Tids: ";
                        List<Integer> trans = new LinkedList<>();
                        for(int i=0; i<itemsets.length-1; ++i){
                            trans.add(encodedItems.get(Integer.valueOf(itemsets[i])));
                        }
                        Collections.sort(trans);

                        for(int i=0; i<transactionDatabase.size(); ++i){
                            if(transactionDatabase.get(i).containsAll(trans))
                                tids += String.valueOf(i)+",";
                        }
                        //print all items (paths) in the trans
                        for(int i=0; i<trans.size(); ++i){
                            String[] patTemp = pathsIndex.get(trans.get(i)).split(",");
                            //print path from root to leaf
                            for(int j = patTemp.length-1; j>0; --j) {
                                //System.out.print(patTemp[j] + ",");
                                out.write(patTemp[j] + ",");
                            }
                            //print leaf
                            String leaf = patTemp[0].substring(1,patTemp[0].length());
                            out.write(leaf+" ");
                        }
                        //print support
                        out.write(supp);
                        //print tids
                        out.write(tids.substring(0,tids.length()-1)+"\n");
                    }
                }
            }
            //closed files
            out.flush();
            out.close();

        }catch (Exception e){
            System.out.println(e);
        }
    }

    //#SUP: 4 #Tids: 6,11,21,31
    private String foo(int size){
        String f = "#SUP: 4 #Tids: ";
        for(int i=0; i<= size; ++i) {
            f += String.valueOf(i);
            if(i < size) f += ",";
        }

        return  f;
    }
    //print itemsets found by PICC
    private void printSequentialItemset(String inputFile, String outputFile){
        try{
            FileWriter out = new FileWriter(outputFile);
            //read output sequential itemsets
            BufferedReader readerItem = new BufferedReader(new FileReader(inputFile));
            String line;
            while((line = readerItem.readLine()) != null){
                if(!line.isEmpty()){
                    String[] string = line.split(":");
                    //if the line is not of the form <support> : <pattern> then return
                    if(string.length < 2){
                        out.flush();
                        out.close();
                        return;
                    }
                    //print paths correspond to items in the itemset
                    String[] items = string[0].trim().split(" ");
                    for(int i = items.length-3 ; i > 0 ; --i){
                        int index = Integer.parseInt(items[i].trim());
                        //print path from root to leaf
                        String[] path = pathsIndex.get(index).split(",");
                        for(int j=path.length-1; j > 0; --j) {
                            out.write(path[j]+",");
                        }
                        //write leaf
                        out.write(path[0].substring(1,path[0].length())+" ");
                    }
                    //write new line
                    out.write(foo(39) + "\n");

                }
            }
            //closed output files
            out.flush();
            out.close();

        }catch (Exception e){
            System.out.println("print sequential itemsets error "+ e);
        }
    }

    private String encodeString(String leaf){
        String result="";
        for(int t=0; t<leaf.length(); ++t) {
            if (xmlCharacters.containsKey(String.valueOf(leaf.charAt(t))))
                result += xmlCharacters.get(String.valueOf(leaf.charAt(t)));
            else result += leaf.charAt(t);
        }

        return result;
    }

    //convert integer itemsets into String representation.
    private void printPattern(String inputPattern, Map<Integer,String> _indexLabels){
        try (BufferedReader br = new BufferedReader(new FileReader(inputPattern))) {
            String line;
            while ((line = br.readLine()) != null) {
                if( ! line.isEmpty() ) {
                    String[] itemsets = line.split(" ");
                    for(int i=0; i<itemsets.length-2; ++i){
                        String[] items = itemsets[i].split(",");
                        for(int j=items.length-1; j>=0; --j)
                            System.out.print(_indexLabels.get(Integer.valueOf(items[j]))+",");
                        System.out.println();
                    }
                }
                System.out.println();
            }
        }catch (IOException e) {System.out.println("Reading file error ");}
    }

    /**
     * read list of special XML characters
     */
    public static void readXMLCharacter(String path, Map<String,String> listCharacters){

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if( ! line.isEmpty() && line.charAt(0) != '#' ){
                    String[] str_tmp = line.split("\t");
                    listCharacters.put(str_tmp[0],str_tmp[1]);
                }
            }
        }catch (IOException e) {System.out.println("Error: reading XMLCharater "+e);}
    }

    //read white labels from given file
    public void readWhiteLabel(String path, Map<String,Set<String> > _whiteLabels){

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

    }

    //run sequence/itemset pattern mining algorithm
    public void run(String algorithm){
        try{
            long startTime = System.currentTimeMillis();
            //read input data
            readXMLCharacter(basicConf.getXmlCharacterFile(), xmlCharacters);
            readWhiteLabel(basicConf.getWhiteLabelFile(), whiteLabels);
            ReadDatabase td = new ReadDatabase();
            treeDatabase = td.readAST(new File(basicConf.getInputFiles()), whiteLabels, labelsIndex);

            //naming input, output files for itemset mining algorithms
            String[] inputPaths = basicConf.getInputFiles().split("/");
            String transactionDatabaseName = inputPaths[inputPaths.length-1];
            String prefixName = basicConf.getOutputFile()+"/"+transactionDatabaseName + "_" + basicConf.getMinSup()*100 + "_percent";
            String inputItemsetMining = prefixName + "_transactionDatabase.txt";
            String outputItemsetMining = prefixName + "_patterns.txt";

            //running itemset mining algorithms
            switch(algorithm){
                case "ppicgap":
                    System.out.print("creating sequence transaction Database ... ");
                    createSequenceTimeTransactionDatabase(inputItemsetMining);

                    //run sequences mining algorithm with gap constraint
                    System.out.println("running sequence mining algorithm ... ");
                    String commandSeq = "java -jar lib/ppict.2.0.0-assembly-0.1-SNAPSHOT.jar " + inputItemsetMining+ " 20 0 -f 4 -t -m 0 -n 1 -v";
                    Process procSeq = Runtime.getRuntime().exec(commandSeq);

                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(procSeq.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(procSeq.getErrorStream()));

                    String outputPPIC = prefixName+"_sequentialItemsets.txt";
                    FileWriter out = new FileWriter(outputPPIC);

                    // Read the output from the command
                    System.out.println("Here is the standard output of the command:\n");
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                        //System.out.println(s);
                        out.write(s + "\n");
                    }
                    out.flush();
                    out.close();

                    //TODO: convert itemsets to paths from s
                    printSequentialItemset(outputPPIC, outputItemsetMining);

//                    // Read any errors from the attempted command
//                    System.out.println("Here is the standard error of the command (if any):\n");
//                    while ((s = stdError.readLine()) != null) {
//                        System.out.println(s);
//                    }

                    procSeq.waitFor();
                    System.out.println((System.currentTimeMillis()-startTime)/1000 +" s");
                    break;

                case "fpmax":
                    //create itemset transaction Database
                    System.out.print("creating transactionDatabase ... ");
                    createTransactionDatabase(inputItemsetMining);
                    //using the FPMax algorithm: http://www.philippe-fournier-viger.com/spmf/FPMax.php: OK
                    System.out.print("running itemset mining algorithm ... ");
                    AlgoFPMax algoFPMax = new AlgoFPMax();
                    double sup = basicConf.getMinSup();
                    algoFPMax.runAlgorithm(inputItemsetMining, outputItemsetMining, sup, basicConf.getMinNode());
                    algoFPMax.printStats();
                    break;

                case "vmsp":
                    //create itemset transactionDatabase
                    System.out.print("creating transactionDatabase ... ");
                    createTransactionDatabase(inputItemsetMining);
                    //using VMSP algorithm to find MFP: Slow
                    System.out.println("running itemset mining algorithm ... "); //very slow
                    String commandStr = "java -jar lib/spmf.jar run VMSP "+inputItemsetMining+" "+ outputItemsetMining+" "+"15%";
                    Process proc = Runtime.getRuntime().exec(commandStr);
                    proc.waitFor();
                    break;

                case "eclat":
                    //create itemset transactionDatabase
                    System.out.print("creating transactionDatabase ... ");
                    createTransactionDatabase(inputItemsetMining);
                    //using Eclat algorithm to find FP : very slow !
                    TransactionDatabase database = new TransactionDatabase();
                    for(int i = 0; i<transactionDatabase.size(); ++i){
                        database.addTransaction(transactionDatabase.get(i));
                    }
                    System.out.println("running itemset mining algorithm ... ");
                    AlgoEclat algoEclat = new AlgoEclat();
                    algoEclat.runAlgorithm(outputItemsetMining, database , basicConf.getMinSup()*100, true);
                    algoEclat.printStats();

            }

              //run krimp .... do it manually and get the results: ...file-name.ct and ...analysis.txt
//            String itemsetFile = "krimp/jhotdraw/ct-jhotdraw-closed-5d-pop-20191220112049-5-48579.ct";
//            String analysisFile = "krimp/jhotdraw/jhotdraw.db.analysis.txt";
//            int minSize = basicConf.getMinPaths();
//            String outputFile = "krimp/jhotdraw/jhotdraw-patterns.txt";
//            //print itemset found by krimp
//            printKrimpItemset(itemsetFile, outputFile, analysisFile, minSize);
//            System.out.println( (System.currentTimeMillis()-startTime)/1000 +"s" );
//            startTime = System.currentTimeMillis();
//            System.exit(-1);


            //run forestmatcher for itemsets
            System.out.println("running forestmatcher ...");
            String outputMatching = basicConf.getOutputFile()+"/"+transactionDatabaseName +"_" + basicConf.getMinSup()*100 + "_percent" + "_matches.xml";
            String commandMatch = "java -jar lib/forestmatcher.jar -pathmatching "+basicConf.getInputFiles()+" "+outputItemsetMining+" "+outputMatching;
            Process procMatch = Runtime.getRuntime().exec(commandMatch);
            procMatch.waitFor();

            //delete temporary files: transactionDatabase.txt?
            //Files.deleteIfExists(Paths.get(inputItemsetMining));
            System.out.println("Finish!");

        }catch (Exception e){
            System.out.println("error: running itemset mining algorithms - "+e);
        }
    }

}
