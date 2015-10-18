/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

package fall2015.b565.wisBreastCancer;

import com.google.common.primitives.Ints;
import fall2015.b565.wisBreastCancer.utils.Constants;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class Assignment2 {
    private static final Logger logger = LoggerFactory.getLogger(Assignment2.class);
    private static  boolean correlation = false;
    private static  boolean ppv = false;
    private static  boolean powerSetPPV = false;
    private static  boolean vfoldCrossValidation = false;
    private static boolean useReplaceDataSet = true;
    private static String cleanedFilePath;
    private static int[] allAttributeHeaders = {0,1,2,3,4,5,6,7,8};
    public static void main(String[] args) throws Exception {
        try {
            parseArguments(args);
            FileReader fileReader = new FileReader();
            if (useReplaceDataSet){
                cleanedFilePath = Constants.REPLACED_DATA_FILE_PATH;
            }else {
                cleanedFilePath = Constants.REMOVED_DATA_FILE_PATH;
            }
            KMeans kMeans = new KMeans();
            System.out.println("=============== Pre-Processing of Data ===============");
            fileReader.cleanDataSet();
            System.out.println("=============== Data Cleaned ===============");
            if (correlation){
                System.out.println("=============== Finding Correlation between attributes ===============");
                kMeans.findAttributeCorrelations();
            }
            if (ppv){
                System.out.println("=============== Finding PPV considering all the attributes ===============");
                KMeansResult kMeansResult = kMeans.findKmeansToAllAttributes(cleanedFilePath);
                double ppv = kMeans.calculatePPV(kMeansResult.getFinalCentroids(), kMeans.getRecords(cleanedFilePath));
                System.out.println("Calculated PPV : " + ppv);
            }
            if (powerSetPPV){
                System.out.println("=============== Finding PPV considering power set of the attributes ===============");
                kMeans.findKmeansToAttributePowerSet(cleanedFilePath);
            }
            if (vfoldCrossValidation){
                System.out.println("=============== Finding V Fold cross validation considering all the attribute set ===============");
                KMeansResult kMeansResult = kMeans.findKmeansToAllAttributes(cleanedFilePath);
                HashSet<Integer> attributes = new HashSet<Integer>(Ints.asList(allAttributeHeaders));
                double vPPV= kMeans.vFoldCrossValidation(kMeansResult.getInitialRecords(), attributes);
                System.out.println("VFold cross validation PPV : " + vPPV );
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void parseArguments(String[] args) throws Exception{
        try{
            Options options = new Options();

            options.addOption("c", false , "To find correlation between the attributes");
            options.addOption("ppv", true, "Find ppv when considering all the attributes. You should provide which cleaning method to be used - removing (rm), replace (rp)");
            options.addOption("powPPV", true, "Find ppv of the power set of the attributes.  You should provide which cleaning method to be used - removing (rm), replace (rp)");
            options.addOption("vfold", true, "V Fold cross validation.  You should provide which cleaning method to be used - removing (rm), replace (rp)");

            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);
            if (cmd.getOptions() == null || cmd.getOptions().length == 0){
                System.out.println("You have not specified any options. Please provide one of the options : c, ppv, powPPV or vfold");
                throw new Exception("You have not specified any options. Please provide one of the options : c, ppv, powPPV or vfold");
            }
            if (cmd.hasOption("c")){
                logger.info("Finding correlation between attributes...");
                correlation = true;
            }else if (cmd.hasOption("ppv")){
                String optionValue = cmd.getOptionValue("ppv");
                if (optionValue == null){
                    System.out.println("User forget to give which data cleaning method to use. Hence we assume, we use replace data set");
                }else useReplaceDataSet = !optionValue.equals("rm");

                System.out.println("Finding PPV considering all the attributes with data clean method : " + optionValue + "...");
                ppv = true;
            }else if (cmd.hasOption("powPPV")){
                String optionValue = cmd.getOptionValue("powPPV");
                if (optionValue == null){
                    System.out.println("User forget to give which data cleaning method to use. Hence we assume, we use replace data set");
                }else useReplaceDataSet = !optionValue.equals("rm");
                System.out.println("Finding PPV for the power set of the attributes with data clean method : " + optionValue + "...");
                powerSetPPV = true;
            }else if (cmd.hasOption("vfold")){
                String optionValue = cmd.getOptionValue("vfold");
                if (optionValue == null){
                    System.out.println("User forget to give which data cleaning method to use. Hence we assume, we use replace data set");
                }else useReplaceDataSet = !optionValue.equals("rm");
                System.out.println("Finding VFold cross validation considering all the attributes with data clean method : " + optionValue + "...");
                vfoldCrossValidation = true;
            }
        } catch (ParseException e) {
            logger.error("Error while reading command line parameters" , e);
            throw new Exception("Error while reading command line parameters" , e);
        }
    }
}
