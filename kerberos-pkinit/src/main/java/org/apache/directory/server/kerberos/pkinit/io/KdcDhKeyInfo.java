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


import org.apache.directory.shared.kerberos.KerberosTime;


/**
 * KDCDHKeyInfo ::= SEQUENCE {
 *    subjectPublicKey        [0] BIT STRING,
 *             -- The KDC's DH public key.
 *             -- The DH public key value is encoded as a BIT
 *             -- STRING according to [RFC3279].
 *    nonce                   [1] INTEGER (0..4294967295),
 *             -- Contains the nonce in the pkAuthenticator field
 *             -- in the request if the DH keys are NOT reused,
 *             -- 0 otherwise.
 *    dhKeyExpiration         [2] KerberosTime OPTIONAL,
 *             -- Expiration time for KDC's key pair,
 *             -- present if and only if the DH keys are reused.
 *             -- If present, the KDC's DH public key MUST not be
 *             -- used past the point of this expiration time.
 *             -- If this field is omitted then the serverDHNonce
 *             -- field MUST also be omitted.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class KdcDhKeyInfo
{
    /**
     * The KDC's DH public key.  The DH public key value is encoded as a BIT STRING
     * according to [RFC3279].
     */
    private byte[] subjectPublicKey;

    /**
     * Contains the nonce in the pkAuthenticator field in the request if the DH
     * keys are NOT reused, 0 otherwise.
     */
    private int nonce;

    /**
     * Expiration time for KDC's key pair, present if and only if the DH keys are
     * reused.  If present, the KDC's DH public key MUST not be used past the point
     * of this expiration time.  If this field is omitted then the serverDHNonce
     * field MUST also be omitted.
     */
    private KerberosTime dhKeyExpiration;


    /**
     * Creates a new instance of KdcDhKeyInfo.
     *
     * @param subjectPublicKey
     * @param nonce
     * @param dhKeyExpiration
     */
    public KdcDhKeyInfo( byte[] subjectPublicKey, int nonce, KerberosTime dhKeyExpiration )
    {
        this.subjectPublicKey = subjectPublicKey;
        this.nonce = nonce;
        this.dhKeyExpiration = dhKeyExpiration;
    }


    /**
     * Creates a new instance of KdcDhKeyInfo.
     *
     * @param encodedKdcDhKeyInfo
     */
    public KdcDhKeyInfo( byte[] encodedKdcDhKeyInfo )
    {
        // TODO - Decode.
    }


    /**
     * @return the subjectPublicKey
     */
    public byte[] getSubjectPublicKey()
    {
        return subjectPublicKey;
    }


    /**
     * @return the nonce
     */
    public int getNonce()
    {
        return nonce;
    }


    /**
     * @return the dhKeyExpiration
     */
    public KerberosTime getDhKeyExpiration()
    {
        return dhKeyExpiration;
    }


    /**
     * @return the encoded
     */
    public byte[] getEncoded()
    {
        return new byte[0];
    }
}
