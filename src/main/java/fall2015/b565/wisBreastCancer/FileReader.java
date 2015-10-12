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

import fall2015.b565.wisBreastCancer.utils.AttributeNames;
import fall2015.b565.wisBreastCancer.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class FileReader {
    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);
    public static void main(String[] args) {
        try {
            FileReader fileReader = new FileReader();
            Map<Integer, String> dataMapWithoutDuplicates = fileReader.dataMapWithoutDuplicates();
            System.out.println(dataMapWithoutDuplicates.size());
            Map<Integer, String> benignMap = fileReader.getBenignMap(dataMapWithoutDuplicates);
            System.out.println(benignMap.size());
            Map<Integer, String> malignantMap = fileReader.getMalignantMap(dataMapWithoutDuplicates);
            System.out.println(malignantMap.size());
            Map<Integer, String> missingDataMap = fileReader.getMissingDataMap(dataMapWithoutDuplicates);
            Map<Integer, String> benignMapFromMissingData = fileReader.getBenignMap(missingDataMap);
            System.out.println(benignMapFromMissingData.size());
            Map<Integer, String> malignantMapFromMissingDate = fileReader.getMalignantMap(missingDataMap);
            System.out.println(malignantMapFromMissingDate.size());
            for (int i: missingDataMap.keySet()){
                System.out.println("*************** before updating **************");
                System.out.println(missingDataMap.get(i));
            }
            fileReader.updateMissingValuesDataMap(dataMapWithoutDuplicates);
            fileReader.writeCleanData(dataMapWithoutDuplicates);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, String> dataMapWithoutDuplicates () throws Exception{
        Map<Integer, String> dataMap = new HashMap<Integer, String>();
        InputStream is = FileReader.class.getClassLoader().getResourceAsStream(Constants.DATA_FILE_NAME);
        // Construct BufferedReader from FileReader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            String[] lineSplits = line.split(",");
            String rest = line.substring(lineSplits[0].length() + 1, line.length());
            dataMap.put(Integer.valueOf(lineSplits[0]), rest);
        }
        br.close();
        // sorting the map
        return new TreeMap<Integer, String>(dataMap);
    }

    public Map <Integer, String> getBenignMap (Map<Integer, String> sortedDataMap) throws Exception{
         Map <Integer, String> benignMap = new HashMap<Integer, String>();
        for (Integer id : sortedDataMap.keySet()){
            String data = sortedDataMap.get(id);
            String[] dataSplits = data.split(",");
            String classAttribute = dataSplits[dataSplits.length - 1];
            if (Integer.valueOf(classAttribute).equals(Constants.BENIGN)){
                benignMap.put(id, data);
            }
        }
        return new TreeMap<Integer, String>(benignMap);
    }

    public Map <Integer, String> getMalignantMap (Map<Integer, String> sortedDataMap) throws Exception{
        Map <Integer, String> malignantMap = new HashMap<Integer, String>();
        for (Integer id : sortedDataMap.keySet()){
            String data = sortedDataMap.get(id);
            String[] dataSplits = data.split(",");
            String classAttribute = dataSplits[dataSplits.length - 1];
            if (Integer.valueOf(classAttribute).equals(Constants.MALIGNANT)){
                malignantMap.put(id, data);
            }
        }
        return new TreeMap<Integer, String>(malignantMap);
    }

    public Map<Integer, String> getMissingDataMap (Map<Integer, String> sortedDataMap) throws Exception {
        Map <Integer, String> missingDataMap = new HashMap<Integer, String>();
        for (Integer id : sortedDataMap.keySet()){
            String data = sortedDataMap.get(id);
            if (data.contains("?")){
                missingDataMap.put(id, data);
            }
        }
        return new TreeMap<Integer, String>(missingDataMap);
    }

    public void updateMissingValuesDataMap (Map<Integer, String> sortedMapWithoutDuplicates) throws Exception {
        // read attribute 7 and get the mean
        int meanForBareNuclei = getMean(sortedMapWithoutDuplicates, AttributeNames.BARE_NUCLEI);
        for (Integer id : sortedMapWithoutDuplicates.keySet()){
            String data = sortedMapWithoutDuplicates.get(id);
            String replacedRecord = data.replace("?", String.valueOf(meanForBareNuclei));
            System.out.println(replacedRecord);
            sortedMapWithoutDuplicates.put(id, replacedRecord);
        }
    }

    public int getMean (Map<Integer, String> sortedMapWithoutDuplicates, AttributeNames attribute) throws Exception{
        int size = sortedMapWithoutDuplicates.size();
        double count = 0;
        int mean;
        switch (attribute){
            case CLUMP_THICKNESS:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 0){
                        String clump = split[0];
                        if (!clump.contains("?")) {
                            count += Double.valueOf(clump);
                        }
                    }
                }
                break;
            case CELL_SIZE:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 1){
                        String cellSize = split[1];
                        if (!cellSize.contains("?")) {
                            count += Double.valueOf(cellSize);
                        }
                    }
                }
                break;
            case CELL_SHAPE:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 2){
                        String cellShape = split[2];
                        if (!cellShape.contains("?")) {
                            count += Double.valueOf(cellShape);
                        }
                    }
                }
                break;
            case MARGINAL_ADHESION:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 3){
                        String marginAdh = split[3];
                        if (!marginAdh.contains("?")) {
                            count += Double.valueOf(marginAdh);
                        }
                    }
                }
                break;
            case SINGLE_CELL_SIZE:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 4){
                        String singleCellSize = split[4];
                        if (!singleCellSize.contains("?")) {
                            count += Double.valueOf(singleCellSize);
                        }
                    }
                }
                break;
            case BARE_NUCLEI:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 5){
                        String bareNuc = split[5];
                        if (!bareNuc.contains("?")) {
                            count += Double.valueOf(bareNuc);
                        }
                    }
                }
                break;
            case BLAND_CHROMATION:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 6){
                        String blandChro = split[6];
                        if (!blandChro.contains("?")) {
                            count += Double.valueOf(blandChro);
                        }
                    }
                }
                break;
            case NORMAL_NUCLEOLI:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 7){
                        String normalNuc = split[7];
                        if (!normalNuc.contains("?")) {
                            count += Double.valueOf(normalNuc);
                        }
                    }
                }
                break;
            case MITOSES:
                for (Integer id : sortedMapWithoutDuplicates.keySet()){
                    String record = sortedMapWithoutDuplicates.get(id);
                    String[] split = record.split(",");
                    if (split.length > 8){
                        String mitoses = split[8];
                        if (!mitoses.contains("?")) {
                            count += Double.valueOf(mitoses);
                        }
                    }
                }
                break;
            default:
                logger.error("Unrecognized attribute type");
                throw new Exception("Unrecognized attribute type");
        }

        mean = (int)(count / size);
        System.out.println("mean : " + mean);
        return mean;

    }

    public void writeCleanData(Map<Integer, String> cleanedMap) throws Exception{
        try {
            PrintWriter writer = new PrintWriter(Constants.CLEANED_DATA_FILE_PATH);
            for (Integer id : cleanedMap.keySet()){
                writer.println(String.valueOf(id) + "," + cleanedMap.get(id));
            }
            writer.close();
        }catch (Exception e){
            logger.error("Error occurred while writing data to file :" + Constants.CLEANED_DATA_FILE_NAME);
            throw new Exception("Error occurred while writing data to file :" + Constants.CLEANED_DATA_FILE_NAME, e);
        }

    }

}
