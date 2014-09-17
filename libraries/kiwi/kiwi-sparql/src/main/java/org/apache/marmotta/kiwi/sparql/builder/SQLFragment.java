/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.sparql.builder;

import org.openrdf.query.algebra.ValueExpr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An SQL fragment is a part of the SQL query where all patterns are joinedwith  INNER JOINS and not LEFT JOINS. Several
 * patterns are then joined using a left join.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLFragment {

    /**
     * Indicate where the fragment's conditions should be placed (ON part of the JOIN clause or WHERE part of the query).
     * This distinction is necessary when OPTIONAL constructs are used, i.e. the created SQL uses LEFT JOINs. We cannot
     * always place it in JOIN conditions, because the first pattern will not have a JOIN.
     */
    public static enum ConditionPosition {
        JOIN, WHERE
    };


    /**
     * The patterns contained in this fragment. All patterns are joined using an INNER JOIN.
     */
    private List<SQLPattern> patterns;

    private List<String> conditions;

    private List<ValueExpr> filters;

    private ConditionPosition conditionPosition = ConditionPosition.JOIN;

    public SQLFragment() {
        this.patterns   = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.filters    = new ArrayList<>();
    }

    public List<SQLPattern> getPatterns() {
        return patterns;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public List<ValueExpr> getFilters() {
        return filters;
    }


    /**
     * Indicate where the fragment's conditions should be placed (ON part of the JOIN clause or WHERE part of the query).
     * For the first fragment in a query this will always be WHERE, while for all other fragments it should be JOIN. Note
     * that JOIN is strictly necessary for all fragments that are OPTIONAL.
     */
    public void setConditionPosition(ConditionPosition conditionPosition) {
        this.conditionPosition = conditionPosition;
    }

    public ConditionPosition getConditionPosition() {
        return conditionPosition;
    }

    /**
     * Build the FROM clause by joining together all patterns appropriately and adding the filter conditions
     * @return
     */
    public String buildFromClause() {
        StringBuilder fromClause = new StringBuilder();

        for (Iterator<SQLPattern> it = patterns.iterator(); it.hasNext(); ) {

            SQLPattern p = it.next();


            StringBuilder conditionClause = new StringBuilder();

            // in case we add the condition to the JOIN, build first the conditions for the pattern; otherwise, the
            // conditions for the pattern will be added to the WHERE clause
            if(conditionPosition == ConditionPosition.JOIN) {
                conditionClause.append(p.buildConditionClause());
            }


            // in case the pattern is the last of the fragment, also add the filter conditions of the fragment (TODO: verify this does indeed the right thing)
            if(conditionPosition == ConditionPosition.JOIN && !it.hasNext()) {
                // if this is the last pattern of the fragment, add the filter conditions
                for(Iterator<String> cit = getConditions().iterator(); cit.hasNext(); ) {
                    if(conditionClause.length() > 0) {
                        conditionClause.append("\n       AND ");
                    }
                    conditionClause.append(cit.next());
                }
            }


            // when the pattern builds a join with the nodes table and we have fragment-wide conditions, we need to
            // wrap the pattern's from clause in parentheses
            if(conditionClause.length() > 0) {
                if(p.hasJoinFields())
                    fromClause.append("(");
                fromClause.append(p.buildFromClause());
                if(p.hasJoinFields())
                    fromClause.append(")");
                fromClause.append(" ON (");
                fromClause.append(conditionClause);
                fromClause.append(")");

            } else {
                fromClause.append(p.buildFromClause());
            }


            if (it.hasNext()) {
                if(conditionPosition == ConditionPosition.JOIN) {
                    fromClause.append("\n JOIN \n  ");
                } else {
                    fromClause.append("\n CROSS JOIN \n  ");
                }
            }
        }

        return fromClause.toString();
    }

    /**
     * Build the combined condition clause for this fragment. This will be the empty string when the conditionPosition is JOIN.
     * @return
     */
    public String buildConditionClause() {
        StringBuilder conditionClause = new StringBuilder();

        if(conditionPosition == ConditionPosition.WHERE) {
            for (Iterator<SQLPattern> it = patterns.iterator(); it.hasNext(); ) {
                SQLPattern p = it.next();

                // in case we add the condition to the JOIN, build first the conditions for the pattern; otherwise, the
                // conditions for the pattern will be added to the WHERE clause
                if(conditionClause.length() > 0) {
                    conditionClause.append("\n       AND ");
                }
                conditionClause.append(p.buildConditionClause());

            }

            // in case the pattern is the last of the fragment, also add the filter conditions of the fragment
            // if this is the last pattern of the fragment, add the filter conditions
            for(Iterator<String> cit = getConditions().iterator(); cit.hasNext(); ) {
                if(conditionClause.length() > 0) {
                    conditionClause.append("\n       AND ");
                }
                conditionClause.append(cit.next());
            }
        }

        return conditionClause.toString();
    }
}
