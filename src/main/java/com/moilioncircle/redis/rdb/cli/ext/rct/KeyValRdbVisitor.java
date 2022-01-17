/*
 * Copyright 2018-2019 Baoyi Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moilioncircle.redis.rdb.cli.ext.rct;

import static com.moilioncircle.redis.replicator.Constants.RDB_LOAD_NONE;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.moilioncircle.redis.rdb.cli.api.format.escape.Escaper;
import com.moilioncircle.redis.rdb.cli.conf.Configure;
import com.moilioncircle.redis.rdb.cli.ext.AbstractRdbVisitor;
import com.moilioncircle.redis.rdb.cli.ext.DumpRawByteListener;
import com.moilioncircle.redis.rdb.cli.ext.datatype.DummyKeyValuePair;
import com.moilioncircle.redis.rdb.cli.glossary.DataType;
import com.moilioncircle.redis.rdb.cli.util.OutputStreams;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.io.RedisInputStream;
import com.moilioncircle.redis.replicator.rdb.BaseRdbParser;
import com.moilioncircle.redis.replicator.rdb.datatype.ContextKeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.Module;
import com.moilioncircle.redis.replicator.rdb.module.ModuleParser;
import com.moilioncircle.redis.replicator.util.Strings;

/**
 * @author Baoyi Chen
 */
public class KeyValRdbVisitor extends AbstractRdbVisitor {
    
    public KeyValRdbVisitor(Replicator replicator, Configure configure, File out, List<Long> db, List<String> regexs, List<DataType> types, Escaper escaper) {
        super(replicator, configure, out, db, regexs, types, escaper);
    }
    
