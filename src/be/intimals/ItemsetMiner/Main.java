/*
   $Id: freqt.cpp,v 1.5 2003/01/22 08:37:19 taku-ku Exp $;

   Copyright (C) 2003 Taku Kudo, All rights reserved.
   This is free software with ABSOLUTELY NO WARRANTY.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
   02111-1307, USA
   ===============================
   java implementation: 16 May 2018
   by PHAM Hoang Son
*/

package be.intimals.ItemsetMiner;

import be.intimals.ItemsetMiner.config.Config;

import java.io.*;
import java.nio.file.Paths;
import java.lang.String;
import java.nio.file.Files;

public class Main {

    static public void main(String[] args) throws IOException {

        //#String inputDir = "/Users/user/Working/INTIMALS/softs-gitlab/tree-miner/input-ASTs/synthetic-data/4files";
        String inputDir = "/Users/user/Working/INTIMALS/softs-gitlab/tree-miner/input-ASTs/jhotdraw/jhotdraw-original";
        //String inputDir = "/Users/user/Working/INTIMALS/softs-gitlab/tree-miner/input-ASTs/jhotdraw/jhotdraw-folds/fold4";
        //String inputDir = "/Users/user/Working/INTIMALS/softs-gitlab/tree-miner/input-ASTs/checkstyle/checkstyle-original";
        String outputDir = "output1";
        String inputBlackLabel="conf/java/listWhiteLabel.txt";
        double minsup = 0.015;
        int minsize = 10;

//        String configPathBasic = args[0];
//        Config configBasic = new Config(configPathBasic);
//
//        String inputDir = configBasic.getInputFiles();
//        String outputDir = configBasic.getOutputFile();
//        String inputBlackLabel = configBasic.getWhiteLabelFile();
//        double minsup = configBasic.getMinSup();
//        int minsize = configBasic.getMinLeaf();

        if(!Files.exists(Paths.get(outputDir)))
            Files.createDirectory(Paths.get(outputDir));

        ItemsetMiner miner = new ItemsetMiner(inputDir, outputDir, inputBlackLabel, minsup, minsize);
        miner.run();
    }

}
