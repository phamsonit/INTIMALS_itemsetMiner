package be.intimals.ItemsetMiner;

import be.intimals.ItemsetMiner.structure.NodeFreqT;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPMax;


import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ItemsetMiner {

    private ArrayList  <ArrayList<NodeFreqT>>  transaction = new ArrayList <>();
    private Map<Integer,String> indexLabels = new HashMap<>();
    private Map<String,Set<String>> whiteLabels = new HashMap<>();

    private String inputDir;
    private String inputBlackLabel;
    private double minsupp;
    private int minsize;

    public static final String uniChar = "\u00a5";

    ///////////////////
    public ItemsetMiner(String _inputDir, String _inputBlackLabel, double _minsupp, int _minsize){
        inputDir = _inputDir;
        inputBlackLabel = _inputBlackLabel;
        minsupp = _minsupp;
        minsize = _minsize;
    }

    //create itemset database
    private void createItemsetDatabase(String output){
        try{
            //create file for output
            FileWriter out = new FileWriter(output);
            //create itemset database
            List<String> itemIndex = new LinkedList<>();
            List<List<Integer>> database = new LinkedList<>();
            for(int i=0; i<transaction.size(); ++i){
                List<Integer> trans = new LinkedList<>();
                for(int j=0; j<transaction.get(i).size(); ++j){
                    if(transaction.get(i).get(j).getNode_label_int() < 0) {
                        //TODO compute support of the leaf. if support > minSup then create an item
                        //leaf label
                        String item = String.valueOf(transaction.get(i).get(j).getNode_label_int());
                        //find ancestors label
                        NodeFreqT currentNode = transaction.get(i).get(j);
                        while(currentNode.getNodeParent() != -1){
                            int parentPos = currentNode.getNodeParent();
                            currentNode = transaction.get(i).get(parentPos);
                            item = item + "," + String.valueOf(currentNode.getNode_label_int());
                        }
                        //System.out.println(item);
                        if(!itemIndex.contains(item))
                            itemIndex.add(item);
                        //System.out.println(item);
                        if(!trans.contains(itemIndex.indexOf(item)))
                            trans.add(itemIndex.indexOf(item));
                    }
                }
                //System.out.println();
                database.add(trans);
            }

            out.write("#CONVERTED_FROM_TEXT\n");
            //System.out.println("@CONVERTED_FROM_TEXT");
            for(int i=0; i< itemIndex.size(); ++i){
                out.write("@ITEM"+uniChar+i+uniChar+toStringLabels(itemIndex.get(i))+"\n");
                //System.out.println("@ITEM="+i+"="+itemIndex.get(i));
            }

            for(List<Integer> list : database){
                Collections.sort(list);
                for(int i = 0; i< list.size(); ++i)
                    out.write(String.valueOf(list.get(i))+" ");
                    //System.out.println(list);
                out.write("\n");
            }
            //close file
            out.flush();
            out.close();

        }catch (Exception e){
            System.out.println(e);
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

    //Read itemsets from output of itemset mining algorithm
    //convert labels of itemsets from Integer to String.
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
            String outputItemsetMining = databaseName +"_" + minsupp*100 + "_percent" + "_output.txt";

            //create itemset database
            System.out.print("creating itemset database ... ");
            createItemsetDatabase(inputItemsetMining);
            System.out.println( (System.currentTimeMillis()-startTime)/1000 +"s" );
            startTime = System.currentTimeMillis();

            //using the FPMax algorithm: http://www.philippe-fournier-viger.com/spmf/FPMax.php
            System.out.println("running itemset mining algorithm ... ");
            AlgoFPMax algo = new AlgoFPMax();
            algo.runAlgorithm(inputItemsetMining, outputItemsetMining, minsupp, minsize);
            algo.printStats();

            //delete temporary files: database.txt?
            //Files.deleteIfExists(Paths.get(inputItemsetMining));

            System.out.println((System.currentTimeMillis()-startTime)/1000 +" s");

        }catch (Exception e){
            System.out.println("error: runing itemset mining - "+e);
        }


    }

}
