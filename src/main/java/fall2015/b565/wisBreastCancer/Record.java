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

public class Record {
    private int scn;
    private int dataClass;

    private Object[] attributes;

    public Record(int scn, int noOfAttributes) {
        attributes = new Object[noOfAttributes];
        this.scn = scn;
    }

    public void setAttribute(int index, Object value) {
        attributes[index] = value;
    }

    public int getScn() {
        return scn;
    }

    public Object[] getAttributes() {
        return attributes;
    }

    public int getDataClass() {
        return dataClass;
    }

    public void setDataClass(int dataClass) {
        this.dataClass = dataClass;
    }
}
