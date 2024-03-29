package be.intimals.ItemsetMiner.structure;

import java.util.*;

public class Projected {
    private int depth = -1;
    private int support = -1;
    private int rootSupport = -1;
    //private Vector<Location> locations = new Vector<Location>();
    //private Vector<Location> rootLocations = new Vector<Location>();
    private List<int[]> locations = new  ArrayList<>();
    private List<int[]> rootLocations = new ArrayList<>();
    //private Set<Location> rootLocations = new LinkedHashSet<>();
    private List<List<Integer>> lineNr = new ArrayList<>();

    //////////////////////////////////////////////////////////
    public void Projected(){}

    public void setProjectedDepth(int d)
    {
        this.depth = d;
    }
    public int getProjectedDepth() {
        return this.depth;
    }

    public void setProjectedSupport(int s) {
        this.support = s;
    }
    public int getProjectedSupport(){
        return this.support;
    }

    public void setProjectedRootSupport(int s) {
        this.rootSupport = s;
    }
    public int getProjectedRootSupport(){
        return this.rootSupport;
    }

    //////////////locations////////////////////
    //keep right most position
    public void setProjectLocation(int i, int j) {
//        Location l = new Location();
        int[] l = Location.init();
        Location.setLocationId(l, i);
        l = Location.addLocationPos(l, j);
//        l.setLocationId(i);
//        l.addLocationPos(j);
        this.locations.add(l);
    }

    public int[] getProjectLocation(int i){
        return this.locations.get(i);
    }

    public void deleteProjectLocation(int i){
        this.locations.remove(i);
    }

    public int getProjectLocationSize(){
        return this.locations.size();
    }

    //keep positions of all occurrences
    public void addProjectLocation(int i, int j, int[] occurrences) {
        int[] l = Location.init(occurrences);
        Location.setLocationId(l, i);
        l = Location.addLocationPos(l, j);
//        Location l = new Location(occurrences);
//        l.setLocationId(i);
//        l.addLocationPos(j);
        this.locations.add(l);
    }

    public void removeProjectLocation(int[] location){
        this.locations.remove(location);
    }

    /////////////root locations ///////////////
    //add a position to root locations
    public void setProjectRootLocation(int i, int j) {
        int[] l = Location.init();
        Location.setLocationId(l, i);
        l = Location.addLocationPos(l, j);

//        Location l = new Location();
//        l.setLocationId(i);
//        l.addLocationPos(j);

        //check if l exists in rootLocations ????

        boolean dup=false;
        for(int k=0; k<rootLocations.size(); ++k)
            if(Location.getLocationId(rootLocations.get(k)) == i &&
                    Location.getLocationPos(rootLocations.get(k)) == j)
                dup=true;

        if(!dup) this.rootLocations.add(l);

    }

    public int[] getProjectRootLocation(int i){
        return this.rootLocations.get(i);
    }

    public int getProjectRootLocationSize(){
        return this.rootLocations.size();
    }

}
