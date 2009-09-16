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
package org.apache.directory.server.core.schema;


import static org.apache.directory.server.core.integ.IntegrationUtils.getSchemaContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.integ.CiRunner;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.apache.directory.shared.ldap.constants.MetaSchemaConstants;
import org.apache.directory.shared.ldap.constants.SchemaConstants;
import org.apache.directory.shared.ldap.exception.LdapInvalidNameException;
import org.apache.directory.shared.ldap.exception.LdapOperationNotSupportedException;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.shared.ldap.schema.LdapSyntax;
import org.apache.directory.shared.ldap.schema.SyntaxChecker;
import org.apache.directory.shared.ldap.schema.registries.LdapSyntaxRegistry;
import org.apache.directory.shared.ldap.schema.registries.OidRegistry;
import org.apache.directory.shared.ldap.schema.registries.SyntaxCheckerRegistry;
import org.apache.directory.shared.ldap.schema.syntaxCheckers.AcceptAllSyntaxChecker;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * A test case which tests the addition of various schema elements
 * to the ldap server.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
@RunWith ( CiRunner.class )
@CleanupLevel( Level.CLASS )
public class MetaSyntaxCheckerHandlerIT
{
    private static final String OID = "1.3.6.1.4.1.18060.0.4.0.0.100000";
    private static final String NEW_OID = "1.3.6.1.4.1.18060.0.4.0.0.100001";


    public static DirectoryService service;


    private static SyntaxCheckerRegistry getSyntaxCheckerRegistry()
    {
        return service.getRegistries().getSyntaxCheckerRegistry();
    }


    private static LdapSyntaxRegistry getLdapSyntaxRegistry()
    {
        return service.getRegistries().getLdapSyntaxRegistry();
    }


    private static OidRegistry getOidRegistry()
    {
        return service.getRegistries().getOidRegistry();
    }


    /**
     * Gets relative DN to ou=schema.
     *
     * @param schemaName the name of the schema
     * @return the dn of the container holding syntax checkers for the schema
     * @throws Exception on dn parse errors
     */
    private LdapDN getSyntaxCheckerContainer( String schemaName ) throws Exception
    {
        return new LdapDN( "ou=syntaxCheckers,cn=" + schemaName );
    }
    
    
    // ----------------------------------------------------------------------
    // Test all core methods with normal operational pathways
    // ----------------------------------------------------------------------


    @Test
    public void testAddSyntaxChecker() throws Exception
    {
        Attributes attrs = new BasicAttributes( true );
        Attribute oc = new BasicAttribute( SchemaConstants.OBJECT_CLASS_AT, "top" );
        oc.add( MetaSchemaConstants.META_TOP_OC );
        oc.add( MetaSchemaConstants.META_SYNTAX_CHECKER_OC );
        attrs.put( oc );
        attrs.put( MetaSchemaConstants.M_FQCN_AT, AcceptAllSyntaxChecker.class.getName() );
        attrs.put( MetaSchemaConstants.M_OID_AT, OID );
        attrs.put( MetaSchemaConstants.M_DESCRIPTION_AT, "A test syntaxChecker" );
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        getSchemaContext( service ).createSubcontext( dn, attrs );
        
        assertTrue( getSyntaxCheckerRegistry().contains( OID ) );
        assertEquals( getSyntaxCheckerRegistry().getSchemaName( OID ), "apachemeta" );
        Class<?> clazz = getSyntaxCheckerRegistry().lookup( OID ).getClass();
        assertEquals( clazz, AcceptAllSyntaxChecker.class );
    }
    
    
    @Test
    public void testAddSyntaxCheckerWithByteCode() throws Exception
    {
        InputStream in = getClass().getResourceAsStream( "DummySyntaxChecker.bytecode" );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ( in.available() > 0 )
        {
            out.write( in.read() );
        }
        
        Attributes attrs = new BasicAttributes( true );
        Attribute oc = new BasicAttribute( SchemaConstants.OBJECT_CLASS_AT, "top" );
        oc.add( MetaSchemaConstants.META_TOP_OC );
        oc.add( MetaSchemaConstants.META_SYNTAX_CHECKER_OC );
        attrs.put( oc );
        attrs.put( MetaSchemaConstants.M_FQCN_AT, "DummySyntaxChecker" );
        attrs.put( MetaSchemaConstants.M_BYTECODE_AT, out.toByteArray() );
        attrs.put( MetaSchemaConstants.M_OID_AT, OID );
        attrs.put( MetaSchemaConstants.M_DESCRIPTION_AT, "A test syntaxChecker" );
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        getSchemaContext( service ).createSubcontext( dn, attrs );
        
        assertTrue( getSyntaxCheckerRegistry().contains( OID ) );
        assertEquals( getSyntaxCheckerRegistry().getSchemaName( OID ), "apachemeta" );
        Class<?> clazz = getSyntaxCheckerRegistry().lookup( OID ).getClass();
        assertEquals( clazz.getName(), "DummySyntaxChecker" );
    }
    
    
    @Test
    public void testDeleteSyntaxChecker() throws Exception
    {
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        testAddSyntaxChecker();
        
        getSchemaContext( service ).destroySubcontext( dn );

        assertFalse( "syntaxChecker should be removed from the registry after being deleted", 
            getSyntaxCheckerRegistry().contains( OID ) );

        //noinspection EmptyCatchBlock
        try
        {
            getSyntaxCheckerRegistry().lookup( OID );
            fail( "syntaxChecker lookup should fail after deleting the syntaxChecker" );
        }
        catch( NamingException e )
        {
        }
    }


