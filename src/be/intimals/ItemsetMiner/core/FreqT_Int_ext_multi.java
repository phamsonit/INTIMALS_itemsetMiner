package be.intimals.ItemsetMiner.core;

import be.intimals.ItemsetMiner.config.Config;
import be.intimals.ItemsetMiner.structure.*;

import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/*
    extended FREQT + without using max size constraints
 */

public class FreqT_Int_ext_multi extends FreqT_Int {

    private Map<ArrayList<Integer>,String> MFP = new ConcurrentHashMap<>();
    //store root occurrences for the second round
    private Map<String,ArrayList<Integer>> interruptedRootID = null;

    private long timePerGroup;
    private long timeStart2nd;
    private long timeFor2nd;
    private long timeSpent;
    private int roundCount;
    private boolean timeout;


    ////////////////////////////////////////////////////////////////////////////////

    public FreqT_Int_ext_multi(Config _config,
                               Map<String,ArrayList<String>> _grammar,
                               Map<Integer,ArrayList<String>> _grammarInt,
                               Map<Integer,ArrayList<Integer>> _blackLabelsInt,
                               Map<Integer,ArrayList<Integer>> _whiteLabelsInt,
                               Map<String,String> _xmlCharacters,
                               Map<Integer,String> _labelIndex,
                               ArrayList <ArrayList<NodeFreqT> >  _transaction) {
        super(_config);
        this.grammar = _grammar;
        this.grammarInt = _grammarInt;
        this.blackLabelsInt = _blackLabelsInt;
        this.whiteLabelsInt = _whiteLabelsInt;
        this.xmlCharacters = _xmlCharacters;
        this.labelIndex = _labelIndex;
        this.transaction = _transaction;
    }

    private void project(ArrayList<Integer> largestPattern, Projected projected,long timeStartGroup) {
        try{
            //check timeout for the current group
            long diff = System.currentTimeMillis( )-timeStartGroup;
            if( diff  > timePerGroup) {
                //finished = false;
                //keep the depth of projector
                String rootOccurrences = String.valueOf(projected.getProjectedDepth())+"\t";
                //keep root occurrences and right-most occurrences
                for (int i = 0; i < projected.getProjectRootLocationSize(); ++i) {
                    rootOccurrences = rootOccurrences +
                            Location.getLocationId(projected.getProjectRootLocation(i)) + ("-") +
                            Location.getLocationPos(projected.getProjectRootLocation(i)) + ";";
                    //Location.getLocationPos(projected.getProjectLocation(i)) + ";";
                }
                //keep right-most occurrences and right-most occurrences
                String rightmostOccurrences="";
                for (int i = 0; i < projected.getProjectLocationSize(); ++i) {
                    rightmostOccurrences = rightmostOccurrences +
                            Location.getLocationId(projected.getProjectLocation(i)) + ("-") +
                            Location.getLocationPos(projected.getProjectLocation(i)) + ";";
                }
                rootOccurrences = rootOccurrences+"\t"+rightmostOccurrences;
                //store the current pattern for the next round
                //System.out.println((rootOccurrences+"--"+largestPattern.toString()));
                interruptedRootID.put(rootOccurrences,largestPattern);
                return;
            }
            //System.out.println(largestPattern);
            //find candidates
            Map<ArrayList<Integer>, Projected> candidates = generateCandidates(projected,transaction);
            //System.out.println("all candidates     " + candidates.keySet());
            prune(candidates,config.getMinSupport());
            //System.out.println("after support pruning " + candidates.keySet());
            //pruning based on blacklist: for each candidate if it occurs in the blacklist --> remove
            pruneBlackList(largestPattern,candidates,blackLabelsInt);
            //System.out.println("after blacklist pruning " + candidates.keySet());
            //if there is no candidate then report pattern --> stop
            if( candidates.isEmpty() ){
                addFP(largestPattern,projected,MFP);
                return;
            }
            //expand the current pattern with each candidate
            Iterator < Map.Entry<ArrayList<Integer>,Projected> > iter = candidates.entrySet().iterator();
            while (iter.hasNext()) {
                int oldSize = largestPattern.size();
                Map.Entry<ArrayList<Integer>, Projected> entry = iter.next();
                //constraints for mining COBOL
                //delete candidate that belongs to black-section
                //String candidateLabel = Pattern.getPotentialCandidateLabel(entry.getKey());
//                String candidateLabel = labelIndex.get(entry.getKey().get(entry.getKey().size()-1));
//                if(candidateLabel.equals("SectionStatementBlock"))
//                    checkBlackSection(entry,transaction);
//                //delete paragraph locations which are not continuous
//                if(candidateLabel.equals("ParagraphStatementBlock")) {
//                    checkContinuousParagraph(largestPattern, entry, transaction);
//                }
                largestPattern.addAll(entry.getKey());
                //don't check maximal size constraint ....
                //constraint on leaf node
                if(Pattern_Int.checkMissingLeaf(largestPattern)) {
                    //System.out.println("missing leaf "+ pattern);
                    addFP(largestPattern, entry.getValue(), MFP);
                }else{
                    //constraint on obligatory children
                    if(checkObligatoryChild(largestPattern, entry.getKey(), grammarInt, blackLabelsInt)){
                        //System.out.println("missing obligatory child "+pattern);
                        addFP(largestPattern, entry.getValue(), MFP);
                    }else{
                        project(largestPattern, entry.getValue(), timeStartGroup);
                    }
                }
                //largestPattern.setSize(oldSize);
                largestPattern = new ArrayList<>(largestPattern.subList(0,oldSize));
            }
        }catch (Exception e){
            System.out.println("Error: Freqt_ext projected " + e);
            e.printStackTrace();
        }
    }


