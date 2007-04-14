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
package org.apache.directory.server.core.exception;


import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.apache.directory.server.core.DirectoryServiceConfiguration;
import org.apache.directory.server.core.configuration.InterceptorConfiguration;
import org.apache.directory.server.core.interceptor.BaseInterceptor;
import org.apache.directory.server.core.interceptor.NextInterceptor;
import org.apache.directory.server.core.interceptor.context.EntryServiceContext;
import org.apache.directory.server.core.interceptor.context.LookupServiceContext;
import org.apache.directory.server.core.interceptor.context.RenameServiceContext;
import org.apache.directory.server.core.interceptor.context.ModifyServiceContext;
import org.apache.directory.server.core.interceptor.context.MoveAndRenameServiceContext;
import org.apache.directory.server.core.interceptor.context.MoveServiceContext;
import org.apache.directory.server.core.interceptor.context.ServiceContext;
import org.apache.directory.server.core.invocation.Invocation;
import org.apache.directory.server.core.invocation.InvocationStack;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.PartitionNexus;
import org.apache.directory.server.core.partition.PartitionNexusProxy;
import org.apache.directory.shared.ldap.constants.SchemaConstants;
import org.apache.directory.shared.ldap.exception.LdapAttributeInUseException;
import org.apache.directory.shared.ldap.exception.LdapContextNotEmptyException;
import org.apache.directory.shared.ldap.exception.LdapNameAlreadyBoundException;
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.apache.directory.shared.ldap.exception.LdapNamingException;
import org.apache.directory.shared.ldap.exception.LdapOperationNotSupportedException;
import org.apache.directory.shared.ldap.filter.ExprNode;
import org.apache.directory.shared.ldap.message.ModificationItemImpl;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.shared.ldap.schema.OidNormalizer;
import org.apache.directory.shared.ldap.util.EmptyEnumeration;


