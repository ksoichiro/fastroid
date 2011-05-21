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

import android.fastroid.entity.annotation.Order;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * フィールドを{@link Order}アノテーションを使って順序付けます。<br>
 * アノテーションの値が小さいものが前方になります。<br>
 * アノテーションのないフィールドは無条件に後方になり、アノテーションのないフィールド同士は フィールド名によって順序付けます。
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class FieldOrderComparator implements Comparator<Field> {
    @Override
    public int compare(final Field object1, final Field object2) {
        // Orderがついているフィールドの方が小さく評価される
        final Order order1 = object1.getAnnotation(Order.class);
        final Order order2 = object2.getAnnotation(Order.class);
        if (order1 != null && order2 != null) {
            return order1.value() - order2.value();
        } else if (order1 == null && order2 != null) {
            return 1;
        } else if (order1 != null && order2 == null) {
            return -1;
        }
        return object1.getName().compareTo(object2.getName());
    }
}