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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.ErrorEvent;
import com.iris.client.IrisClient;
import com.iris.client.connection.ConnectionEvent;
import com.iris.client.connection.ConnectionState;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.session.Credentials;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;
import com.iris.client.session.SessionExpiredEvent;
import com.iris.client.session.SessionInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class MockClient implements IrisClient {
    private boolean authenticated = true;
    private UUID placeId = UUID.randomUUID();
    private String connectionUrl = "http://bc.irisbylowes.com:8081/";
    private ConnectionState state = ConnectionState.CONNECTED;

    private ListenerList<ConnectionEvent> connectionListeners = new ListenerList<>();
    private ListenerList<ClientRequest>   requestListeners    = new ListenerList<>();
    private ListenerList<ClientMessage>   messageListeners    = new ListenerList<>();
    private ListenerList<SessionEvent>    sessionListeners    = new ListenerList<>();

    private List<Expectation<ClientRequest, ClientFuture<ClientEvent>>> requestExpectations = new ArrayList<>();
    private List<Expectation<ClientRequest, Void>> submitExpectations = new ArrayList<>();

    @Override
    public ClientFuture<SessionInfo> login(Credentials credentials) {
        // TODO implement me
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConnected() {
        return authenticated;
    }

    @Override
    public void setClientAgent(String s) {
        // No Op
    }

    @Override
    public void setClientVersion(String s) {
        // No Op
    }

    @Override
    public ClientFuture<String> linkToWeb() {
        // No Op
        return null;
    }

    @Override
    public ClientFuture<String> linkToWeb(String destination) {
        // No Op
        return null;
    }

    @Override
    public ClientFuture<String> linkToWeb(String s, Map<String,String> m) {
        // No Op
        return null;
    }

    @Override
    public ClientFuture<?> logout() {
        if(!authenticated) {
            return Futures.succeededFuture(null);
        }
        authenticated = false;
        sessionListeners.fireEvent(new SessionExpiredEvent());
        return Futures.succeededFuture(null);
    }

    @Override
    public ClientFuture<UUID> setActivePlace(String s) {
        this.placeId = UUID.fromString(s);
        sessionListeners.fireEvent(new SessionActivePlaceSetEvent(this.placeId));
        return Futures.succeededFuture(this.placeId);
    }

    @Override
    public UUID getActivePlace() {
        return placeId;
    }

    @Override
    public void submit(ClientRequest clientRequest) {
        fireSent(clientRequest);
        for(Expectation<ClientRequest, Void> expectation: submitExpectations) {
            if(expectation.matches(clientRequest)) {
                try {
                    expectation.invoke(clientRequest);
                    return;
                }
                catch(RuntimeException e) {
                    throw e;
                }
                catch(Error e) {
                    throw e;
                }
                catch(Throwable t) {
                    throw new UncheckedExecutionException(t);
                }
            }
        }
    }

    @Override
    public ClientFuture<ClientEvent> request(ClientRequest clientRequest) {
        fireSent(clientRequest);
        for(Expectation<ClientRequest, ClientFuture<ClientEvent>> expectation: requestExpectations) {
            if(expectation.matches(clientRequest)) {
                try {
                    return expectation.invoke(clientRequest);
                }
                catch(RuntimeException e) {
                    throw e;
                }
                catch(Error e) {
                    throw e;
                }
                catch(Throwable t) {
                    throw new UncheckedExecutionException(t);
                }
            }
        }
        throw new IllegalStateException("No response for request " + clientRequest);
    }

    @Override
    public SessionInfo getSessionInfo() {
        return null;
    }

    @Override
    public String getConnectionURL() {
        return connectionUrl;
    }

    @Override
    public void setConnectionURL(String connectionUrl) throws IllegalStateException {
        this.connectionUrl = connectionUrl;
    }

    @Override
    public ConnectionState getConnectionState() {
        return state;
    }

    public void setConnectionState(ConnectionState state) {
        if(this.state != state) {
            this.state = state;
            this.connectionListeners.fireEvent(new ConnectionEvent(state));
        }
    }

    @Override
    public ListenerRegistration addConnectionListener(Listener<? super ConnectionEvent> listener) {
        return connectionListeners.addListener(listener);
    }

    @Override
    public ListenerRegistration addSessionListener(Listener<? super SessionEvent> listener) {
        return sessionListeners.addListener(listener);
    }

    @Override
    public ListenerRegistration addRequestListener(Listener<? super ClientRequest> listener) {
        return requestListeners.addListener(listener);
    }

    @Override
    public ListenerRegistration addMessageListener(Listener<? super ClientMessage> listener) {
        return messageListeners.addListener(listener);
    }

    @Override
    public void close() throws IOException {
        boolean closed = true;
    }

    /**
     * Triggers the response handler chains
     * @param message
     */
    public void received(ClientMessage message) {
        messageListeners.fireEvent(message);
    }

    protected void fireSent(ClientRequest request) {
        requestListeners.fireEvent(request);
    }

    public ClientResponseBuilder expectRequestTo(final String address) {
        return expectRequest(
                new Predicate<ClientRequest>() {
                    @Override
                    public boolean apply(ClientRequest input) {
                        return address.equals(input.getAddress());
                    }
                }
        );
    }

    public ClientResponseBuilder expectRequestOfType(final String name) {
        return expectRequest(
                new Predicate<ClientRequest>() {
                    @Override
                    public boolean apply(ClientRequest input) {
                        return name.equals(input.getCommand());
                    }
                }
        );
    }

    public ClientResponseBuilder expectRequest(final String to, final String command) {
        return expectRequest(
                new Predicate<ClientRequest>() {
                    @Override
                    public boolean apply(ClientRequest input) {
                        return to.equals(input.getAddress()) && command.equals(input.getCommand());
                    }
                }
        );
    }

    public ClientResponseBuilder expectRequest(final String to, final String command, final Map<String, Object> attributes) {
        return expectRequest(
                new Predicate<ClientRequest>() {
                    @Override
                    public boolean apply(ClientRequest input) {
                        return
                                to.equals(input.getAddress()) &&
                                command.equals(input.getCommand()) &&
                                attributes.equals(input.getAttributes());
                    }
                }
        );
    }

    public ClientResponseBuilder expectRequest(ClientRequest request) {
        return expectRequest(Predicates.equalTo(request));
    }

    public ClientResponseBuilder expectRequest(Predicate<? super ClientRequest> requestMatcher) {
        return new ClientResponseBuilder(requestMatcher, requestExpectations);
    }

    public AndThenBuilder<ClientRequest> expectSubmit(Predicate<? super ClientRequest> requestMatcher) {
        Expectation<ClientRequest, Void> expectation = new Expectation<>(requestMatcher, Answers.returnVoid());
        submitExpectations.add(expectation);
        return new AndThenBuilder<>(expectation);
    }

    public interface Answer<I, O> {

        O respond(I input) throws Throwable;
    }

    public interface PostProcessor<I> {

        void andThen(I input);
    }

    private static class Expectation<I, O> {
        private final Predicate<? super I> predicate;
        private final Answer<? super I, ? extends O> answer;
        private final List<PostProcessor<? super I>> postProcessors;

        Expectation(Predicate<? super I> predicate, Answer<? super I, ? extends O> answer) {
            this.predicate = predicate;
            this.answer = answer;
            // TODO synchronize this?
            this.postProcessors = new ArrayList<>();
        }

        public void addPostProcessor(PostProcessor<? super I> postProcessor) {
            this.postProcessors.add(postProcessor);
        }

        public boolean matches(I input) {
            return this.predicate.apply(input);
        }

        public O invoke(I input) {
            try {
                return answer.respond(input);
            }
            catch(RuntimeException e) {
                throw e;
            }
            catch(Error e) {
                throw e;
            }
            catch(Throwable t) {
                throw new UncheckedExecutionException(t);
            }
            finally {
                for(PostProcessor<? super I> pp: postProcessors) {
                    pp.andThen(input);
                }
            }
        }

        @Override
        public String toString() {
            return "Expect " + predicate + " and then " + answer;
        }

    }

    public class ResponseBuilder<I, O> {
        private boolean added = false;
        private Predicate<? super I> predicate;
        private List<Expectation<I, O>> expectations;

        ResponseBuilder(Predicate<? super I> predicate, List<Expectation<I,O>> expectations) {
            this.predicate = predicate;
            this.expectations = expectations;
        }

        protected AndThenBuilder andThen(Answer<? super I, ? extends O> answer) {
            if(added) {
                throw new IllegalStateException("An expectation has already been added, may only call andXXX once");
            }
            Expectation<I, O> expectation = new Expectation<I,O>(predicate, answer);
            expectations.add(expectation);
            added = true;
            return new AndThenBuilder<>(expectation);
        }

        public AndThenBuilder andReturn(O value) {
            return andThen(Answers.returnValue(value));
        }

        public AndThenBuilder andThrow(Throwable cause) {
            return andThen(Answers.<I,O>throwException(cause));
        }

        public AndThenBuilder andAnswer(Answer<? super I, ? extends O> answer) {
            return andThen(answer);
        }
    }

    public class ClientResponseBuilder extends ResponseBuilder<ClientRequest, ClientFuture<ClientEvent>> {

        ClientResponseBuilder(Predicate<? super ClientRequest> predicate, List<Expectation<ClientRequest, ClientFuture<ClientEvent>>> expectations) {
            super(predicate, expectations);
        }

        public AndThenBuilder<ClientRequest> andRespondWithMessage(String type) {
            return andRespondWithMessage(type, ImmutableMap.<String, Object>of());
        }

        public AndThenBuilder<ClientRequest> andRespondFromPath(String path) {
            return andRespondWithMessage(Responses.load(path).create());
        }

        public AndThenBuilder<ClientRequest> andRespondFromPath(String path, Class<?> relativeTo) {
            return andRespondWithMessage(Responses.load(path, relativeTo).create());
        }

        public AndThenBuilder<ClientRequest> andRespondWithMessage(final String type, final Map<String, Object> attributes) {
            return andAnswer(
                    new Answer<ClientRequest, ClientFuture<ClientEvent>>() {
                        @Override
                        public ClientFuture<ClientEvent> respond(ClientRequest input) {
                            ClientMessage message =
                                    ClientMessage
                                            .builder()
                                            .withDestination(input.getAddress())
                                            .withCorrelationId(UUID.randomUUID().toString())
                                            .withType(type)
                                            .withAttributes(attributes)
                                            .create();
                            return Futures.succeededFuture(message.getEvent());
                        }
                    }
            );
        }

        public AndThenBuilder<ClientRequest> andRespondWithMessage(final ClientMessage message) {
            return andAnswer(
                    new Answer<ClientRequest, ClientFuture<ClientEvent>>() {
                        @Override
                        public ClientFuture<ClientEvent> respond(ClientRequest input) {
                            received(message);
                            return Futures.succeededFuture(message.getEvent());
                        }
                    }
            );
        }

        public AndThenBuilder<ClientRequest> andRespondWithError(String code, String message) {
            return andRespondWithException(new ErrorResponseException(code, message));
        }

        public AndThenBuilder<ClientRequest> andRespondWithError(ErrorEvent error) {
            return andRespondWithError(error.getCode(), error.getMessage());
        }

        public AndThenBuilder<ClientRequest> andRespondWithException(Throwable cause) {
            return andReturn(Futures.<ClientEvent>failedFuture(cause));
        }

    }

    public class AndThenBuilder<I> {
        private Expectation<I, ?> expectation;

        protected AndThenBuilder(Expectation<I, ?> expectation) {
            this.expectation = expectation;
        }

        public AndThenBuilder andThen(PostProcessor<? super I> postProcessor) {
            expectation.addPostProcessor(postProcessor);
            return this;
        }

        public AndThenBuilder andThenSend(final ClientMessage message) {
            return andThen(
                    new PostProcessor<I>() {
                        @Override
                        public void andThen(I input) {
                            received(message);
                        }
                    }
            );
        }

    }
}
