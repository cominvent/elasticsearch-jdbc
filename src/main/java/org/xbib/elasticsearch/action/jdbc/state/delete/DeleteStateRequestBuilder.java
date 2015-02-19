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
package org.xbib.elasticsearch.action.jdbc.state.delete;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.master.AcknowledgedRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;

public class DeleteStateRequestBuilder extends AcknowledgedRequestBuilder<DeleteStateRequest, DeleteStateResponse, DeleteStateRequestBuilder, ClusterAdminClient> {

    public DeleteStateRequestBuilder(ClusterAdminClient client) {
        super(client, new DeleteStateRequest());
    }

    public DeleteStateRequestBuilder setName(String name) {
        request.setName(name);
        return this;
    }

    @Override
    protected void doExecute(ActionListener<DeleteStateResponse> listener) {
        client.execute(DeleteStateAction.INSTANCE, request, listener);
    }
}