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

package android.fastroid.util;

import android.content.Context;
import android.fastroid.form.annotation.Radio;
import android.fastroid.form.annotation.RadioValue;
import android.fastroid.form.annotation.Text;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.lang.reflect.Field;

/**
 * Utility about the form operations.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class FormUtil {
    /**
     * Creates a {@code StringUtil}.<br>
     * This is allowed to use only for the inside of this class because this is
     * the utility class.
     */
    private FormUtil() {
    }

    /**
     * Reads the form information from the activity, and creates a new object.
     * 
     * @param context target context
     * @param rootView root view of the form
     * @param dstFormClass the destination form class
     * @return created object
     */
    public static Object create(final Context context, final View rootView,
            final Class<?> dstFormClass) {
        try {
            final Object form = dstFormClass.newInstance();
            final Field[] fields = dstFormClass.getFields();
            for (Field field : fields) {
                Class<?> type = field.getType();
                // Text box type
                Text text = (Text) field.getAnnotation(Text.class);
                if (text != null && type.equals(String.class)) {
                    String value =
                            ((EditText) rootView.findViewById(text.id())).getText().toString();
                    field.set(form, value);
                    continue;
                }

                // Radio button type
                Radio radio = (Radio) field.getAnnotation(Radio.class);
                if (radio != null && type.equals(String.class)) {
                    int groupId = radio.groupId();
                    RadioGroup radioGroup = (RadioGroup) rootView.findViewById(groupId);
                    int checkedId = radioGroup.getCheckedRadioButtonId();
                    RadioValue[] values = radio.values();
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].id() == checkedId) {
                            field.set(form, values[i].value());
                            break;
                        }
                    }
                    continue;
                }
            }
            return form;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
