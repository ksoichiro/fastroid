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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.fastroid.entity.Join.JoinType;
import android.fastroid.entity.annotation.Column;
import android.fastroid.entity.annotation.Id;
import android.fastroid.entity.annotation.JoinColumn;
import android.fastroid.entity.annotation.ManyToOne;
import android.fastroid.entity.annotation.OneToMany;
import android.fastroid.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simplifies the database operations.<br>
 * TODO copy method from existing instance.<br>
 * TODO upsert method<br>
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class DatabaseManager {
    /**
     * Represents the type of the database operation.
     */
    private enum ProcessType {
        /** SELECT operation. */
        SELECT,
        /** INSERT operation. */
        INSERT,
        /** UPDATE operation. */
        UPDATE,
        /** DELETE operation. */
        DELETE;
    }

    /** SQL operation type of this instance. */
    private ProcessType mProcessType;

    /** Helper instance of this instance to execute SQL operation. */
    private SQLiteOpenHelper mHelper;

    /** Target object of the operations. */
    private Object mTarget;

    /** Map of the values to update. */
    private ContentValues mContentValues;

    /** Taples of the names of the column and values for WHERE clause. */
    private Map<String, String> mWhereClauseMap;

    /** Tables to join. */
    private List<Join> mJoinList;

    /** WHERE clause. */
    private String mWhereClause;

    /** Arguments of the WHERE clause. */
    private String[] mWhereArgs;

    /** GROUP BY clause. */
    private String mGroupByClause;

    /** HAVING clause. */
    private String mHavingClause;

    /** ORDER BY clause. */
    private String mOrderByClause;

    /**
     * Creates a {@code DatabaseManager}.<br>
     * To prepare database operations certainly, this is only accessible inside
     * this class.
     * <p>
     * The method {@link #execute()} cannot be called before execute the
     * preparation methods such as {@link #insert(SQLiteOpenHelper, Object)}.
     * 
     * @param helper db helper to access the target table
     * @param target the object which has values to database operations
     */
    private DatabaseManager(final SQLiteOpenHelper helper, final Object target) {
        mTarget = target;
        mHelper = helper;
        mContentValues = new ContentValues();
        mWhereClause = null;
        mWhereArgs = null;
        mWhereClauseMap = new LinkedHashMap<String, String>();
        mGroupByClause = null;
        mHavingClause = null;
        mOrderByClause = null;
        mJoinList = new ArrayList<Join>();
    }

    /**
     * Prepares to select from tables specifying an entity object.
     * 
     * @param helper db helper to access the target table
     * @param target object which has conditions to select
     * @return database manager
     */
    public static DatabaseManager select(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.SELECT;
        return manager;
    }

    /**
     * Prepares to insert to tables specifying an entity object.
     * <p>
     * Call {@link #execute()} to execute insert after calling this method.
     * 
     * @param helper db helper to access the target table
     * @param target object which has values to insert
     * @return database manager
     */
    public static DatabaseManager insert(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.INSERT;
        // Creates db columns by fields
        Field[] fields = target.getClass().getFields();
        for (Field field : fields) {
            // Skip non-column fields
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            // Skip auto-increment columns
            Id id = field.getAnnotation(Id.class);
            if (id != null && id.autoIncrement()) {
                continue;
            }
            final String columnName = manager.toDbName(field.getName());
            final String valueString = manager.getFieldValueAsString(field, target);
            manager.mContentValues.put(columnName, valueString);
        }
        return manager;
    }

    /**
     * Prepares to update tables.<br>
     * <p>
     * Call {@link #execute()} to execute update after calling this method.
     * 
     * @param helper db helper to access the target table
     * @param target object which has values to update
     * @return database manager
     */
    public static DatabaseManager update(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.UPDATE;
        // Creates db columns by fields
        Field[] fields = target.getClass().getFields();
        for (Field field : fields) {
            // Skip non-column fields
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            final String columnName = manager.toDbName(field.getName());
            final String valueString = manager.getFieldValueAsString(field, target);
            // Sets the primary keys as condition to update, and other fields as
            // update values.
            if (field.getAnnotation(Id.class) == null) {
                manager.mContentValues.put(columnName, valueString);
            } else {
                manager.mWhereClauseMap.put(columnName, valueString);
            }
        }
        return manager;
    }

    /**
     * Prepares to delete data from table.<br>
     * Call {@link #execute()} after executing this method to delete.
     * 
     * @param helper db helper to access the target table
     * @param target object which has conditions to delete
     * @return database manager which is ready to delete
     */
    public static DatabaseManager delete(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.DELETE;
        // Creates db columns by fields
        Field[] fields = target.getClass().getFields();
        for (Field field : fields) {
            // Skip non-column fields
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            // Sets the where conditions by primary keys
            if (field.getAnnotation(Id.class) == null) {
                continue;
            }
            final String columnName = manager.toDbName(field.getName());
            final String valueString = manager.getFieldValueAsString(field, target);
            manager.mWhereClauseMap.put(columnName, valueString);
        }
        return manager;
    }

    /**
     * Joins table by left-outer-join.
     * <p>
     * The table to be joined is automatically determined by the field's
     * annotaion.
     * 
     * @param relationFieldName field name of the column to join
     * @param additionalCondClause additional condition clause
     * @param additionalCondArgs arguments for the adittional conditions
     * @return database manager
     */
    public DatabaseManager leftOuterJoin(final String relationFieldName,
            final String additionalCondClause, final String... additionalCondArgs) {
        Join join = new Join();
        join.setType(JoinType.LEFT_OUTER_JOIN);
        join.setFieldName(relationFieldName);
        try {
            // Determines the class to join
            Field relationField = mTarget.getClass().getField(relationFieldName);
            OneToMany oneToMany = relationField.getAnnotation(OneToMany.class);
            Class<?> joinClass;
            if (oneToMany == null) {
                // Must be a normal class
                joinClass = relationField.getType();
            } else {
                // Must be a parameterized List
                ParameterizedType parameterizedType = (ParameterizedType) relationField
                        .getGenericType();
                joinClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
            join.setTableClass(joinClass);
            join.setTableName(toDbName(joinClass.getSimpleName()));

            ManyToOne manyToOne = relationField.getAnnotation(ManyToOne.class);
            JoinColumn joinColumn;
            if (manyToOne == null) {
                String childFieldName = oneToMany.mappedBy();
                joinColumn = joinClass.getField(childFieldName)
                        .getAnnotation(JoinColumn.class);
            } else {
                joinColumn = relationField.getAnnotation(JoinColumn.class);
            }

            join.setColumnName(toDbName(joinColumn.name()));
            join.setAdditionalCondClause(additionalCondClause);
            join.setAdditionalCondArgs(additionalCondArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mJoinList.add(join);
        return this;
    }

    /**
     * Execute search.
     * 
     * @param <T> type of the entity to be searched
     * @return list of the entities
     */
    public <T> List<T> executeQuery() {
        @SuppressWarnings("unchecked")
        Class<T> targetClass = (Class<T>) mTarget.getClass();
        ArrayList<T> result = new ArrayList<T>();
        // Cannot execute other than SELECT operation
        if (mProcessType != ProcessType.SELECT) {
            return result;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            // Gets fields of the target class.
            final String tableName = toDbName(targetClass.getSimpleName());
            constructWhereClause();

            final Field[] fields = getFields(targetClass);
            final List<String> fieldNames = getFieldNames(fields);
            db = mHelper.getReadableDatabase();
            StringBuilder columns = new StringBuilder();
            for (String fieldName : fieldNames) {
                if (columns.length() > 0) {
                    columns.append(", ");
                }
                columns.append(tableName);
                columns.append(".");
                columns.append(fieldName);
            }
            List<String> sqlArgsList = new ArrayList<String>();
            // Creates JOIN phrase
            StringBuilder sql = new StringBuilder();
            for (Join join : mJoinList) {
                if (join.getType().equals(JoinType.INNER_JOIN)) {
                    sql.append(" INNER JOIN ");
                    sql.append(join.getTableName());
                    sql.append(" ON ");
                    sql.append(tableName);
                    sql.append(".");
                    sql.append(join.getColumnName());
                    sql.append(" = ");
                    sql.append(join.getTableName());
                    sql.append(".");
                    sql.append(join.getColumnName());
                    // sql += " INNER JOIN " + join.getTableName()
                    // + " ON " + tableName + "." + join.getColumnName()
                    // + " = " + join.getTableName() + "." +
                    // join.getColumnName();
                } else if (join.getType().equals(JoinType.LEFT_OUTER_JOIN)) {
                    sql.append(" LEFT OUTER JOIN ");
                    sql.append(join.getTableName());
                    sql.append(" ON ");
                    sql.append(tableName);
                    sql.append(".");
                    sql.append(join.getColumnName());
                    sql.append(" = ");
                    sql.append(join.getTableName());
                    sql.append(".");
                    sql.append(join.getColumnName());
                    // sql += " LEFT OUTER JOIN " + join.getTableName()
                    // + " ON " + tableName + "." + join.getColumnName()
                    // + " = " + join.getTableName() + "." +
                    // join.getColumnName();
                }
                String additionalCondClause = join.getAdditionalCondClause();
                if (!StringUtil.isEmpty(additionalCondClause)) {
                    sql.append(" AND ");
                    sql.append(additionalCondClause);
                }
                if (join.getAdditionalCondArgs() != null) {
                    for (String arg : join.getAdditionalCondArgs()) {
                        sqlArgsList.add(arg);
                    }
                }
                // Adds the columns of the joined table.
                final Field[] joinFields = getFields(join.getTableClass());
                final List<String> joinFieldNames = getFieldNames(joinFields);
                db = mHelper.getReadableDatabase();
                StringBuilder joinColumns = new StringBuilder();
                for (String fieldName : joinFieldNames) {
                    if (joinColumns.length() > 0) {
                        joinColumns.append(", ");
                    }
                    joinColumns.append(join.getTableName());
                    joinColumns.append(".");
                    joinColumns.append(fieldName);
                }
                if (joinColumns.length() > 0) {
                    if (columns.length() > 0) {
                        columns.append(", ");
                    }
                    columns.append(joinColumns);
                }
            }
            StringBuilder sqlSelect = new StringBuilder();
            sqlSelect.append("SELECT ");
            sqlSelect.append(columns);
            sqlSelect.append(" FROM ");
            sqlSelect.append(tableName);
            sqlSelect.append(sql);
            // sql = "SELECT " + columns + " FROM " + tableName + sql;
            if (!StringUtil.isEmpty(mWhereClause)) {
                sqlSelect.append(" WHERE ");
                sqlSelect.append(mWhereClause);
                // sql += " WHERE " + mWhereClause;
                for (String arg : mWhereArgs) {
                    sqlArgsList.add(arg);
                }
            }
            if (!StringUtil.isEmpty(mGroupByClause)) {
                sqlSelect.append(" GROUP BY ");
                sqlSelect.append(mGroupByClause);
                // sql += " GROUP BY " + mGroupByClause;
                if (!StringUtil.isEmpty(mHavingClause)) {
                    sqlSelect.append(" HAVING ");
                    sqlSelect.append(mHavingClause);
                    // sql += " HAVING " + mHavingClause;
                }
            }
            if (!StringUtil.isEmpty(mOrderByClause)) {
                sqlSelect.append(" ORDER BY ");
                sqlSelect.append(mOrderByClause);
                // sql += " ORDER BY " + mOrderByClause;
            }
            String[] sqlArgs = sqlArgsList.toArray(new String[] {});

            // Now, execute the complete query!
            cursor = db.rawQuery(sqlSelect.toString(), sqlArgs);

            // Retrieves the selected values from the cursor
            if (cursor.moveToFirst()) {
                do {
                    retrieveFromCursor(cursor, targetClass);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return result;
    }

    /**
     * Executes database operation.<br>
     * This returns the result code of the operation. Result code is the return
     * values of {@link SQLiteDatabase#insert(String, String, ContentValues)} or
     * other manipulation methods. This returns {@code -1} if this fails to
     * execute.
     * 
     * @return result of this operation
     */
    public long execute() {
        long ret = -1;
        SQLiteDatabase db = null;
        try {
            final String tableName = toDbName(mTarget.getClass().getSimpleName());
            constructWhereClause();
            switch (mProcessType) {
                case INSERT:
                    if (mContentValues.size() > 0) {
                        db = mHelper.getWritableDatabase();
                        ret = db.insert(tableName, null, mContentValues);
                    }
                    break;
                case UPDATE:
                    if (mContentValues.size() > 0) {
                        db = mHelper.getWritableDatabase();
                        ret = db.update(
                                    tableName,
                                    mContentValues,
                                    mWhereClause,
                                    mWhereArgs);
                    }
                    break;
                case DELETE:
                    db = mHelper.getWritableDatabase();
                    ret = db.delete(tableName, mWhereClause, mWhereArgs);
                    break;
                default:
                    throw new RuntimeException("Undefined process type!: " + mProcessType);
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return ret;
    }

    /**
     * Sets the WHERE clause.
     * 
     * @param whereClause WHERE clause which is parameterized by '?'
     * @param whereArgs arguments for parameterized WHERE clause
     * @return database manager
     */
    public DatabaseManager where(final String whereClause, final String... whereArgs) {
        mWhereClause = whereClause;
        mWhereArgs = whereArgs;
        return this;
    }

    /**
     * Sets the ORDER BY clause.
     * 
     * @param orderByClause ORDER BY clause
     * @return database manager
     */
    public DatabaseManager orderBy(final String orderByClause) {
        mOrderByClause = orderByClause;
        return this;
    }

    /**
     * Excludes the columns which value is {@code null}.
     * 
     * @return database manager
     */
    public DatabaseManager excludesNull() {
        ContentValues newValues = new ContentValues();
        for (Entry<String, Object> entry : mContentValues.valueSet()) {
            if (entry.getValue() != null) {
                newValues.put(entry.getKey(), (String) entry.getValue());
            }
        }
        mContentValues = newValues;
        return this;
    }

    /**
     * Retrieves the entity from the cursor.
     * 
     * @param <T> type of the entity
     * @param cursor the opened cursor to get an entity
     * @param targetClass the class of the entity
     * @return the entity retrieved from the cursor
     * @throws Exception if the instantiation of the entity failed
     */
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private <T> T retrieveFromCursor(final Cursor cursor, final Class<T> targetClass)
            throws Exception {
        final T entity = targetClass.newInstance();
        final Field[] fields = getFields(targetClass);
        int cursorPos = 0;
        for (Field field : fields) {
            // Skip non-column fields
            if (field.getAnnotation(Column.class) != null) {
                cursorPos = setFieldValuesByCursor(field, entity, cursor, cursorPos);
            }
        }
        for (Join join : mJoinList) {
            // Initializes the relation field.
            // If it is annotated as OneToMany, then assumes it as
            // an List, and adds an new entity to the list.
            final Field relationField = entity.getClass().getField(join.getFieldName());
            final OneToMany oneToMany = relationField.getAnnotation(OneToMany.class);
            final Object relation;
            if (oneToMany == null) {
                // Must be a normal class
                relation = relationField.getType().newInstance();
                relationField.set(entity, relation);
            } else {
                // Must be a parameterized List
                ParameterizedType parameterizedType = (ParameterizedType) relationField
                        .getGenericType();
                Class<?> joinClass = (Class<?>) parameterizedType
                        .getActualTypeArguments()[0];
                relation = joinClass.newInstance();
                ((List) relationField.get(entity)).add(relation);
            }

            final Field[] joinFields = getFields(join.getTableClass());
            for (Field joinField : joinFields) {
                // Skip non-column fields
                if (joinField.getAnnotation(Column.class) == null) {
                    continue;
                }
                cursorPos = setFieldValuesByCursor(joinField, relation, cursor,
                        cursorPos);
            }
        }
        return entity;
    }

    /**
     * Convert the name(class or field name in camel format) to database name
     * format; All characters are upper-case and are delimited by
     * underscore('_').
     * <p>
     * The argument name is assumed to be the following format.
     * 
     * <pre>
     * {@code ([a-zA-Z][a-z0-9_\\$]*)([A-Z][a-z0-9_\\$]*)* }
     * </pre>
     * 
     * @param name name to convert
     * @return converted name
     */
    private String toDbName(final String name) {
        String dbName = "";
        for (int i = 0; i < name.length(); i++) {
            String c = name.substring(i, i + 1);
            // Assume upper case characters as the delimiters of the words.
            if (c.matches("[A-Z]")) {
                dbName += "_" + c;
            } else {
                dbName += c;
            }
        }
        dbName = dbName.toUpperCase(Locale.ENGLISH);
        // Removes the underscores if this name is the class name
        if (dbName.startsWith("_")) {
            dbName = dbName.substring(1);
        }
        return dbName;
    }

    /**
     * Returns the value as string from target object's field. Returns
     * {@code null}, if the value is {@code null}.
     * 
     * @param field target field
     * @param targetObject target object
     * @return value in string
     */
    private String getFieldValueAsString(final Field field, final Object targetObject) {
        Object value;
        try {
            value = field.get(targetObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (value == null) ? null : value.toString();
    }

    /**
     * Constructs the WHERE clause.<br>
     * This creates the parameters for
     * {@link SQLiteDatabase#update(String, ContentValues, String, String[])},
     * {@link SQLiteDatabase#delete(String, String, String[])}.
     */
    private void constructWhereClause() {
        if (mWhereClauseMap.size() > 0) {
            mWhereClause = "";
            final ArrayList<String> whereArgsList = new ArrayList<String>();
            for (String columnName : mWhereClauseMap.keySet()) {
                if (mWhereClause.length() > 0) {
                    mWhereClause += " AND ";
                }
                final String valueString = mWhereClauseMap.get(columnName);
                if (valueString == null) {
                    mWhereClause += columnName + " IS NULL";
                } else {
                    mWhereClause += columnName + " = ?";
                    whereArgsList.add(valueString);
                }
            }
            mWhereArgs = whereArgsList.toArray(new String[] {});
        }
    }

    /**
     * Returns the field array of the target class.
     * 
     * @param targetClass the target class to get fields
     * @return the array of the fields
     */
    private Field[] getFields(final Class<?> targetClass) {
        final Field[] fields = targetClass.getFields();
        Arrays.sort(fields, new FieldOrderComparator());
        return fields;
    }

    /**
     * Returns the names of the fields as an list.
     * 
     * @param fields the target fields in array
     * @return the names of the fields
     */
    private List<String> getFieldNames(final Field[] fields) {
        final List<String> fieldNames = new ArrayList<String>();
        for (Field field : fields) {
            // Skip non-column fields
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            fieldNames.add(toDbName(field.getName()));
        }
        return fieldNames;
    }

    /**
     * Sets the field value by the database cursor.
     * 
     * @param field the field to access
     * @param entity the entity object to access
     * @param cursor the opened cursor
     * @param initCusorPos the position(index) of the cursor
     * @return the position(index) of the cursor after getting the value
     * @throws IllegalAccessException if this field is not accessible
     */
    private int setFieldValuesByCursor(final Field field, final Object entity, final Cursor cursor,
            final int initCusorPos) throws IllegalAccessException {
        int cursorPos = initCusorPos;
        final Class<?> type = field.getType();
        if (type.equals(double.class)) {
            field.setDouble(entity, cursor.getDouble(cursorPos++));
        } else if (type.equals(float.class)) {
            field.setFloat(entity, cursor.getFloat(cursorPos++));
        } else if (type.equals(int.class)) {
            field.setInt(entity, cursor.getInt(cursorPos++));
        } else if (type.equals(long.class)) {
            field.setLong(entity, cursor.getLong(cursorPos++));
        } else if (type.equals(String.class)) {
            field.set(entity, cursor.getString(cursorPos++));
        } else if (type.equals(byte[].class)) {
            field.set(entity, cursor.getBlob(cursorPos++));
        } else if (type.equals(short.class)) {
            field.setShort(entity, cursor.getShort(cursorPos++));
        }
        return cursorPos;
    }

}
