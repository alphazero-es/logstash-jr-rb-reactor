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
import org.elasticsearch.logstash.pipeline.reactive.ReactivePipelineFilter;
import org.elasticsearch.logstash.pipeline.reactive.ReactivePipelineInput;
import org.elasticsearch.logstash.pipeline.reactive.ReactivePipelineOutput;
import org.elasticsearch.ruby.Woof;
import org.elasticsearch.ruby.WoofDelegate;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.function.Consumer;

import java.util.concurrent.CountDownLatch;

import static reactor.event.selector.Selectors.$;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application implements CommandLineRunner {

	@Bean
	ScriptingContainer scriptingContainer () {
		final ScriptingContainer container;
		container = new ScriptingContainer(LocalContextScope.SINGLETON);
		container.setCompileMode(RubyInstanceConfig.CompileMode.JIT);
		return container;
	}


	@Bean
	Environment env () {
		return new Environment();
	}

	@Bean Reactor inputReactor (Environment env) {
		return Reactors.reactor()
				.env(env)
				.dispatcher(Environment.EVENT_LOOP)
				.get();
	}

	@Bean Reactor filterReactor (Environment env) {
		return Reactors.reactor()
				.env(env)
				.dispatcher(Environment.EVENT_LOOP)
				.get();
	}

	@Bean Reactor outputReactor (Environment env) {
		return Reactors.reactor()
				.env(env)
				.dispatcher(Environment.EVENT_LOOP)
				.get();
	}

	@Autowired private Reactor inputReactor;
	@Autowired private Reactor filterReactor;
	@Autowired private Reactor outputReactor;

	@Autowired private ReactivePipelineInput<String> pipelineInput;
	@Autowired private ReactivePipelineFilter<String> pipelineFilter;
	@Autowired private ReactivePipelineOutput<String> pipelineOutput;

	@Autowired private Server server;

	@Bean Integer messageCount () {
		return 10000;
	}
	@Bean public CountDownLatch latch (Integer numberOfJokes) {
		return new CountDownLatch(numberOfJokes);
	}

	@Override final public void run (String... args) throws Exception {

		inputReactor.on($(Pipeline.Stage.input), pipelineInput);
		filterReactor.on($(Pipeline.Stage.filter), pipelineFilter);
		outputReactor.on($(Pipeline.Stage.output), pipelineOutput);

        // adhoc test - works --
//		woof();

        server.start();
	}

	public static void main (String[] args) throws InterruptedException {
		SpringApplication.run(Application.class, args);
	}

	// ------------------------------------------------------------------
	// ad-hoc test of ruby class wrapping
    // TODO : pick this up from a jar of .rb files.
    // TODO : precompile it
	// ------------------------------------------------------------------
	public static void woof() {
		// todo: pick this up from FS ..
		String script =
			"class Woof \n " +
				"def initialize() \n" +
					"puts 'Salaam!'\n" +
				"end\n" +
				"def greet() \n" +
					"return 'Salaam!'" +
				"end\n" +
			"end\n";

		final ScriptingContainer container;
		container = new ScriptingContainer(LocalContextScope.SINGLETON);
		container.setCompileMode(RubyInstanceConfig.CompileMode.JIT);

		// load ruby class defs ..
		container.runScriptlet(script);

		final Woof woof = new WoofDelegate(container);
		final String says = woof.greet();
		System.out.format("%s says %s\n", woof.toString(), says);
	}
}
