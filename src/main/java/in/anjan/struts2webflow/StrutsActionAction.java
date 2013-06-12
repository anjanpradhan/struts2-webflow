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

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.dispatcher.Dispatcher;

import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionProxyFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * Executes Struts 2 Action.
 * TODO: enrich
 */
public class StrutsActionAction
        extends AbstractAction {

    /**
     * Default key to put the {@link RequestContext request context} to extra
     * context map for the execution of the Struts action.
     */
    public static final String DEFAULT_REQUEST_CONTEXT_KEY = StrutsActionAction.class.getName();

    /**
     * Struts namespace attribute name.
     */
    private static final String NAMESPACE_ATTRIBUTE_NAME = "namespace";
    /**
     * Struts action attribute name.
     */
    private static final String ACTION_ATTRIBUTE_NAME = "action";
    /**
     * Struts action method attribute name.
     */
    private static final String METHOD_ATTRIBUTE_NAME = "method";

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected Event doExecute(RequestContext context)
            throws Exception {
        // need to find out the action invocation on the context's request map
        // need it to execute Struts action
        ActionInvocation invocation =
                (ActionInvocation) context.getExternalContext()
                                          .getRequestMap()
                                          .get(ActionInvocation.class.getName());

        // find out the Struts namespace
        String namespace = (String) context.getAttributes().get(NAMESPACE_ATTRIBUTE_NAME);
        if (!StringUtils.hasText(namespace))
            namespace = invocation.getProxy().getNamespace();

        // find out the Struts action
        String action = (String) context.getAttributes().get(ACTION_ATTRIBUTE_NAME);
        if (!StringUtils.hasText(action))
            action = context.getCurrentState().getId();

        // find out the Struts action method
        String method = (String) context.getAttributes().get(METHOD_ATTRIBUTE_NAME);

        // find the value stack
        // and finalize the Struts namespace, action and action method
        ValueStack stack = ActionContext.getContext().getValueStack();
        String finalNamespace = TextParseUtil.translateVariables(namespace, stack);
        String finalAction = TextParseUtil.translateVariables(action, stack);
        String finalMethod = StringUtils.hasText(method) ? TextParseUtil.translateVariables(method, stack) : null;

        // need to prepare the extra criteria
        // for the execution of the Struts action
        Map<String, Object> extraContext = new HashMap();
        extraContext.put(DEFAULT_REQUEST_CONTEXT_KEY, context);
        extraContext.put(ActionContext.VALUE_STACK, ActionContext.getContext().getValueStack());
        extraContext.put(ActionContext.PARAMETERS, ActionContext.getContext().getParameters());

        // prepare Struts action proxy
        // with the finalized Struts namespace, action and action method
        Dispatcher dispatcher = Dispatcher.getInstance();
        Configuration config = dispatcher.getConfigurationManager().getConfiguration();
        ActionProxyFactory factory = config.getContainer().getInstance(ActionProxyFactory.class);

        ActionProxy proxy =
                factory.createActionProxy(
                        finalNamespace,
                        finalAction,
                        finalMethod,
                        extraContext,
                        false,
                        true);

        // execute and handover the result
        return result(proxy.execute());
    }
}
