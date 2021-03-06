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

package org.apache.directory.server.core.authn;


import static org.junit.Assert.assertEquals;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Strings;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Test case for helper methods within SimpleAuthenticator.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class SimpleAuthenticatorOneWayEncryptedTest
{
    private static SimpleAuthenticator auth = null;


    @BeforeClass
    public static void setUp() throws Exception
    {

        auth = new SimpleAuthenticator( Dn.ROOT_DSE );
    }


    @Test
    public void testGetAlgorithmForHashedPassword()
    {
        String digestetValue = "{SHA}LhkDrSoM6qr0fW6hzlfOJQW61tc=";
        assertEquals( "SHA", auth.getAlgorithmForHashedPassword( Strings.getBytesUtf8( digestetValue ) ) );
        assertEquals( "SHA", auth.getAlgorithmForHashedPassword( digestetValue.getBytes() ) );

        String noAlgorithm = "Secret1!";
        assertEquals( null, auth.getAlgorithmForHashedPassword( Strings.getBytesUtf8( noAlgorithm ) ) );
        assertEquals( null, auth.getAlgorithmForHashedPassword( noAlgorithm.getBytes() ) );

        String unknownAlgorithm = "{XYZ}LhkDrSoM6qr0fW6hzlfOJQW61tc=";
        assertEquals( null, auth.getAlgorithmForHashedPassword( Strings.getBytesUtf8( unknownAlgorithm ) ) );
        assertEquals( null, auth.getAlgorithmForHashedPassword( unknownAlgorithm.getBytes() ) );
    }


    @Test
    public void testCreateDigestedPassword() throws IllegalArgumentException
    {
        String pwd = "Secret1!";
        String expected = "{SHA}znbJr3+tymFoQD4+Njh4ITtI7Cc=";
        String digested = auth.createDigestedPassword( "SHA", Strings.getBytesUtf8( pwd ) );

        assertEquals( expected, digested );
    }
}