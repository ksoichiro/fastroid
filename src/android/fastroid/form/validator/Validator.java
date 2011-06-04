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

package android.fastroid.form.validator;

import android.content.Context;
import android.content.res.Resources;
import android.fastroid.entity.FieldOrderComparator;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The utility to validate input values.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/03
 */
public final class Validator {

    /**
     * Creates the validator.
     * <p>
     * This is allowed to use only for the inside of this class because this is
     * the utility class.
     */
    private Validator() {
    }

    /**
     * Validates the input values.
     * <p>
     * Validations are executed in the orders specified by the
     * {@link android.androsuit.entity.annotation.Order}. If this annotation is
     * not specified, the order is determined by field names(asc). The fields
     * with the annotations are prior to the others.
     * 
     * @param context context to access the message resources
     * @param target target object to be validated (the form)
     * @return list to save the error messages
     */
    public static ArrayList<String> validate(final Context context, final Object target) {
        ArrayList<String> errorMessages = new ArrayList<String>();
        Resources res = context.getResources();

        // Gets all the public fields
        Field[] fields = target.getClass().getFields();
        Arrays.sort(fields, new FieldOrderComparator());
        final RequiredValidator required = new RequiredValidator(target, res, errorMessages);
        for (Field field : fields) {
            Object value;
            try {
                value = field.get(target);
            } catch (Exception e) {
                Log.v("fastroid", e.getMessage());
                continue;
            }

            // Required
            if (required.detectError(value, field)) {
                continue;
            }

            // TODO Int value

            // TODO Min value

            // TODO Max value

            // TODO Regex value
        }
        return errorMessages;
    }
}
