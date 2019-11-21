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


import java.io.*;


import java.lang.String;

public class Main {

    static public void main(String[] args) throws IOException {


        String inputDir = "/Users/user/Working/INTIMALS/softs-gitlab/freqt/input-ASTs/synthetic-data/4files";
        //String inputDir = "/Users/user/Working/INTIMALS/softs-gitlab/freqt/input-ASTs/jhotdraw/jhotdraw-folds/fold4";
        //String inputDir = "/Users/user/Working/INTIMALS/softs-gitlab/freqt/input-ASTs/checkstyle/checkstyle-original";
        String inputBlackLabel="conf/java/listWhiteLabel.txt";
        double minsup = 0.3;
        int minsize = 2;

        ItemsetMiner miner = new ItemsetMiner(inputDir, inputBlackLabel, minsup, minsize);
        miner.run();
    }

}
