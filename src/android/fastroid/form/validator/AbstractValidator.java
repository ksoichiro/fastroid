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

import java.lang.reflect.Field;
import java.util.List;

/**
 * 入力チェックを行います。<br>
 * このクラスは、サブクラスに実装される実際の入力チェックに必要なオブジェクトを準備します。
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
abstract class AbstractValidator {

    /** チェック対象のオブジェクトです。 */
    private Object mTarget;

    /** エラーメッセージ取得用のリソースです。 */
    private Resources mResources;

    /** エラーメッセージのリストです。 */
    private List<String> mErrorMessages;

    /**
     * コンストラクタです。
     * 
     * @param target チェック対象のオブジェクト
     * @param resources エラーメッセージ取得用のリソース
     * @param errorMessages 入力エラーメッセージを格納するリスト
     */
    public AbstractValidator(final Object target, final Resources resources,
            final List<String> errorMessages) {
        mTarget = target;
        mResources = resources;
        mErrorMessages = errorMessages;
    }

    /**
     * 入力チェックします。
     * 
     * @param value 入力値
     * @param field 対象のフィールド
     * @return 入力エラーがある場合は{@code true}
     */
    public abstract boolean detectError(final Object value, final Field field);

    /**
     * @return mTarget
     */
    public Object getTarget() {
        return mTarget;
    }

    /**
     * @param target セットする mTarget
     */
    public void setTarget(final Object target) {
        mTarget = target;
    }

    /**
     * @return Resources
     */
    protected Resources getResources() {
        return mResources;
    }

    /**
     * @param resources セットする Resources
     */
    protected void setResources(final Resources resources) {
        mResources = resources;
    }

    /**
     * @return errorMessages
     */
    protected List<String> getErrorMessages() {
        return mErrorMessages;
    }

    /**
     * @param errorMessages セットする errorMessages
     */
    protected void setErrorMessages(final List<String> errorMessages) {
        mErrorMessages = errorMessages;
    }
}
