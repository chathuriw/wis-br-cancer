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

package fall2015.b565.wisBreastCancer.utils;

public class Constants {
    public static final String DATA_FILE_NAME = "breast-cancer-wisconsin.data.txt";
    public static final String CLEANED_DATA_FILE_NAME = "breast-cancer-wisconsin_cleaned.data.txt";
    public static final String CLEANED_DATA_FILE_PATH = "src/main/resources/" + CLEANED_DATA_FILE_NAME;
    public static final String KMEANS_PROPERTIES_FILE = "kmeans.properties";
    public static final String NUMBER_OF_CENTROIDS = "number.of.centroids";
    public static final int DEFAULT_NUMBER_OF_CENTROIDS = 2;
    public static final String THRESHHOLD = "threshold";
    public static final String ITERATIONS = "iterations";
    public static final Integer BENIGN = 2;
    public static final Integer MALIGNANT = 4;
    public static final Integer ATTR_SCN = 1;
    public static final Integer ATTR_CLUMP_THICKNESS = 2;
    public static final Integer ATTR_CELL_SIZE = 3;
    public static final Integer ATTR_CELL_SHAPE = 4;
    public static final Integer ATTR_MARGINAL_ADHESION = 5;
    public static final Integer ATTR_SINGLE_CELL_SIZE = 6;
    public static final Integer ATTR_BARE_NUCLEI = 7;
    public static final Integer ATTR_BLAND_CHROMATION = 8;
    public static final Integer ATTR_NORMAL_NUCLEOLI = 9;
    public static final Integer ATTR_MITOSES = 10;
    public static final Integer ATTR_CLASS = 11;


}
