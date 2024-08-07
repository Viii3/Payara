/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) [2016-2021] Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/main/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.appserver.context;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.util.Utility;
import java.util.List;
import java.util.Map;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.internal.api.JavaEEContextUtil;
import org.jboss.weld.context.bound.BoundRequestContext;

/**
 * JavaEE context holder implementation
 *
 * @author lprimak
 */
class ContextImpl {
    public static class Context implements JavaEEContextUtil.Context {
        @Override
        public void close() {
            if (invocation != null) {
                invMgr.postInvoke(invocation);
                Utility.setContextClassLoader(oldClassLoader);
            }
        }

        @Override
        public boolean isValid() {
            return invocation != null && !JavaEEContextUtilImpl.isLeaked(compEnvMgr,
                    invocation, invocation.getComponentId());
        }


        private final ComponentInvocation invocation;
        private final InvocationManager invMgr;
        private final ComponentEnvManager compEnvMgr;
        private final ClassLoader oldClassLoader;

        public Context(ComponentInvocation invocation, InvocationManager invMgr, ComponentEnvManager compEnvMgr,
                ClassLoader oldClassLoader) {
            this.invocation = invocation;
            this.invMgr = invMgr;
            this.compEnvMgr = compEnvMgr;
            this.oldClassLoader = oldClassLoader;
        }
    }

    public static class ClassLoaderContext implements JavaEEContextUtil.Context {
        private final ClassLoader oldClassLoader;
        private final boolean resetOldClassLoader;

        public ClassLoaderContext(ClassLoader oldClassLoader, boolean resetOldClassLoader) {
            this.oldClassLoader = oldClassLoader;
            this.resetOldClassLoader = resetOldClassLoader;
        }

        @Override
        public void close() {
            if (resetOldClassLoader) {
                Utility.setContextClassLoader(oldClassLoader);
            }
        }
    }

    public static class EmptyContext implements JavaEEContextUtil.Context {
        private final InvocationManager invocationManager;
        private final List<? extends ComponentInvocation> previousInvocations;

        EmptyContext(InvocationManager invocationManager) {
            this.invocationManager = invocationManager;
            previousInvocations = invocationManager.popAllInvocations();
        }

        @Override
        public void close() {
            invocationManager.putAllInvocations(previousInvocations);
        }
    }

    public static class RequestContext implements JavaEEContextUtil.Context {
        @Override
        public void close() {
            if (ctx != null) {
                ctx.deactivate();
                ctx.dissociate(storage);
            }
            rootCtx.close();
        }

        private final JavaEEContextUtil.Context rootCtx;
        final BoundRequestContext ctx;
        final Map<String, Object> storage;

        public RequestContext(org.glassfish.internal.api.JavaEEContextUtil.Context rootCtx, BoundRequestContext ctx,
                Map<String, Object> storage) {
            this.rootCtx = rootCtx;
            this.ctx = ctx;
            this.storage = storage;
        }

        @Override
        public boolean isValid() {
            return rootCtx.isValid();
        }
    }
}
