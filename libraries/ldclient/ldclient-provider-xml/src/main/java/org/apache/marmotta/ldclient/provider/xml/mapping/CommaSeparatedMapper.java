/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldclient.provider.xml.mapping;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Takes a comma separated list and converts it into a list of literals
 * <p/>
 * Author: Sebastian Schaffert
 */
public class CommaSeparatedMapper extends XPathLiteralMapper {

    public CommaSeparatedMapper(String xpath) {
        super(xpath);
    }

    public CommaSeparatedMapper(String xpath, Map<String, String> namespaces) {
        super(xpath, namespaces);
    }

    public CommaSeparatedMapper(String xpath, String datatype) {
        super(xpath, datatype);
    }

    public CommaSeparatedMapper(String xpath, Map<String, String> namespaces, String datatype) {
        super(xpath, namespaces, datatype);
    }

    /**
     * Take the selected value, process it according to the mapping definition, and create Sesame Values using the
     * factory passed as argument.
     *
     *
     * @param resourceUri
     * @param selectedValue
     * @param factory
     * @return
     */
    @Override
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory) {
        String[] values = selectedValue.split(",");
        List<Value> result = new ArrayList<Value>(values.length);
        for(String value : values) {
            if(datatype != null) {
                result.add(factory.createLiteral(value.trim(), factory.createURI(Namespaces.NS_XSD + datatype)));
            } else {
                result.add(factory.createLiteral(value.trim()));
            }
        }

        return result;
    }
}
