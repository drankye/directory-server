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
package org.apache.directory.server.kerberos.pkinit.certs;


import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;


/**
 * Generates an X.509 "intermediate CA" certificate programmatically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class IntermediateCaGenerator
{
    /**
     * Create certificate.
     * 
     * @param issuerCert 
     * @param issuerPrivateKey 
     * @param publicKey 
     * @param dn
     * @param validityDays
     * @param friendlyName
     * @return The certificate.
     * @throws InvalidKeyException
     * @throws SecurityException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws DataLengthException
     * @throws CertificateException
     */
    public static X509Certificate generate( X509Certificate issuerCert, PrivateKey issuerPrivateKey,
        PublicKey publicKey, String dn, int validityDays, String friendlyName ) throws InvalidKeyException,
        SecurityException, SignatureException, NoSuchAlgorithmException, DataLengthException, CertificateException
    {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        // Set certificate attributes.
        certGen.setSerialNumber( BigInteger.valueOf( System.currentTimeMillis() ) );

        certGen.setIssuerDN( PrincipalUtil.getSubjectX509Principal( issuerCert ) );
        certGen.setSubjectDN( new X509Principal( dn ) );

        certGen.setNotBefore( new Date() );

        Calendar expiry = Calendar.getInstance();
        expiry.add( Calendar.DAY_OF_YEAR, validityDays );

        certGen.setNotAfter( expiry.getTime() );

        certGen.setPublicKey( publicKey );
        certGen.setSignatureAlgorithm( "SHA1WithRSAEncryption" );

        certGen
            .addExtension( X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure( publicKey ) );

        certGen.addExtension( X509Extensions.BasicConstraints, true, new BasicConstraints( 0 ) );

        certGen.addExtension( X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(
            issuerCert ) );

        certGen.addExtension( X509Extensions.KeyUsage, true, new KeyUsage( KeyUsage.digitalSignature
            | KeyUsage.keyCertSign | KeyUsage.cRLSign ) );

        X509Certificate cert = certGen.generate( issuerPrivateKey );

        PKCS12BagAttributeCarrier bagAttr = ( PKCS12BagAttributeCarrier ) cert;

        bagAttr.setBagAttribute( PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString( friendlyName ) );
        bagAttr.setBagAttribute( PKCSObjectIdentifiers.pkcs_9_at_localKeyId, new SubjectKeyIdentifierStructure(
            publicKey ) );

        return cert;
    }
}
