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


/**
 * PA-PK-AS-REQ ::= SEQUENCE {
 *    signedAuthPack          [0] IMPLICIT OCTET STRING,
 *             -- Contains a CMS type ContentInfo encoded
 *             -- according to [RFC3852].
 *             -- The contentType field of the type ContentInfo
 *             -- is id-signedData (1.2.840.113549.1.7.2),
 *             -- and the content field is a SignedData.
 *             -- The eContentType field for the type SignedData is
 *             -- id-pkinit-authData (1.3.6.1.5.2.3.1), and the
 *             -- eContent field contains the DER encoding of the
 *             -- type AuthPack.
 *             -- AuthPack is defined below.
 *    trustedCertifiers       [1] SEQUENCE OF
 *                ExternalPrincipalIdentifier OPTIONAL,
 *             -- Contains a list of CAs, trusted by the client,
 *             -- that can be used to certify the KDC.
 *             -- Each ExternalPrincipalIdentifier identifies a CA
 *             -- or a CA certificate (thereby its public key).
 *             -- The information contained in the
 *             -- trustedCertifiers SHOULD be used by the KDC as
 *             -- hints to guide its selection of an appropriate
 *             -- certificate chain to return to the client.
 *    kdcPkId                 [2] IMPLICIT OCTET STRING
 *                                OPTIONAL,
 *             -- Contains a CMS type SignerIdentifier encoded
 *             -- according to [RFC3852].
 *             -- Identifies, if present, a particular KDC
 *             -- public key that the client already has.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class PaPkAsReq
{
    /**
     * Contains a CMS type ContentInfo encoded according to [RFC3852].  The contentType
     * field of the type ContentInfo is id-signedData (1.2.840.113549.1.7.2), and the
     * content field is a SignedData.  The eContentType field for the type SignedData
     * is id-pkinit-authData (1.3.6.1.5.2.3.1), and the eContent field contains the
     * DER encoding of the type AuthPack.
     */
    private byte[] signedAuthPack;

    /**
     * Contains a list of CAs, trusted by the client, that can be used to certify
     * the KDC.  Each ExternalPrincipalIdentifier identifies a CA or a CA certificate
     * (thereby its public key).  The information contained in the trustedCertifiers
     * SHOULD be used by the KDC as hints to guide its selection of an appropriate
     * certificate chain to return to the client.
     */
    private List<ExternalPrincipalIdentifier> trustedCertifiers;

    /**
     * Contains a CMS type SignerIdentifier encoded according to [RFC3852].  Identifies,
     * if present, a particular KDC public key that the client already has.
     */
    private byte[] kdcPkId;


    /**
     * Creates a new instance of PaPkAsReq.
     *
     * @param signedAuthPack
     * @param trustedCertifiers
     * @param kdcPkId
     */
    public PaPkAsReq( byte[] signedAuthPack, List<ExternalPrincipalIdentifier> trustedCertifiers, byte[] kdcPkId )
    {
        this.signedAuthPack = signedAuthPack;
        this.trustedCertifiers = trustedCertifiers;
        this.kdcPkId = kdcPkId;
    }


    /**
     * @return the signedAuthPack
     */
    public byte[] getSignedAuthPack()
    {
        return signedAuthPack;
    }


    /**
     * @return the trustedCertifiers
     */
    public List<ExternalPrincipalIdentifier> getTrustedCertifiers()
    {
        return trustedCertifiers;
    }


    /**
     * @return the kdcPkId
     */
    public byte[] getKdcPkId()
    {
        return kdcPkId;
    }


    /**
     * Returns the encoded form.
     *
     * @return The encoded form.
     */
    public byte[] getEncoded()
    {
        return new byte[0];
    }
}
