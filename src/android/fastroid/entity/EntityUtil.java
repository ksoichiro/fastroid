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

import android.app.Activity;
import android.fastroid.form.validator.Validator;
import android.fastroid.util.FormUtil;
import android.fastroid.util.MessageUtil;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utilities for the entities.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class EntityUtil {
    /**
     * コンストラクタです。<br>
     * ユーティリティクラスであるため、クラス外からのインスタンス化を禁止します。
     */
    private EntityUtil() {
    }

    /**
     * 指定のアクティビティからフォームクラスの定義に従って入力値を取得し、 入力チェックしてエンティティを取得します。<br>
     * 入力エラーがあった場合、入力チェックのタイプに対応する規定のエラーメッセージを {@link Toast}で表示し、{@code null}
     * を返します。
     * 
     * @param activity 対象のアクティビティ
     * @param formClass アクティビティに関連付けられたフォームクラス
     * @param entityClass フォームと対になるエンティティクラス
     * @return 入力チェックの済んだエンティティ
     */
    public static Object create(final Activity activity, final Class<?> formClass,
            final Class<?> entityClass) {
        // 入力値を取得
        Object form = FormUtil.create(activity, formClass);

        // 入力チェック
        ArrayList<String> errorMessages = Validator.validate(activity, form);
        if (errorMessages.size() > 0) {
            // 入力エラー
            Toast.makeText(
                    activity,
                    MessageUtil.serialize(errorMessages),
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        // エンティティへコピー
        return copy(form, entityClass);
    }

    /**
     * 対象オブジェクトのpublicフィールドを、{@link Parcel}からフィールド名の昇順で読み込みんで設定します。
     * 
     * @param target 設定対象のオブジェクト
     * @param in 値を読み込む{@link Parcel}
     */
    public static void readFieldsByNameOrder(final Object target, final Parcel in) {
        Field[] fields = target.getClass().getFields();
        Arrays.sort(fields, new FieldNameComparator());
        try {
            for (Field field : fields) {
                Class<?> type = field.getType();
                // finalフィールドには設定しない
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (type.equals(byte.class)) {
                    field.setByte(target, in.readByte());
                } else if (type.equals(double.class)) {
                    field.setDouble(target, in.readDouble());
                } else if (type.equals(float.class)) {
                    field.setFloat(target, in.readFloat());
                } else if (type.equals(int.class)) {
                    field.setInt(target, in.readInt());
                } else if (type.equals(long.class)) {
                    field.setLong(target, in.readLong());
                } else if (type.equals(String.class)) {
                    field.set(target, in.readString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 対象のオブジェクトのpublicフィールドを、フィールド名の昇順で{@link Parcel}に書き込みます。
     * 
     * @param target 出力対象のオブジェクト
     * @param out 出力先の{@link Parcel}
     */
    public static void writeFieldsByNameOrder(final Object target, final Parcel out) {
        Field[] fields = target.getClass().getFields();
        Arrays.sort(fields, new FieldNameComparator());
        try {
            for (Field field : fields) {
                Class<?> type = field.getType();
                // finalフィールドからは取得しない
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (type.equals(byte.class)) {
                    out.writeByte(field.getByte(target));
                } else if (type.equals(double.class)) {
                    out.writeDouble(field.getDouble(target));
                } else if (type.equals(float.class)) {
                    out.writeFloat(field.getFloat(target));
                } else if (type.equals(int.class)) {
                    out.writeInt(field.getInt(target));
                } else if (type.equals(long.class)) {
                    out.writeLong(field.getLong(target));
                } else if (type.equals(String.class)) {
                    out.writeString((String) field.get(target));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 指定のオブジェクトを、別のオブジェクトへコピーします。<br>
     * フィールド名が同じ場合にコピーされます。
     * 
     * @param src コピー元のオブジェクト
     * @param dstClass コピー先オブジェクトのクラス
     * @return コピーされたオブジェクト
     */
    public static Object copy(final Object src, final Class<?> dstClass) {
        Field[] srcFields = src.getClass().getFields();

        Object dst = null;
        try {
            dst = dstClass.newInstance();
            for (Field srcField : srcFields) {
                try {
                    Object value = srcField.get(src);
                    if (value == null) {
                        continue;
                    }
                    String name = srcField.getName();
                    Class<?> srcType = srcField.getType();
                    Field dstField = dstClass.getField(name);
                    Class<?> dstType = dstField.getType();
                    if (srcType.equals(String.class)) {
                        String valueString = (String) value;
                        if (dstType.equals(int.class)) {
                            dstField.setInt(dst, Integer.parseInt(valueString));
                        } else {
                            // TODO ディープコピーが必要
                            dstField.set(dst, value);
                        }
                    } else {
                        // TODO ディープコピーが必要
                        dstField.set(dst, value);
                    }
                } catch (Exception e) {
                    Log.v("fastroid",
                            "Failed to set field value: " + srcField.getName() + ": "
                                    + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.v("fastroid", e.getMessage());
        }

        return dst;
    }
}
