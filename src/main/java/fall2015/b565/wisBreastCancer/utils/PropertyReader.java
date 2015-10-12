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

import fall2015.b565.wisBreastCancer.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private static final Logger logger = LoggerFactory.getLogger(PropertyReader.class);
    private static Properties properties = new Properties();
    public static void  loadProperties() throws Exception {
        try {
            InputStream is = FileReader.class.getClassLoader().getResourceAsStream(Constants.KMEANS_PROPERTIES_FILE);
            properties.load(is);
        }catch (Exception e){
            logger.error("Error while loading the properties file", e);
            throw new Exception("Error while loading the properties file", e);
        }
    }

    public static String getProperty (String propertyName) throws Exception {
        loadProperties();
        return properties.getProperty(propertyName);
    }
}