    @Override
    public Event doApplyString(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        byte[] val = parser.rdbLoadEncodedStringObject().first();
        quote(val, out);
        OutputStreams.write('\n', out);
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplyList(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            quote(element, out);
            if (len - 1 > 0) delimiter(out);
            else OutputStreams.write('\n', out);
            len--;
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplySet(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            quote(element, out);
            if (len - 1 > 0) delimiter(out);
            else OutputStreams.write('\n', out);
            len--;
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplyZSet(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            double score = parser.rdbLoadDoubleValue();
            quote(element, out);
            delimiter(out);
            escaper.encode(score, out);
            if (len - 1 > 0) delimiter(out);
            else OutputStreams.write('\n', out);
            len--;
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplyZSet2(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            double score = parser.rdbLoadBinaryDoubleValue();
            quote(element, out);
            delimiter(out);
            escaper.encode(score, out);
            if (len - 1 > 0) delimiter(out);
            else OutputStreams.write('\n', out);
            len--;
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplyHash(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        while (len > 0) {
            byte[] field = parser.rdbLoadEncodedStringObject().first();
            byte[] value = parser.rdbLoadEncodedStringObject().first();
            quote(field, out);
            delimiter(out);
            quote(value, out);
            if (len - 1 > 0) delimiter(out);
            else OutputStreams.write('\n', out);
            len--;
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplyHashZipMap(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zmlen(stream); // zmlen
        boolean flag = true;
        while (true) {
            int zmEleLen = BaseRdbParser.LenHelper.zmElementLen(stream);
            if (zmEleLen == 255) {
                OutputStreams.write('\n', out);
                return context.valueOf(new DummyKeyValuePair());
            }
            byte[] field = BaseRdbParser.StringHelper.bytes(stream, zmEleLen);
            zmEleLen = BaseRdbParser.LenHelper.zmElementLen(stream);
            if (zmEleLen == 255) {
                if (!flag) delimiter(out);
                quote(field, out);
                delimiter(out);
                quote(null, out);
                OutputStreams.write('\n', out);
                return context.valueOf(new DummyKeyValuePair());
            }
            int free = BaseRdbParser.LenHelper.free(stream);
            byte[] value = BaseRdbParser.StringHelper.bytes(stream, zmEleLen);
            BaseRdbParser.StringHelper.skip(stream, free);
            if (flag) flag = false;
            else delimiter(out);
            quote(field, out);
            delimiter(out);
            quote(value, out);
        }
    }
    
    @Override
    public Event doApplyListZipList(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        for (int i = 0; i < zllen; i++) {
            byte[] e = BaseRdbParser.StringHelper.zipListEntry(stream);
            quote(e, out);
            if (i + 1 < zllen) delimiter(out);
            else OutputStreams.write('\n', out);
        }
        int zlend = BaseRdbParser.LenHelper.zlend(stream);
        if (zlend != 255) {
            throw new AssertionError("zlend expect 255 but " + zlend);
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplySetIntSet(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        int encoding = BaseRdbParser.LenHelper.encoding(stream);
        long lenOfContent = BaseRdbParser.LenHelper.lenOfContent(stream);
        for (long i = 0; i < lenOfContent; i++) {
            String element;
            switch (encoding) {
                case 2:
                    element = String.valueOf(stream.readInt(2));
                    break;
                case 4:
                    element = String.valueOf(stream.readInt(4));
                    break;
                case 8:
                    element = String.valueOf(stream.readLong(8));
                    break;
                default:
                    throw new AssertionError("expect encoding [2,4,8] but:" + encoding);
            }
            quote(element.getBytes(), out);
            if (i + 1 < lenOfContent) delimiter(out);
            else OutputStreams.write('\n', out);
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplyZSetZipList(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        while (zllen > 0) {
            byte[] element = BaseRdbParser.StringHelper.zipListEntry(stream);
            zllen--;
            double score = Double.valueOf(Strings.toString(BaseRdbParser.StringHelper.zipListEntry(stream)));
            zllen--;
            quote(element, out);
            delimiter(out);
            escaper.encode(score, out);
            if (zllen - 1 > 0) delimiter(out);
            else OutputStreams.write('\n', out);
        }
        int zlend = BaseRdbParser.LenHelper.zlend(stream);
        if (zlend != 255) {
            throw new AssertionError("zlend expect 255 but " + zlend);
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    protected Event doApplyZSetListPack(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        // TODO
        return null;
    }
    
    @Override
    public Event doApplyHashZipList(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        while (zllen > 0) {
            byte[] field = BaseRdbParser.StringHelper.zipListEntry(stream);
            zllen--;
            byte[] value = BaseRdbParser.StringHelper.zipListEntry(stream);
            zllen--;
            quote(field, out);
            delimiter(out);
            quote(value, out);
            if (zllen - 1 > 0) delimiter(out);
            else OutputStreams.write('\n', out);
        }
        int zlend = BaseRdbParser.LenHelper.zlend(stream);
        if (zlend != 255) {
            throw new AssertionError("zlend expect 255 but " + zlend);
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    protected Event doApplyHashListPack(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        // TODO
        return null;
    }
    
    @Override
    public Event doApplyListQuickList(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        for (long i = 0; i < len; i++) {
            RedisInputStream stream = new RedisInputStream(parser.rdbGenericLoadStringObject(RDB_LOAD_NONE));
            BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
            BaseRdbParser.LenHelper.zltail(stream); // zltail
            int zllen = BaseRdbParser.LenHelper.zllen(stream);
            for (int j = 0; j < zllen; j++) {
                byte[] e = BaseRdbParser.StringHelper.zipListEntry(stream);
                quote(e, out);
                if (i + 1 < len) delimiter(out);
                else if (j + 1 < zllen) delimiter(out);
                else OutputStreams.write('\n', out);
            }
            int zlend = BaseRdbParser.LenHelper.zlend(stream);
            if (zlend != 255) {
                throw new AssertionError("zlend expect 255 but " + zlend);
            }
        }
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    protected Event doApplyListQuickList2(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        // TODO
        return null;
    }
    
    @Override
    public Event doApplyModule(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        out.write(configure.getQuote());
        version = configure.getDumpRdbVersion() == -1 ? version : configure.getDumpRdbVersion();
        try (DumpRawByteListener listener = new DumpRawByteListener(replicator, version, out, escaper)) {
            listener.write((byte) type);
            super.doApplyModule(in, version, key, contains, type, context);
        }
        OutputStreams.write('\n', out);
        out.write(configure.getQuote());
        return context.valueOf(new DummyKeyValuePair());
    }
    
    @Override
    public Event doApplyModule2(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        out.write(configure.getQuote());
        version = configure.getDumpRdbVersion() == -1 ? version : configure.getDumpRdbVersion();
        try (DumpRawByteListener listener = new DumpRawByteListener(replicator, version, out, escaper)) {
            listener.write((byte) type);
            super.doApplyModule2(in, version, key, contains, type, context);
        }
        out.write(configure.getQuote());
        OutputStreams.write('\n', out);
        return context.valueOf(new DummyKeyValuePair());
    }
    
    protected ModuleParser<? extends Module> lookupModuleParser(String moduleName, int moduleVersion) {
        return replicator.getModuleParser(moduleName, moduleVersion);
    }
    
    @Override
    public Event doApplyStreamListPacks(RedisInputStream in, int version, byte[] key, boolean contains, int type, ContextKeyValuePair context) throws IOException {
        quote(key, out);
        delimiter(out);
        out.write(configure.getQuote());
        version = configure.getDumpRdbVersion() == -1 ? version : configure.getDumpRdbVersion();
        try (DumpRawByteListener listener = new DumpRawByteListener(replicator, version, out, escaper)) {
            listener.write((byte) type);
            super.doApplyStreamListPacks(in, version, key, contains, type, context);
        }
        out.write(configure.getQuote());
        OutputStreams.write('\n', out);
        return context.valueOf(new DummyKeyValuePair());
    }
}