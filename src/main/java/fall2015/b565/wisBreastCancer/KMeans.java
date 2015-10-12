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

import fall2015.b565.wisBreastCancer.utils.Constants;
import fall2015.b565.wisBreastCancer.utils.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {
    private static final Logger logger = LoggerFactory.getLogger(KMeans.class);
    private static int k;
    public static void main(String[] args) throws Exception{
        KMeans kMeans = new KMeans();
        try {
            // read cleaned data file
            kMeans.readConfigurations();
            // make data structure of attributes
            List<Record> records = kMeans.getRecords();
            // randomly select K centroids
            List<Centroid> centroids = kMeans.generateCentroids(k);
            // calculate distance for each data point
            // assign data points to centroids
            kMeans.assignRecordsToCentroids(centroids, records);

        } catch (Exception e) {
            logger.error("Error occurred while calculating KMeans algorithm", e);
            throw new Exception("Error occurred while calculating KMeans algorithm", e);
        }

        // calculate average of the cluster and make them as new data point
        // do the same again until threshold satisfies
    }

    public void readConfigurations() throws Exception{
        try {
            k = Integer.valueOf(PropertyReader.getProperty(Constants.NUMBER_OF_CENTROIDS));
            if (k == 0) {
                logger.info("Number of centroids are not defined in the properties file. Hence using default value: " + Constants.DEFAULT_NUMBER_OF_CENTROIDS);
                k = Constants.DEFAULT_NUMBER_OF_CENTROIDS;
            }
        } catch (Exception e) {
            logger.error("Error occurred while reading configuration file", e);
            throw new Exception("Error occurred while reading configuration file", e);
        }
    }

    public List<Record> getRecords () throws Exception{
        List<Record> recordList = new ArrayList<Record>();
        try {
            InputStream is = FileReader.class.getClassLoader().getResourceAsStream(Constants.CLEANED_DATA_FILE_NAME);
            // Construct BufferedReader from FileReader
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] lineSplits = line.split(",");
                if (lineSplits.length > 10){
                    Record record = new Record(Integer.valueOf(lineSplits[0]), 9);
                    for (int i = 1; i < lineSplits.length- 1; i++) {
                        record.setAttribute(i - 1, lineSplits[i]);
                    }
                    record.setDataClass(Integer.valueOf(lineSplits[10]));
                    recordList.add(record);
                }
            }
            br.close();
            return recordList;
        }catch (Exception e){
            logger.error("Error occurred while reading cleaned data file", e);
            throw new Exception("Error occurred while reading cleaned data file", e);
        }
    }

    public double euclideanDistance(Object[] attrib1, Object[] attrib2) {
        double v = 0;
        for (int i = 0; i < attrib1.length; i++){
            v += Math.pow(((Integer) attrib1[i] - (Integer) attrib2[i]), 2);
        }
        return Math.sqrt(v);
    }

    public List<Centroid> generateCentroids (int k) throws Exception{
        // read number of centroids from properties file
        List<Centroid> randomCentroids = new ArrayList<Centroid>();
        Random random = new Random();
        for (int j = 0; j < k; j++) {
            Record record = new Record(0, 9);
            for (int i = 0; i < 9; i++) {
                int r = (int) (random.nextDouble() * 10);
                record.setAttribute(i, r);
            }
            Centroid centroid = new Centroid(record, j);
            randomCentroids.add(centroid);
        }
        return randomCentroids;
    }



    public void assignRecordsToCentroids (List<Centroid> centroidList, List<Record> recordList){

            for (int j =0; j < recordList.size(); j++) {
                double[] distancesToEachCentroid = new double[centroidList.size()];
                Record record = recordList.get(j);
                Object[] attributeList1 = record.getAttributes();
                for (int i = 0; i < centroidList.size(); i++) {
                    Centroid centroid = centroidList.get(i);
                    Record randomRecord = centroid.getRandomRecord();
                    Object[] attributeList2 = randomRecord.getAttributes();
                    distancesToEachCentroid[i] = euclideanDistance(attributeList1, attributeList2);
                }
                int index = calculateMin(distancesToEachCentroid);
                centroidList.get(index).addRecordToAssignedList(j);
            }
    }

    public int calculateMin(double[] distancesToEachCentroid) {
        double min = distancesToEachCentroid[0];
        int index = 0;
        for (int k=1; k<distancesToEachCentroid.length; k++){
            if (distancesToEachCentroid[k] < min){
                min = distancesToEachCentroid[k];
                index = k;
            }
        }
        return index;
    }
}
