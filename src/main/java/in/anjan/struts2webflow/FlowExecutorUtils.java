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

import org.apache.struts2.ServletActionContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * The {@link FlowExecutor flow executor} utils.
 * <p/>
 * Finds the {@link FlowExecutor flow executor} from the
 * {@link
 *  WebApplicationContextUtils#getRequiredWebApplicationContext(javax.servlet.ServletContext)
 *  Spring web application context}.
 */
public final class FlowExecutorUtils {

    /**
     * {@link FlowExecutor Flow executor} must be configured in the Spring
     * web application context hierarchy.
     *
     * @param flowExecutorBean {@link FlowExecutor flow executor} bean name to
     *                         be used
     * @return {@link FlowExecutor flow executor} bean
     * @throws RuntimeException in case {@link FlowExecutor flow executor} is
     *                          not configured in the Spring web application
     *                          context hierarchy.
     */
    public static FlowExecutor getRequiredFlowExecutor(String flowExecutorBean) {
        // need to find the Spring web application context
        WebApplicationContext context =
                WebApplicationContextUtils
                        .getRequiredWebApplicationContext(
                                ServletActionContext.getServletContext());

        // have the flow executor configured?
        // if yes, get the flow execution
        // else, blame
        if (context.containsBean(flowExecutorBean))
            return context.getBean(flowExecutorBean, FlowExecutor.class);

        throw new RuntimeException("Flow executor named as '" + flowExecutorBean + "' not found!");
    }
}
