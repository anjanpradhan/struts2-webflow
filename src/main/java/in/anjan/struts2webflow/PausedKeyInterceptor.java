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
     * Default key to put the
     * {@link
     *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *  flow execution paused key}
     * to the session.
     */
    public static final String DEFAULT_PAUSED_KEY_SESSION_KEY = PausedKeyInterceptor.class.getName();

    /**
     * {@link
     *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *  Flow execution paused key}
     * as configured.
     * <p/>
     * Can be set through {@link #setPausedKeySessionKey(String)}.
     */
    private String pausedKeySessionKey = DEFAULT_PAUSED_KEY_SESSION_KEY;

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
        String pausedKey = (String) session.get(pausedKeySessionKey);
        if (pausedKey != null)
            stack.setValue(FlowAction.DEFAULT_PAUSED_KEY_EXPRESSION, pausedKey);

        // filter out event and other parameters
        // to avoid error notifications on Struts dev mode
        // TODO: need to?

        // let's execute the flow action
        // and collect the result
        String result = invocation.invoke();

        // get the paused key from value stack
        // and set it to session
        pausedKey = (String) stack.findValue(FlowAction.DEFAULT_PAUSED_KEY_EXPRESSION);
        session.put(pausedKeySessionKey, pausedKey);

        // handover the result
        return result;
    }

    /**
     * @return {@link
     *          org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *          flow execution paused key}
     */
    public String getPausedKeySessionKey() {
        return pausedKeySessionKey;
    }

    /**
     * {@link
     *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *  Flow execution paused key}
     * as configured.
     *
     * @param pausedKeySessionKey {@link
     *                             org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *                             flow execution paused key}
     *                            to be set
     */
    public void setPausedKeySessionKey(String pausedKeySessionKey) {
        this.pausedKeySessionKey = pausedKeySessionKey;
    }
}
