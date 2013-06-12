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

import org.apache.struts2.ServletActionContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;

/**
 * Interceptor abstraction to
 * {@link #intercept(ActionInvocation) intercept the invocation} of the
 * Struts 2 Action to set the required values to value stack.
 */
public abstract class AbstractFlowScopeInterceptor
        implements Interceptor, PreResultListener {

    /**
     * {@link
     *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *  Flow execution paused key}
     * as configured.
     * <p/>
     * Can be set through {@link #setPausedKeySessionKey(String)}.
     */
    private String pausedKeySessionKey = PausedKeyInterceptor.DEFAULT_PAUSED_KEY_SESSION_KEY;
    /**
     * {@link FlowExecutor Flow executor} bean name as configured in the Spring
     * context hierarchy.
     * <p/>
     * Can be set through {@link #setFlowExecutorBean(String)}.
     * <br/>This action will try to find the {@link FlowExecutor flow executor}
     * bean by this name from the Spring context hierarchy.
     */
    private String flowExecutorBean = FlowAction.DEFAULT_FLOW_EXECUTOR_BEAN;
    /**
     * {@link FlowExecutor Flow executor} bean.
     * <p/>
     * Must be configured in the Spring context.
     */
    private FlowExecutor flowExecutor;

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
     * @return {@link RequestContext request context}
     */
    private RequestContext getRequestContext() {
        Map<String, Object> contextMap = ActionContext.getContext().getContextMap();
        return (RequestContext) contextMap.get(StrutsActionAction.DEFAULT_REQUEST_CONTEXT_KEY);
    }

    /**
     * @return {@code true} if {@link #getRequestContext() request context}
     *         exists, {@code false} otherwise
     */
    private boolean hasRequestContext() {
        return getRequestContext() != null;
    }

    /**
     * @return {@link
     *          org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *          flow execution paused key}
     */
    private String getPauseKey() {
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
            pauseKey = (String) session.get(PausedKeyInterceptor.DEFAULT_PAUSED_KEY_SESSION_KEY);
        }

        return pauseKey;
    }

    /**
     * @return {@code true} if {@link #getPauseKey() paused key} exists,
     *         {@code false} otherwise
     */
    private boolean hasPauseKey() {
        return getPauseKey() != null;
    }

    /**
     * Finds the flow execution scope from flow execution repository.
     *
     * @param flowExecutor {@link FlowExecutor flow executor} bean
     * @return flow execution scope as map
     */
    private Map getExternalFlowScopeAsMap(FlowExecutor flowExecutor) {
        // don't have the external context set?
        // if no, need to create and set the external context
        if (ExternalContextHolder.getExternalContext() == null) {
            ExternalContext context =
                    new ServletExternalContext(
                            ServletActionContext.getServletContext(),
                            ServletActionContext.getRequest(),
                            ServletActionContext.getResponse());

            // keeping safe
            context.getRequestMap().put(
                    ActionInvocation.class.getName(),
                    ActionContext.getContext().getActionInvocation());

            ExternalContextHolder.setExternalContext(context);
        }

        // find the flow execution and its scape
        FlowExecutionRepository repository = ((FlowExecutorImpl) flowExecutor).getExecutionRepository();
        FlowExecutionKey key = repository.parseFlowExecutionKey(getPauseKey());
        FlowExecution flowExecution = repository.getFlowExecution(key);
        MutableAttributeMap scope = flowExecution.getActiveSession().getScope();
        return scope.asMap();
    }

    /**
     * @return {@code true} if flow execution scope exists, {@code false}
     *         otherwise
     */
    protected final boolean hasFlowScope() {
        return hasRequestContext() || hasPauseKey();
    }

    /**
     * @return flow execution scope
     */
    protected final Map getFlowScopeAsMap() {
        // have request context?
        // if yes, get scope from request context
        // else get it from flow execution repository
        return hasRequestContext()
               ? getRequestContext().getFlowScope().asMap()
               : getExternalFlowScopeAsMap(getFlowExecutor());
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

    /**
     * @return {@link FlowExecutor flow executor} bean name
     */
    public String getFlowExecutorBean() {
        return flowExecutorBean;
    }

    /**
     * {@link FlowExecutor Flow executor} bean name as configured in the Spring
     * context hierarchy.
     * <p/>
     * This action will try to find the {@link FlowExecutor flow executor} bean
     * by this name from the Spring context hierarchy.
     *
     * @param flowExecutorBean {@link FlowExecutor flow executor} bean name to
     *                         be set
     */
    public void setFlowExecutorBean(String flowExecutorBean) {
        this.flowExecutorBean = flowExecutorBean;
    }

    /**
     * {@link FlowExecutor Flow executor} must be configured in the Spring
     * context.
     *
     * @return {@link FlowExecutor flow executor} bean
     */
    public FlowExecutor getFlowExecutor() {
        // need to find the Spring web application context
        WebApplicationContext context =
                WebApplicationContextUtils
                        .getRequiredWebApplicationContext(
                                ServletActionContext.getServletContext());

        // have the flow executor configured?
        // if yes, get the flow execution
        // else, blame
        if (context.containsBean(flowExecutorBean))
            flowExecutor = context.getBean(flowExecutorBean, FlowExecutor.class);
        else
            throw new RuntimeException("Flow executor named as '" + flowExecutorBean + "' not found!");

        return flowExecutor;
    }

    /**
     * @param flowExecutor {@link FlowExecutor flow executor} bean to be set
     */
    public void setFlowExecutor(FlowExecutor flowExecutor) {
        this.flowExecutor = flowExecutor;
    }
}
