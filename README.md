# redis-rdb-cli

<a href="https://www.buymeacoffee.com/leonchen83" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>

A tool that can parse, filter, split, merge rdb and analyze memory usage offline. It can also sync 2 redis data and allow user define there own sink service to migrate redis data to somewhere.

[![Java CI](https://github.com/leonchen83/redis-rdb-cli/actions/workflows/maven.yml/badge.svg)](https://github.com/leonchen83/redis-rdb-cli/actions/workflows/maven.yml)
[![Gitter](https://badges.gitter.im/leonchen83/redis-rdb-cli.svg)](https://gitter.im/leonchen83/redis-rdb-cli?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)](https://github.com/leonchen83/redis-rdb-cli/blob/master/LICENSE)  
  
## Chat with author  
  
[![Join the chat at https://gitter.im/leonchen83/redis-rdb-cli](https://badges.gitter.im/leonchen83/redis-rdb-cli.svg)](https://gitter.im/leonchen83/redis-rdb-cli?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)  
  
## Contract the author
  
**chen.bao.yi@gmail.com**  
  
## Binary release

[binary releases](https://github.com/leonchen83/redis-rdb-cli/releases)

## Runtime requirement

```text
jdk 1.8+
```

## Install

```shell
$ wget https://github.com/leonchen83/redis-rdb-cli/releases/download/${version}/redis-rdb-cli-release.zip
$ unzip redis-rdb-cli-release.zip
$ cd ./redis-rdb-cli/bin
$ ./rct -h
```

## Compile requirement

```text

jdk 1.8+
maven-3.3.1+

```

## Compile & run

```shell
$ git clone https://github.com/leonchen83/redis-rdb-cli.git
$ cd redis-rdb-cli
$ mvn clean install -Dmaven.test.skip=true
$ cd target/redis-rdb-cli-release/redis-rdb-cli/bin
$ ./rct -h 
```

## Run in docker

```shell
# run with jvm
$ docker run -it --rm redisrdbcli/redis-rdb-cli:latest
$ rct -V

# run without jvm
$ docker run -it --rm redisrdbcli/redis-rdb-cli:latest-native
$ rct -V
```

## Build native image via graalvm in docker
```shell
$ docker build -m 8g -f DockerfileNative -t redisrdbcli:redis-rdb-cli .
$ docker run -it redisrdbcli:redis-rdb-cli bash
$ bash-5.1# rct -V
```

## Windows Environment Variables
  
Add `/path/to/redis-rdb-cli/bin` to `Path` environment variable  
  
## Usage

```text

Usage: rmonitor [-hV] -s <uri> [-n <name>]

Options:
  -h, --help           Show this help message and exit.
  -n, --name <name>    Monitor name.
  -s, --source <uri>   Source uri. eg: redis://host:port?authPassword=foobar.
  -V, --version        Print version information and exit.

Examples:
  rmonitor -s redis://127.0.0.1:6379 -n default

```

```text

Usage: rct [-hV] -f <format> -s <source> -o <file> [-e <escape>]
       [-d <db>...] [-k <regex>...>] [-t <type>...] [-b <bytes>]
       [-l <n>] [-r]

Options:
  -b, --bytes <bytes>     Limit memory output(--format mem) to keys
                          greater to or equal to this value (in bytes)
  -d, --db <db>...        Database number. multiple databases can be
                          provided. if not specified, all databases
                          will be included.
  -e, --escape <escape>   Escape strings to encoding: raw (default),
                          redis, json.
  -f, --format <format>   Format to export. valid formats are json,
                          jsonl, dump, diff, key, keyval, count, mem
                          and resp
  -h, --help              Show this help message and exit.
  -k, --key <regex>...    Keys to export. this can be a regex. if not
                          specified, all keys will be returned.
  -l, --largest <n>       Limit memory output(--format mem) to only the
                          top n keys (by size).
  -o, --out <file>        Output file.
  -r, --replace           Whether the generated aof with <replace>
                          parameter(--format dump). if not specified,
                          default value is false.
  -s, --source <source>   Source file or uri. eg:
                          /path/to/dump.rdb
                          redis://host:port?authPassword=foobar
                          redis:///path/to/dump.rdb.
  -t, --type <type>...    Data type to export. possible values are
                          string, hash, set, sortedset, list, module,
                          stream. multiple types can be provided. if not
                          specified, all data types will be returned.
  -V, --version           Print version information and exit.

Examples:
  rct -f dump -s ./dump.rdb -o ./appendonly.aof -r
  rct -f resp -s redis://127.0.0.1:6379 -o ./target.aof -d 0 1
  rct -f json -s ./dump.rdb -o ./target.json -k user.* product.*
  rct -f mem -s ./dump.rdb -o ./target.aof -e redis -t list -l 10 -b 1024

```


```text

Usage: rmt [-hV] -s <source> (-m <uri> | -c <conf>) [-d <db>...]
       [-k <regex>...] [-t <type>...] [-rl]

Options:
  -c, --config <conf>     Migrate data to cluster via redis cluster's
                          <nodes.conf> file, if specified, no need to
                          specify --migrate.
  -d, --db <db>...        Database number. multiple databases can be
                          provided. if not specified, all databases
                          will be included.
  -h, --help              Show this help message and exit.
  -k, --key <regex>...    Keys to export. this can be a regex. if not
                          specified, all keys will be returned.
  -l, --legacy            If specify the <replace> and this parameter.
                          then use lua script to migrate data to target.
                          if target redis version is greater than 3.0.
                          no need to add this parameter.
  -m, --migrate <uri>     Migrate to uri. eg:
                          redis://host:port?authPassword=foobar.
  -r, --replace           Replace exist key value. if not specified,
                          default value is false.
  -s, --source <source>   Source file or uri. eg:
                          /path/to/dump.rdb
                          redis://host:port?authPassword=foobar
                          redis:///path/to/dump.rdb.
  -t, --type <type>...    Data type to export. possible values are
                          string, hash, set, sortedset, list, module,
                          stream. multiple types can be provided. if not
                          specified, all data types will be returned.
  -V, --version           Print version information and exit.

Examples:
  rmt -s ./dump.rdb -c ./nodes.conf -t string -r
  rmt -s ./dump.rdb -m redis://127.0.0.1:6380 -t list -d 0
  rmt -s redis://127.0.0.1:6379 -m redis://127.0.0.1:6380 -d 0

```

```text

Usage: rdt [-hV] (-b <source> [-g <db>] | -s <source> -c <conf>
       | -m <file>...) -o <file> [-d <db>...] [-k <regex>...]
       [-t <type>...]

Options:
  -b, --backup <source>   Backup <source> to local rdb file. eg:
                          /path/to/dump.rdb
                          redis://host:port?authPassword=foobar
                          redis:///path/to/dump.rdb
  -c, --config <conf>     Redis cluster's <nodes.conf> file(--split
                          <source>).
  -d, --db <db>...        Database number. multiple databases can be
                          provided. if not specified, all databases
                          will be included.
  -g, --goal <db>         Convert db from <source> and save to rdb
                          file as <db>.
  -h, --help              Show this help message and exit.
  -k, --key <regex>...    Keys to export. this can be a regex. if not
                          specified, all keys will be returned.
  -m, --merge <file>...   Merge multi rdb files to one rdb file.
  -o, --out <file>        If --backup <source> or --merge <file>...
                          specified. the <file> is the target file.
                          if --split <source> specified. the <file>
                          is the target path.
  -s, --split <source>    Split rdb to multi rdb files via cluster's
                          <nodes.conf>. eg:
                          /path/to/dump.rdb
                          redis://host:port?authPassword=foobar
                          redis:///path/to/dump
  -t, --type <type>...    Data type to export. possible values are
                          string, hash, set, sortedset, list, module,
                          stream. multiple types can be provided. if not
                          specified, all data types will be returned.
  -V, --version           Print version information and exit.

Examples:
  rdt -b ./dump.rdb -o ./dump.rdb1 -d 0 1
  rdt -b ./dump.rdb -o ./dump.rdb1 -d 0 1 -g 3
  rdt -b redis://127.0.0.1:6379 -o ./dump.rdb -k user.*
  rdt -m ./dump1.rdb ./dump2.rdb -o ./dump.rdb -t hash
  rdt -s ./dump.rdb -c ./nodes.conf -o /path/to/folder -t hash -d 0
  rdt -s redis://127.0.0.1:6379 -c ./nodes.conf -o /path/to/folder -d 0

```

```text

Usage: rst [-hV] -s <uri> (-m <uri> | -c <conf>) [-d <db>...] [-rl]

Options:
  -c, --config <conf>   Migrate data to cluster via redis cluster's
                        <nodes.conf> file, if specified, no need to
                        specify --migrate.
  -d, --db <db>...      Database number. multiple databases can be
                        provided. if not specified, all databases
                        will be included.
  -h, --help            Show this help message and exit.
  -l, --legacy          If specify the <replace> and this parameter.
                        then use lua script to migrate data to target.
                        if target redis version is greater than 3.0.
                        no need to add this parameter.
  -m, --migrate <uri>   Migrate to uri. eg:
                        redis://host:port?authPassword=foobar.
  -r, --replace         Replace exist key value. if not specified,
                        default value is false.
  -s, --source <uri>    Redis uri. eg:
                        redis://host:port?authPassword=foobar
  -V, --version         Print version information and exit.

Examples:
  rst -s redis://127.0.0.1:6379 -c ./nodes.conf -r
  rst -s redis://127.0.0.1:6379 -m redis://127.0.0.1:6380 -d 0

```

```text

Usage: ret [-hV] -s <uri> [-c <conf>] [-p <parser>] -n <sink>

Options:
  -c, --config <conf>     External config file, if not specified,
                          default value is null.
  -h, --help              Show this help message and exit.
  -n, --name <sink>       Sink service name, registered sink service:
                          example.
  -p, --parser <parser>   Parser service name, registered parser
                          service: default, dump. if not specified,
                          default value is default
  -s, --source <uri>      Redis uri. eg:
                          redis://host:port?authPassword=foobar
  -V, --version           Print version information and exit.

Examples:
  ret -s redis://127.0.0.1:6379 -c ./config.conf -n example
  ret -s redis://127.0.0.1:6379 -c ./config.conf -p dump -n example

```

```text

Usage: rcut [-hV] -s <source> -r <file> -a <file>

Options:
  -a, --aof <file>        Output aof file.
  -h, --help              Show this help message and exit.
  -r, --rdb <file>        Output rdb file.
  -s, --source <source>   Source file that be cutted. the file
                          format MUST BE aof-use-rdb-preamble.
                          eg: /path/to/appendonly.aof
  -V, --version           Print version information and exit.

Examples:
  rcut -s ./aof-use-rdb-preamble.aof -r ./dump.rdb -a ./appendonly.aof

```

### Filter

1. `rct`, `rdt` and `rmt` these 3 commands support data filter by `type`,`db` and `key` RegEx(Java style).  
2. `rst` this command only support data filter by `db`.  
  
For example:

```shell

$ rct -f dump -s /path/to/dump.rdb -o /path/to/dump.aof -d 0
$ rct -f dump -s /path/to/dump.rdb -o /path/to/dump.aof -t string hash
$ rmt -s /path/to/dump.rdb -m redis://192.168.1.105:6379 -r -d 0 1 -t list
$ rst -s redis://127.0.0.1:6379 -m redis://127.0.0.1:6380 -d 0
```

### Monitor redis server

```shell
# step1 
# open file `/path/to/redis-rdb-cli/conf/redis-rdb-cli.conf`
# change property `metric_gateway from `none` to `influxdb`
#
# step2
$ cd /path/to/redis-rdb-cli/dashboard
$ docker-compose up -d
#
# step3
$ rmonitor -s redis://127.0.0.1:6379 -n standalone
$ rmonitor -s redis://127.0.0.1:30001 -n cluster
$ rmonitor -s redis-sentinel://sntnl-usr:sntnl-pwd@127.0.0.1:26379?master=mymaster&authUser=usr&authPassword=pwd -n sentinel
#
# step4
# open url `http://localhost:3000/d/monitor/monitor`, login grafana use `admin`, `admin` and check monitor result.
```

![monitor](./images/monitor.png)

### Redis mass insertion

```shell

$ rct -f dump -s /path/to/dump.rdb -o /path/to/dump.aof -r
$ cat /path/to/dump.aof | /redis/src/redis-cli -p 6379 --pipe

```

### Convert rdb to dump format

```shell
$ rct -f dump -s /path/to/dump.rdb -o /path/to/dump.aof
```

### Convert rdb to json format

```shell
$ rct -f json -s /path/to/dump.rdb -o /path/to/dump.json
```

### Numbers of key in rdb

```shell
$ rct -f count -s /path/to/dump.rdb -o /path/to/dump.csv
```

### Find top 50 largest keys

```shell
$ rct -f mem -s /path/to/dump.rdb -o /path/to/dump.mem -l 50
```

### Diff rdb

```shell
$ rct -f diff -s /path/to/dump1.rdb -o /path/to/dump1.diff
$ rct -f diff -s /path/to/dump2.rdb -o /path/to/dump2.diff
$ diff /path/to/dump1.diff /path/to/dump2.diff
```

### Convert rdb to RESP

```shell
$ rct -f resp -s /path/to/dump.rdb -o /path/to/appendonly.aof
```

### Sync with 2 redis
```shell
$ rst -s redis://127.0.0.1:6379 -m redis://127.0.0.1:6380 -r
```

### Sync single redis to redis cluster
```shell
$ rst -s redis://127.0.0.1:6379 -m redis://127.0.0.1:30001 -r -d 0
```

### Migrate rdb to remote redis

```shell
$ rmt -s /path/to/dump.rdb -m redis://192.168.1.105:6379 -r
```

### Migrate rdb to remote redis cluster

```shell
$ rmt -s /path/to/dump.rdb -c ./nodes-30001.conf -r
```
  
or simply use following cmd without `nodes-30001.conf`  
  
```shell
$ rmt -s /path/to/dump.rdb -m redis://127.0.0.1:30001 -r
```

### Backup remote rdb

```shell
$ rdt -b redis://192.168.1.105:6379 -o /path/to/dump.rdb
```

### Backup remote rdb and convert db to dest db

```shell
$ rdt -b redis://192.168.1.105:6379 -o /path/to/dump.rdb --goal 3
```

### Filter rdb

```shell
$ rdt -b /path/to/dump.rdb -o /path/to/filtered-dump.rdb -d 0 -t string
```

### Split rdb via cluster's nodes.conf

```shell
$ rdt -s ./dump.rdb -c ./nodes.conf -o /path/to/folder -d 0
```

### Merge multi rdb to one

```shell
$ rdt -m ./dump1.rdb ./dump2.rdb -o ./dump.rdb -t hash
```

### Cut aof-use-rdb-preamble file to rdb file and aof file

```shell
$ rcut -s ./aof-use-rdb-preamble.aof -r ./dump.rdb -a ./appendonly.aof
```

### Other parameter

More configurable parameter can be modified in `/path/to/redis-rdb-cli/conf/redis-rdb-cli.conf`

## Difference between rmt and rst

1. When `rmt` started. source redis first do `BGSAVE` and generate a snapshot rdb file. `rmt` command migrate this snapshot file to target redis. after this process done, `rmt` terminated.  
2. `rst` not only migrate snapshot rdb file but also incremental data from source redis. so `rst` never terminated except type `CTRL+C`. `rst` only support `db` filter more details please refer to [Limitation of migration](#limitation-of-migration) 

## Dashboard

Since `v0.1.9`, the `rct -f mem` support showing result in grafana dashboard like the following:  
![memory](./images/memory.png)  

If you want to turn it on. you **MUST** install `docker` and `docker-compose` first, the installation please refer to [docker](https://docs.docker.com/install/)  
Then run the following command:  

```shell
$ cd /path/to/redis-rdb-cli/dashboard

# start
$ docker-compose up -d

# stop
$ docker-compose down
```
  
`cd /path/to/redis-rdb-cli/conf/redis-rdb-cli.conf`  
Then change parameter [metric_gateway](https://github.com/leonchen83/redis-rdb-cli/blob/master/src/main/resources/redis-rdb-cli.conf#L169) from `none` to `influxdb`.  
  
Open `http://localhost:3000` to check the `rct -f mem`'s result.  
  
If you deployed this tool in multi instance, you need to change parameter [metric_instance](https://github.com/leonchen83/redis-rdb-cli/blob/master/src/main/resources/redis-rdb-cli.conf#L215) to make sure unique between instances.  
  
## Redis 6
  
### Redis 6 SSL
  
1. use openssl to generate keystore
  
```shell

$ cd /path/to/redis-6.0-rc1
$ ./utils/gen-test-certs.sh
$ cd tests/tls
$ openssl pkcs12 -export -CAfile ca.crt -in redis.crt -inkey redis.key -out redis.p12

```
  
2. If source redis and target redis use the same keystore. then config following parameters  
[source_keystore_path](https://github.com/leonchen83/redis-rdb-cli/blob/master/src/main/resources/redis-rdb-cli.conf#L230) and [target_keystore_path](https://github.com/leonchen83/redis-rdb-cli/blob/master/src/main/resources/redis-rdb-cli.conf#L259) to point to `/path/to/redis-6.0-rc1/tests/tls/redis.p12`  
set [source_keystore_pass](https://github.com/leonchen83/redis-rdb-cli/blob/master/src/main/resources/redis-rdb-cli.conf#L238) and [target_keystore_pass](https://github.com/leonchen83/redis-rdb-cli/blob/master/src/main/resources/redis-rdb-cli.conf#L267)  
  
3. after config ssl parameters use `rediss://host:port` in your command to open ssl, for example: `rst -s rediss://127.0.0.1:6379 -m rediss://127.0.0.1:30001 -r -d 0`
  
### Redis 6 ACL

1. use following URI to open redis ACL support  
  
```shell
$ rst -s redis://user:pass@127.0.0.1:6379 -m redis://user:pass@127.0.0.1:6380 -r -d 0
```
  
2. `user` **MUST** have `+@all` permission to handle commands
  
## Hack rmt

### Rmt threading model

The `rmt` command use the following 4 parameters([redis-rdb-cli.conf](https://github.com/leonchen83/redis-rdb-cli/blob/master/src/main/resources/redis-rdb-cli.conf)) to migrate data to remote.  
  
```properties
migrate_batch_size=4096
migrate_threads=4
migrate_flush=yes
migrate_retries=1
```

The most important parameter is `migrate_threads=4`. this means we use the following threading model to migrate data.  

```text

single redis ----> single redis

+--------------+         +----------+     thread 1      +--------------+
|              |    +----| Endpoint |-------------------|              |
|              |    |    +----------+                   |              |
|              |    |                                   |              |
|              |    |    +----------+     thread 2      |              |
|              |    |----| Endpoint |-------------------|              |
|              |    |    +----------+                   |              |
| Source Redis |----|                                   | Target Redis |
|              |    |    +----------+     thread 3      |              |
|              |    |----| Endpoint |-------------------|              |
|              |    |    +----------+                   |              |
|              |    |                                   |              |
|              |    |    +----------+     thread 4      |              |
|              |    +----| Endpoint |-------------------|              |
+--------------+         +----------+                   +--------------+

``` 

```text

single redis ----> redis cluster

+--------------+         +----------+     thread 1      +--------------+
|              |    +----| Endpoints|-------------------|              |
|              |    |    +----------+                   |              |
|              |    |                                   |              |
|              |    |    +----------+     thread 2      |              |
|              |    |----| Endpoints|-------------------|              |
|              |    |    +----------+                   |              |
| Source Redis |----|                                   | Redis cluster|
|              |    |    +----------+     thread 3      |              |
|              |    |----| Endpoints|-------------------|              |
|              |    |    +----------+                   |              |
|              |    |                                   |              |
|              |    |    +----------+     thread 4      |              |
|              |    +----| Endpoints|-------------------|              |
+--------------+         +----------+                   +--------------+

``` 

The difference between cluster migration and single migration is `Endpoint` and `Endpoints`. In cluster migration the `Endpoints` contains multi `Endpoint` to point to every `master` instance in cluster. For example:  
  
3 masters 3 replicas redis cluster. if `migrate_threads=4` then we have `3 * 4 = 12` connections that connected with `master` instance. 

### Migration performance

The following 3 parameters affect migration performance  
  
```properties
migrate_batch_size=4096
migrate_retries=1
migrate_flush=yes
```

1. `migrate_batch_size`: By default we use redis `pipeline` to migrate data to remote. the `migrate_batch_size` is the `pipeline` batch size. if `migrate_batch_size=1` then the `pipeline` devolved into 1 single command to sent and wait the response from remote.  
2. `migrate_retries`: The `migrate_retries=1` means if socket error occurred. we recreate a new socket and retry to send that failed command to target redis with `migrate_retries` times.  
3. `migrate_flush`: The `migrate_flush=yes` means we write every 1 command to socket. then we invoke `SocketOutputStream.flush()` immediately. if `migrate_flush=no` we invoke `SocketOutputStream.flush()` when write to socket every 64KB. notice that this parameter also affect `migrate_retries`. the `migrate_retries` only take effect when `migrate_flush=yes`.  

### Migration principle

```text

+---------------+             +-------------------+    restore      +---------------+ 
|               |             | redis dump format |---------------->|               |
|               |             |-------------------|    restore      |               |
|               |   convert   | redis dump format |---------------->|               |
|    Dump rdb   |------------>|-------------------|    restore      |  Targe Redis  |
|               |             | redis dump format |---------------->|               |
|               |             |-------------------|    restore      |               |
|               |             | redis dump format |---------------->|               |
+---------------+             +-------------------+                 +---------------+
```

## Limitation of migration

1. We use cluster's `nodes.conf` to migrate data to cluster. because of we didn't handle the `MOVED` `ASK` redirection. so limitation of cluster migration is that the cluster **MUST** in stable state during the migration. this means the cluster **MUST** have no `migrating`, `importing` slot and no switch slave to master. 
2. If use `rst` migrate data to cluster. the following commands not supported `PUBLISH,SWAPDB,MOVE,FLUSHALL,FLUSHDB,MULTI,EXEC,SCRIPT FLUSH,SCRIPT LOAD,EVAL,EVALSHA`. and the following commands `RPOPLPUSH,SDIFFSTORE,SINTERSTORE,SMOVE,ZINTERSTORE,ZUNIONSTORE,DEL,UNLINK,RENAME,RENAMENX,PFMERGE,PFCOUNT,MSETNX,BRPOPLPUSH,BITOP,MSET,COPY,BLMOVE,LMOVE,ZDIFFSTORE,GEOSEARCHSTORE` **ONLY SUPPORT WHEN THESE COMMAND KEYS IN THE SAME SLOT**(eg: `del {user}:1 {user}:2`)

## Hack ret

### What ret command do

1. `ret` command that allow user define there own sink service like sink redis data to `mysql` or `mongodb`.
2. `ret` command using Java SPI extension to do this job.

### How to implement a sink service

User should follow the steps below to implement a sink service.  

1. create a java project using maven pom.xml

```xml

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.your.company</groupId>
    <artifactId>your-sink-service</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.moilioncircle</groupId>
            <artifactId>redis-rdb-cli-api</artifactId>
            <version>1.8.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.moilioncircle</groupId>
            <artifactId>redis-replicator</artifactId>
            <version>[3.6.4, )</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- 
        <dependency>
            other dependencies
        </dependency>
        -->
        
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.target}</target>
                    <encoding>${project.build.sourceEncoding}</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

```

2. implement `SinkService` interface

```java

public class YourSinkService implements SinkService {

    @Override
    public String sink() {
        return "your-sink-service";
    }

    @Override
    public void init(File config) throws IOException {
        // parse your external sink config
    }

    @Override
    public void onEvent(Replicator replicator, Event event) {
        // your sink business
    }
}

```
3. register this service using Java SPI

```text
# create com.moilioncircle.redis.rdb.cli.api.sink.SinkService file in src/main/resources/META-INF/services/

|-src
|____main
| |____resources
| | |____META-INF
| | | |____services
| | | | |____com.moilioncircle.redis.rdb.cli.api.sink.SinkService

# add following content in com.moilioncircle.redis.rdb.cli.api.sink.SinkService

your.package.YourSinkService

```

4. package and deploy

```shell

$ mvn clean install

$ cp ./target/your-sink-service-1.0.0-jar-with-dependencies.jar /path/to/redis-rdb-cli/lib
```
5. run your sink service

```shell

$ ret -s redis://127.0.0.1:6379 -c config.conf -n your-sink-service
```

6. debug your sink service

```java  

    public static void main(String[] args) throws Exception {
        Replicator replicator = new RedisReplicator("redis://127.0.0.1:6379");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Replicators.closeQuietly(replicator);
        }));
        replicator.addExceptionListener((rep, tx, e) -> {
            throw new RuntimeException(tx.getMessage(), tx);
        });
        SinkService sink = new YourSinkService();
        sink.init(new File("/path/to/your-sink.conf"));
        replicator.addEventListener(new AsyncEventListener(sink, replicator, 4, Executors.defaultThreadFactory()));
        replicator.open();
    }

```

### How to implement a formatter service

1. create `YourFormatterService` extend `AbstractFormatterService`  

```java

public class YourFormatterService extends AbstractFormatterService {

    @Override
    public String format() {
        return "test";
    }

    @Override
    public Event applyString(Replicator replicator, RedisInputStream in, int version, byte[] key, int type, ContextKeyValuePair context) throws IOException {
        byte[] val = new DefaultRdbValueVisitor(replicator).applyString(in, version);
        getEscaper().encode(key, getOutputStream());
        getEscaper().encode(val, getOutputStream());
        getOutputStream().write('\n');
        return context;
    }
}

```

2. register this formatter using Java SPI  

```text
# create com.moilioncircle.redis.rdb.cli.api.format.FormatterService file in src/main/resources/META-INF/services/

|-src
|____main
| |____resources
| | |____META-INF
| | | |____services
| | | | |____com.moilioncircle.redis.rdb.cli.api.format.FormatterService

# add following content in com.moilioncircle.redis.rdb.cli.api.format.FormatterService

your.package.YourFormatterService

```

3. package and deploy

```shell

$ mvn clean install

$ cp ./target/your-service-1.0.0-jar-with-dependencies.jar /path/to/redis-rdb-cli/lib
```

4. run your formatter service

```shell

$ rct -f test -s redis://127.0.0.1:6379 -o ./out.csv -t string -d 0 -e json
```

## Contributors
  
* [Baoyi Chen](https://github.com/leonchen83)
* [Jintao Zhang](https://github.com/tao12345666333)
* [Maz Ahmadi](https://github.com/cmdshepard)
* [Anish Karandikar](https://github.com/anishkny)
* Special thanks to [Kater Technologies](https://www.kater.com/)

## Supported by IntelliJ IDEA

[IntelliJ IDEA](https://www.jetbrains.com/?from=redis-rdb-cli) is a Java integrated development environment (IDE) for developing computer software.  
It is developed by JetBrains (formerly known as IntelliJ), and is available as an Apache 2 Licensed community edition,  
and in a proprietary commercial edition. Both can be used for commercial development.  
