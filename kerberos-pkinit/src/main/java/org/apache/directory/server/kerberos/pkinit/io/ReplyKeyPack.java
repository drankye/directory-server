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


import org.apache.directory.shared.kerberos.components.Checksum;
import org.apache.directory.shared.kerberos.components.EncryptionKey;


/**
 * ReplyKeyPack ::= SEQUENCE {
 *    replyKey                [0] EncryptionKey,
 *             -- Contains the session key used to encrypt the
 *             -- enc-part field in the AS-REP, i.e., the
 *             -- AS reply key.
 *    asChecksum              [1] Checksum,
 *             -- Contains the checksum of the AS-REQ
 *             -- corresponding to the containing AS-REP.
 *             -- The checksum is performed over the type AS-REQ.
 *             -- The protocol key [RFC3961] of the checksum is the
 *             -- replyKey and the key usage number is 6.
 *             -- If the replyKey's enctype is "newer" [RFC4120]
 *             -- [RFC4121], the checksum is the required
 *             -- checksum operation [RFC3961] for that enctype.
 *             -- The client MUST verify this checksum upon receipt
 *             -- of the AS-REP.
 *    ...
 * }
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class ReplyKeyPack
{
    /**
     * Contains the session key used to encrypt the enc-part field in the AS-REP,
     * i.e., the AS reply key.
     */
    private EncryptionKey replyKey;

    /**
     * Contains the checksum of the AS-REQ corresponding to the containing AS-REP.
     * The checksum is performed over the type AS-REQ.  The protocol key [RFC3961]
     * of the checksum is the replyKey and the key usage number is 6.  If the replyKey's
     * enctype is "newer" [RFC4120] [RFC4121], the checksum is the required checksum
     * operation [RFC3961] for that enctype.  The client MUST verify this checksum
     * upon receipt of the AS-REP.
     */
    private Checksum asChecksum;


    /**
     * Creates a new instance of ReplyKeyPack.
     *
     * @param replyKey
     * @param asChecksum
     */
    public ReplyKeyPack( EncryptionKey replyKey, Checksum asChecksum )
    {
        this.replyKey = replyKey;
        this.asChecksum = asChecksum;
    }


    /**
     * @return the replyKey
     */
    public EncryptionKey getReplyKey()
    {
        return replyKey;
    }


    /**
     * @return the asChecksum
     */
    public Checksum getAsChecksum()
    {
        return asChecksum;
    }


    /**
     * @return the encoded
     */
    public byte[] getEncoded()
    {
        return new byte[0];
    }
}
