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
package org.apache.directory.server.kerberos.pkinit.io;


import org.apache.directory.server.kerberos.shared.messages.value.PrincipalName;
import org.bouncycastle.asn1.DERSequence;


/**
 * KRB5PrincipalName ::= SEQUENCE {
 *    realm                   [0] Realm,
 *    principalName           [1] PrincipalName
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class Krb5PrincipalName
{
    private String realm;

    private PrincipalName principalName;


    /**
     * @return the realm
     */
    public String getRealm()
    {
        return realm;
    }


    /**
     * @return the principalName
     */
    public PrincipalName getPrincipalName()
    {
        return principalName;
    }


    /**
     * @return the sequence
     */
    public DERSequence getSequence()
    {
        return new DERSequence();
    }


    /**
     * @return the encoded
     */
    public byte[] getEncoded()
    {
        return new byte[0];
    }
}
