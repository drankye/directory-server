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


import java.util.List;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;


/**
 * AuthPack ::= SEQUENCE {
 *    pkAuthenticator         [0] PKAuthenticator,
 *    clientPublicValue       [1] SubjectPublicKeyInfo OPTIONAL,
 *             -- Type SubjectPublicKeyInfo is defined in
 *             -- [RFC3280].
 *             -- Specifies Diffie-Hellman domain parameters
 *             -- and the client's public key value [IEEE1363].
 *             -- The DH public key value is encoded as a BIT
 *             -- STRING according to [RFC3279].
 *             -- This field is present only if the client wishes
 *             -- to use the Diffie-Hellman key agreement method.
 *    supportedCMSTypes       [2] SEQUENCE OF AlgorithmIdentifier
 *                                OPTIONAL,
 *             -- Type AlgorithmIdentifier is defined in
 *             -- [RFC3280].
 *             -- List of CMS algorithm [RFC3370] identifiers
 *             -- that identify key transport algorithms, or
 *             -- content encryption algorithms, or signature
 *             -- algorithms supported by the client in order of
 *             -- (decreasing) preference.
 *    clientDHNonce           [3] DHNonce OPTIONAL,
 *             -- Present only if the client indicates that it
 *             -- wishes to reuse DH keys or to allow the KDC to
 *             -- do so.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class AuthPack
{
    private PkAuthenticator pkAuthenticator;

    /**
     * Type SubjectPublicKeyInfo is defined in [RFC3280].  Specifies Diffie-Hellman
     * domain parameters and the client's public key value [IEEE1363].  The DH public
     * key value is encoded as a BIT STRING according to [RFC3279].  This field is
     * present only if the client wishes to use the Diffie-Hellman key agreement method.
     */
    private byte[] clientPublicValue;

    /**
     * Type AlgorithmIdentifier is defined in [RFC3280].  List of CMS algorithm
     * [RFC3370] identifiers that identify key transport algorithms, or content
     * encryption algorithms, or signature algorithms supported by the client in
     * order of (decreasing) preference.
     */
    private List<AlgorithmIdentifier> supportedCMSTypes;

    /**
     * Present only if the client indicates that it wishes to reuse DH keys or to
     * allow the KDC to do so.
     */
    private byte[] clientDHNonce;


    /**
     * Creates a new instance of AuthPack.
     *
     * @param pkAuthenticator
     * @param clientPublicValue
     * @param supportedCMSTypes
     * @param clientDHNonce
     */
    public AuthPack( PkAuthenticator pkAuthenticator, byte[] clientPublicValue,
        List<AlgorithmIdentifier> supportedCMSTypes, byte[] clientDHNonce )
    {
        this.pkAuthenticator = pkAuthenticator;
        this.clientPublicValue = clientPublicValue;
        this.supportedCMSTypes = supportedCMSTypes;
        this.clientDHNonce = clientDHNonce;
    }


    /**
     * Creates a new instance of AuthPack.
     *
     * @param encodedAuthPack
     */
    public AuthPack( byte[] encodedAuthPack )
    {
        // TODO - Decode the AuthPack.
    }


    /**
     * @return the pkAuthenticator
     */
    public PkAuthenticator getPkAuthenticator()
    {
        return pkAuthenticator;
    }


    /**
     * @return the clientPublicValue
     */
    public byte[] getClientPublicValue()
    {
        return clientPublicValue;
    }


    /**
     * @return the supportedCMSTypes
     */
    public List<AlgorithmIdentifier> getSupportedCMSTypes()
    {
        return supportedCMSTypes;
    }


    /**
     * @return the clientDHNonce
     */
    public byte[] getClientDHNonce()
    {
        return clientDHNonce;
    }


    /**
     * @return the encoded
     */
    public byte[] getEncoded()
    {
        return new byte[0];
    }
}
