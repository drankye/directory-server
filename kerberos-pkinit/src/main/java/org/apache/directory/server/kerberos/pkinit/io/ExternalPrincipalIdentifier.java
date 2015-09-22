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
 * ExternalPrincipalIdentifier ::= SEQUENCE {
 *    subjectName            [0] IMPLICIT OCTET STRING OPTIONAL,
 *             -- Contains a PKIX type Name encoded according to
 *             -- [RFC3280].
 *             -- Identifies the certificate subject by the
 *             -- distinguished subject name.
 *             -- REQUIRED when there is a distinguished subject
 *             -- name present in the certificate.
 *    issuerAndSerialNumber   [1] IMPLICIT OCTET STRING OPTIONAL,
 *             -- Contains a CMS type IssuerAndSerialNumber encoded
 *             -- according to [RFC3852].
 *             -- Identifies a certificate of the subject.
 *             -- REQUIRED for TD-INVALID-CERTIFICATES and
 *             -- TD-TRUSTED-CERTIFIERS.
 *    subjectKeyIdentifier    [2] IMPLICIT OCTET STRING OPTIONAL,
 *             -- Identifies the subject's public key by a key
 *             -- identifier.  When an X.509 certificate is
 *             -- referenced, this key identifier matches the X.509
 *             -- subjectKeyIdentifier extension value.  When other
 *             -- certificate formats are referenced, the documents
 *             -- that specify the certificate format and their use
 *             -- with the CMS must include details on matching the
 *             -- key identifier to the appropriate certificate
 *             -- field.
 *             -- RECOMMENDED for TD-TRUSTED-CERTIFIERS.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class ExternalPrincipalIdentifier
{
    /**
     * Contains a PKIX type Name encoded according to [RFC3280].  Identifies the
     * certificate subject by the distinguished subject name.  REQUIRED when there
     * is a distinguished subject name present in the certificate.
     */
    private byte[] subjectName;

    /**
     * Contains a CMS type IssuerAndSerialNumber encoded according to [RFC3852].
     * Identifies a certificate of the subject.  REQUIRED for TD-INVALID-CERTIFICATES
     * and TD-TRUSTED-CERTIFIERS.
     */
    private byte[] issuerAndSerialNumber;

    /**
     * Identifies the subject's public key by a key identifier.  When an X.509 certificate
     * is referenced, this key identifier matches the X.509 subjectKeyIdentifier extension
     * value.  When other certificate formats are referenced, the documents that specify
     * the certificate format and their use with the CMS must include details on matching
     * the key identifier to the appropriate certificate field.  RECOMMENDED for
     * TD-TRUSTED-CERTIFIERS.
     */
    private byte[] subjectKeyIdentifier;


    /**
     * Creates a new instance of ExternalPrincipalIdentifier.
     *
     * @param subjectName
     * @param issuerAndSerialNumber
     * @param subjectKeyIdentifier
     */
    public ExternalPrincipalIdentifier( byte[] subjectName, byte[] issuerAndSerialNumber, byte[] subjectKeyIdentifier )
    {
        this.subjectName = subjectName;
        this.issuerAndSerialNumber = issuerAndSerialNumber;
        this.subjectKeyIdentifier = subjectKeyIdentifier;
    }


    /**
     * @return the subjectName
     */
    public byte[] getSubjectName()
    {
        return subjectName;
    }


    /**
     * @return the issuerAndSerialNumber
     */
    public byte[] getIssuerAndSerialNumber()
    {
        return issuerAndSerialNumber;
    }


    /**
     * @return the subjectKeyIdentifier
     */
    public byte[] getSubjectKeyIdentifier()
    {
        return subjectKeyIdentifier;
    }
}
