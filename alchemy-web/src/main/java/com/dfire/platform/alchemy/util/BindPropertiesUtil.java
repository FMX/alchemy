package com.dfire.platform.alchemy.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonToken;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.io.IOContext;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.yaml.YAMLParser;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class BindPropertiesUtil {

    private static final char IGNORE_CHAR = '.';

    private static final char SPECIAL_CHAR = '-';

    private static final ObjectMapper OBJECT_MAPPER = new LowerCaseYamlMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T bindProperties(Map<String, Object> params, Class<T> beanClass) {
        return JsonUtil.fromJson(JsonUtil.toJson(params), beanClass);
    }

    public static <T> T bindProperties(String value, Class<T> clazz) throws Exception {
        return OBJECT_MAPPER.readValue(value, clazz);
    }

    public static <T> T bindProperties(File inputFile, Class<T> clazz) throws Exception {
        return OBJECT_MAPPER.readValue(inputFile.toURL(), clazz);
    }

    public static class LowerCaseYamlMapper extends ObjectMapper {
        public LowerCaseYamlMapper() {
            super(new YAMLFactory() {
                @Override
                protected YAMLParser _createParser(InputStream in, IOContext ctxt) throws IOException {
                    final Reader r = _createReader(in, null, ctxt);
                    // normalize all key to lower case keys
                    return new YAMLParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
                        _objectCodec, r) {
                        @Override
                        public String getCurrentName() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getCurrentName();
                        }

                        @Override
                        public String getText() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getText();
                        }
                    };
                }

                @Override
                public YAMLParser createParser(String content) throws IOException {
                    final Reader reader = new StringReader(content);
                    IOContext ctxt = this._createContext(reader, true);
                    Reader r = this._decorate(reader, ctxt);
                    return new YAMLParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
                        _objectCodec, r) {
                        @Override
                        public String getCurrentName() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getCurrentName();
                        }

                        @Override
                        public String getText() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getText();
                        }
                    };
                }
            });
        }
    }

    static String translate(String input) {
        if (input == null) {
            return input;
        } else {
            int length = input.length();
            if (length == 0) {
                return input;
            } else {
                StringBuilder result = new StringBuilder(length + (length << 1));
                int upperCount = 0;
                int ignoreCount = 0 ;
                for (int i = 0; i < length; ++i) {
                    char ch = input.charAt(i);
                    if(upperCount > 0 && ignoreCount > 0){
                        return input;
                    }
                    if (upperCount > 0) {
                        char uc = Character.toUpperCase(ch);
                        result.append(uc);
                        upperCount = 0;
                        continue;
                    }
                    if (ch == SPECIAL_CHAR) {
                        ++upperCount;
                    }else if(ch == IGNORE_CHAR){
                        result.append(ch);
                        ++ ignoreCount;
                    }else {
                        result.append(Character.toLowerCase(ch));
                    }
                }

                return result.toString();
            }
        }
    }
}
