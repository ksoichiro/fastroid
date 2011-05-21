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
 * 入力内容を検証するユーティリティです。.<br>
 * 
 * @author Soichiro Kashima
 * @since 2011/05/03
 */
public final class Validator {

    /**
     * コンストラクタです。<br>
     * ユーティリティクラスであるため、クラス外からのインスタンス化を禁止します。
     */
    private Validator() {
    }

    /**
     * 入力内容を検証します。<br>
     * フィールドの検証は、{@link android.androsuit.entity.annotation.Order}アノテーションで
     * 指定された順序の昇順で行います。 このアノテーションが指定されていない項目は、フィールド名の昇順で検証します。
     * アノテーションが指定されているものは、指定しているものより優先されます。
     * 
     * @param context リソースにアクセスするためのアプリケーションコンテキスト
     * @param target 検証対象のオブジェクト(フォームクラス)
     * @return 入力エラーメッセージを格納するリスト
     */
    public static ArrayList<String> validate(final Context context, final Object target) {
        ArrayList<String> errorMessages = new ArrayList<String>();
        Resources res = context.getResources();

        // 全てのpublicフィールドを取得
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

            // 必須チェック
            if (required.detectError(value, field)) {
                continue;
            }

            // 数値チェック

            // 最小値チェック

            // 最大値チェック

            // 正規表現チェック
        }
        return errorMessages;
    }
}
