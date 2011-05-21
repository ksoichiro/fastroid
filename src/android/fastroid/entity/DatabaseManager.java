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
import android.fastroid.entity.annotation.Column;
import android.fastroid.entity.annotation.Id;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simplifies the database operations.
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
     * 各種データベース操作の準備を確実に行うため、コンストラクタを公開せずに クラスメソッドからインスタンスを生成します。
     * {@link #insert(SQLiteOpenHelper, Object)}等の各種準備用メソッドを呼び出した後で なければ
     * {@link #execute()}を実行できません。
     * 
     * @param helper 対象のテーブルにアクセスするためのヘルパー
     * @param target 登録値を格納したオブジェクト
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
    }

    /**
     * 指定のオブジェクトを使ってデータベースを検索する準備をします。
     * 
     * @param helper 対象のテーブルにアクセスするためのヘルパー
     * @param target 検索条件を格納したオブジェクト
     * @return 検索の準備がされたデータベースマネージャ
     */
    public static DatabaseManager select(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.SELECT;
        return manager;
    }

    /**
     * 指定のオブジェクトをデータベースへ登録する準備をする.<br>
     * このメソッドの呼び出し後, {@link #execute()}を呼び出すことで登録が実行される.
     * 
     * @param helper 対象のテーブルにアクセスするためのヘルパー
     * @param target 登録値を格納したオブジェクト
     * @return 登録の準備がされたデータベースマネージャ
     */
    public static DatabaseManager insert(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.INSERT;
        // DB項目名を作成
        Field[] fields = target.getClass().getFields();
        for (Field field : fields) {
            // DBカラムでないフィールドはスキップ
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            // 自動採番されるキー属性はスキップ
            Id id = (Id) field.getAnnotation(Id.class);
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
     * データベースを更新する準備をする.<br>
     * このメソッドの呼び出し後, {@link #execute()}を呼び出すことで更新が実行される.
     * 
     * @param helper 対象のテーブルにアクセスするためのヘルパー
     * @param target 更新値, 更新条件を格納したオブジェクト
     * @return 更新の準備がされたデータベースマネージャ
     */
    public static DatabaseManager update(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.UPDATE;
        // DB項目名を作成
        Field[] fields = target.getClass().getFields();
        for (Field field : fields) {
            // DBカラムでないフィールドはスキップ
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            final String columnName = manager.toDbName(field.getName());
            final String valueString = manager.getFieldValueAsString(field, target);
            // キー属性は条件、その他は更新値として設定
            if (field.getAnnotation(Id.class) == null) {
                manager.mContentValues.put(columnName, valueString);
            } else {
                manager.mWhereClauseMap.put(columnName, valueString);
            }
        }
        return manager;
    }

    /**
     * データベースから削除する準備をする.<br>
     * このメソッドの呼び出し後, {@link #execute()}を呼び出すことで削除が実行される.
     * 
     * @param helper 対象のテーブルにアクセスするためのヘルパー
     * @param target 削除条件を格納したオブジェクト
     * @return 削除の準備がされたデータベースマネージャ
     */
    public static DatabaseManager delete(final SQLiteOpenHelper helper, final Object target) {
        DatabaseManager manager = new DatabaseManager(helper, target);
        manager.mProcessType = ProcessType.DELETE;
        // DB項目名を作成
        Field[] fields = target.getClass().getFields();
        for (Field field : fields) {
            // DBカラムでないフィールドはスキップ
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            // キー属性を条件として設定
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
     * 左外部結合します。<br>
     * 検索対象のエンティティのうち、結合するフィールド名を指定することで 結合先テーブルを自動的に設定します。
     * 結合するフィールド名にはアノテーションによる結合先テーブル名の指定が必要です。
     * 
     * @param joinOnFieldName 結合するフィールド名
     * @return データベースマネージャ
     */
    public DatabaseManager leftOuterJoin(final String joinOnFieldName) {
        // TODO LEFT JOINするテーブル名とフィールド名の算出・保存
        return this;
    }

    /**
     * 検索を実行します。
     * 
     * @param <T> 検索されるエンティティの型
     * @return 検索結果のエンティティのリスト
     */
    public <T> List<T> executeQuery() {
        @SuppressWarnings("unchecked")
        Class<T> targetClass = (Class<T>) mTarget.getClass();
        ArrayList<T> result = new ArrayList<T>();
        if (mProcessType != ProcessType.SELECT) {
            return result;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            final String tableName = toDbName(targetClass.getSimpleName());
            constructWhereClause();

            final Field[] fields = targetClass.getFields();
            Arrays.sort(fields, new FieldOrderComparator());
            final ArrayList<String> fieldNames = new ArrayList<String>();
            for (Field field : fields) {
                // DBカラムでないフィールドはスキップ
                if (field.getAnnotation(Column.class) == null) {
                    continue;
                }
                fieldNames.add(toDbName(field.getName()));
            }
            db = mHelper.getReadableDatabase();
            // TODO JOINするためにdb.rawQuery()を使う
            // String columns = fieldNames...
            // String sql = "SELECT " + columns + " FROM " + tableName;
            // for (Join join : joinList) {
            // if (join.getType().equals(JoinType.INNER_JOIN) {
            // sql += " INNER JOIN " + join.getTableName() + " ON ";
            // } else if (join.getType().equals(JoinType.LEFT_OUTER_JOIN) {
            // sql += " LEFT OUTER JOIN " + join.getTableName() + " ON ";
            // }
            // }
            // cursor = db.rawQuery(sql, sqlArgs);
            cursor = db.query(
                    tableName,
                    fieldNames.toArray(new String[] {}),
                    mWhereClause,
                    mWhereArgs,
                    mGroupByClause,
                    mHavingClause,
                    mOrderByClause);
            if (cursor.moveToFirst()) {
                do {
                    final T entity = targetClass.newInstance();
                    int cursorPos = 0;
                    for (Field field : fields) {
                        // DBカラムでないフィールドはスキップ
                        if (field.getAnnotation(Column.class) == null) {
                            continue;
                        }
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
                    }
                    result.add(entity);
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
     * データベース操作を実行する.<br>
     * 操作の結果コードを戻り値として返す. 結果コードは,
     * {@link SQLiteDatabase#insert(String, String, ContentValues)}
     * 等の操作メソッドの戻り値である. 操作に失敗した場合は{@code -1}を返す.
     * 
     * @return 操作の結果
     */
    public long execute() {
        long ret = -1;
        SQLiteDatabase db = null;
        try {
            final String tableName = toDbName(mTarget.getClass().getSimpleName());
            constructWhereClause();
            switch (mProcessType) {
                case INSERT:
                    db = mHelper.getWritableDatabase();
                    ret = db.insert(tableName, null, mContentValues);
                    break;
                case UPDATE:
                    db = mHelper.getWritableDatabase();
                    ret =
                            db.update(
                                    tableName,
                                    mContentValues,
                                    mWhereClause,
                                    mWhereArgs);
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
     * WHERE句を指定します。
     * 
     * @param whereClause WHERE句(パラメータは「?」)
     * @param whereArgs WHERE句のパラメータ
     * @return WHERE句が設定されたデータベースマネージャ
     */
    public DatabaseManager where(final String whereClause, final String... whereArgs) {
        this.mWhereClause = whereClause;
        this.mWhereArgs = whereArgs;
        return this;
    }

    /**
     * ORDER BY句を指定します。
     * 
     * @param orderByClause ORDER BY句
     * @return ORDER BY句が設定されたデータベースマネージャ
     */
    public DatabaseManager orderBy(final String orderByClause) {
        this.mOrderByClause = orderByClause;
        return this;
    }

    /**
     * 登録・更新対象のカラムから, 値が{@code null}であるものを除外する.
     * 
     * @return データベースマネージャ
     */
    public DatabaseManager excludesNull() {
        ContentValues newValues = new ContentValues();
        for (Entry<String, Object> entry : this.mContentValues.valueSet()) {
            if (entry.getValue() != null) {
                newValues.put(entry.getKey(), (String) entry.getValue());
            }
        }
        this.mContentValues = newValues;
        return this;
    }

    /**
     * 指定の名前(クラス名, フィールド名)を大文字・アンダースコア('_')区切りの名前に変換する.<br>
     * 変換元の名前は, 以下のような形式であるものとする.
     * 
     * <pre>
     * {@code ([a-zA-Z][a-z0-9_\\$]*)([A-Z][a-z0-9_\\$]*)* }
     * </pre>
     * 
     * @param name 変換対象の名前
     * @return 変換後の名前
     */
    private String toDbName(final String name) {
        String dbName = "";
        for (int i = 0; i < name.length(); i++) {
            String c = name.substring(i, i + 1);
            // 名前内の大文字を単語区切りとみなす
            if (c.matches("[A-Z]")) {
                dbName += "_" + c;
            } else {
                dbName += c;
            }
        }
        dbName = dbName.toUpperCase(Locale.ENGLISH);
        // クラス名からテーブル名を作成する場合、先頭に区切り文字が付くので除去
        if (dbName.startsWith("_")) {
            dbName = dbName.substring(1);
        }
        return dbName;
    }

    /**
     * 対象オブジェクトのフィールドから, 値を文字列として取得する.<br>
     * 値が{@code null}の場合は{@code null}を返す.
     * 
     * @param field 対象のフィールド
     * @param targetObject 対象のオブジェクト
     * @return フィールド値の文字列
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
     * WHERE句を確定して組み立てる.<br>
     * {@link SQLiteDatabase#update(String, ContentValues, String, String[])},
     * {@link SQLiteDatabase#delete(String, String, String[])}
     * に指定するWHERE句の引数を作成する.
     */
    private void constructWhereClause() {
        if (this.mWhereClauseMap.size() > 0) {
            this.mWhereClause = "";
            final ArrayList<String> whereArgsList = new ArrayList<String>();
            for (String columnName : this.mWhereClauseMap.keySet()) {
                if (this.mWhereClause.length() > 0) {
                    this.mWhereClause += " and ";
                }
                final String valueString = this.mWhereClauseMap.get(columnName);
                if (valueString == null) {
                    this.mWhereClause += columnName + " is null";
                } else {
                    this.mWhereClause += columnName + " = ?";
                    whereArgsList.add(valueString);
                }
            }
            this.mWhereArgs = whereArgsList.toArray(new String[] {});
        }
    }
}
