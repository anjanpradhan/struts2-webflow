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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * Interceptor to {@link #intercept(ActionInvocation) intercept the invocation}
 * of the {@link FlowAction flow action} to set the
 * {@link
 *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
 *  flow execution paused key}
 * from session to value stack and the other way round.
 */
public class PausedKeyInterceptor
        implements Interceptor {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PausedKeyInterceptor.class);

    /**
     * The {@link PluginConfiguration plugin configuration} as configured.
     * <p/>
     * Can be set through {@link #setConfiguration(PluginConfiguration)}.
     */
    private PluginConfiguration configuration = new PluginConfiguration();

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        // no-op;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // no-op;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String intercept(ActionInvocation invocation)
            throws Exception {
        // need to get the session and value stack
        Map<String, Object> session = invocation.getInvocationContext().getSession();
        ValueStack stack = invocation.getStack();

        // have the paused key in session?
        // if yes, set it to value stack
        // (eventually it will turn up to flow action)
        String pausedKey = (String) session.get(configuration.getPausedKeySessionKey());
        if (pausedKey != null) {
            LOGGER.debug("found paused key {} in session", pausedKey);
            stack.setValue(FlowAction.DEFAULT_PAUSED_KEY_EXPRESSION, pausedKey);
        }

        // need to set the scope values from session to value stack
        // this is required nested property to work with OGNL
        if (FlowScopeUtils.hasFlowScope(configuration)) {
            Map flowScopeMap = FlowScopeUtils.getFlowScopeAsMap(configuration);

            for (Object key : flowScopeMap.keySet()) {
                String name = (String) key;
                Object value = flowScopeMap.get(name);
                if (value != null) {
                    LOGGER.debug("found {} with value {} in flow scope", name, value);
                    stack.set(name, value);
                }
            }
        }

        // filter out event and other parameters
        // to avoid error notifications on Struts dev mode
        // TODO: need to?

        // let's execute the flow action
        // and collect the result
        String result = invocation.invoke();

        // get the paused key from value stack
        // and set it to session
        pausedKey = (String) stack.findValue(FlowAction.DEFAULT_PAUSED_KEY_EXPRESSION);
        session.put(configuration.getPausedKeySessionKey(), pausedKey);

        // handover the result
        LOGGER.debug("returning result {}", result);
        return result;
    }

    /**
     * {@link PluginConfiguration Plugin configuration} as configured.
     *
     * @param configuration {@link PluginConfiguration plugin configuration} to
     *                      be set
     */
    public void setConfiguration(PluginConfiguration configuration) {
        this.configuration = configuration;
    }
}
