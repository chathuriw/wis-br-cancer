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

import java.util.List;
import java.util.Map;
import java.util.Set;

public class KMeansResult {
    private List<Centroid> finalCentroids;
    private List<Record> initialRecords;
    private Map<Double, Set<Integer>> ppvs;

    public List<Centroid> getFinalCentroids() {
        return finalCentroids;
    }

    public void setFinalCentroids(List<Centroid> finalCentroids) {
        this.finalCentroids = finalCentroids;
    }

    public List<Record> getInitialRecords() {
        return initialRecords;
    }

    public void setInitialRecords(List<Record> initialRecords) {
        this.initialRecords = initialRecords;
    }

    public Map<Double, Set<Integer>> getPpvs() {
        return ppvs;
    }

    public void setPpvs(Map<Double, Set<Integer>> ppvs) {
        this.ppvs = ppvs;
    }
}
