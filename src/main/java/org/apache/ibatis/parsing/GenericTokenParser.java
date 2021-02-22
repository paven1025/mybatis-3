/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * @author Clinton Begin
 */
public class GenericTokenParser {

  /**
   * 起始Token字符串
   */
  private final String openToken;

  /**
   * 结束Token字符串
   */
  private final String closeToken;

  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    // 空文本返回
    if (text == null || text.isEmpty()) {
      return "";
    }
    // 查询起始Token位置
    int start = text.indexOf(openToken);
    // 找不到就返回
    if (start == -1) {
      return text;
    }
    char[] src = text.toCharArray();
    // 起始查找位置
    int offset = 0;
    // 结果
    final StringBuilder builder = new StringBuilder();
    StringBuilder expression = null;
    do {
      // 判断openToken前一位是转义符
      if (start > 0 && src[start - 1] == '\\') {
        // 如果openToken前一位是转义符则忽略
        // 添加 [offset, start - offset - 1] 和 openToken 的内容，添加到 builder 中
        builder.append(src, offset, start - offset - 1).append(openToken);
        // 指针移到openToken后一位
        offset = start + openToken.length();
      }
      else {
        // found open token. let's search close token.
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        builder.append(src, offset, start - offset);
        // 指针移到openToken后一位
        offset = start + openToken.length();
        // 指针后的第一个closeToken
        int end = text.indexOf(closeToken, offset);
        // 有 closeToken
        while (end > -1) {
          // 判断closeToken前一位是转义符
          if (end > offset && src[end - 1] == '\\') {
            // 如果 closeToken 前一位是转义符则忽略
            // 添加 [offset, end - offset - 1] 和 closeToken 的内容，添加到 builder 中
            expression.append(src, offset, end - offset - 1).append(closeToken);
            // 指针移到closeToken后一位
            offset = end + closeToken.length();
            // 指针后的第一个closeToken
            end = text.indexOf(closeToken, offset);
          } else {
            // 添加 [offset, end - offset] 的内容，添加到 builder 中
            expression.append(src, offset, end - offset);
            break;
          }
        }
        // 没有closeToken
        if (end == -1) {
          // close token was not found.
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          builder.append(handler.handleToken(expression.toString()));
          offset = end + closeToken.length();
        }
      }
      start = text.indexOf(openToken, offset);
    } while (start > -1);
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
