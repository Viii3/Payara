/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) [2016-2025] Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.weld.services;

import java.util.HashMap;
import java.util.Map;
import static org.jboss.weld.config.ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION;
import static org.jboss.weld.config.ConfigurationKey.ROLLING_UPGRADES_ID_DELIMITER;
import static org.jboss.weld.config.ConfigurationKey.CONCURRENT_DEPLOYMENT;
import static org.jboss.weld.config.ConfigurationKey.NON_PORTABLE_MODE;
import static org.jboss.weld.config.ConfigurationKey.PRELOADER_THREAD_POOL_SIZE;

import org.jboss.weld.configuration.spi.ExternalConfiguration;

/**
 * Configurator for CDI / Weld
 *
 * @author lprimak
 */
public class ExternalConfigurationImpl implements ExternalConfiguration {

    private final Map<String, Object> propsMap = new HashMap<>();
    
    public void setPreLoaderThreadPoolSize(int size) {
        propsMap.put(PRELOADER_THREAD_POOL_SIZE.get(), size);
    }
    
    public void setNonPortableMode(boolean nonPortableMode) {
        propsMap.put(NON_PORTABLE_MODE.get(), nonPortableMode);
    }
    
    public void setConcurrentDeployment(boolean concurrentDeployment) {
        propsMap.put(CONCURRENT_DEPLOYMENT.get(), concurrentDeployment);
    }

    public void setRollingUpgradesDelimiter(String rollingUpgradesDelimiter) {
        propsMap.put(ROLLING_UPGRADES_ID_DELIMITER.get(), rollingUpgradesDelimiter);
    }

    public void setBeanIndexOptimization(boolean beanIndexOptimization) {
        propsMap.put(BEAN_IDENTIFIER_INDEX_OPTIMIZATION.get(), beanIndexOptimization);
    }

    @Override
    public Map<String, Object> getConfigurationProperties() {
        return propsMap;
    }

    @Override
    public void cleanup() {
        // intentionally left blank
    }

}
