/*
 * Copyright (C) 2014 Jörg Prante
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbib.elasticsearch.rest.action.jdbc;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.support.RestToXContentListener;
import org.xbib.elasticsearch.action.jdbc.state.delete.DeleteStateAction;
import org.xbib.elasticsearch.action.jdbc.state.delete.DeleteStateRequest;
import org.xbib.elasticsearch.action.jdbc.state.delete.DeleteStateResponse;
import org.xbib.elasticsearch.action.jdbc.state.get.GetStateAction;
import org.xbib.elasticsearch.action.jdbc.state.get.GetStateRequest;
import org.xbib.elasticsearch.action.jdbc.state.get.GetStateResponse;
import org.xbib.elasticsearch.action.jdbc.state.post.PostStateAction;
import org.xbib.elasticsearch.action.jdbc.state.post.PostStateRequest;
import org.xbib.elasticsearch.action.jdbc.state.post.PostStateResponse;
import org.xbib.elasticsearch.jdbc.state.State;

import java.io.IOException;

public class RestStateAction extends BaseRestHandler {

    private final Client client;

    @Inject
    public RestStateAction(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);
        this.client = client;

        controller.registerHandler(RestRequest.Method.GET,
                "/_jdbc/{name}/_state", new Get());
        controller.registerHandler(RestRequest.Method.POST,
                "/_jdbc/{name}/_state", new Post(false, false, false));
        controller.registerHandler(RestRequest.Method.DELETE,
                "/_jdbc/{name}/_state", new Delete());

        controller.registerHandler(RestRequest.Method.POST,
                "/_jdbc/{name}/_abort", new Post(true, false, false));
        controller.registerHandler(RestRequest.Method.POST,
                "/_jdbc/{name}/_suspend", new Post(false, true, false));
        controller.registerHandler(RestRequest.Method.POST,
                "/_jdbc/{name}/_resume", new Post(false, false, true));
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        channel.sendResponse(new BytesRestResponse(RestStatus.NOT_IMPLEMENTED));
    }

    class Get implements RestHandler {

        @Override
        public void handleRequest(RestRequest request, RestChannel channel) throws Exception {
            try {
                String name = request.param("name");
                GetStateRequest stateRequest = new GetStateRequest();
                stateRequest.setName(name);
                client.admin().cluster().execute(GetStateAction.INSTANCE, stateRequest,
                        new RestToXContentListener<GetStateResponse>(channel));
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                try {
                    channel.sendResponse(new BytesRestResponse(channel, t));
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR));
                }
            }
        }
    }

    class Post implements RestHandler {

        boolean abort;
        boolean suspend;
        boolean resume;

        Post(boolean abort, boolean suspend, boolean resume) {
            this.abort = abort;
            this.resume = resume;
            this.suspend = suspend;
        }

        @Override
        public void handleRequest(RestRequest request, RestChannel channel) throws Exception {
            try {
                String name = request.param("name");
                PostStateRequest postStateRequest = new PostStateRequest();
                postStateRequest.setName(name);
                if (request.hasContent()) {
                    State state = new State().setName(name);
                    state.setMap(XContentHelper.convertToMap(request.content(), true).v2());
                    postStateRequest.setState(state);
                }
                if (abort) {
                    postStateRequest.setAbort();
                }
                if (suspend) {
                    postStateRequest.setSuspend();
                }
                if (resume) {
                    postStateRequest.setResume();
                }
                client.admin().cluster().execute(PostStateAction.INSTANCE, postStateRequest,
                        new RestToXContentListener<PostStateResponse>(channel));
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                try {
                    channel.sendResponse(new BytesRestResponse(channel, t));
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR));
                }
            }
        }
    }

    class Delete implements RestHandler {

        @Override
        public void handleRequest(RestRequest request, RestChannel channel) throws Exception {
            try {
                String name = request.param("name");
                DeleteStateRequest stateRequest = new DeleteStateRequest();
                stateRequest.setName(name);
                client.admin().cluster().execute(DeleteStateAction.INSTANCE, stateRequest,
                        new RestToXContentListener<DeleteStateResponse>(channel));
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                try {
                    channel.sendResponse(new BytesRestResponse(channel, t));
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR));
                }
            }
        }
    }
}