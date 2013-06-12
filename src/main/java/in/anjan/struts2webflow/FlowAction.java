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

import javax.servlet.ServletContext;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.DefaultActionSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * The adapter between the Struts 2 Action layer and the Spring Web Flow
 * engine, enables to execute Spring Web Flow under the Struts 2.
 * <p/>
 * It assumes that the web application is running with a
 * {@link
 *  org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)
 *  Spring Web Application Context}
 * and the {@link FlowExecutor Flow Executor} is defined in that Spring Web
 * Application Context (or somewhere in its hierarchy).
 * <p/>
 * Additionally,
 * {@link org.springframework.webflow.engine.ViewState every View} in a
 * {@link org.springframework.webflow.engine.Flow} must have it set as the
 * target action and submit the
 * {@link
 *  org.springframework.webflow.definition.TransitionDefinition#getId()
 *  transition event id}
 * by the request parameter named as {@code _eventId} along with others.
 */
public class FlowAction
        extends DefaultActionSupport {

    /**
     * Default {@link FlowExecutor flow executor} bean name.
     */
    public static final String DEFAULT_FLOW_EXECUTOR_BEAN = "flowExecutor";

    /**
     * Default expression to find/set the
     * {@link
     *  org.springframework.webflow.executor.FlowExecutionResult#getPausedKey()
     *  flow execution paused key}
     * from/to the value stack.
     */
    public static final String DEFAULT_PAUSED_KEY_EXPRESSION = "pausedKey";

    /**
     * {@link FlowExecutor Flow executor} bean name as configured in the Spring
     * context hierarchy.
     * <p/>
     * Can be set through {@link #setFlowExecutorBean(String)}.
     * <br/>This action will try to find the {@link FlowExecutor flow executor}
     * bean by this name from the Spring context hierarchy.
     */
    private String flowExecutorBean = DEFAULT_FLOW_EXECUTOR_BEAN;
    /**
     * {@link FlowExecutor Flow executor} bean.
     * <p/>
     * Must be configured in the Spring context.
     */
    private FlowExecutor flowExecutor;
    /**
     * {@link org.springframework.webflow.engine.Flow} id.
     * <p/>
     * Must be set through {@link #setFlowId(String)}.
     * <br/>This action will execute the
     * {@link org.springframework.webflow.engine.Flow flow} by this id.
     */
    private String flowId;
    /**
     * {@link FlowExecutionResult#getPausedKey() Flow execution paused key} to
     * resume the
     * {@link
     *  org.springframework.webflow.execution.FlowExecution
     *  flow execution}
     * .
     * <p/>
     * Will be set every time either from the session or the value stack.
     */
    private String pausedKey;

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute() {
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

        // don't have the paused key?
        // if no, launch the flow execution
        // else, resume the flow execution
        FlowExecutionResult result =
                pausedKey == null
                        ? getFlowExecutor().launchExecution(flowId, null, context)
                        : getFlowExecutor().resumeExecution(pausedKey, context);

        // need to store the paused key
        // will be put to the session
        // so, next time, can resume the flow execution
        pausedKey = result.isEnded() ? null : result.getPausedKey();

        // hoping view resolver had put it correctly
        // let the Struts handle it
        return (String) context.getRequestMap().get(JspViewResolver.DEFAULT_VIEW_ATTRIBUTE_NAME);
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

    /**
     * @return {@link org.springframework.webflow.engine.Flow flow} id.
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * This action will execute the
     * {@link org.springframework.webflow.engine.Flow flow} by this id.
     *
     * @param flowId {@link org.springframework.webflow.engine.Flow flow} id to
     *               be set
     */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /**
     * @return {@link
     *          FlowExecutionResult#getPausedKey()
     *          flow execution paused key}
     */
    public String getPausedKey() {
        return pausedKey;
    }

    /**
     * This action will try to resume the
     * {@link
     *  org.springframework.webflow.execution.FlowExecution
     *  flow execution}
     * by this paused key.
     * <p/>
     * Will be set every time either from the session or the value stack.
     *
     * @param pausedKey {@link
     *                   FlowExecutionResult#getPausedKey()
     *                   flow execution paused key}
     *                  to be set
     */
    public void setPausedKey(String pausedKey) {
        this.pausedKey = pausedKey;
    }
}
