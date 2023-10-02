/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.apache.wayang.api.sql.calcite.converter;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.wayang.api.sql.calcite.rel.WayangTableScan;
import org.apache.wayang.api.sql.calcite.utils.ModelParser;
import org.apache.wayang.api.sql.sources.fs.JavaCSVTableSource;
import org.apache.wayang.core.plan.wayangplan.Operator;
import org.apache.wayang.core.types.DataSetType;
import org.apache.wayang.postgres.operators.PostgresTableSource;
import org.apache.wayang.basic.data.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


//TODO: create tablesource with column types
//TODO: support other sources
public class WayangTableScanVisitor extends WayangRelNodeVisitor<WayangTableScan> {
    WayangTableScanVisitor(WayangRelConverter wayangRelConverter) {
        super(wayangRelConverter);
    }

    @Override
    Operator visit(WayangTableScan wayangRelNode) {

        String tableName = wayangRelNode.getTableName();
        List<String> columnNames = wayangRelNode.getColumnNames();

        // Get the source platform for this table
        String tableSource = wayangRelNode.getTable().getQualifiedName().get(0);
        if (tableSource.equals("postgres")) {
            return new PostgresTableSource(tableName, columnNames.toArray(new String[]{}));
        }
        if (tableSource.equals("fs")) {
            ModelParser modelParser;
            try {
                modelParser = new ModelParser();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            RelDataType rowType = wayangRelNode.getRowType();
            List<RelDataType> fieldTypes = new ArrayList<>();
            for (RelDataTypeField field : rowType.getFieldList()) {
                fieldTypes.add(field.getType());
            }
            String url = String.format("file:/%s/%s.csv", modelParser.getFsPath(), wayangRelNode.getTableName());

            String separator = modelParser.getSeparator();

            if (Objects.equals(separator, "")) {
                return new JavaCSVTableSource(url,
                        DataSetType.createDefault(Record.class), fieldTypes);
            } else {
                return new JavaCSVTableSource(url,
                        DataSetType.createDefault(Record.class), fieldTypes, separator.charAt(0));
            }
        } else throw new RuntimeException("Source not supported");
    }
}
