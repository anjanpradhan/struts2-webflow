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

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * The {@link org.springframework.webflow.engine.Flow flow} scope utils.
 */
public final class FlowScopeUtils {

    /**
     * @return {@link RequestContext request context}
     */
    private static RequestContext getRequestContext() {
        Map<String, Object> contextMap = ActionContext.getContext().getContextMap();
        return (RequestContext) contextMap.get(StrutsActionAction.DEFAULT_REQUEST_CONTEXT_KEY);
    }

    /**
     * @return {@code true} if {@link #getRequestContext() request context}
     *         exists, {@code false} otherwise
     */
    private static boolean hasRequestContext() {
        return getRequestContext() != null;
    }

    /**
     * @param pausedKeySessionKey {@link
     *                             org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *                             flow execution paused key}
     *                            to be used
     * @return {@link
     *          org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *          flow execution paused key}
     */
    private static String getPauseKey(String pausedKeySessionKey) {
        ActionContext context = ActionContext.getContext();
        ActionInvocation invocation = context.getActionInvocation();

        // trying to find the paused key from
        // a) parameters
        // b) session

        String pausedKeys[] =
                (String[]) invocation.getInvocationContext()
                                     .getParameters()
                                     .get(FlowAction.DEFAULT_PAUSED_KEY_EXPRESSION);

        String pauseKey = pausedKeys != null ? pausedKeys[0] : null;
        if (pauseKey == null) {
            Map<String, Object> session = context.getSession();
            pauseKey = (String) session.get(pausedKeySessionKey);
        }

        return pauseKey;
    }

    /**
     * @param pausedKeySessionKey {@link
     *                             org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *                             flow execution paused key}
     *                            to be used
     * @return {@code true} if {@link #getPauseKey(String) paused key} exists,
     *         {@code false} otherwise
     */
    private static boolean hasPauseKey(String pausedKeySessionKey) {
        return getPauseKey(pausedKeySessionKey) != null;
    }

    /**
     * Finds the flow execution scope from flow execution repository.
     *
     * @param pausedKeySessionKey {@link
     *                             org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *                             flow execution paused key}
     *                            to be used
     * @param flowExecutorBean    {@link FlowExecutor flow executor} bean name to
     *                            be used
     * @return flow execution scope as map
     */
    private static Map getExternalFlowScopeAsMap(String pausedKeySessionKey, String flowExecutorBean) {
        ExternalContext oldContext = ExternalContextHolder.getExternalContext();

        // need to create and set the external context
        ExternalContext context = ExternalContextUtils.createExternalContext();
        ExternalContextHolder.setExternalContext(context);

        // find the flow execution and its scape
        FlowExecutionRepository repository =
                ((FlowExecutorImpl) FlowExecutorUtils.getRequiredFlowExecutor(flowExecutorBean))
                        .getExecutionRepository();

        FlowExecutionKey key = repository.parseFlowExecutionKey(getPauseKey(pausedKeySessionKey));
        FlowExecution flowExecution = repository.getFlowExecution(key);
        MutableAttributeMap scope = flowExecution.getActiveSession().getScope();

        ExternalContextHolder.setExternalContext(oldContext);

        return scope.asMap();
    }

    /**
     * @param configuration {@link PluginConfiguration plugin configuration} to
     *                      be used
     * @return {@code true} if flow execution scope exists, {@code false}
     *         otherwise
     */
    public static boolean hasFlowScope(PluginConfiguration configuration) {
        return hasRequestContext() || hasPauseKey(configuration.getPausedKeySessionKey());
    }

    /**
     * @param configuration {@link PluginConfiguration plugin configuration} to
     *                      be used
     * @return flow execution scope
     */
    public static Map getFlowScopeAsMap(PluginConfiguration configuration) {
        // have request context?
        // if yes, get scope from request context
        // else get it from flow execution repository
        return hasRequestContext()
                ? getRequestContext().getFlowScope().asMap()
                : getExternalFlowScopeAsMap(
                        configuration.getPausedKeySessionKey(),
                        configuration.getFlowExecutorBean());
    }
}
