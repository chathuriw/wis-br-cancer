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

import java.util.ArrayList;
import java.util.List;

public class Record {
    private int scn;
    private int dataClass;
    private List<CentroidDistance> centroidDistances = new ArrayList<CentroidDistance>();

    private int[] attributes;

    public Record(int scn, int noOfAttributes) {
        attributes = new int[noOfAttributes];
        for (int i = 0; i < noOfAttributes; i++) {
            attributes[i] = 0;
        }
        this.scn = scn;
    }

    public void setAttribute(int index, int value) {
        attributes[index] = value;
    }

    public int getScn() {
        return scn;
    }

    public int[] getAttributes() {
        return attributes;
    }

    public int getDataClass() {
        return dataClass;
    }

    public void setDataClass(int dataClass) {
        this.dataClass = dataClass;
    }

    public List<CentroidDistance> getCentroidDistances() {
        return centroidDistances;
    }

    public void addCentroidDistance(CentroidDistance centroidDistance) {
        this.centroidDistances.add(centroidDistance);
    }
}
