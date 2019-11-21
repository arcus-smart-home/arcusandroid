/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.cornea.mock;

import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;


public class Answers {
    private static MockClient.Answer<?, Void> VOID = new MockClient.Answer<Object, Void>() {
        public Void respond(Object input) {
            return null;
        }

        public String toString() { return "return void"; }
    };
    private static MockClient.Answer<?, ?> NULL = new MockClient.Answer<Object, Object>() {
        public Object respond(Object input) {
            return null;
        }

        public String toString() { return "return null"; }
    };

    public static <I> MockClient.Answer<I, Void> returnVoid() {
        return (MockClient.Answer<I, Void>) VOID;
    }

    public static <I, O> MockClient.Answer<I, O> returnNull() {
        return (MockClient.Answer<I, O>) NULL;
    }

    public static <I, O> MockClient.Answer<I,O> returnValue(@Nullable O value) {
        if(value == null) {
            return Answers.returnNull();
        }
        return new ReturnValue<>(value);
    }

    public static <I, O> MockClient.Answer<I, O> throwException(Throwable cause) {
        Preconditions.checkNotNull(cause);
        return new ThrowException<>(cause);
    }

    private static class ReturnValue<I, O> implements MockClient.Answer<I, O> {
        private final O value;

        ReturnValue(O value) {
            this.value = value;
        }

        public O respond(I input) {
            return value;
        }

        public String toString() {
            return "return " + value;
        }
    }

    private static class ThrowException<I, O> implements MockClient.Answer<I, O> {
        private final Throwable cause;

        ThrowException(Throwable cause) {
            this.cause = cause;
        }

        public O respond(I input) throws Throwable {
            throw cause;
        }

        public String toString() {
            return "throw " + cause;
        }
    }

}
