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

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.view.FlowViewResolver;

/**
 * Resolves Struts 2 {@link org.springframework.webflow.engine.ViewState View}.
 * TODO: enrich
 */
public class JspViewResolver
        implements FlowViewResolver, ViewResolver {

    /**
     * Default attribute name to put the view to the context's request map.
     */
    public static final String DEFAULT_VIEW_ATTRIBUTE_NAME = JspViewResolver.class.getName();

    /**
     * NoOp view implementation.
     * <p/>
     * To make Spring Web Flow and us happy.
     */
    private static final View NO_OP_VIEW = new View() {
        @Override public String getContentType() {
            return null;
        }

        @Override public void render(Map<String, ?> model,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
            // no-op;
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public View resolveView(String viewId, RequestContext context) {
        return resolveView(viewId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewIdByConvention(String viewStateId) {
        return viewStateId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View resolveViewName(String viewName, Locale locale)
            throws Exception {
        return resolveView(viewName);
    }

    private View resolveView(String view) {
        // need to put the view to the context's request map
        // otherwise, flow action will be unhappy
        ExternalContextHolder.getExternalContext().getRequestMap().put(DEFAULT_VIEW_ATTRIBUTE_NAME, view);

        // let Spring Web Flow be happy
        return NO_OP_VIEW;
    }
}
