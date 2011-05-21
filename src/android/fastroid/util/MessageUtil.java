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

import java.util.List;

/**
 * Utility about the messages.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class MessageUtil {
    /**
     * Creates a {@code StringUtil}.<br>
     * This is allowed to use only for the inside of this class because this is
     * the utility class.
     */
    private MessageUtil() {
    }

    /**
     * メッセージテンプレートに埋められたパラメータを置換して、メッセージを作成します。<br>
     * メッセージのパラメータは「{n}」(n = 0, 1, ...)の形式です。
     * 
     * @param template メッセージテンプレート
     * @param args テンプレートパラメータ
     * @return パラメータが置換されたメッセージ
     */
    public static String get(final String template, final Object... args) {
        if (template == null) {
            return null;
        }
        // {n}を置換(nが見つからなくなるまで(途切れるまで)ループ
        int n = 0;
        String message = template;
        while (true) {
            if (!message.matches(".*\\{" + n + "\\}.*")) {
                break;
            }
            if (args.length > n && args[n] != null) {
                message = message.replaceAll("\\{" + n + "\\}", args[n].toString());
            }
            n++;
        }
        return message;
    }

    /**
     * エラーメッセージのリストを、改行文字で連結した文字列に変換します。
     * 
     * @param errorMessages エラーメッセージのリスト
     * @return メッセージ
     */
    public static String serialize(final List<String> errorMessages) {
        final StringBuilder sb = new StringBuilder();
        final String lineSeparator = System.getProperty("line.separator");
        for (String error : errorMessages) {
            sb.append(lineSeparator + error);
        }
        return sb.toString().substring(lineSeparator.length());
    }
}
