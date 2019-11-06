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

package com.moilioncircle.redis.rdb.cli.ext;

import com.moilioncircle.redis.rdb.cli.conf.Configure;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.event.PostCommandSyncEvent;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.event.PreCommandSyncEvent;
import com.moilioncircle.redis.replicator.event.PreRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.moilioncircle.redis.replicator.util.Concurrents.terminateQuietly;

/**
 * @author Baoyi Chen
 */
public class AsyncEventListener implements EventListener {

    private int count;
    private ExecutorService[] executors;
    private final EventListener listener;
    private final Syncer syncer = new Syncer();

    public AsyncEventListener(EventListener listener, Replicator r, Configure c) {
        this.listener = listener;
        int n = c.getMigrateThreads();
        if (n > 0) {
            if ((n & (n - 1)) != 0) {
                throw new IllegalArgumentException("migrate_thread_size " + n + " must power of 2");
            }
            this.executors = new ExecutorService[n];
            for (int i = 0; i < this.executors.length; i++) {
                this.executors[i] = Executors.newSingleThreadExecutor();
            }
            r.addCloseListener(rep -> {
                for (int i = 0; i < this.executors.length; i++) {
                    terminateQuietly(this.executors[i], c.getTimeout(), TimeUnit.MILLISECONDS);
                }
            });
        }
    }

    @Override
    public void onEvent(Replicator replicator, Event event) {
        if (executors == null) {
            this.listener.onEvent(replicator, event);
        } else {
            if (event instanceof PreRdbSyncEvent ||
                    event instanceof PostRdbSyncEvent ||
                    event instanceof PreCommandSyncEvent ||
                    event instanceof PostCommandSyncEvent) {
                // 1
                if (event instanceof PreRdbSyncEvent) {
                    syncer.reset();
                }
                
                // 2
                for (int i = 0; i < this.executors.length; i++) {
                    this.executors[i].submit(() -> this.listener.onEvent(replicator, event));
                }
                
                // 3
                if (event instanceof PostRdbSyncEvent) {
                    for (int i = 0; i < this.executors.length; i++) {
                        this.executors[i].submit(() -> this.listener.onEvent(replicator, syncer));
                    }
                }
            } else if (event instanceof DumpKeyValuePair) {
                int i = count++ & (executors.length - 1);
                this.executors[i].submit(() -> this.listener.onEvent(replicator, event));
            } else if (event instanceof Command) {
                this.executors[0].submit(() -> this.listener.onEvent(replicator, event));
            }
        }
    }
    
    public class Syncer implements Event {
        
        private CyclicBarrier barrier = new CyclicBarrier(executors.length);
        
        public int await() {
            try {
                return barrier.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return 0;
            } catch (BrokenBarrierException e) {
                return 0;
            }
        }
        
        public void reset() {
            barrier.reset();
        }
    }
}