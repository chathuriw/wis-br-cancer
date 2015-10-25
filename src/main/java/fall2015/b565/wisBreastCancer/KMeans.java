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

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import fall2015.b565.wisBreastCancer.utils.Constants;
import fall2015.b565.wisBreastCancer.utils.PropertyReader;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class KMeans {
    private static final Logger logger = LoggerFactory.getLogger(KMeans.class);
    private static int k;
    private static int l;
    private static double threshold = .1;
    private static int iterations = 10;
    private static int[] allAttributeHeaders = {0,1,2,3,4,5,6,7,8};
    private static int totalAttributes = 9;
    private static int vfoldBlocks = 0;

    public KMeans() {
        try{
            readConfigurations();
        } catch (Exception e) {
            logger.error("Error occurred while reading configuration files !!!");
        }
    }

    public List<PPV> findKmeansToAttributePowerSet(String cleanedfilePath) throws Exception {
        Set<Set<Integer>> kMeansPowerSet = getPowerSet(new HashSet<Integer>(Ints.asList(allAttributeHeaders)));
        List<PPV> ppvList = new ArrayList<PPV>();
        List<Record> records = getRecords(cleanedfilePath);
        for (Set<Integer> set : kMeansPowerSet){
            if (set.size() != 0) {
                // changing k
                for (int i = 2; i < 20; i += 2){
                    PPV ppvInstance = new PPV();
                    KMeansResult result = calculate(records, set, i, l);
                    double ppv = calculatePPV(result.getFinalCentroids(), result.getInitialRecords());
                    ppvInstance.setPpv(ppv);
                    ppvInstance.setAttributeHeaders(Ints.toArray(set));
                    ppvInstance.setAssociateKVal(i);
                    ppvList.add(ppvInstance);
                }
            }
        }
        Double maxPPV = 0.0;
        int associateKVal = 2;
        int[] attributeHeaders = null;
        String attributeHeader = "";
        for (PPV ppv : ppvList){
            double ppvVal = ppv.getPpv();
            if (ppvVal > maxPPV){
                maxPPV = ppvVal;
                associateKVal = ppv.getAssociateKVal();
                attributeHeaders = ppv.getAttributeHeaders();
            }
        }
        if (attributeHeaders != null){
            for (int header : attributeHeaders){
                attributeHeader += header + ",";
            }
        }
        System.out.println("Max PPV : " + maxPPV + " comes when Attribute Set is : " + attributeHeader + " and when k = " + associateKVal);
        return ppvList;
    }

    public KMeansResult findKmeansToAllAttributes(String cleanedFilePath) throws Exception {
        List<Record> records = getRecords(cleanedFilePath);
        // make data structure of attributes
        HashSet<Integer> attributes = new HashSet<Integer>(Ints.asList(allAttributeHeaders));
        return calculate(records, attributes, k,l);
    }

    public void findAttributeCorrelations() throws Exception {
        List<Record> records = getRecords(Constants.REPLACED_DATA_FILE_PATH);
        Map<Integer, List<Integer>> attributes = getAttributes(records);
        Map<int[], Double> corelationMap = findCorrelation(attributes);
        double max = 0;
        int[] maxPair = {0,0};
        for (int[] pair : corelationMap.keySet()){
            double correlation = corelationMap.get(pair);
            if (max < correlation){
                max = correlation;
                maxPair = pair;
            }
            System.out.println("Attribute pair : {" + pair[0] + "," + pair[1] + "} : correlation : " + correlation);
        }
        System.out.println("Highest Correlation : " + max + " comes for attribute pair : {" + maxPair[0] + ", " + maxPair[1] + "}" );

    }

    public Map<int[], Double> findCorrelation(Map<Integer, List<Integer>> attributes){
        Set<Integer> indexes = attributes.keySet();
        Map<int[], Double> correlationMap = new HashMap<int[], Double>();
        List<int[]> possiblePairs = getPossiblePairs(indexes);
        for (int[] pair : possiblePairs){
            List<Integer> attribute1 = attributes.get(pair[0]);
            List<Integer> attribute2 = attributes.get(pair[1]);
            double[] attributeArray1 = Doubles.toArray(attribute1);
            double[] attributeArray2 = Doubles.toArray(attribute2);
            PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
            double correlation = pearsonsCorrelation.correlation(attributeArray1, attributeArray2);
            correlationMap.put( pair, correlation);
        }
        return correlationMap;
    }

    private List<int[]> getPossiblePairs(Set<Integer> indexes) {
        List<int[]> pairs = new ArrayList<int[]>();
        for (int i = 1; i < indexes.size() + 1; i++){
            for (int j=i+1; j < indexes.size() + 1; j++){
                if (i !=j){
                    int[] indexPair = {i, j};
                    pairs.add(indexPair);
                }
            }
        }
        return pairs;
    }

    public KMeansResult calculate(List<Record> records, Set<Integer> attributes, int k, int l) {
        try {
            KMeansResult result = new KMeansResult();
            // randomly select K centroids
            List<Centroid> centroids = generateCentroids(k);
            List<Centroid> randomCentroids = generateCentroids(k);

            double previousCentroidDifference = centroidDistance(centroids, randomCentroids, attributes);
//            double previousCentroidDifference = Double.MIN_VALUE;
            double currentCentroidDifference = 0;
            // calculate distance for each data point
            // assign data points to centroids
            List<Centroid> lastAssignedCentroids = centroids;
            for (int i = 0; i < iterations; i++) {
                assignRecordsToCentroids(centroids, records, attributes,l);
                lastAssignedCentroids = centroids;
                // calculate average of the cluster and make them as new data point
                // do the same again until threshold satisfies
                List<Centroid> newCentroids = calculateAvgCentroid(centroids, records);

                // break if the centroids doesn't change
                currentCentroidDifference = centroidDistance(newCentroids, centroids, attributes);
                double difference = Math.abs(currentCentroidDifference - previousCentroidDifference);
                if (difference <= threshold * previousCentroidDifference) {
//                    System.out.println("KMeans finished in iterations: " + i + " with threshold difference: " + difference);
                    break;
                }
                previousCentroidDifference = currentCentroidDifference;
                centroids = newCentroids;
            }
            for (Centroid centroid : lastAssignedCentroids){
                centroid.getAssignedRecords().clear();
            }
            assignRecordsToCentroids(lastAssignedCentroids,records, attributes);
            result.setFinalCentroids(lastAssignedCentroids);
            result.setInitialRecords(records);
            printClasses(lastAssignedCentroids, records);
            return result;
        } catch (Exception e) {
            String s = "Error occurred while calculating KMeans algorithm";
            logger.error(s, e);
            throw new RuntimeException(s, e);
        }
    }

    public double centroidDistance(List<Centroid> centroids1, List<Centroid> centroids2, Set<Integer> attributeIndexes) {
        double sum = 0;
        for (int i = 0; i < centroids1.size(); i++) {
            Centroid c1 = centroids1.get(i);
            Centroid c2 = centroids2.get(i);

            double distance = euclideanDistance(c1.getRandomRecord().getAttributes(), c2.getRandomRecord().getAttributes(), attributeIndexes);
            sum += distance;
        }
        return sum;
    }

    public Set<Set<Integer>> getPowerSet(Set<Integer> originalSet) {
        Set<Set<Integer>> sets = new HashSet<Set<Integer>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<Integer>());
            return sets;
        }
        List<Integer> list = new ArrayList<Integer>(originalSet);
        Integer head = list.get(0);
        Set<Integer> rest = new HashSet<Integer>(list.subList(1, list.size()));
        for (Set<Integer> set : getPowerSet(rest)) {
            Set<Integer> newSet = new HashSet<Integer>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public double calculatePPV (List<Centroid> centroids, List<Record> records) {
        HashMap<Integer, HashMap<Integer, Integer>> centroidClassMap = new HashMap<Integer, HashMap<Integer, Integer>>();
        for (Centroid centroid : centroids) {
            List<Integer> assignedRecords = centroid.getAssignedRecords();
            HashMap<Integer, Integer> classCountMap = new HashMap<Integer, Integer>();
            for (Integer assignedRecord : assignedRecords) {
                Record record = records.get(assignedRecord);
                int dataClass = record.getDataClass();
                Integer count = classCountMap.get(dataClass);
                if (count == null) {
                    count = 0;
                }
                count++;
                classCountMap.put(dataClass, count);
            }
            centroidClassMap.put(centroid.getCentroidId(), classCountMap);
        }
        Integer totalTP = 0;
        Integer totalFP = 0;
        for (Integer centroidId : centroidClassMap.keySet()) {
            HashMap<Integer, Integer> classCountMap = centroidClassMap.get(centroidId);
            Integer maxCount = 0;
            Integer totalCount = 0;
            for (Integer classNumber : classCountMap.keySet()) {
                Integer class1Count = classCountMap.get(classNumber);
                totalCount += class1Count;
                if (class1Count > maxCount) {
                    maxCount = class1Count;
                }
            }
            totalTP += maxCount;
            totalFP += (totalCount - maxCount);
        }
        double ppval = (double) totalTP / (totalTP + totalFP);
//        System.out.println("Total TP : " + totalTP + " TotalTP + TotalFP : " + (totalTP + totalFP) + " PPV : " + ppval);
        return ppval;

    }

    public void printClasses(List<Centroid> centroids, List<Record> records) {
        for (int i = 0; i < centroids.size(); i++) {
            System.out.println("Centroid: " + i + " has " + centroids.get(i).getAssignedRecords().size());
        }
        for (int i = 0; i < records.size(); i++) {
//            System.out.println("Record: " + records.get(i).getScn() + " c = " + records.get(i).getDataClass() + " cent = " + records.get(i).getCentroidDistances() + " record attribute length: " + records.get(i).getAttributes().length);
        }
    }

    public void readConfigurations() throws Exception {
        try {
            k = Integer.valueOf(PropertyReader.getProperty(Constants.NUMBER_OF_CENTROIDS));
            if (k == 0) {
                logger.info("Number of centroids are not defined in the properties file. Hence using default value: " + Constants.DEFAULT_NUMBER_OF_CENTROIDS);
                k = Constants.DEFAULT_NUMBER_OF_CENTROIDS;
            }
            l = Integer.valueOf(PropertyReader.getProperty(Constants.NUMBER_OF_CENTROIDS_DATA_ASSIGNED));
//            if (l == 0 || l > k){
//                logger.info("L is not defined in the properties file or defined L is larger than K. Hence using default value: " + Constants.DEFAULT_L);
//                l = Constants.DEFAULT_L;
//            }
            threshold = Double.valueOf(PropertyReader.getProperty(Constants.THRESHHOLD));
            iterations = Integer.parseInt(PropertyReader.getProperty(Constants.ITERATIONS));
            vfoldBlocks = Integer.parseInt(PropertyReader.getProperty(Constants.VFOLD_VALIDATION_BLOCKS));

        } catch (Exception e) {
            logger.error("Error occurred while reading configuration file", e);
            throw new Exception("Error occurred while reading configuration file", e);
        }
    }

    public List<Record> getRecords(String filePath) throws Exception {
        List<Record> recordList = new ArrayList<Record>();
        try {
            InputStream is = new FileInputStream(filePath);
            // Construct BufferedReader from FileReader
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] lineSplits = line.split(",");
                if (lineSplits.length > totalAttributes) {
                    Record record = new Record(Integer.valueOf(lineSplits[0]), totalAttributes);
                    for (int i = 1; i < totalAttributes - 1; i++) {
                        record.setAttribute(i - 1, Integer.parseInt(lineSplits[i]));
                    }
                    record.setDataClass(Integer.valueOf(lineSplits[10]));
                    recordList.add(record);
                }
            }
            br.close();
            return recordList;
        } catch (Exception e) {
            logger.error("Error occurred while reading cleaned data file", e);
            throw new Exception("Error occurred while reading cleaned data file", e);
        }
    }

    public Map<Integer, List<Integer>> getAttributes (List<Record> records){
        Map<Integer, List<Integer>> attributeMap = new HashMap<Integer, List<Integer>>();
        List<Integer> attriA1 = new ArrayList<Integer>();
        List<Integer> attriA2 = new ArrayList<Integer>();
        List<Integer> attriA3 = new ArrayList<Integer>();
        List<Integer> attriA4 = new ArrayList<Integer>();
        List<Integer> attriA5 = new ArrayList<Integer>();
        List<Integer> attriA6 = new ArrayList<Integer>();
        List<Integer> attriA7 = new ArrayList<Integer>();
        List<Integer> attriA8 = new ArrayList<Integer>();
        List<Integer> attriA9 = new ArrayList<Integer>();
        for (Record record : records) {
            int[] attributes = record.getAttributes();
            attriA1.add(attributes[0]);
            attriA2.add(attributes[1]);
            attriA3.add(attributes[2]);
            attriA4.add(attributes[3]);
            attriA5.add(attributes[4]);
            attriA6.add(attributes[5]);
            attriA7.add(attributes[6]);
            attriA8.add(attributes[7]);
            attriA9.add(attributes[8]);
        }
        attributeMap.put(1, attriA1);
        attributeMap.put(2, attriA2);
        attributeMap.put(3, attriA3);
        attributeMap.put(4, attriA4);
        attributeMap.put(5, attriA5);
        attributeMap.put(6, attriA6);
        attributeMap.put(7, attriA7);
        attributeMap.put(8, attriA8);
        attributeMap.put(9, attriA9);
        return attributeMap;
    }

    public double euclideanDistance(int[] attrib1, int[] attrib2, Set<Integer> attributeIndexes) {
        double v = 0;
        for (int i : attributeIndexes) {
            v += Math.pow(((Integer) attrib1[i] - (Integer) attrib2[i]), 2);
        }
        return Math.sqrt(v);
    }

    public List<Centroid> generateCentroids(int k) throws Exception {
        // read number of centroids from properties file
        List<Centroid> randomCentroids = new ArrayList<Centroid>();
        Random random = new Random();
        for (int j = 0; j < k; j++) {
            Record record = new Record(0, totalAttributes);
            for (int i = 0; i < totalAttributes; i++) {
                int r = (int) (random.nextDouble() * 10);
                record.setAttribute(i, r);
            }
            Centroid centroid = new Centroid(record, j);
            randomCentroids.add(centroid);
        }
        return randomCentroids;
    }


    public void assignRecordsToCentroids(List<Centroid> centroidList, List<Record> recordList, Set<Integer> attributeIndexes) {
        for (int j = 0; j < recordList.size(); j++) {
            double[] distancesToEachCentroid = new double[centroidList.size()];
            Record record = recordList.get(j);
            int[] attributeList1 = record.getAttributes();
            for (int i = 0; i < centroidList.size(); i++) {
                Centroid centroid = centroidList.get(i);
                Record randomRecord = centroid.getRandomRecord();
                int[] attributeList2 = randomRecord.getAttributes();
                distancesToEachCentroid[i] = euclideanDistance(attributeList1, attributeList2, attributeIndexes);
            }
            Map<Integer, Double> indexDistanceMap = calculateMin(distancesToEachCentroid);
            for (Integer index : indexDistanceMap.keySet()){
                centroidList.get(index).addRecordToAssignedList(j);
                CentroidDistance centroidDistance = new CentroidDistance(index, indexDistanceMap.get(index));
                record.addCentroidDistance(centroidDistance);
            }
        }
    }

    // This need to be changed according to l
    public void assignRecordsToCentroids(List<Centroid> centroidList, List<Record> recordList, Set<Integer> attributeIndexes, int l) {
        for (int j = 0; j < recordList.size(); j++) {
            List<CentroidDistance> distancesToEachCentroid = new ArrayList<CentroidDistance>();
            Record record = recordList.get(j);
            record.getCentroidDistances().clear();
            int[] attributeList1 = record.getAttributes();
            for (int i = 0; i < centroidList.size(); i++) {
                Centroid centroid = centroidList.get(i);
                Record randomRecord = centroid.getRandomRecord();
                int[] attributeList2 = randomRecord.getAttributes();
                distancesToEachCentroid.add(new CentroidDistance(i, euclideanDistance(attributeList1, attributeList2, attributeIndexes)));
            }
            Collections.sort(distancesToEachCentroid);
            for (int i = 0; i < l; i++) {
                CentroidDistance centroidDistance = distancesToEachCentroid.get(i);
                int index = centroidDistance.centroidIndex;
                centroidList.get(index).addRecordToAssignedList(j);
                record.addCentroidDistance(centroidDistance);
            }
        }
    }

    public Map<Integer, Double> calculateMin(double[] distancesToEachCentroid) {
        Map<Integer, Double> distanceIndexMap = new HashMap<Integer, Double>();
        double min = distancesToEachCentroid[0];
        int index = 0;
        for (int k = 1; k < distancesToEachCentroid.length; k++) {
            if (distancesToEachCentroid[k] < min) {
                min = distancesToEachCentroid[k];
                index = k;
            }
        }
        distanceIndexMap.put(index, min);
        return distanceIndexMap;
    }

    public List<Centroid> calculateAvgCentroid(List<Centroid> centroidList, List<Record> recordList) {
        List<Centroid> newCentroidList = new ArrayList<Centroid>();

        for (int i = 0; i < centroidList.size(); i++) {
            Centroid centroid = new Centroid(new Record(0, totalAttributes), i);
            newCentroidList.add(centroid);
        }

        double []sum = new double[newCentroidList.size()];

        for (Record r : recordList) {
            double totalDistance = 0;
            List<CentroidDistance> centroidDistances = r.getCentroidDistances();
            for (CentroidDistance distance : centroidDistances){
                totalDistance += 1 / distance.getDistance();
            }
            for (CentroidDistance centroidDistance : centroidDistances) {
                Centroid c = newCentroidList.get(centroidDistance.getCentroidIndex());
                Record centroidRecord = c.getRandomRecord();
                double probability = (1 / centroidDistance.getDistance()) / totalDistance;

                int[] attrbs = centroidRecord.getAttributes();
                for (int j = 0; j < totalAttributes; j++) {
                    int d = r.getAttributes()[j];
                    // System.out.println(probability);
                    attrbs[j] = (int) (attrbs[j] + d * probability);
                }
                sum[centroidDistance.getCentroidIndex()] += probability;
            }
        }

        for (int i = 0; i < centroidList.size(); i++) {
            Centroid centroid = newCentroidList.get(i);
            Centroid oldCentroid = centroidList.get(i);
            if (oldCentroid.getAssignedRecords().size() != 0){
                Record r = centroid.getRandomRecord();
                int[] attrbs = r.getAttributes();
                for (int j = 0; j < totalAttributes; j++) {
                    int d = r.getAttributes()[j];
                    attrbs[j] = (int) (d / sum[i]);
                }
            }
        }

        return newCentroidList;
    }

    private class VFoldRecords {
        List<Record> train = new ArrayList<Record>();
        List<Record> test = new ArrayList<Record>();
    }

    public double vFoldCrossValidation (List<Record> recordList, Set<Integer> attributeIndexes){
        double ppv = 0;
        for (int i = 0; i < vfoldBlocks; i++) {
            VFoldRecords vFoldRecords = vFoldRecords(recordList, i, vfoldBlocks);
            KMeansResult result = calculate(vFoldRecords.train, attributeIndexes, k, l);

            List<Centroid> finalCentroids = result.getFinalCentroids();
            for (Centroid centroid : finalCentroids){
                centroid.getAssignedRecords().clear();
            }
            assignRecordsToCentroids(finalCentroids, vFoldRecords.test, attributeIndexes);
            ppv += calculatePPV(finalCentroids, vFoldRecords.test);
        }
        return ppv / vfoldBlocks;
    }

    private VFoldRecords vFoldRecords(List<Record> recordList, int index, int folds) {
        VFoldRecords vFoldRecords = new VFoldRecords();
        int foldSize = recordList.size() / folds;
        int start = index * foldSize;
        int end = start + foldSize > recordList.size() ? recordList.size() - 1 : start + foldSize - 1;

        for (int i = 0; i < recordList.size(); i++) {
            if (i >= start && i <= end) {
                vFoldRecords.test.add(recordList.get(i));
            } else {
                vFoldRecords.train.add(recordList.get(i));
            }
        }
        return vFoldRecords;
    }

}