/**
 * An {@link org.apache.directory.server.core.interceptor.Interceptor} that detects any operations that breaks integrity
 * of {@link Partition} and terminates the current invocation chain by
 * throwing a {@link NamingException}. Those operations include when an entry
 * already exists at a DN and is added once again to the same DN.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class ExceptionService extends BaseInterceptor
{
    private PartitionNexus nexus;
    private LdapDN subschemSubentryDn;
    
    /**
     * The OIDs normalizer map
     */
    private Map<String, OidNormalizer> normalizerMap;

    /**
     * Creates an interceptor that is also the exception handling service.
     */
    public ExceptionService()
    {
    }


    public void init( DirectoryServiceConfiguration factoryCfg, InterceptorConfiguration cfg ) throws NamingException
    {
        nexus = factoryCfg.getPartitionNexus();
        normalizerMap = factoryCfg.getRegistries().getAttributeTypeRegistry().getNormalizerMapping();
        Attribute attr = nexus.getRootDSE( null ).get( "subschemaSubentry" );
        subschemSubentryDn = new LdapDN( ( String ) attr.get() );
        subschemSubentryDn.normalize( normalizerMap );
    }


    public void destroy()
    {
    }


    /**
     * In the pre-invocation state this interceptor method checks to see if the entry to be added already exists.  If it
     * does an exception is raised.
     */
    public void add( NextInterceptor nextInterceptor, ServiceContext addContext )
        throws NamingException
    {
    	LdapDN name = addContext.getDn();
    	
        if ( subschemSubentryDn.getNormName().equals( name.getNormName() ) )
        {
            throw new LdapNameAlreadyBoundException( 
                "The global schema subentry cannot be added since it exists by default." );
        }
        
        // check if the entry already exists
        if ( nextInterceptor.hasEntry( new EntryServiceContext( name ) ) )
        {
            NamingException ne = new LdapNameAlreadyBoundException( name.getUpName() + " already exists!" );
            ne.setResolvedName( new LdapDN( name.getUpName() ) );
            throw ne;
        }

        LdapDN parentDn = ( LdapDN ) name.clone();
        parentDn.remove( name.size() - 1 );

        // check if we're trying to add to a parent that is an alias
        Attributes attrs = null;
        
        try
        {
            attrs = nextInterceptor.lookup( new LookupServiceContext( parentDn ) );
        }
        catch ( Exception e )
        {
            LdapNameNotFoundException e2 = new LdapNameNotFoundException( "Parent " + parentDn.getUpName() 
                + " not found" );
            e2.setResolvedName( new LdapDN( nexus.getMatchedName( parentDn ).getUpName() ) );
            throw e2;
        }
        
        Attribute objectClass = attrs.get( SchemaConstants.OBJECT_CLASS_AT );
        
        if ( objectClass.contains( "alias" ) )
        {
            String msg = "Attempt to add entry to alias '" + name.getUpName() + "' not allowed.";
            ResultCodeEnum rc = ResultCodeEnum.ALIAS_PROBLEM;
            NamingException e = new LdapNamingException( msg, rc );
            e.setResolvedName( new LdapDN( parentDn.getUpName() ) );
            throw e;
        }

        nextInterceptor.add( addContext );
    }


    /**
     * Checks to make sure the entry being deleted exists, and has no children, otherwise throws the appropriate
     * LdapException.
     */
    public void delete( NextInterceptor nextInterceptor, ServiceContext deleteContext ) throws NamingException
    {
    	LdapDN name = deleteContext.getDn();
    	
        if ( name.getNormName().equalsIgnoreCase( subschemSubentryDn.getNormName() ) )
        {
            throw new LdapOperationNotSupportedException( 
                "Can not allow the deletion of the subschemaSubentry (" + 
                subschemSubentryDn + ") for the global schema.",
                ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        // check if entry to delete exists
        String msg = "Attempt to delete non-existant entry: ";
        assertHasEntry( nextInterceptor, msg, name );

        // check if entry to delete has children (only leaves can be deleted)
        boolean hasChildren = false;
        NamingEnumeration list = nextInterceptor.list( name );
        
        if ( list.hasMore() )
        {
            hasChildren = true;
        }

        list.close();
        
        if ( hasChildren )
        {
            LdapContextNotEmptyException e = new LdapContextNotEmptyException();
            e.setResolvedName( new LdapDN( name.getUpName() ) );
            throw e;
        }

        nextInterceptor.delete( deleteContext );
    }


    /**
     * Checks to see the base being searched exists, otherwise throws the appropriate LdapException.
     */
    public NamingEnumeration list( NextInterceptor nextInterceptor, LdapDN baseName ) throws NamingException
    {
        if ( baseName.getNormName().equals( subschemSubentryDn.getNormName() ) )
        {
            // there is nothing under the schema subentry
            return new EmptyEnumeration();
        }
        
        // check if entry to search exists
        String msg = "Attempt to search under non-existant entry: ";
        assertHasEntry( nextInterceptor, msg, baseName );

        return nextInterceptor.list( baseName );
    }


    /**
     * Checks to see the base being searched exists, otherwise throws the appropriate LdapException.
     */
    public Attributes lookup( NextInterceptor nextInterceptor, ServiceContext lookupContext ) throws NamingException
    {
        LookupServiceContext ctx = (LookupServiceContext)lookupContext;
        
        if ( ctx.getDn().getNormName().equals( subschemSubentryDn.getNormName() ) )
        {
            return nexus.getRootDSE( null );
        }
        
        // check if entry to lookup exists
        String msg = "Attempt to lookup non-existant entry: ";
        assertHasEntry( nextInterceptor, msg, ctx.getDn() );

        return nextInterceptor.lookup( lookupContext );
    }


    /**
     * Checks to see the entry being modified exists, otherwise throws the appropriate LdapException.
     */
    public void modify( NextInterceptor nextInterceptor, ServiceContext modifyContext )
        throws NamingException
    {
    	ModifyServiceContext ctx = (ModifyServiceContext)modifyContext;

        // check if entry to modify exists
        String msg = "Attempt to modify non-existant entry: ";

        // handle operations against the schema subentry in the schema service
        // and never try to look it up in the nexus below
        if ( ctx.getDn().getNormName().equalsIgnoreCase( subschemSubentryDn.getNormName() ) )
        {
            nextInterceptor.modify( modifyContext );
            return;
        }
        
        assertHasEntry( nextInterceptor, msg, ctx.getDn() );

        Attributes entry = nexus.lookup( new LookupServiceContext( ctx.getDn() ) );
        NamingEnumeration attrIds = ctx.getMods().getIDs();
        
        while ( attrIds.hasMore() )
        {
            String attrId = ( String ) attrIds.next();
            Attribute modAttr = ctx.getMods().get( attrId );
            Attribute entryAttr = entry.get( attrId );

            if ( ctx.getModOp() == DirContext.ADD_ATTRIBUTE )
            {
                if ( entryAttr != null )
                {
                    for ( int ii = 0; ii < modAttr.size(); ii++ )
                    {
                        if ( entryAttr.contains( modAttr.get( ii ) ) )
                        {
                            throw new LdapAttributeInUseException( "Trying to add existing value '" + modAttr.get( ii )
                                + "' to attribute " + attrId );
                        }
                    }
                }
            }
        }
        
        nextInterceptor.modify( modifyContext );
    }


    /**
     * Checks to see the entry being modified exists, otherwise throws the appropriate LdapException.
     */
    public void modify( NextInterceptor nextInterceptor, LdapDN name, ModificationItemImpl[] items ) throws NamingException
    {
        // check if entry to modify exists
        String msg = "Attempt to modify non-existant entry: ";

        // handle operations against the schema subentry in the schema service
        // and never try to look it up in the nexus below
        if ( name.getNormName().equalsIgnoreCase( subschemSubentryDn.getNormName() ) )
        {
            nextInterceptor.modify( name, items );
            return;
        }
        
        assertHasEntry( nextInterceptor, msg, name );

        Attributes entry = nexus.lookup( new LookupServiceContext( name ) );
        for ( int ii = 0; ii < items.length; ii++ )
        {
            if ( items[ii].getModificationOp() == DirContext.ADD_ATTRIBUTE )
            {
                Attribute modAttr = items[ii].getAttribute();
                Attribute entryAttr = entry.get( modAttr.getID() );

                if ( entryAttr != null )
                {
                    for ( int jj = 0; jj < modAttr.size(); jj++ )
                    {
                        if ( entryAttr.contains( modAttr.get( jj ) ) )
                        {
                            throw new LdapAttributeInUseException( "Trying to add existing value '" + modAttr.get( jj )
                                + "' to attribute " + modAttr.getID() );
                        }
                    }
                }
            }
        }
        nextInterceptor.modify( name, items );
    }


    /**
     * Checks to see the entry being renamed exists, otherwise throws the appropriate LdapException.
     */
    public void rename( NextInterceptor nextInterceptor, ServiceContext renameContext )
        throws NamingException
    {
        LdapDN dn = renameContext.getDn();
        
        if ( dn.getNormName().equalsIgnoreCase( subschemSubentryDn.getNormName() ) )
        {
            throw new LdapOperationNotSupportedException( 
                "Can not allow the renaming of the subschemaSubentry (" + 
                subschemSubentryDn + ") for the global schema: it is fixed at " + subschemSubentryDn,
                ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        // check if entry to rename exists
        String msg = "Attempt to rename non-existant entry: ";
        assertHasEntry( nextInterceptor, msg, dn );

        // check to see if target entry exists
        LdapDN newDn = ( LdapDN ) dn.clone();
        newDn.remove( dn.size() - 1 );
        newDn.add( ((RenameServiceContext)renameContext).getNewRdn() );
        newDn.normalize( normalizerMap );
        
        if ( nextInterceptor.hasEntry( new EntryServiceContext( newDn ) ) )
        {
            LdapNameAlreadyBoundException e;
            e = new LdapNameAlreadyBoundException( "target entry " + newDn.getUpName() + " already exists!" );
            e.setResolvedName( new LdapDN( newDn.getUpName() ) );
            throw e;
        }

        nextInterceptor.rename( renameContext );
    }


    /**
     * Checks to see the entry being moved exists, and so does its parent, otherwise throws the appropriate
     * LdapException.
     */
    public void move( NextInterceptor nextInterceptor, ServiceContext moveContext ) throws NamingException
    {
        LdapDN oriChildName = moveContext.getDn();
        LdapDN newParentName = ((MoveServiceContext)moveContext).getParent();
        
        if ( oriChildName.getNormName().equalsIgnoreCase( subschemSubentryDn.getNormName() ) )
        {
            throw new LdapOperationNotSupportedException( 
                "Can not allow the move of the subschemaSubentry (" + 
                subschemSubentryDn + ") for the global schema: it is fixed at " + subschemSubentryDn,
                ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        // check if child to move exists
        String msg = "Attempt to move to non-existant parent: ";
        assertHasEntry( nextInterceptor, msg, oriChildName );

        // check if parent to move to exists
        msg = "Attempt to move to non-existant parent: ";
        assertHasEntry( nextInterceptor, msg, newParentName );

        // check to see if target entry exists
        String rdn = oriChildName.get( oriChildName.size() - 1 );
        LdapDN target = ( LdapDN ) newParentName.clone();
        target.add( rdn );
        
        if ( nextInterceptor.hasEntry( new EntryServiceContext( target ) ) )
        {
            // we must calculate the resolved name using the user provided Rdn value
            String upRdn = new LdapDN( oriChildName.getUpName() ).get( oriChildName.size() - 1 );
            LdapDN upTarget = ( LdapDN ) newParentName.clone();
            upTarget.add( upRdn );

            LdapNameAlreadyBoundException e;
            e = new LdapNameAlreadyBoundException( "target entry " + upTarget.getUpName() + " already exists!" );
            e.setResolvedName( new LdapDN( upTarget.getUpName() ) );
            throw e;
        }

        nextInterceptor.move( moveContext );
    }


    /**
     * Checks to see the entry being moved exists, and so does its parent, otherwise throws the appropriate
     * LdapException.
     */
    public void moveAndRename( NextInterceptor nextInterceptor, ServiceContext moveAndRenameContext ) throws NamingException
    {
        LdapDN oriChildName = moveAndRenameContext.getDn();
        LdapDN parent = ((MoveAndRenameServiceContext)moveAndRenameContext).getParent();
        String newRn = ((MoveAndRenameServiceContext)moveAndRenameContext).getNewRdn();
        
        if ( oriChildName.getNormName().equalsIgnoreCase( subschemSubentryDn.getNormName() ) )
        {
            throw new LdapOperationNotSupportedException( 
                "Can not allow the move of the subschemaSubentry (" + 
                subschemSubentryDn + ") for the global schema: it is fixed at " + subschemSubentryDn,
                ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        // check if child to move exists
        String msg = "Attempt to move to non-existant parent: ";
        assertHasEntry( nextInterceptor, msg, oriChildName );

        // check if parent to move to exists
        msg = "Attempt to move to non-existant parent: ";
        assertHasEntry( nextInterceptor, msg, parent );

        // check to see if target entry exists
        LdapDN target = ( LdapDN ) parent.clone();
        target.add( newRn );
        target.normalize( normalizerMap );
        
        if ( nextInterceptor.hasEntry( new EntryServiceContext( target ) ) )
        {
            // we must calculate the resolved name using the user provided Rdn value
            LdapDN upTarget = ( LdapDN ) parent.clone();
            upTarget.add( newRn );

            LdapNameAlreadyBoundException e;
            e = new LdapNameAlreadyBoundException( "target entry " + upTarget.getUpName() + " already exists!" );
            e.setResolvedName( new LdapDN( upTarget.getUpName() ) );
            throw e;
        }

        nextInterceptor.moveAndRename( moveAndRenameContext );
    }


    /**
     * Checks to see the entry being searched exists, otherwise throws the appropriate LdapException.
     */
    public NamingEnumeration search( NextInterceptor nextInterceptor, LdapDN base, Map env, ExprNode filter,
        SearchControls searchCtls ) throws NamingException
    {
        String msg = "Attempt to search under non-existant entry: ";

        if ( base.size() == 0 )
        {
            return nextInterceptor.search( base, env, filter, searchCtls );
        }

        if ( ( subschemSubentryDn.toNormName() ).equalsIgnoreCase( base.toNormName() ) )
        {
            return nextInterceptor.search( base, env, filter, searchCtls );
        }

        assertHasEntry( nextInterceptor, msg, base );

        return nextInterceptor.search( base, env, filter, searchCtls );
    }


    /**
     * Asserts that an entry is present and as a side effect if it is not, creates a LdapNameNotFoundException, which is
     * used to set the before exception on the invocation - eventually the exception is thrown.
     *
     * @param msg        the message to prefix to the distinguished name for explanation
     * @param dn         the distinguished name of the entry that is asserted
     * @throws NamingException if the entry does not exist
     */
    private void assertHasEntry( NextInterceptor nextInterceptor, String msg, LdapDN dn ) throws NamingException
    {
        if ( subschemSubentryDn.getNormName().equals( dn.getNormName() ) )
        {
            return;
        }
        
        Invocation invocation = InvocationStack.getInstance().peek();
        PartitionNexusProxy proxy = invocation.getProxy();
        if ( !nextInterceptor.hasEntry( new EntryServiceContext( dn ) ) )
        {
            LdapNameNotFoundException e;

            if ( msg != null )
            {
                e = new LdapNameNotFoundException( msg + dn );
            }
            else
            {
                e = new LdapNameNotFoundException( dn.toString() );
            }

            e.setResolvedName( new LdapDN( proxy.getMatchedName( dn ).getUpName() ) );
            throw e;
        }
    }
}
