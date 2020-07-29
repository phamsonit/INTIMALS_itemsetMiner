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

        //algorithm:
        //- ppicgap : frequential pattern mining with GAP constraint: ppict.2.0.0-assembly-0.1-SNAPSHOT
        //- ppic    : frequential pattern mining
        //- fpmax   : maximal frequent pattern mining by FPMax
        //- elat    : frequent pattern mining by Eclat
        //- krimp
        String algorithm = "ppicgap";

        //String configPathBasic = "conf/synthetic/config.properties";
        String configPathBasic = "conf/java/config.properties";
        Config configBasic = new Config(configPathBasic);

        if(!Files.exists(Paths.get(configBasic.getOutputFile())))
            Files.createDirectory(Paths.get(configBasic.getOutputFile()));

        ItemsetMiner miner = new ItemsetMiner(configBasic);
        miner.run(algorithm);

        //group ASTs by tids of itemset


        //for each group run Freqt to discover subtrees

    }

}
