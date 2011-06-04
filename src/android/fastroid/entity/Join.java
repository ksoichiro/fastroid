/*
 * Copyright (c) 2011 Soichiro Kashima
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package android.fastroid.entity;

/**
 * Information about joining tables.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/31
 */
public final class Join {
    /**
     * Defines the types of the joins.
     * 
     * @author Soichiro Kashima
     * @since 2011/05/31
     */
    public enum JoinType {
        /** Expresses the inner join. */
        INNER_JOIN,
        /** Expresses the left outer join. */
        LEFT_OUTER_JOIN;
    }

    /** Type of this join. */
    private JoinType mType;

    /** CLass of the table to join. */
    private Class<?> mTableClass;

    /** Table name to join. */
    private String mTableName;

    /** Field name of the column to join. */
    private String mFieldName;

    /** Column name to join. */
    private String mColumnName;

    /** Additinal condition clause. */
    private String mAdditionalCondClause;

    /** Arugments of the additional condition clause. */
    private String[] mAdditionalCondArgs;

    /**
     * Returns the type of this join.
     * 
     * @return type of this join
     */
    public JoinType getType() {
        return mType;
    }

    /**
     * Set the type of this join.
     * 
     * @param type type of this join
     */
    public void setType(final JoinType type) {
        mType = type;
    }

    /**
     * Returs the table class to join.
     * 
     * @return table class to join
     */
    public Class<?> getTableClass() {
        return mTableClass;
    }

    /**
     * Set the class of the table to join.
     * 
     * @param tableClass table class to join
     */
    public void setTableClass(final Class<?> tableClass) {
        mTableClass = tableClass;
    }

    /**
     * Returns the table name to join.
     * 
     * @return table name to join
     */
    public String getTableName() {
        return mTableName;
    }

    /**
     * Set the table name to join.
     * 
     * @param tableName table name to join
     */
    public void setTableName(final String tableName) {
        mTableName = tableName;
    }

    /**
     * Returns the field name of the column to join.
     * 
     * @return column name to join
     */
    public String getFieldName() {
        return mFieldName;
    }

    /**
     * Set the field name of the column to join.
     * 
     * @param fieldName field name of the column to join
     */
    public void setFieldName(final String fieldName) {
        mFieldName = fieldName;
    }

    /**
     * Returns the column name to join.
     * 
     * @return column name to join
     */
    public String getColumnName() {
        return mColumnName;
    }

    /**
     * Set the column name to join.
     * 
     * @param columnName column name to join
     */
    public void setColumnName(final String columnName) {
        mColumnName = columnName;
    }

    /**
     * Returns the additional condition clause.
     * 
     * @return additional condition clause
     */
    public String getAdditionalCondClause() {
        return mAdditionalCondClause;
    }

    /**
     * Set the additional condition clause.
     * 
     * @param additionalCondClause additional condition clause to set
     */
    public void setAdditionalCondClause(final String additionalCondClause) {
        mAdditionalCondClause = additionalCondClause;
    }

    /**
     * Returns the arguments of the additional condition clause.
     * 
     * @return arguments of the additional condition clause
     */
    public String[] getAdditionalCondArgs() {
        return mAdditionalCondArgs;
    }

    /**
     * Set the arguments of the additional condition clause.
     * 
     * @param additionalCondArgs arguments of the additional condition clause to
     *            set.
     */
    public void setAdditionalCondArgs(final String[] additionalCondArgs) {
        mAdditionalCondArgs = additionalCondArgs;
    }

}
