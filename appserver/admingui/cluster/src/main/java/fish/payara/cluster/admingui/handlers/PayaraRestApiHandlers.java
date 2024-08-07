/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2019 Payara Foundation and/or its affiliates. All rights reserved.
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

package fish.payara.cluster.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import org.glassfish.admingui.common.util.GuiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.glassfish.admingui.common.util.RestUtil.buildChildEntityList;

/**
 * Handler methods for the admingui cluster module.
 *
 * @author AndrewPielage <andrew.pielage@payara.fish>
 */
public class PayaraRestApiHandlers {

    /**
     * Method that returns a List of the nodes within the domain, ignoring TEMP nodes.
     * @param handlerCtx The handler context
     */
    @Handler(id = "py.getNodesList",
            input = {
                    @HandlerInput(name = "parentEndpoint", type = String.class, required = true),
                    @HandlerInput(name = "childType", type = String.class, required = true),
                    @HandlerInput(name = "skipList", type = List.class, required = false),
                    @HandlerInput(name = "includeList", type = List.class, required = false),
                    @HandlerInput(name = "id", type = String.class, defaultValue = "name")},
            output = {
                    @HandlerOutput(name = "result", type = List.class)
            })
    public static void getNodesList(HandlerContext handlerCtx) {
        try {
            List<Map<String, Object>> allNodes = buildChildEntityList(
                    (String)handlerCtx.getInputValue("parentEndpoint"),
                    (String)handlerCtx.getInputValue("childType"),
                    (List)handlerCtx.getInputValue("skipList"),
                    (List)handlerCtx.getInputValue("includeList"),
                    (String)handlerCtx.getInputValue("id"));

            List<Map<String, Object>> nonTempNodes = new ArrayList<>();
            for (Map<String, Object> node : allNodes) {
                if (!node.get("type").equals("TEMP")) {
                    nonTempNodes.add(node);
                }
            }

            handlerCtx.setOutputValue("result", nonTempNodes);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     * Method that gets the nodes list for the navigation side bar, ignoring TEMP nodes.
     * @param handlerCtx The handler context
     */
    @Handler(id = "py.getNodesListNavTree",
            input = {
                    @HandlerInput(name = "parentEndpoint", type = String.class, required = true),
                    @HandlerInput(name = "childType", type = String.class, required = true),
                    @HandlerInput(name = "skipList", type = List.class, required = false),
                    @HandlerInput(name = "includeList", type = List.class, required = false),
                    @HandlerInput(name = "id", type = String.class, defaultValue = "name")},
            output = {
                    @HandlerOutput(name = "result", type = Map.class)
            })
    public static void getNodesListNavTree(HandlerContext handlerCtx) {
        try {
            String parentEndpoint = (String) handlerCtx.getInputValue("parentEndpoint");
            List<Map<String, Object>> allNodes = buildChildEntityList(
                    parentEndpoint,
                    (String)handlerCtx.getInputValue("childType"),
                    (List)handlerCtx.getInputValue("skipList"),
                    (List)handlerCtx.getInputValue("includeList"),
                    (String)handlerCtx.getInputValue("id"));

            Map<String, String> nonTempNodes = new HashMap<>();
            for (Map<String, Object> node : allNodes) {
                if (!node.get("type").equals("TEMP")) {
                    String name = (String) node.get("name");
                    nonTempNodes.put(name, parentEndpoint + name);
                }
            }

            handlerCtx.setOutputValue("result", nonTempNodes);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     * Method that returns a List of names of the nodes within the domain, ignoring TEMP nodes
     * @param handlerCtx The handler context
     */
    @Handler(id = "py.getNodeNamesList",
            input = {
                    @HandlerInput(name = "parentEndpoint", type = String.class, required = true),
                    @HandlerInput(name = "childType", type = String.class, required = true),
                    @HandlerInput(name = "skipList", type = List.class, required = false),
                    @HandlerInput(name = "includeList", type = List.class, required = false),
                    @HandlerInput(name = "id", type = String.class, defaultValue = "name")},
            output = {
                    @HandlerOutput(name = "result", type = List.class)
            })
    public static void getNodeNamesList(HandlerContext handlerCtx) {
        try {
            String parentEndpoint = (String) handlerCtx.getInputValue("parentEndpoint");
            List<Map<String, Object>> allNodes = buildChildEntityList(
                    parentEndpoint,
                    (String)handlerCtx.getInputValue("childType"),
                    (List)handlerCtx.getInputValue("skipList"),
                    (List)handlerCtx.getInputValue("includeList"),
                    (String)handlerCtx.getInputValue("id"));

            List<String> nonTempNodes = new ArrayList<>();
            for (Map<String, Object> node : allNodes) {
                if (!node.get("type").equals("TEMP")) {
                    String name = (String) node.get("name");
                    nonTempNodes.add(name);
                }
            }

            handlerCtx.setOutputValue("result", nonTempNodes);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
}
