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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility about the date operations.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class DateUtil {
    /**
     * Creates a {@code StringUtil}.<br>
     * This is allowed to use only for the inside of this class because this is
     * the utility class.
     */
    private DateUtil() {
    }

    /**
     * Returns the current date and time as string.<br>
     * The format is yyyy-MM-dd.
     * 
     * @return the current date and time
     */
    public static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
}
