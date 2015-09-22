/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.kerberos.pkinit;


import junit.framework.TestCase;


/**
 * Tests the 3 main PKINIT request types.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class PkinitRequestTest extends TestCase
{
    /**
     * Tests request with a signed PkAuthenticator, using the Public Key mechanism.
     */
    public void testPublicKeyMechanism()
    {
        // TODO - Make request with no clientSubjectPublicKeyInfo nor clientDhNonce.
    }


    /**
     * Tests request with a signed PkAuthenticator, using the Diffie-Hellman mechanism.
     */
    public void testDiffieHellmanMechanism()
    {
        // TODO - Make request with clientSubjectPublicKeyInfo, but no clientDhNonce.
    }


    /**
     * Tests request with a signedPkAuthenticator, using the Diffie-Hellman mechanism and sending a client nonce.
     */
    public void testDiffieHellmanWithNonce()
    {
        // TODO - Make request with clientSubjectPublicKeyInfo and with a clientDhNonce.
    }
}