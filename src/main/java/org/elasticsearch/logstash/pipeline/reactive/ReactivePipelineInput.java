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

package org.elasticsearch.logstash.pipeline.reactive;

import org.elasticsearch.logstash.pipeline.Pipeline;
import org.jruby.embed.ScriptingContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.function.Consumer;

@Service(value = "pipelineInput")
public class ReactivePipelineInput<T> extends Pipeline.Input implements Consumer<Event<T>> {

	@Autowired
	Reactor filterReactor;

	@Autowired
	ScriptingContainer scriptingContainer;

	@Override final
	public void accept (Event<T> ev) {

		final T message = ev.getData();

		final String script = String.format("puts 'digesting: %s'", message.toString());
		scriptingContainer.runScriptlet(script);

		// notify next stage
		final String digested = String.format("digested : %s", message);
        filterReactor.notify(Pipeline.Stage.filter, Event.wrap(digested));
	}
}