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


/**
 * DHRepInfo ::= SEQUENCE {
 *    dhSignedData            [0] IMPLICIT OCTET STRING,
 *             -- Contains a CMS type ContentInfo encoded according
 *             -- to [RFC3852].
 *             -- The contentType field of the type ContentInfo is
 *             -- id-signedData (1.2.840.113549.1.7.2), and the
 *             -- content field is a SignedData.
 *             -- The eContentType field for the type SignedData is
 *             -- id-pkinit-DHKeyData (1.3.6.1.5.2.3.2), and the
 *             -- eContent field contains the DER encoding of the
 *             -- type KDCDHKeyInfo.
 *             -- KDCDHKeyInfo is defined below.
 *    serverDHNonce           [1] DHNonce OPTIONAL,
 *             -- Present if and only if dhKeyExpiration is
 *             -- present.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class DhRepInfo
{
    /**
     * Contains a CMS type ContentInfo encoded according to [RFC3852].  The contentType
     * field of the type ContentInfo is id-signedData (1.2.840.113549.1.7.2), and the
     * content field is a SignedData.  The eContentType field for the type SignedData
     * is id-pkinit-DHKeyData (1.3.6.1.5.2.3.2), and the eContent field contains the
     * DER encoding of the type KDCDHKeyInfo.
     */
    private byte[] dhSignedData;

    /**
     * Present if and only if dhKeyExpiration is present.
     */
    private byte[] serverDHNonce;


    /**
     * Creates a new instance of DhRepInfo.
     *
     * @param dhSignedData
     * @param serverDHNonce
     */
    public DhRepInfo( byte[] dhSignedData, byte[] serverDHNonce )
    {
        this.dhSignedData = dhSignedData;
        this.serverDHNonce = serverDHNonce;
    }


    /**
     * @return the dhSignedData
     */
    public byte[] getDhSignedData()
    {
        return dhSignedData;
    }


    /**
     * @return the serverDHNonce
     */
    public byte[] getServerDHNonce()
    {
        return serverDHNonce;
    }
}
