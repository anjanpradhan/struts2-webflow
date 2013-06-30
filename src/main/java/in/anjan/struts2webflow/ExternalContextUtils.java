/*
 * Copyright 2013 Anjan Pradhan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.anjan.struts2webflow;

import org.apache.struts2.ServletActionContext;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * The {@link ExternalContext external context} utils.
 */
public final class ExternalContextUtils {

    /**
     * Creates the {@link ExternalContext external context} and additionally
     * attaches the
     * {@link ActionContext#getActionInvocation() action invocation} to its
     * request map.
     *
     * @return {@link ExternalContext external context}
     */
    public static ExternalContext createExternalContext() {
        // need to create the external context
        // flow executor needs it
        ExternalContext context =
                new ServletExternalContext(
                        ServletActionContext.getServletContext(),
                        ServletActionContext.getRequest(),
                        ServletActionContext.getResponse());

        // need to put the action invocation on the context's request map
        // need it later to execute Struts action
        context.getRequestMap().put(
                ActionInvocation.class.getName(),
                ActionContext.getContext().getActionInvocation());

        return context;
    }
}
