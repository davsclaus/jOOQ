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
package org.jooq;

/**
 * A JSON wrapper type for JSON data obtained from the database.
 * <p>
 * The wrapper represents JSON {@link #data()} in serialised string form. A
 * <code>CAST(NULL AS JSON)</code> value is represented by a <code>null</code>
 * reference of type {@link JSON}, not as <code>data() == null</code>. This is
 * consistent with jOOQ's general way of returning <code>NULL</code> from
 * {@link Result} and {@link Record} methods.
 */
public final class JSON {

    private final String data;

    private JSON(String data) {
        this.data = data;
    }

    public final String data() {
        return data;
    }

    public static final JSON valueOf(String data) {
        return new JSON(data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JSON other = (JSON) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        }
        else if (!data.equals(other.data))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }
}
