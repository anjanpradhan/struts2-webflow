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

package in.anjan.struts2webflow.annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.AnnotationUtils;
import com.opensymphony.xwork2.util.ValueStack;
import in.anjan.struts2webflow.AbstractFlowScopeInterceptor;

/**
 * Interceptor to {@link #intercept(ActionInvocation) intercept the invocation}
 * of the Struts 2 Action to set the required values to value stack.
 * <p/>
 * Acts on {@link FlowIn} and {@link FlowOut}.
 */
public class AnnotationFlowScopeInterceptor
        extends AbstractFlowScopeInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public String intercept(ActionInvocation invocation)
            throws Exception {
        if (hasFlowScope()) {
            invocation.addPreResultListener(this);

            Map flowScopeAsMap = getFlowScopeAsMap();
            ValueStack stack = invocation.getStack();

            // from flow execution scope to value stack
            List<Field> fields = new ArrayList<>();
            AnnotationUtils.addAllFields(FlowIn.class, invocation.getAction().getClass(), fields);
            for (Field field : fields) {
                String fieldName = field.getName();
                Object fieldValue = flowScopeAsMap.get(fieldName);
                if (fieldValue != null)
                    stack.setValue(fieldName, fieldValue);
            }
        }

        return invocation.invoke();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void beforeResult(ActionInvocation invocation, String resultCode) {
        if (hasFlowScope()) {
            ValueStack stack = invocation.getStack();
            Map flowScopeAsMap = getFlowScopeAsMap();

            // from value stack to flow execution scope
            List<Field> fields = new ArrayList<>();
            AnnotationUtils.addAllFields(FlowOut.class, invocation.getAction().getClass(), fields);
            for (Field field : fields) {
                String fieldName = field.getName();
                Object fieldValue = stack.findValue(fieldName);
                if (fieldValue != null)
                    flowScopeAsMap.put(fieldName, fieldValue);
            }
        }
    }
}
