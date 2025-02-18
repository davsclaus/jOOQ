/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package org.jooq.meta.postgres;

import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.nvl;
import static org.jooq.impl.DSL.when;
import static org.jooq.meta.postgres.information_schema.Tables.COLUMNS;
import static org.jooq.meta.postgres.pg_catalog.Tables.PG_ATTRIBUTE;
import static org.jooq.meta.postgres.pg_catalog.Tables.PG_CLASS;
import static org.jooq.meta.postgres.pg_catalog.Tables.PG_DESCRIPTION;
import static org.jooq.meta.postgres.pg_catalog.Tables.PG_NAMESPACE;
import static org.jooq.tools.StringUtils.defaultString;
import static org.jooq.util.postgres.PostgresDSL.oid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.meta.AbstractTableDefinition;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.DataTypeDefinition;
import org.jooq.meta.DefaultColumnDefinition;
import org.jooq.meta.DefaultDataTypeDefinition;
import org.jooq.meta.SchemaDefinition;

/**
 * @author Lukas Eder
 */
public class PostgresTableDefinition extends AbstractTableDefinition {

    public PostgresTableDefinition(SchemaDefinition schema, String name, String comment) {
        super(schema, name, comment);
    }

    @Override
    public List<ColumnDefinition> getElements0() throws SQLException {
        List<ColumnDefinition> result = new ArrayList<>();

        Field<String> dataType = COLUMNS.DATA_TYPE;





        for (Record record : create().select(
                COLUMNS.COLUMN_NAME,
                COLUMNS.ORDINAL_POSITION,
                dataType,

                // [#8067] A more robust / sophisticated decoding might be available
                nvl(
                    COLUMNS.CHARACTER_MAXIMUM_LENGTH,
                    when(COLUMNS.UDT_NAME.eq(inline("_varchar")), PG_ATTRIBUTE.ATTTYPMOD.sub(inline(4)))).as(COLUMNS.CHARACTER_MAXIMUM_LENGTH),
                COLUMNS.NUMERIC_PRECISION,
                COLUMNS.NUMERIC_SCALE,
                COLUMNS.IS_NULLABLE,
                COLUMNS.COLUMN_DEFAULT,
                COLUMNS.UDT_SCHEMA,
                COLUMNS.UDT_NAME,
                PG_DESCRIPTION.DESCRIPTION)
            .from(COLUMNS)
            .join(PG_NAMESPACE)
                .on(COLUMNS.TABLE_SCHEMA.eq(PG_NAMESPACE.NSPNAME))
            .join(PG_CLASS)
                .on(PG_CLASS.RELNAME.eq(COLUMNS.TABLE_NAME))
                .and(PG_CLASS.RELNAMESPACE.eq(oid(PG_NAMESPACE)))
            .join(PG_ATTRIBUTE)
                .on(PG_ATTRIBUTE.ATTRELID.eq(oid(PG_CLASS)))
                .and(PG_ATTRIBUTE.ATTNAME.eq(COLUMNS.COLUMN_NAME))
            .leftOuterJoin(PG_DESCRIPTION)
                .on(PG_DESCRIPTION.OBJOID.eq(oid(PG_CLASS)))
                .and(PG_DESCRIPTION.OBJSUBID.eq(COLUMNS.ORDINAL_POSITION))
            .where(COLUMNS.TABLE_SCHEMA.equal(getSchema().getName()))
            .and(COLUMNS.TABLE_NAME.equal(getName()))



            .orderBy(COLUMNS.ORDINAL_POSITION)
            .fetch()) {

            SchemaDefinition typeSchema = null;

            String schemaName = record.get(COLUMNS.UDT_SCHEMA);
            if (schemaName != null)
                typeSchema = getDatabase().getSchema(schemaName);

            DataTypeDefinition type = new DefaultDataTypeDefinition(
                getDatabase(),
                typeSchema,
                record.get(dataType),
                record.get(COLUMNS.CHARACTER_MAXIMUM_LENGTH),
                record.get(COLUMNS.NUMERIC_PRECISION),
                record.get(COLUMNS.NUMERIC_SCALE),
                record.get(COLUMNS.IS_NULLABLE, boolean.class),
                record.get(COLUMNS.COLUMN_DEFAULT),
                name(
                    record.get(COLUMNS.UDT_SCHEMA),
                    record.get(COLUMNS.UDT_NAME)
                )
            );

            ColumnDefinition column = new DefaultColumnDefinition(
                getDatabase().getTable(getSchema(), getName()),
                record.get(COLUMNS.COLUMN_NAME),
                record.get(COLUMNS.ORDINAL_POSITION, int.class),
                type,
                defaultString(record.get(COLUMNS.COLUMN_DEFAULT)).trim().toLowerCase().startsWith("nextval"),
                record.get(PG_DESCRIPTION.DESCRIPTION)
            );

            result.add(column);
        }

        return result;
    }
}
