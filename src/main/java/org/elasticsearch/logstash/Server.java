/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.logstash;

import org.elasticsearch.logstash.pipeline.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.Reactor;
import reactor.event.Event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Server {

	@Autowired
	Reactor inputReactor;

	@Autowired
	CountDownLatch latch;

	@Autowired
	Integer messageCount;

	final RestTemplate restTemplate = new RestTemplate();

	public void start () throws InterruptedException {

		long start = System.nanoTime();

		AtomicInteger counter = new AtomicInteger(1);

		for (int i = 0; i < messageCount; i++) {
		/* wait for input -- e.g. wait on a NETTY pipeline input */
//        JokeResource jokeResource = restTemplate.getForObject("http://api.icndb.com/jokes/random", JokeResource.class);

			final String message = String.format("message: %d", counter.getAndIncrement());
            inputReactor.notify(Pipeline.Stage.input, Event.wrap(message));
		}

		latch.await();
		long elapsed = System.nanoTime() - start;

		System.out.println("Elapsed time: " + elapsed + "ns");
		System.out.println("Average pipeline process time per message: " + elapsed / messageCount + "ns");
	}

}
