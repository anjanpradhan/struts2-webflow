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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts2.dispatcher.DefaultActionSupport;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * The adapter between the Struts 2 action layer and the Spring Web Flow
 * engine, enables to execute Spring Web Flow under the Struts 2.
 * <p/>
 * It assumes that the web application is running with a
 * {@link
 *  org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext(javax.servlet.ServletContext)
 *  Spring web application context}
 * and the {@link FlowExecutor flow executor} is defined in that Spring web
 * application context (or somewhere in its hierarchy).
 * <p/>
 * Additionally,
 * {@link org.springframework.webflow.engine.ViewState every view} in a
 * {@link org.springframework.webflow.engine.Flow flow} must have it set as the
 * target action and submit the
 * {@link
 *  org.springframework.webflow.definition.TransitionDefinition#getId()
 *  transition event id}
 * by the request parameter named as {@code _eventId} along with others.
 */
public class FlowAction
        extends DefaultActionSupport {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAction.class);

    /**
     * Default expression to find/set the
     * {@link FlowExecutionResult#getPausedKey() flow execution paused key}
     * from/to the value stack.
     */
    public static final String DEFAULT_PAUSED_KEY_EXPRESSION = "pausedKey";


    /**
     * The {@link PluginConfiguration plugin configuration} as configured.
     * <p/>
     * Can be set through {@link #setConfiguration(PluginConfiguration)}.
     */
    private PluginConfiguration configuration = new PluginConfiguration();
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
     * <p/>
     * Must be set through {@link #setPausedKey(String)}.
     * <br/>Will be set every time either from the session or the value stack.
     */
    private String pausedKey;

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute() {
        // create the external context
        ExternalContext context = ExternalContextUtils.createExternalContext();
        // get the flow executor
        FlowExecutor executor = FlowExecutorUtils.getRequiredFlowExecutor(configuration.getFlowExecutorBean());

        LOGGER.debug("old paused key {}", pausedKey);

        // don't have the paused key?
        // if no, launch the flow execution
        // else, resume the flow execution
        FlowExecutionResult result =
                pausedKey == null
                        ? executor.launchExecution(flowId, null, context)
                        : executor.resumeExecution(pausedKey, context);

        // need to store the paused key
        // will be put to the session
        // so, next time, can resume the flow execution
        pausedKey = result.isEnded() ? null : result.getPausedKey();

        LOGGER.debug("new paused key {}", pausedKey);

        // hoping view resolver had put it correctly
        // let the Struts handle it
        return (String) context.getRequestMap().get(JspViewResolver.DEFAULT_VIEW_ATTRIBUTE_NAME);
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