    @Test
    public void testRenameSyntaxChecker() throws Exception
    {
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        testAddSyntaxChecker();
        
        LdapDN newdn = getSyntaxCheckerContainer( "apachemeta" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + NEW_OID );
        getSchemaContext( service ).rename( dn, newdn );

        assertFalse( "old syntaxChecker OID should be removed from the registry after being renamed", 
            getSyntaxCheckerRegistry().contains( OID ) );

        //noinspection EmptyCatchBlock
        try
        {
            getSyntaxCheckerRegistry().lookup( OID );
            fail( "syntaxChecker lookup should fail after deleting the syntaxChecker" );
        }
        catch( NamingException e )
        {
        }

        assertTrue( getSyntaxCheckerRegistry().contains( NEW_OID ) );
        Class<?> clazz = getSyntaxCheckerRegistry().lookup( NEW_OID ).getClass();
        assertEquals( clazz, AcceptAllSyntaxChecker.class );
    }


    @Test
    public void testMoveSyntaxChecker() throws Exception
    {
        testAddSyntaxChecker();
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        LdapDN newdn = getSyntaxCheckerContainer( "apache" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        getSchemaContext( service ).rename( dn, newdn );

        assertTrue( "syntaxChecker OID should still be present", 
            getSyntaxCheckerRegistry().contains( OID ) );
        
        assertEquals( "syntaxChecker schema should be set to apache not apachemeta", 
            getSyntaxCheckerRegistry().getSchemaName( OID ), "apache" );

        Class<?> clazz = getSyntaxCheckerRegistry().lookup( OID ).getClass();
        assertEquals( clazz, AcceptAllSyntaxChecker.class );
    }


    @Test
    public void testMoveSyntaxCheckerAndChangeRdn() throws Exception
    {
        testAddSyntaxChecker();
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        LdapDN newdn = getSyntaxCheckerContainer( "apache" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + NEW_OID );
        
        getSchemaContext( service ).rename( dn, newdn );

        assertFalse( "old syntaxChecker OID should NOT be present", 
            getSyntaxCheckerRegistry().contains( OID ) );
        
        assertTrue( "new syntaxChecker OID should be present", 
            getSyntaxCheckerRegistry().contains( NEW_OID ) );
        
        assertEquals( "syntaxChecker with new oid should have schema set to apache NOT apachemeta", 
            getSyntaxCheckerRegistry().getSchemaName( NEW_OID ), "apache" );

        Class<?> clazz = getSyntaxCheckerRegistry().lookup( NEW_OID ).getClass();
        assertEquals( clazz, AcceptAllSyntaxChecker.class );
    }

    
    @Test
    public void testModifySyntaxCheckerWithModificationItems() throws Exception
    {
        testAddSyntaxChecker();
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        ModificationItem[] mods = new ModificationItem[1];
        Attribute attr = new BasicAttribute( MetaSchemaConstants.M_FQCN_AT, BogusSyntaxChecker.class.getName() );
        mods[0] = new ModificationItem( DirContext.REPLACE_ATTRIBUTE, attr );
        getSchemaContext( service ).modifyAttributes( dn, mods );

        assertTrue( "syntaxChecker OID should still be present", 
            getSyntaxCheckerRegistry().contains( OID ) );
        
        assertEquals( "syntaxChecker schema should be set to apachemeta", 
            getSyntaxCheckerRegistry().getSchemaName( OID ), "apachemeta" );

        Class<?> clazz = getSyntaxCheckerRegistry().lookup( OID ).getClass();
        assertEquals( clazz, BogusSyntaxChecker.class );
    }

    
    @Test
    public void testModifySyntaxCheckerWithAttributes() throws Exception
    {
        testAddSyntaxChecker();
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        Attributes mods = new BasicAttributes( true );
        mods.put( MetaSchemaConstants.M_FQCN_AT, BogusSyntaxChecker.class.getName() );
        getSchemaContext( service ).modifyAttributes( dn, DirContext.REPLACE_ATTRIBUTE, mods );

        assertTrue( "syntaxChecker OID should still be present", 
            getSyntaxCheckerRegistry().contains( OID ) );
        
        assertEquals( "syntaxChecker schema should be set to apachemeta", 
            getSyntaxCheckerRegistry().getSchemaName( OID ), "apachemeta" );

        Class<?> clazz = getSyntaxCheckerRegistry().lookup( OID ).getClass();
        assertEquals( clazz, BogusSyntaxChecker.class );
    }
    

    // ----------------------------------------------------------------------
    // Test move, rename, and delete when a MR exists and uses the Normalizer
    // ----------------------------------------------------------------------

    
    @Test
    public void testDeleteSyntaxCheckerWhenInUse() throws Exception
    {
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        testAddSyntaxChecker();
        getLdapSyntaxRegistry().register( new DummySyntax() );
        
        try
        {
            getSchemaContext( service ).destroySubcontext( dn );
            fail( "should not be able to delete a syntaxChecker in use" );
        }
        catch( LdapOperationNotSupportedException e ) 
        {
            assertEquals( e.getResultCode(), ResultCodeEnum.UNWILLING_TO_PERFORM );
        }

        assertTrue( "syntaxChecker should still be in the registry after delete failure", 
            getSyntaxCheckerRegistry().contains( OID ) );
        getLdapSyntaxRegistry().unregister( OID );
        getOidRegistry().unregister( OID );
    }
    
    
    @Test
    public void testMoveSyntaxCheckerWhenInUse() throws Exception
    {
        testAddSyntaxChecker();
        getLdapSyntaxRegistry().register( new DummySyntax() );
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        LdapDN newdn = getSyntaxCheckerContainer( "apache" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        try
        {
            getSchemaContext( service ).rename( dn, newdn );
            fail( "should not be able to move a syntaxChecker in use" );
        }
        catch( LdapOperationNotSupportedException e ) 
        {
            assertEquals( e.getResultCode(), ResultCodeEnum.UNWILLING_TO_PERFORM );
        }

        assertTrue( "syntaxChecker should still be in the registry after move failure", 
            getSyntaxCheckerRegistry().contains( OID ) );
        getLdapSyntaxRegistry().unregister( OID );
        getOidRegistry().unregister( OID );
    }


    @Test
    public void testMoveSyntaxCheckerAndChangeRdnWhenInUse() throws Exception
    {
        testAddSyntaxChecker();
        getLdapSyntaxRegistry().register( new DummySyntax() );
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        LdapDN newdn = getSyntaxCheckerContainer( "apache" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + NEW_OID );
        
        try
        {
            getSchemaContext( service ).rename( dn, newdn );
            fail( "should not be able to move a syntaxChecker in use" );
        }
        catch( LdapOperationNotSupportedException e ) 
        {
            assertEquals( e.getResultCode(), ResultCodeEnum.UNWILLING_TO_PERFORM );
        }

        assertTrue( "syntaxChecker should still be in the registry after move failure", 
            getSyntaxCheckerRegistry().contains( OID ) );
        getLdapSyntaxRegistry().unregister( OID );
        getOidRegistry().unregister( OID );
    }

    
    @Test
    public void testRenameSyntaxCheckerWhenInUse() throws Exception
    {
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        testAddSyntaxChecker();
        getLdapSyntaxRegistry().register( new DummySyntax() );
        
        LdapDN newdn = getSyntaxCheckerContainer( "apachemeta" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + NEW_OID );
        
        try
        {
            getSchemaContext( service ).rename( dn, newdn );
            fail( "should not be able to rename a syntaxChecker in use" );
        }
        catch( LdapOperationNotSupportedException e ) 
        {
            assertEquals( e.getResultCode(), ResultCodeEnum.UNWILLING_TO_PERFORM );
        }

        assertTrue( "syntaxChecker should still be in the registry after rename failure", 
            getSyntaxCheckerRegistry().contains( OID ) );
        getLdapSyntaxRegistry().unregister( OID );
        getOidRegistry().unregister( OID );
    }


    // ----------------------------------------------------------------------
    // Let's try some freaky stuff
    // ----------------------------------------------------------------------


    @Test
    public void testMoveSyntaxCheckerToTop() throws Exception
    {
        testAddSyntaxChecker();
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        LdapDN top = new LdapDN();
        top.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        try
        {
            getSchemaContext( service ).rename( dn, top );
            fail( "should not be able to move a syntaxChecker up to ou=schema" );
        }
        catch( LdapInvalidNameException e ) 
        {
            assertEquals( e.getResultCode(), ResultCodeEnum.NAMING_VIOLATION );
        }

        assertTrue( "syntaxChecker should still be in the registry after move failure", 
            getSyntaxCheckerRegistry().contains( OID ) );
    }


    @Test
    public void testMoveSyntaxCheckerToComparatorContainer() throws Exception
    {
        testAddSyntaxChecker();
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        LdapDN newdn = new LdapDN( "ou=comparators,cn=apachemeta" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        try
        {
            getSchemaContext( service ).rename( dn, newdn );
            fail( "should not be able to move a syntaxChecker into comparators container" );
        }
        catch( LdapInvalidNameException e ) 
        {
            assertEquals( e.getResultCode(), ResultCodeEnum.NAMING_VIOLATION );
        }

        assertTrue( "syntaxChecker should still be in the registry after move failure", 
            getSyntaxCheckerRegistry().contains( OID ) );
    }
    
    
    @Test
    public void testAddSyntaxCheckerToDisabledSchema() throws Exception
    {
        Attributes attrs = new BasicAttributes( true );
        Attribute oc = new BasicAttribute( SchemaConstants.OBJECT_CLASS_AT, "top" );
        oc.add( MetaSchemaConstants.META_TOP_OC );
        oc.add( MetaSchemaConstants.META_SYNTAX_CHECKER_OC );
        attrs.put( oc );
        attrs.put( MetaSchemaConstants.M_FQCN_AT, AcceptAllSyntaxChecker.class.getName() );
        attrs.put( MetaSchemaConstants.M_OID_AT, OID );
        attrs.put( MetaSchemaConstants.M_DESCRIPTION_AT, "A test syntaxChecker" );
        
        // nis is by default inactive
        LdapDN dn = getSyntaxCheckerContainer( "nis" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        getSchemaContext( service ).createSubcontext( dn, attrs );
        
        assertFalse( "adding new syntaxChecker to disabled schema should not register it into the registries", 
            getSyntaxCheckerRegistry().contains( OID ) );
    }


    @Test
    public void testMoveSyntaxCheckerToDisabledSchema() throws Exception
    {
        testAddSyntaxChecker();
        
        LdapDN dn = getSyntaxCheckerContainer( "apachemeta" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        // nis is inactive by default
        LdapDN newdn = getSyntaxCheckerContainer( "nis" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        getSchemaContext( service ).rename( dn, newdn );

        assertFalse( "syntaxChecker OID should no longer be present", 
            getSyntaxCheckerRegistry().contains( OID ) );
    }


    @Test
    public void testMoveSyntaxCheckerToEnabledSchema() throws Exception
    {
        testAddSyntaxCheckerToDisabledSchema();
        
        // nis is inactive by default
        LdapDN dn = getSyntaxCheckerContainer( "nis" );
        dn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );

        assertFalse( "syntaxChecker OID should NOT be present when added to disabled nis schema", 
            getSyntaxCheckerRegistry().contains( OID ) );

        LdapDN newdn = getSyntaxCheckerContainer( "apachemeta" );
        newdn.add( MetaSchemaConstants.M_OID_AT + "=" + OID );
        
        getSchemaContext( service ).rename( dn, newdn );

        assertTrue( "syntaxChecker OID should be present when moved to enabled schema", 
            getSyntaxCheckerRegistry().contains( OID ) );
        
        assertEquals( "syntaxChecker should be in apachemeta schema after move", 
            getSyntaxCheckerRegistry().getSchemaName( OID ), "apachemeta" );
    }

    
    public static class BogusSyntaxChecker extends SyntaxChecker
    {
        private static final long serialVersionUID = 1L;

        public BogusSyntaxChecker()
        {
        	super(OID);
        }
        
        public boolean isValidSyntax( Object value )
        {
            return false;
        }
    }

    
    class DummySyntax extends LdapSyntax
    {
        private static final long serialVersionUID = 1L;

        public DummySyntax()
        {
            super( OID );
            addName( "dummy" );
            isObsolete = false; 
            isHumanReadable = false;
            syntaxChecker = null;
        }
    }
}
