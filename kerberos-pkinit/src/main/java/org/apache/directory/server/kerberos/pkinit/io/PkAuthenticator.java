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
 * PKAuthenticator ::= SEQUENCE {
 *    cusec                   [0] INTEGER (0..999999),
 *    ctime                   [1] KerberosTime,
 *             -- cusec and ctime are used as in [RFC4120], for
 *             -- replay prevention.
 *    nonce                   [2] INTEGER (0..4294967295),
 *             -- Chosen randomly; this nonce does not need to
 *             -- match with the nonce in the KDC-REQ-BODY.
 *    paChecksum              [3] OCTET STRING OPTIONAL,
 *             -- MUST be present.
 *             -- Contains the SHA1 checksum, performed over
 *             -- KDC-REQ-BODY.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class PkAuthenticator
{
    /**
     * cusec is used as in [RFC4120], for replay prevention.
     */
    private int cusec;

    /**
     * ctime is used as in [RFC4120], for replay prevention.
     */
    private KerberosTime ctime;

    /**
     * Chosen randomly; this nonce does not need to match with the nonce in the
     * KDC-REQ-BODY.
     */
    private int nonce;

    /**
     * MUST be present.  Contains the SHA1 checksum, performed over KDC-REQ-BODY.
     */
    private byte[] paChecksum;


    /**
     * Creates a new instance of PkAuthenticator.
     *
     * @param cusec
     * @param ctime
     * @param nonce
     * @param paChecksum
     */
    public PkAuthenticator( int cusec, KerberosTime ctime, int nonce, byte[] paChecksum )
    {
        this.cusec = cusec;
        this.ctime = ctime;
        this.nonce = nonce;
        this.paChecksum = paChecksum;
    }


    /**
     * @return the cusec
     */
    public int getCusec()
    {
        return cusec;
    }


    /**
     * @return the ctime
     */
    public KerberosTime getCtime()
    {
        return ctime;
    }


    /**
     * @return the nonce
     */
    public int getNonce()
    {
        return nonce;
    }


    /**
     * @return the paChecksum
     */
    public byte[] getPaChecksum()
    {
        return paChecksum;
    }
}
