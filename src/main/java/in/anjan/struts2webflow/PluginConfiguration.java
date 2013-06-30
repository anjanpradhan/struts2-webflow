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

/**
 * The plugin configuration object to hold:
 * <ul>
 *  <li>
 *    {@link
 *     org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
 *     flow execution paused key} session key
 *  </li>
 *  <li>
 *    {@link org.springframework.webflow.executor.FlowExecutor flow executor}
 *    bean name
 *  </li>
 * </ul>
 */
public class PluginConfiguration {

    /**
     * Default
     * {@link org.springframework.webflow.executor.FlowExecutor flow executor}
     * bean name.
     */
    public static final String DEFAULT_FLOW_EXECUTOR_BEAN = "flowExecutor";

    /**
     * Default key to put the
     * {@link
     *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *  flow execution paused key}
     * to the session.
     */
    public static final String DEFAULT_PAUSED_KEY_SESSION_KEY =
            PluginConfiguration.class.getName() + ".pausedKeySessionKey";

    /**
     * {@link org.springframework.webflow.executor.FlowExecutor Flow executor}
     * bean name as configured in the Spring web application context hierarchy.
     * <p/>
     * Can be set through {@link #setFlowExecutorBean(String)}.
     */
    private String flowExecutorBean = DEFAULT_FLOW_EXECUTOR_BEAN;
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
     * @return {@link
     *          org.springframework.webflow.executor.FlowExecutor
     *          flow executor}
     *         bean name
     */
    public String getFlowExecutorBean() {
        return flowExecutorBean;
    }

    /**
     * {@link org.springframework.webflow.executor.FlowExecutor Flow executor}
     * bean name as configured in the Spring web application context hierarchy.
     * <p/>
     * This action will try to find the
     * {@link org.springframework.webflow.executor.FlowExecutor flow executor}
     * bean by this name from the Spring web application context.
     *
     * @param flowExecutorBean {@link
     *                          org.springframework.webflow.executor.FlowExecutor
     *                          flow executor}
     *                         bean name to be set
     */
    public void setFlowExecutorBean(String flowExecutorBean) {
        this.flowExecutorBean = flowExecutorBean;
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
