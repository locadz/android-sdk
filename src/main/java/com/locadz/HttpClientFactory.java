/*
 * Copyright 2012. Blue Tang Studio LLC.
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

package com.locadz;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.lang.ref.SoftReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  Factory class that returns a shared http client instance.
 */
public class HttpClientFactory {

    /** soft reference to the current client. */
    private static SoftReference<HttpClient> instance = new SoftReference<HttpClient>(null);

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Get a client instance. <br/>
     * Note: You should not assign the instance to a class variable.
     * @return a HttpClient instance.
     */
    public static final HttpClient getInstance() {

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

        try {
            readLock.lock();
            HttpClient httpClient = instance.get();
            if (httpClient == null) {
                readLock.unlock();
                lock.writeLock().lock();

                httpClient = instance.get();
                if (httpClient == null) {
                    httpClient = new DefaultHttpClient();
                    instance = new SoftReference<HttpClient>(httpClient);
                }
                readLock.lock();
                lock.writeLock().unlock();
            }
            return httpClient;

        } finally {
            readLock.unlock();
        }
    }
}
