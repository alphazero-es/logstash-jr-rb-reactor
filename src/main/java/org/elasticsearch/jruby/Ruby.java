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

package org.elasticsearch.jruby;

import org.jruby.embed.ScriptingContainer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Created by alphazero on 5/14/14.
 */
public interface Ruby {

    /**
     *
     */
    @Target({TYPE, FIELD, PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Class  {
        String name ();
    }

    /**
     * Note: If name attribute is not specified it will default to annotated method's name.
     */
    @Target({METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Method  {
        String name () default "";
    }


    /**
     *
     */
    public static class ObjectDelegate {

        /** */
        final Object delegate;
        /** */
        final String classname;
        /** */
        final ScriptingContainer container;

        /** */
        protected ObjectDelegate (final ScriptingContainer container) throws RuntimeException {
            this.container = container;

            final java.lang.Class<?> clazz = this.getClass();
            final Ruby.Class annotation = clazz.getAnnotation(Ruby.Class.class);
            assert annotation != null : "RubyObjectDelegate must be annotated with " + Ruby.Class.class.getCanonicalName();

            this.classname = annotation.name();
            assert this.classname != null : "RubyClass name not specified";

            try {
                final String callFnNew = String.format("%s::new()", classname);
                this.delegate = container.runScriptlet(callFnNew);
                assert this.delegate != null : "BUG - delegate is null";
            } catch (Throwable fault) {
                final String msg = String.format("failed to instantiate class <%s>", classname);
                throw new RuntimeException(msg, fault);
            }
        }

        /** */
        @SuppressWarnings("unchecked")
        protected <T> T invoke(final String method, final java.lang.Class<?> retClass) {
            return (T) container.callMethod(delegate, method, retClass);
        }
    }
}
