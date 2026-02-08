/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *    Copyright (c) 2025-2026 Payara Foundation and/or its affiliates. All rights reserved.
 *
 *     The contents of this file are subject to the terms of either the GNU
 *     General Public License Version 2 only ("GPL") or the Common Development
 *     and Distribution License("CDDL") (collectively, the "License").  You
 *     may not use this file except in compliance with the License.  You can
 *     obtain a copy of the License at
 *     https://github.com/payara/Payara/blob/main/LICENSE.txt
 *     See the License for the specific
 *     language governing permissions and limitations under the License.
 *
 *     When distributing the software, include this License Header Notice in each
 *     file and include the License file at legal/OPEN-SOURCE-LICENSE.txt.
 *
 *     GPL Classpath Exception:
 *     The Payara Foundation designates this particular file as subject to the "Classpath"
 *     exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 *     file that accompanied this code.
 *
 *     Modifications:
 *     If applicable, add the following below the License Header, with the fields
 *     enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyright [year] [name of copyright owner]"
 *
 *     Contributor(s):
 *     If you wish your version of this file to be governed by only the CDDL or
 *     only the GPL Version 2, indicate your decision by adding "[Contributor]
 *     elects to include this software in this distribution under the [CDDL or GPL
 *     Version 2] license."  If you don't indicate a single choice of license, a
 *     recipient has the option to distribute your version of this file under
 *     either the CDDL, the GPL Version 2 or to extend the choice of license to
 *     its licensees as provided above.  However, if you add GPL Version 2 code
 *     and therefore, elected the GPL Version 2 license, then the option applies
 *     only if the new code is made subject to such option by the copyright
 *     holder.
 */
package com.sun.enterprise.security.jaspic.callback;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Caller implements Principal, Serializable {
    private static final long serialVersionUID = 1L;
    private Principal callerPrincipal;
    private Set<String> groups = new HashSet();

    public static Caller fromSubject(Subject subject) {
        Set<Caller> callers = subject.getPrincipals(Caller.class);
        return callers != null && !callers.isEmpty() ? (Caller)callers.iterator().next() : null;
    }

    public static void toSubject(Subject subject, Caller caller) {
        subject.getPrincipals().add(caller);
    }

    public Caller() {
    }

    public Caller(Principal callerPrincipal) {
        this.callerPrincipal = callerPrincipal;
    }

    public Caller(String[] groups) {
        this.groups.addAll(Arrays.asList(groups));
    }

    public Caller(Principal callerPrincipal, Set<String> groups) {
        this.callerPrincipal = callerPrincipal;
        this.groups.addAll(groups);
    }

    public String getName() {
        return this.callerPrincipal == null ? null : this.callerPrincipal.getName();
    }

    public Principal getCallerPrincipal() {
        return this.callerPrincipal;
    }

    public void setCallerPrincipal(Principal callerPrincipal) {
        this.callerPrincipal = callerPrincipal;
    }

    public Set<String> getGroups() {
        return this.groups;
    }

    public String[] getGroupsAsArray() {
        return (String[])this.groups.toArray(new String[0]);
    }

    public void addGroups(String[] groups) {
        this.groups.addAll(Arrays.asList(groups));
    }

    public boolean equals(Object o) {
        if (!(o instanceof Caller)) {
            return false;
        } else {
            return Objects.equals(this.getName(), ((Caller)o).getName());
        }
    }

    public int hashCode() {
        return this.callerPrincipal != null && this.callerPrincipal.getName() != null ? 31 * this.callerPrincipal.getName().hashCode() : 0;
    }
}