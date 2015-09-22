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


import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests the detection of installed unlimited strength policy.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class JcePolicyTest extends TestCase
{
    /** the logger for this class */
    private static final Logger logger = LoggerFactory.getLogger( JcePolicyTest.class );


    /**
     * Tests the detection of installed unlimited strength policy.
     */
    public void testPolicy()
    {
        if ( !isStrongCryptoEnabled() )
        {
            assertFalse( isStrongCryptoEnabled() );
            logger.warn( "Skipping AES tests in {} because unlimited strength policy is not installed.",
                JcePolicyTest.class.getSimpleName() );
        }
        else
        {
            assertTrue( isStrongCryptoEnabled() );
        }
    }


    /**
     * Will be 128 for AES weak crypto, "2147483647" for strong (unlimited strength).
     */
    protected boolean isStrongCryptoEnabled()
    {
        try
        {
            return ( Cipher.getMaxAllowedKeyLength( "AES" ) > 128 );
        }
        catch ( NoSuchAlgorithmException nsae )
        {
            return false;
        }
    }
}
