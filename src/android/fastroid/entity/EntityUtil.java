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
import android.content.Context;
import android.fastroid.form.validator.Validator;
import android.fastroid.util.FormUtil;
import android.fastroid.util.MessageUtil;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
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
     * Creates the utility.<br>
     * This is hidden from outside the class because this is the utility class.
     */
    private EntityUtil() {
    }

    /**
     * Gets the input values from the form class in the specified activity,
     * validates the values, and returns the entity. If the input values have
     * errors, this methos shows the error message as {@link Toast}, and returns
     * {@code null}.
     * 
     * @param activity target activity
     * @param formClass the form class related to the activity
     * @param entityClass the entity class corresponding to the form
     * @return validated entity
     */
    public static Object create(final Activity activity,
            final Class<?> formClass, final Class<?> entityClass) {
        return create(activity, activity.findViewById(android.R.id.content),
                formClass, entityClass);
    }

    /**
     * Gets the input values from the form class in the specified activity,
     * validates the values, and returns the entity. If the input values have
     * errors, this methos shows the error message as {@link Toast}, and returns
     * {@code null}.
     * 
     * @param context target context
     * @param rootView root view of the form
     * @param formClass the form class related to the activity
     * @param entityClass the entity class corresponding to the form
     * @return validated entity
     */
    public static Object create(final Context context, final View rootView,
            final Class<?> formClass, final Class<?> entityClass) {
        // Gets the input value from the form
        Object form = FormUtil.create(context, rootView, formClass);

        // Validation
        ArrayList<String> errorMessages = Validator.validate(context, form);
        if (errorMessages.size() > 0) {
            // Error
            Toast.makeText(
                    context,
                    MessageUtil.serialize(errorMessages),
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        // Copies to the entity
        return copy(form, entityClass);
    }

    /**
     * Sets the public field's value of the target object from the
     * {@link Parcel} order by the field name(asc).
     * 
     * @param target target object
     * @param in {@link Parcel} to read
     */
    public static void readFieldsByNameOrder(final Object target, final Parcel in) {
        Field[] fields = target.getClass().getFields();
        Arrays.sort(fields, new FieldNameComparator());
        try {
            for (Field field : fields) {
                Class<?> type = field.getType();
                // Skipps the final fields
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
     * Writes the public field valeus to the {@link Parcel} order by the field
     * names(asc).
     * 
     * @param target target object
     * @param out output {@link Parcel}
     */
    public static void writeFieldsByNameOrder(final Object target, final Parcel out) {
        Field[] fields = target.getClass().getFields();
        Arrays.sort(fields, new FieldNameComparator());
        try {
            for (Field field : fields) {
                Class<?> type = field.getType();
                // Skipps the final fields
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
     * Copies an object fields which have same names to the other object.<br>
     * 
     * @param src source object
     * @param dstClass destination object's class
     * @return copied object
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
                            // TODO Needs deep copy
                            dstField.set(dst, value);
                        }
                    } else {
                        // TODO Needs deep copy
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
