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
import com.opensymphony.xwork2.interceptor.PreResultListener;

/**
 * Interceptor abstraction to
 * {@link #intercept(ActionInvocation) intercept the invocation} of the
 * Struts 2 action to set the required values to value stack.
 */
public abstract class AbstractFlowScopeInterceptor
        implements Interceptor, PreResultListener {

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * @return {@code true} if flow execution scope exists, {@code false}
     *         otherwise
     */
    protected final boolean hasFlowScope() {
        return FlowScopeUtils.hasFlowScope(configuration);
    }

    /**
     * @return flow execution scope
     */
    protected final Map getFlowScopeAsMap() {
        return FlowScopeUtils.getFlowScopeAsMap(configuration);
    }

    /**
     * {@link
     *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *  Flow execution paused key}
     * as configured.
     *
     * @param configuration {@link PluginConfiguration plugin configuration} to
     *                      be set
     */
    public void setConfiguration(PluginConfiguration configuration) {
        this.configuration = configuration;
    }
}
