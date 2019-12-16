package be.intimals.ItemsetMiner;

import be.intimals.ItemsetMiner.structure.NodeFreqT;
import be.intimals.ItemsetMiner.structure.Projected;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPMax;


import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ItemsetMiner {

    public static final String uniChar = "\u00a5";

    private ArrayList<ArrayList<NodeFreqT>> transaction = new ArrayList<>();
    private Map<Integer, String> indexLabels = new LinkedHashMap<>();
    private Map<String, Set<String>> whiteLabels = new HashMap<>();

    private String inputDir;
    private String outputDir;
    private String inputBlackLabel;
    private double minsupp;
    private int minsize;

    ///////////////////
    public ItemsetMiner(String _inputDir, String _outputDir, String _inputBlackLabel, double _minsupp, int _minsize) {
        inputDir = _inputDir;
        outputDir = _outputDir;
        inputBlackLabel = _inputBlackLabel;
        minsupp = _minsupp;
        minsize = _minsize;
    }

    //create sequence database
    private void createSequenceDatabase(String output){
        try{
            //create output file
            FileWriter out = new FileWriter(output);

            //create transaction database
            List<String> itemIndex = new LinkedList<>();
            List<List<Integer>> database = new LinkedList<>();

            for(int i=0; i<transaction.size(); ++i){
                List<Integer> trans = new LinkedList<>();
                for(int j=0; j<transaction.get(i).size(); ++j){
                    //if this is a leaf node
                    if(transaction.get(i).get(j).getNode_label_int() < 0) {
                        //find path from leaf to root
                        String item = String.valueOf(transaction.get(i).get(j).getNode_label_int());
                        NodeFreqT currentNode = transaction.get(i).get(j);
                        while(currentNode.getNodeParent() != -1){
                            int parentPos = currentNode.getNodeParent();
                            currentNode = transaction.get(i).get(parentPos);
                            item = item + "," + String.valueOf(currentNode.getNode_label_int());
                        }
                        //add path to itemIndex
                        if(!itemIndex.contains(item))
                            itemIndex.add(item);
                        //add path id to transaction
                        //if(!trans.contains(itemIndex.indexOf(item)))
                            trans.add(itemIndex.indexOf(item));
                    }

                }
                //add transaction to transaction database
                database.add(trans);
            }

            //write header information: if using sequence pattern mining in spmf software
            for(int i=0; i< itemIndex.size(); ++i){
                out.write("@ITEM"+uniChar+i+uniChar+toStringLabels(itemIndex.get(i))+"\n");
                //out.write("@ITEM"+"="+i+"="+toStringLabels(itemIndex.get(i))+"\n");
            }
            //out.write("@ITEM=-1=|\n");

            //write sequence database to file
            for(List<Integer> list : database){
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
            System.out.println("error create transaction database "+e);
        }
    }

    //create transaction database
    private void createTransactionDatabase(String output){
        try{
            //create output file
            FileWriter out = new FileWriter(output);

            //create transaction database
            List<String> itemIndex = new LinkedList<>();
            List<List<Integer>> database = new LinkedList<>();

            for(int i=0; i<transaction.size(); ++i){
                List<Integer> trans = new LinkedList<>();
                for(int j=0; j<transaction.get(i).size(); ++j){
                    //if this is a leaf node
                    if(transaction.get(i).get(j).getNode_label_int() < 0) {
                        //find path from leaf to root
                        String item = String.valueOf(transaction.get(i).get(j).getNode_label_int());
                        NodeFreqT currentNode = transaction.get(i).get(j);
                        while(currentNode.getNodeParent() != -1){
                            int parentPos = currentNode.getNodeParent();
                            currentNode = transaction.get(i).get(parentPos);
                            item = item + "," + String.valueOf(currentNode.getNode_label_int());
                        }
                        //add path to itemIndex
                        if(!itemIndex.contains(item))
                            itemIndex.add(item);
                        //add path id to transaction
                        if(!trans.contains(itemIndex.indexOf(item)))
                            trans.add(itemIndex.indexOf(item));
                    }

                }
                //add transaction to transaction database
                database.add(trans);
            }

            //write header information to file
            //out.write("@CONVERTED_FROM_TEXT\n");
            for(int i=0; i< itemIndex.size(); ++i){
                out.write("@ITEM"+uniChar+i+uniChar+toStringLabels(itemIndex.get(i))+"\n");
            }
            //write transaction database to file
            for(List<Integer> list : database){
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
            System.out.println("error create transaction database "+e);
        }
    }

    //convert item from Integer to String representation
    private String toStringLabels(String inputItemset){
        String stringLabels = "";
        String[] items = inputItemset.split(",");
        stringLabels += indexLabels.get(Integer.valueOf(items[items.length-1]));
        for(int j=items.length-2; j>0; --j)
            stringLabels += "," + indexLabels.get(Integer.valueOf(items[j]));
        stringLabels += ","+indexLabels.get(Integer.valueOf(items[0])).substring(1,indexLabels.get(Integer.valueOf(items[0])).length());
        return stringLabels;
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

    //run sequence/itemset pattern mining algorithm
    public void run(){
        try{
            long startTime = System.currentTimeMillis();
            //read input databases
            ReadDatabase database = new ReadDatabase();
            whiteLabels = database.readWhiteLabel(inputBlackLabel);
            transaction = database.readAST(new File(inputDir), whiteLabels, indexLabels);

            String[] inputPaths = inputDir.split("/");
            String databaseName = inputPaths[inputPaths.length-1];

            String inputItemsetMining = databaseName + "_" + minsupp*100 + "_percent" + "_database.txt";
            String outputItemsetMining = outputDir+"/"+databaseName +"_" + minsupp*100 + "_percent" + "_patterns.txt";

            //create itemset database
            System.out.print("creating itemset database ... ");
            createTransactionDatabase(inputItemsetMining);

            System.out.println( (System.currentTimeMillis()-startTime)/1000 +"s" );
            startTime = System.currentTimeMillis();

            //using the FPMax algorithm: http://www.philippe-fournier-viger.com/spmf/FPMax.php
            System.out.print("running itemset mining algorithm ... ");
            AlgoFPMax algo = new AlgoFPMax();
            algo.runAlgorithm(inputItemsetMining, outputItemsetMining, minsupp, minsize);
            //algo.printStats();

            //using Eclat algorithm to find FP : slow !
//            System.out.println("running itemset mining algorithm ... ");
//            AlgoEclat algo = new AlgoEclat();
//            algo.runAlgorithm(outputItemsetMining, transactionDatabase, minsupp, true);
//            algo.printStats();

              //using VMSP algorithm to find MFP: Slow
//            System.out.println("running itemset mining algorithm ... "); //running very slowly
//            String commandStr = "java -jar spmf.jar run VMSP "+inputItemsetMining+" "+ outputItemsetMining+" "+"15%";
//            Process proc = Runtime.getRuntime().exec(commandStr);
//            proc.waitFor();

            System.out.println((System.currentTimeMillis()-startTime)/1000 +" s");

            //run forestmatcher for itemsets
            String outputMatching = outputDir+"/"+databaseName +"_" + minsupp*100 + "_percent" + "_matches.xml";
            String commandStr = "java -jar forestmatcher.jar -pathmatching "+inputDir+" "+outputItemsetMining+" "+outputMatching;
            Process proc = Runtime.getRuntime().exec(commandStr);
            proc.waitFor();

            //delete temporary files: database.txt?
            Files.deleteIfExists(Paths.get(inputItemsetMining));
            System.out.println("Finish!");

        }catch (Exception e){
            System.out.println("error: runing itemset mining - "+e);
        }
    }

}
