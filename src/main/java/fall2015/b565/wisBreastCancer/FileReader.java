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

    public void cleanDataSet() throws Exception {
        System.out.println("1: Removing duplicate entries...");
        Map<Integer, String> dataMapWithoutDuplicates = dataMapWithoutDuplicates();
        System.out.println("2.1: Update missing data entries with mean of that data column...");
        Map<Integer, String> updatedDataSetWithReplace = updateMissingValuesWithMeanDataMap(dataMapWithoutDuplicates);
        System.out.println("2.2: Remove missing data entries...");
        Map<Integer, String> updatedDataSetWithRemove = removeMissingValuesDataMap(dataMapWithoutDuplicates);
        System.out.println("3: Write clean data set two files...");
        writeCleanData(updatedDataSetWithReplace, Constants.REPLACED_DATA_FILE_PATH);
        writeCleanData(updatedDataSetWithRemove, Constants.REMOVED_DATA_FILE_PATH);
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

    public Map<Integer, String> updateMissingValuesWithMeanDataMap(Map<Integer, String> sortedMapWithoutDuplicates) throws Exception {
        // read attribute 6 and get the mean
        Map<Integer, String> newDataMap = new HashMap<Integer, String>(sortedMapWithoutDuplicates);
        int meanForBareNuclei = getMean(sortedMapWithoutDuplicates, AttributeNames.BARE_NUCLEI);
        for (Integer id : newDataMap.keySet()){
            String data = newDataMap.get(id);
            if (data.contains("?")){
                String replacedRecord = data.replace("?", String.valueOf(meanForBareNuclei));
                newDataMap.put(id, replacedRecord);
            }
        }
        return newDataMap;
    }

    public Map<Integer, String> removeMissingValuesDataMap (Map<Integer, String> sortedMapWithoutDuplicates) throws Exception {
        Map<Integer, String> newDataMap = new HashMap<Integer, String>(sortedMapWithoutDuplicates);
        for(Iterator<Map.Entry<Integer, String>> it = newDataMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, String> entry = it.next();
            if (entry.getValue().contains("?")){
                it.remove();
            }
        }
        return newDataMap;
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
        return mean;

    }

    public void writeCleanData(Map<Integer, String> cleanedMap, String filePath) throws Exception{
        try {
            PrintWriter writer = new PrintWriter(filePath);
            for (Integer id : cleanedMap.keySet()){
                writer.println(String.valueOf(id) + "," + cleanedMap.get(id));
            }
            writer.close();
            System.out.println("Updated data written to " + filePath);
        }catch (Exception e){
            logger.error("Error occurred while writing data to file :" + filePath);
            throw new Exception("Error occurred while writing data to file :" + filePath, e);
        }

    }

}
