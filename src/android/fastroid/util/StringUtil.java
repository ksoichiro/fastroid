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

/**
 * Utility about strings operation.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class StringUtil {
    /**
     * Creates a {@code StringUtil}.<br>
     * This is allowed to use only for the inside of this class because this is
     * the utility class.
     */
    private StringUtil() {
    }

    /**
     * Returns true if the string is {@code null} or 0-length.
     * 
     * @param value target value
     * @return true if the string is {@code null} or 0-length.
     */
    public static boolean isEmpty(final String value) {
        return value == null || value.length() == 0;
    }
}
