package be.intimals.seqMiner;

import be.intimals.seqMiner.config.Config;
import be.intimals.seqMiner.structure.NodeFreqT;


import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class SeqMiner {

    private Config config;
    private ArrayList  <ArrayList<NodeFreqT>>  transaction = new ArrayList <>();
    private Map<Integer,String> indexLabels = new HashMap<>();
    private Map<String,Set<String>> whiteLabels = new HashMap<>();

    ///////////////////
    public SeqMiner(Config _config){
        config = _config;
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
                        //System.out.print(transaction.get(i).get(j).getNode_label_int() + ",");
                        //find ancestors label
                        NodeFreqT currentNode = transaction.get(i).get(j);
                        while(currentNode.getNodeParent() != -1){
                            int parentPos = currentNode.getNodeParent();
                            currentNode = transaction.get(i).get(parentPos);
                            //System.out.print(currentNode.getNode_label_int()+",");
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

            out.write("@CONVERTED_FROM_TEXT\n");
            //System.out.println("@CONVERTED_FROM_TEXT");
            for(int i=0; i< itemIndex.size(); ++i){
                out.write("@ITEM="+i+"="+toStringLabels(itemIndex.get(i))+"\n");
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

    //convert labels of a item from Integer to String
    private String toStringLabels(String inputItemset){
        String stringLabels = "";
        String[] items = inputItemset.split(",");
        stringLabels += indexLabels.get(Integer.valueOf(items[items.length-1]));
        for(int j=items.length-2; j>=0; --j)
            stringLabels += "," + indexLabels.get(Integer.valueOf(items[j]));

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
            whiteLabels = database.readWhiteLabel(config.getWhiteLabelFile());
            transaction = database.readAST(new File(config.getInputFiles()), whiteLabels, indexLabels);

            //create itemset database
            System.out.print("creating itemset database ... ");
            createItemsetDatabase("database.txt");
            System.out.println(" ended.");

            //run maximal itemset mining algorithm
            System.out.print("running itemset mining algorithm ... ");
            String commandStr = "java -jar spmf.jar run FPMax database.txt output.txt " + config.getMinSup();
            Process proc = Runtime.getRuntime().exec(commandStr);
            proc.waitFor();
            System.out.println(" ended.");

            //mapping itemsets to labels of ASTs
            //printPattern("output.txt", indexLabels);

            //delete temporary files: database.txt, output.txt ?
            long endTime = System.currentTimeMillis();
            System.out.println("Running time : "+ (endTime-startTime)/1000 +" s");

        }catch (Exception e){
            System.out.println("error: runing itemset mining - "+e);
        }


    }

}
