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
 * PA-PK-AS-REP ::= CHOICE {
 *    dhInfo                  [0] DHRepInfo,
 *             -- Selected when Diffie-Hellman key exchange is
 *             -- used.
 *    encKeyPack              [1] IMPLICIT OCTET STRING,
 *             -- Selected when public key encryption is used.
 *             -- Contains a CMS type ContentInfo encoded
 *             -- according to [RFC3852].
 *             -- The contentType field of the type ContentInfo is
 *             -- id-envelopedData (1.2.840.113549.1.7.3).
 *             -- The content field is an EnvelopedData.
 *             -- The contentType field for the type EnvelopedData
 *             -- is id-signedData (1.2.840.113549.1.7.2).
 *             -- The eContentType field for the inner type
 *             -- SignedData (when unencrypted) is
 *             -- id-pkinit-rkeyData (1.3.6.1.5.2.3.3) and the
 *             -- eContent field contains the DER encoding of the
 *             -- type ReplyKeyPack.
 *             -- ReplyKeyPack is defined below.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class PaPkAsRep
{
    /**
     * Selected when Diffie-Hellman key exchange is used.
     */
    private DhRepInfo dhInfo;

    /**
     * Selected when public key encryption is used.  Contains a CMS type ContentInfo
     * encoded according to [RFC3852].  The contentType field of the type ContentInfo
     * is id-envelopedData (1.2.840.113549.1.7.3).  The content field is an EnvelopedData.
     * The contentType field for the type EnvelopedData is id-signedData (1.2.840.113549.1.7.2).
     * The eContentType field for the inner type SignedData (when unencrypted) is
     * id-pkinit-rkeyData (1.3.6.1.5.2.3.3) and the eContent field contains the
     * DER encoding of the type ReplyKeyPack.
     */
    private byte[] encKeyPack;


    /**
     * Creates a new instance of PaPkAsRep using the Diffie-Hellman key exchange method.
     *
     * @param dhInfo
     */
    public PaPkAsRep( DhRepInfo dhInfo )
    {
        this.dhInfo = dhInfo;
    }


    /**
     * Creates a new instance of PaPkAsRep using the public key method.
     *
     * @param encKeyPack
     */
    public PaPkAsRep( byte[] encKeyPack )
    {
        this.encKeyPack = encKeyPack;
    }


    /**
     * @return the dhInfo
     */
    public DhRepInfo getDhInfo()
    {
        return dhInfo;
    }


    /**
     * @return the encKeyPack
     */
    public byte[] getEncKeyPack()
    {
        return encKeyPack;
    }
}