    //parallel expand groups of root occurrences
    private void expandGroupParallel(Map <String, ArrayList<Integer> > _rootIDs) {

        _rootIDs.entrySet().parallelStream().forEach(entry -> {
            //check total running time of the second step
            long currentTimeSpent = (System.currentTimeMillis( ) - timeStart2nd);
            if(currentTimeSpent > timeFor2nd){
                timeout = true;
                return;
            }
            //start expanding for this group
            long timeStartGroup = System.currentTimeMillis();
            //boolean finished = true;
            //System.out.println("Group "+entry.getKey());
            //System.out.println(frequentPatterns.size()+" - "+ nbOutputMaximalPatterns);
            ArrayList<Integer> largestPattern = new ArrayList<>();
            Projected projected = new Projected();
            if (roundCount == 1) {
                largestPattern.addAll(entry.getValue());
                projected.setProjectedDepth(0);
                //calculate the root positions
                String[] temp = entry.getKey().split(";");
                for (int i = 0; i < temp.length; ++i) {
                    String[] pos = temp[i].split("-");
                    projected.setProjectLocation(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
                    projected.setProjectRootLocation(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
                }
            } else {
                //from the second round, expanding from the patterns which interrupted in the previous round
                largestPattern.addAll(entry.getValue());

                String[] projectTemp = entry.getKey().split("\t");
                projected.setProjectedDepth(Integer.valueOf(projectTemp[0]));
                //calculate root and right-most positions
                String[] rootTemp = projectTemp[1].split(";");
                for (int i = 0; i < rootTemp.length; ++i) {
                    String[] pos = rootTemp[i].split("-");
                    projected.setProjectRootLocation(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
                    ////location = (id,[root pos, rightmost pos])
                    //projected.setProjectLocation(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
                    //projected.setProjectLocation(Integer.valueOf(pos[0]), Integer.valueOf(pos[2]));
                }
                String[] rightmostTemp = projectTemp[2].split(";");
                for (int i = 0; i < rightmostTemp.length; ++i) {
                    String[] pos = rightmostTemp[i].split("-");
                    projected.setProjectLocation(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
                }
            }
            //largestMinSup = projected.getProjectedSupport();
            project(largestPattern, projected, timeStartGroup);
            //update size of the pattern for next expansion
            //largestPattern.setSize(largestPattern.size() - 1);
            //largestPattern = new ArrayList<>(largestPattern.subList(0,largestPattern.size()-1));
        });

    }

    public void run(Map <String, ArrayList<Integer> > _rootIDs,long start1st,FileWriter _report){
        try{
            //calculate times for incremental maximal pattern mining
            roundCount = 1;
            timeout = false;
            timeFor2nd = (config.getTimeout())*(60*1000);
            timeStart2nd = System.currentTimeMillis();
            timeSpent = 0;

            //System.out.println("number of root occurrences "+_rootIDs.size());
            //incremental mining
            while(! _rootIDs.isEmpty() && !timeout){
                //store interrupted groups which run over timePerTask
                interruptedRootID = new ConcurrentHashMap<>();
                //calculate time for each group in the current round
                //log(_report,"===================");
                //log(_report,"ROUND: "+ roundCount);
                //output to check each round
                //System.out.println("ROUND: "+ roundCount);
                //System.out.println("groups: "+ _rootIDs.size());
                //System.out.println("#patterns: "+ outputMaximalPatternsMap.size());
                //log(_report,"- nbGroups = "+ _rootIDs.size());
                timePerGroup = (timeFor2nd - timeSpent) / _rootIDs.size() ;
                //log(_report,"- timePerGroup = "+ timePerGroup +" ms");
                //log(_report,"===================");
                //for each group of root occurrences find patterns without max size constraints
                //int groupCount = 1;

                //TODO: add maximal pattern in the parallel mining process
                expandGroupParallel(_rootIDs);

                //update running time
                timeSpent = (System.currentTimeMillis() - timeStart2nd);
                //update lists of root occurrences for next round
                _rootIDs = interruptedRootID;
                roundCount++;
            }

            if(timeout)
                log(_report,"\t + timeout in the second step");
            else
                log(_report,"\t + search finished");
            long currentTimeSpent = (System.currentTimeMillis( ) - timeStart2nd);
            log(_report, "\t + 2nd running time: ..."+currentTimeSpent/1000+"s");
            log(_report,"\t + frequent patterns: "+ MFP.size());
            int nbMFP;
            String outFile = config.getOutputFile();
            long startFilter = System.currentTimeMillis();
            Map<ArrayList<Integer>,String> mfpTemp = filterFP_multi(MFP);
            log(_report,"\t + filtering time: "+(System.currentTimeMillis() - startFilter)/1000+"s");

            //print maximal patterns
            long startPrint = System.currentTimeMillis();
            nbMFP = mfpTemp.size();
            outputPatterns(mfpTemp,outFile);
            log(_report,"\t + printing time: "+ Float.valueOf(System.currentTimeMillis()-startPrint)/1000+"s");

            log(_report,"\t + maximal patterns: "+ nbMFP);
            log(_report,"- total running time: "+(System.currentTimeMillis( )-start1st)/1000+"s");
            _report.flush();
            _report.close();

        }catch (Exception e){}
    }

}