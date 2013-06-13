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

import java.util.Map;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * Interceptor to {@link #intercept(ActionInvocation) intercept the invocation}
 * of the Struts 2 action to set the required values to value stack.
 */
public class FlowScopeInterceptor
        extends AbstractFlowScopeInterceptor {

    private String[] flowScope = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public String intercept(ActionInvocation invocation)
            throws Exception {
        if (hasFlowScope()) {
            invocation.addPreResultListener(this);

            Map flowScopeMap = getFlowScopeAsMap();
            ValueStack stack = ActionContext.getContext().getValueStack();

            if (flowScope != null) {
                for (String key : flowScope) {
                    Object value = flowScopeMap.get(key);
                    if (value != null)
                        stack.setValue(key, value);
                }
            }
        }

        return invocation.invoke();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void beforeResult(ActionInvocation invocation, String resultCode) {
        if (hasFlowScope()) {
            ValueStack stack = ActionContext.getContext().getValueStack();
            Map flowScopeAsMap = getFlowScopeAsMap();

            for (String key : flowScope) {
                Object value = stack.findValue(key);
                if (value != null)
                    flowScopeAsMap.put(key, value);
            }
        }
    }

    public void setFlowScope(String flowScope) {
        if (flowScope != null)
            this.flowScope = flowScope.split(" *, *");
    }
}
