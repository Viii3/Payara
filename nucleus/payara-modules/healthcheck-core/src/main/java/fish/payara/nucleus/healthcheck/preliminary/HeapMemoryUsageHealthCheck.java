/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2021 Payara Foundation and/or its affiliates. All rights reserved.
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
package fish.payara.nucleus.healthcheck.preliminary;

import fish.payara.nucleus.healthcheck.HealthCheckResult;
import fish.payara.monitoring.collect.MonitoringData;
import fish.payara.monitoring.collect.MonitoringDataCollector;
import fish.payara.monitoring.collect.MonitoringDataSource;
import fish.payara.monitoring.collect.MonitoringWatchCollector;
import fish.payara.monitoring.collect.MonitoringWatchSource;
import fish.payara.notification.healthcheck.HealthCheckResultEntry;
import fish.payara.nucleus.healthcheck.HealthCheckWithThresholdExecutionOptions;
import fish.payara.nucleus.healthcheck.configuration.HeapMemoryUsageChecker;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * @author mertcaliskan
 */
@Service(name = "healthcheck-heap")
@RunLevel(StartupRunLevel.VAL)
public class HeapMemoryUsageHealthCheck
extends BaseThresholdHealthCheck<HealthCheckWithThresholdExecutionOptions, HeapMemoryUsageChecker> 
implements MonitoringDataSource, MonitoringWatchSource {

    @PostConstruct
    void postConstruct() {
        postConstruct(this, HeapMemoryUsageChecker.class);
    }

    @Override
    public HealthCheckWithThresholdExecutionOptions constructOptions(HeapMemoryUsageChecker checker) {
        return super.constructThresholdOptions(checker);
    }

    @Override
    protected String getDescription() {
        return "healthcheck.description.heapMemory";
    }

    @Override
    protected HealthCheckResult doCheckInternal() {
        HealthCheckResult result = new HealthCheckResult();
        MemoryUsage heap = getMemoryUsage();

        String heapValueText = String.format("heap: init: %s, used: %s, committed: %s, max.: %s",
                prettyPrintBytes(heap.getInit()),
                prettyPrintBytes(heap.getUsed()),
                prettyPrintBytes(heap.getCommitted()),
                prettyPrintBytes(heap.getMax()));
        long percentage = calculatePercentage(heap);
        result.add(new HealthCheckResultEntry(decideOnStatusWithRatio(percentage), heapValueText + "heap%: " + percentage + "%"));

        return result;
    }

    private static MemoryUsage getMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    }

    @Override
    public void collect(MonitoringWatchCollector collector) {
        collectUsage(collector, "ns:health HeapUsage", "Heap Usage", 15, true);
    }

    @Override
    @MonitoringData(ns = "health", intervalSeconds = 4)
    public void collect(MonitoringDataCollector collector) {
        if (options != null && options.isEnabled()) {
            collector.collect("HeapUsage", calculatePercentage(getMemoryUsage()));
        }
    }

    private static long calculatePercentage(MemoryUsage usage) {
        return usage.getMax() == 0L ? 0L : Math.round(((double)usage.getUsed() / (double)usage.getMax()) * 100d);
    }
}

