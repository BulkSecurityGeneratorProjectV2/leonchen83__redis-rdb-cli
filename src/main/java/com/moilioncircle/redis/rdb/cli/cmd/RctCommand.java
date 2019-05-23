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

package com.moilioncircle.redis.rdb.cli.cmd;

import com.moilioncircle.redis.rdb.cli.conf.Configure;
import com.moilioncircle.redis.rdb.cli.ext.CliRedisReplicator;
import com.moilioncircle.redis.rdb.cli.glossary.DataType;
import com.moilioncircle.redis.rdb.cli.glossary.Escape;
import com.moilioncircle.redis.rdb.cli.glossary.Format;
import com.moilioncircle.redis.rdb.cli.util.ProgressBar;
import com.moilioncircle.redis.replicator.FileType;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.Replicators;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreCommandSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;

import java.io.File;
import java.util.List;

/**
 * @author Baoyi Chen
 */
public class RctCommand extends AbstractCommand {

    private static final Option HELP = Option.builder("h").longOpt("help").required(false).hasArg(false).desc("rct usage.").build();
    private static final Option VERSION = Option.builder("v").longOpt("version").required(false).hasArg(false).desc("rct version.").build();
    private static final Option FORMAT = Option.builder("f").longOpt("format").required(false).hasArg().argName("format").type(String.class).desc("format to export. valid formats are json, dump, diff, key, keyval, count, mem and resp").build();
    private static final Option SOURCE = Option.builder("s").longOpt("source").required(false).hasArg().argName("source").type(String.class).desc("<source> eg:\n /path/to/dump.rdb redis://host:port?authPassword=foobar redis:///path/to/dump.rdb.").build();
    private static final Option OUTPUT = Option.builder("o").longOpt("out").required(false).hasArg().argName("file").type(File.class).desc("output file.").build();
    private static final Option DB = Option.builder("d").longOpt("db").required(false).hasArg().argName("num num...").valueSeparator(' ').type(Number.class).desc("database number. multiple databases can be provided. if not specified, all databases will be included.").build();
    private static final Option KEY = Option.builder("k").longOpt("key").required(false).hasArg().argName("regex regex...").valueSeparator(' ').type(String.class).desc("keys to export. this can be a regex. if not specified, all keys will be returned.").build();
    private static final Option TYPE = Option.builder("t").longOpt("type").required(false).hasArgs().argName("type type...").valueSeparator(' ').type(String.class).desc("data type to export. possible values are string, hash, set, sortedset, list, module, stream. multiple types can be provided. if not specified, all data types will be returned.").build();
    private static final Option BYTES = Option.builder("b").longOpt("bytes").required(false).hasArgs().argName("bytes").type(Number.class).desc("limit memory output(--format mem) to keys greater to or equal to this value (in bytes)").build();
    private static final Option LARGEST = Option.builder("l").longOpt("largest").required(false).hasArg().argName("n").type(Number.class).desc("limit memory output(--format mem) to only the top n keys (by size).").build();
    private static final Option ESCAPE = Option.builder("e").longOpt("escape").required(false).hasArg().argName("escape").type(String.class).desc("escape strings to encoding: raw (default), redis.").build();
    private static final Option REPLACE = Option.builder("r").longOpt("replace").required(false).desc("whether the generated aof with <replace> parameter(--format dump). if not specified, default value is false.").build();

    private static final String HEADER = "rct -f <format> -s <source> -o <file> [-d <num num...>] [-e <escape>] [-k <regex regex...>] [-t <type type...>] [-b <bytes>] [-l <n>] [-r]";
    private static final String EXAMPLE = "\nexamples:\n rct -f dump -s ./dump.rdb -o ./appendonly.aof -r\n rct -f resp -s redis://127.0.0.1:6379 -o ./target.aof -d 0 1\n rct -f json -s ./dump.rdb -o ./target.json -k user.* product.*\n rct -f mem -s ./dump.rdb -o ./target.aof -e redis -t list -l 10 -b 1024\n";

    @Override
    public String name() {
        return "rct";
    }

    private RctCommand() {
        addOption(HELP);
        addOption(VERSION);
        addOption(FORMAT);
        addOption(SOURCE);
        addOption(OUTPUT);
        addOption(DB);
        addOption(KEY);
        addOption(TYPE);
        addOption(BYTES);
        addOption(REPLACE);
        addOption(LARGEST);
        addOption(ESCAPE);
    }

    @Override
    protected void doExecute(CommandLine line) throws Exception {
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(HEADER, "\noptions:", options, EXAMPLE);
        } else if (line.hasOption("version")) {
            writeLine(version());
        } else {
            StringBuilder sb = new StringBuilder();
            if (!line.hasOption("format")) {
                sb.append("f ");
            }

            if (!line.hasOption("source")) {
                sb.append("s ");
            }

            if (!line.hasOption("out")) {
                sb.append("o ");
            }

            if (sb.length() > 0) {
                writeError("Missing required options: " + sb.toString() + ", Try `rct -h` for more information.");
                return;
            }

            String source = line.getOption("source");
            File output = line.getOption("out");
            String format = line.getOption("format");

            List<Long> db = line.getOptions("db");
            Long bytes = line.getOption("bytes");
            Long largest = line.getOption("largest");
            String escape = line.getOption("escape");
            List<String> type = line.getOptions("type");
            boolean replace = line.hasOption("replace");
            List<String> regexs = line.getOptions("key");

            source = normalize(source, FileType.RDB, "Invalid options: s, Try `rct -h` for more information.");

            try (ProgressBar bar = new ProgressBar(-1)) {
                Configure configure = Configure.bind();
                Replicator r = new CliRedisReplicator(source, configure);
                r.addExceptionListener((rep, tx, e) -> {
                    throw new RuntimeException(tx.getMessage(), tx);
                });
                Format.parse(format).dress(r, configure, output, db, regexs, largest, bytes, DataType.parse(type), Escape.parse(escape), replace);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> Replicators.closeQuietly(r)));
                r.addEventListener((rep, event) -> {
                    if (event instanceof PreRdbSyncEvent)
                        rep.addRawByteListener(b -> bar.react(b.length));
                    if (event instanceof PostRdbSyncEvent || event instanceof PreCommandSyncEvent)
	                    Replicators.closeQuietly(rep);
                });
                r.open();
            }
        }
    }

    public static void run(String[] args) throws Exception {
        RctCommand command = new RctCommand();
        command.execute(args);
    }
}
