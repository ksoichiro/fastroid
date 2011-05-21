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

import android.content.res.Resources;
import android.fastroid.form.annotation.Required;
import android.fastroid.form.annotation.When;
import android.fastroid.util.MessageUtil;
import android.fastroid.util.StringUtil;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 必須入力チェックを行います。
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
class RequiredValidator extends AbstractValidator {

    /**
     * コンストラクタです。
     * 
     * @param target チェック対象のオブジェクト
     * @param resources エラーメッセージ取得用のリソース
     * @param errorMessages 入力エラーメッセージを格納するリスト
     */
    public RequiredValidator(final Object target, final Resources resources,
            final List<String> errorMessages) {
        super(target, resources, errorMessages);
    }

    @Override
    public boolean detectError(final Object value, final Field field) {
        Required required = field.getAnnotation(Required.class);
        if (required != null) {
            // 条件チェック
            When[] whenList = required.when();
            boolean validateEnabled;
            if (whenList == null || whenList.length == 0) {
                validateEnabled = true;
            } else {
                validateEnabled = false;
                for (When when : whenList) {
                    String name = when.name();
                    boolean isNotEmpty = when.isNotEmpty();
                    String equalsTo = when.equalsTo();
                    try {
                        Field whenField = getTarget().getClass().getField(name);
                        String whenValue = (String) whenField.get(getTarget());
                        if (isNotEmpty) {
                            if (!TextUtils.isEmpty(whenValue)) {
                                validateEnabled = true;
                            }
                        } else if (equalsTo.equals(whenValue)) {
                            validateEnabled = true;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            // 入力チェックなし
            if (!validateEnabled) {
                return false;
            }
            // 入力チェック
            final Class<?> type = field.getType();
            if (type.equals(String.class)) {
                final String strValue = (String) value;
                if (StringUtil.isEmpty(strValue)) {
                    String name = field.getName();
                    int nameResId = required.nameResId();
                    if (nameResId > 0) {
                        name = getResources().getString(nameResId);
                    }
                    getErrorMessages().add(
                            MessageUtil.get(
                                    getResources().getString(
                                            android.fastroid.R.string.msg_validation_required),
                                    name));
                    return true;
                }
            }
        }
        return false;
    }
}
