/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.env.jexl.JexlContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.w3c.dom.Node;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class WizardsTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public WizardsTest(String name) {
        super(name);
    }

    // Test data
    private URL wizard01, wizard02;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        wizard01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/wizard-01.xml");
        wizard02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/wizard-02.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        wizard01 = wizard02 = null;
    }

    /**
     * Test the wizard style SCXML documents, and send usage
     */
    public void testWizard01Sample() throws Exception {
    	exec = SCXMLTestHelper.getExecutor(wizard01);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state1", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event2");
        assertEquals(1, currentStates.size());
        assertEquals("state2", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event4");
        assertEquals(1, currentStates.size());
        assertEquals("state4", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event3");
        assertEquals(1, currentStates.size());
        assertEquals("state3", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event3"); // ensure we stay put
        assertEquals(1, currentStates.size());
        assertEquals("state3", currentStates.iterator().next().getId());
    }

    public void testWizard02Sample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(wizard02);
        exec = SCXMLTestHelper.getExecutor(new JexlContext(),
            new JexlEvaluator(), scxml, new TestEventDispatcher(),
            new Tracer());
        assertNotNull(exec);
        // If you change this, you must also change
        // the TestEventDispatcher
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state2", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event4");
        assertEquals(1, currentStates.size());
        assertEquals("state4", currentStates.iterator().next().getId());
    }

    static class TestEventDispatcher implements EventDispatcher, Serializable {
        private static final long serialVersionUID = 1L;
        // If you change this, you must also change testWizard02Sample()
        int callback = 0;
        public void send(String sendId, String target, String type,
                String event, Map<String, Object> params, Object hints, long delay,
                List<Node> externalNodes) {
            int i = ((Integer) params.get("aValue")).intValue();
            switch (callback) {
                case 0:
                    assertTrue(i == 2); // state2
                    callback++;
                    break;
                case 1:
                    assertTrue(i == 4); // state4
                    callback++;
                    break;
                default:
                    fail("More than 2 TestEventDispatcher <send> callbacks");
            }
        }
        public void cancel(String sendId) {
            // should never be called
            fail("<cancel> TestEventDispatcher callback unexpected");
        }
    }
}