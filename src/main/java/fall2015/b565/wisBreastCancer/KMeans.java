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
import fall2015.b565.wisBreastCancer.utils.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class KMeans {
    private static final Logger logger = LoggerFactory.getLogger(KMeans.class);
    private static int k;
    private static double threshold = .1;
    private static int iterations = 10;
    private static int[] allAttributeHeaders = {0,1,2,3,4,5,6,7,8};
    private static int totalAttributes = 9;

    public static void main(String[] args) throws Exception {
        KMeans kMeans = new KMeans();
        Set<Set<Integer>> kMeansPowerSet = kMeans.getPowerSet(new HashSet<Integer>(Ints.asList(allAttributeHeaders)));
        Map<Double, Set<Integer>> ppvMap = new HashMap<Double, Set<Integer>>();
        for (Set<Integer> set : kMeansPowerSet){
            if (set.size() != 0) {
                KMeansResult result = kMeans.calculate(set);
                double ppv = kMeans.getPPV(result);
                ppvMap.put(ppv, set);
            }
        }
        Double maxPPV = 0.0;
        for (Double ppv : ppvMap.keySet()){
            if (ppv > maxPPV){
                maxPPV = ppv;
            }
        }
        Set<Integer> attributeSet = ppvMap.get(maxPPV);
        System.out.println("Max PPV : " + maxPPV + " attribute Set : " + attributeSet.toString());
    }

    public KMeansResult calculate(Set<Integer> attributes) {
        try {
            KMeansResult result = new KMeansResult();
            // read cleaned data file
            readConfigurations();
            // make data structure of attributes
            List<Record> records = getRecords();
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
                assignRecordsToCentroids(centroids, records, attributes);
                lastAssignedCentroids = centroids;
                // calculate average of the cluster and make them as new data point
                // do the same again until threshold satisfies
                List<Centroid> newCentroids = calculateAvgCentroid(centroids, records);

                // break if the centroids doesn't change
                currentCentroidDifference = centroidDistance(newCentroids, centroids, attributes);
                double difference = Math.abs(currentCentroidDifference - previousCentroidDifference);
                if (difference <= threshold * previousCentroidDifference) {
                    System.out.println("KMeans finished in iterations: " + i + " with threshold difference: " + difference);
                    break;
                }
                previousCentroidDifference = currentCentroidDifference;
                centroids = newCentroids;
            }
            result.setFinalCentroids(lastAssignedCentroids);
            result.setInitialRecords(records);
            return result;
//            printClasses(lastAssignedCentroids, records);
        } catch (Exception e) {
            String s = "Error occurred while calculating KMeans algorithm";
            logger.error(s, e);
            throw new RuntimeException(s, e);
        }
    }

    private double getPPV(KMeansResult result) throws Exception {
        List<Centroid> lastAssignedCentroids = result.getFinalCentroids();
        List<Record> records = result.getInitialRecords();
        PPV ppv = calculatePPV(lastAssignedCentroids, records);
        System.out.println("PPV val : " + ppv.getPpv());
        return ppv.getPpv();
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

    public PPV calculatePPV (List<Centroid> centroids, List<Record> records) throws Exception {
        PPV ppv = new PPV();
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
            ppv.setCentroidId(centroidId);
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
        System.out.println("totalTp : " + totalTP + " total count : " + (totalTP + totalFP) + " ppv : " + ppval);
        ppv.setPpv(ppval);
        return ppv;

    }

    public void printClasses(List<Centroid> centroids, List<Record> records) {
        for (int i = 0; i < centroids.size(); i++) {
            System.out.println("Centroid: " + i + " has " + centroids.get(i).getAssignedRecords().size());
        }
        for (int i = 0; i < records.size(); i++) {
            System.out.println("Record: " + records.get(i).getScn() + " c = " + records.get(i).getDataClass() + " cent = " + records.get(i).getCentroidIndex() + " record attribute length: " + records.get(i).getAttributes().length);
        }
    }

    public void readConfigurations() throws Exception {
        try {
            k = Integer.valueOf(PropertyReader.getProperty(Constants.NUMBER_OF_CENTROIDS));
            if (k == 0) {
                logger.info("Number of centroids are not defined in the properties file. Hence using default value: " + Constants.DEFAULT_NUMBER_OF_CENTROIDS);
                k = Constants.DEFAULT_NUMBER_OF_CENTROIDS;
            }
            threshold = Double.valueOf(PropertyReader.getProperty(Constants.THRESHHOLD));
            iterations = Integer.parseInt(PropertyReader.getProperty(Constants.ITERATIONS));

        } catch (Exception e) {
            logger.error("Error occurred while reading configuration file", e);
            throw new Exception("Error occurred while reading configuration file", e);
        }
    }

    public List<Record> getRecords() throws Exception {
        List<Record> recordList = new ArrayList<Record>();
        try {
            InputStream is = FileReader.class.getClassLoader().getResourceAsStream(Constants.CLEANED_DATA_FILE_NAME);
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
            int index = calculateMin(distancesToEachCentroid);
            centroidList.get(index).addRecordToAssignedList(j);
            record.setCentroidIndex(index);
        }
    }

    public int calculateMin(double[] distancesToEachCentroid) {
        double min = distancesToEachCentroid[0];
        int index = 0;
        for (int k = 1; k < distancesToEachCentroid.length; k++) {
            if (distancesToEachCentroid[k] < min) {
                min = distancesToEachCentroid[k];
                index = k;
            }
        }
        return index;
    }

    public List<Centroid> calculateAvgCentroid(List<Centroid> centroidList, List<Record> recordList) {
        List<Centroid> newCentroidList = new ArrayList<Centroid>();

        for (int i = 0; i < centroidList.size(); i++) {
            Centroid centroid = new Centroid(new Record(0, totalAttributes), i);
            newCentroidList.add(centroid);
        }

        for (int i = 0; i < recordList.size(); i++) {
            Record r = recordList.get(i);
            Centroid c = newCentroidList.get(r.getCentroidIndex());
            Record centroidRecord = c.getRandomRecord();

            int[] attrbs = centroidRecord.getAttributes();
            for (int j = 0; j < totalAttributes; j++) {
                int d = r.getAttributes()[j];
                attrbs[j] = (attrbs[j] + d);
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
                    attrbs[j] = d / oldCentroid.getAssignedRecords().size();
                }
            }

        }

        return newCentroidList;
    }
}
