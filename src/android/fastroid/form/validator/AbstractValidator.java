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
 * Provides the validation functions.
 * <p>
 * This class prepares the objects to validation for subclasses.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
abstract class AbstractValidator {

    /** Target object to validate. */
    private Object mTarget;

    /** Resource to get the error messages. */
    private Resources mResources;

    /** List of the error messages for the validation. */
    private List<String> mErrorMessages;

    /**
     * Creates the validator.
     * 
     * @param target target object
     * @param resources resource to get the error messages
     * @param errorMessages error messages list
     */
    public AbstractValidator(final Object target, final Resources resources,
            final List<String> errorMessages) {
        mTarget = target;
        mResources = resources;
        mErrorMessages = errorMessages;
    }

    /**
     * Validates the object, and returns whether it has any errors or not.
     * 
     * @param value input value
     * @param field target field
     * @return true if there are errors.
     */
    public abstract boolean detectError(final Object value, final Field field);

    /**
     * Returns the target object.
     * 
     * @return target object
     */
    public Object getTarget() {
        return mTarget;
    }

    /**
     * Sets the target object.
     * 
     * @param target target object to set
     */
    public void setTarget(final Object target) {
        mTarget = target;
    }

    /**
     * Returns the resources to get error messages.
     * 
     * @return resources
     */
    protected Resources getResources() {
        return mResources;
    }

    /**
     * Sets the resources to get error messages.
     * 
     * @param resources resources to set
     */
    protected void setResources(final Resources resources) {
        mResources = resources;
    }

    /**
     * Returns the error messages.
     * 
     * @return errorMessages error messages
     */
    protected List<String> getErrorMessages() {
        return mErrorMessages;
    }

    /**
     * Sets the error messages.
     * 
     * @param errorMessages error messages to set
     */
    protected void setErrorMessages(final List<String> errorMessages) {
        mErrorMessages = errorMessages;
    }
}
