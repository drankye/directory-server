/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.server.core.partition.ldif;


import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.csn.CsnFactory;
import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.exception.LdapOperationErrorException;
import org.apache.directory.api.ldap.model.exception.LdapOperationException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.ldif.LdifUtils;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.util.Strings;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.api.interceptor.context.LookupOperationContext;
import org.apache.directory.server.core.api.interceptor.context.ModifyOperationContext;
import org.apache.directory.server.core.api.interceptor.context.MoveAndRenameOperationContext;
import org.apache.directory.server.core.api.interceptor.context.MoveOperationContext;
import org.apache.directory.server.core.api.interceptor.context.RenameOperationContext;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.xdbm.IndexEntry;
import org.apache.directory.server.xdbm.ParentIdAndRdn;
import org.apache.directory.server.xdbm.SingletonIndexCursor;
import org.apache.directory.server.xdbm.search.cursor.DescendantCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A LDIF based partition. Data are stored on disk as LDIF, following this organization :
 * <li> each entry is associated with a file, post-fixed with LDIF
 * <li> each entry having at least one child will have a directory created using its name.
 * The root is the partition's suffix.
 * <br>
 * So for instance, we may have on disk :
 * <pre>
 * /ou=example,ou=system.ldif
 * /ou=example,ou=system/
 *   |
 *   +--> cn=test.ldif
 *        cn=test/
 *           |
 *           +--> cn=another test.ldif
 *                ...
 * </pre>
 * <br><br>
 * In this exemple, the partition's suffix is <b>ou=example,ou=system</b>.
 * <br>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifPartition extends AbstractLdifPartition
{
    /** A logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( LdifPartition.class );

    /** The directory into which the entries are stored */
    private File suffixDirectory;

    /** Flags used for the getFile() method */
    private static final boolean CREATE = Boolean.TRUE;
    private static final boolean DELETE = Boolean.FALSE;

    /** A filter used to pick all the directories */
    private FileFilter dirFilter = new FileFilter()
    {
        public boolean accept( File dir )
        {
            return dir.isDirectory();
        }
    };

    /** A filter used to pick all the ldif entries */
    private FileFilter entryFilter = new FileFilter()
    {
        public boolean accept( File dir )
        {
            if ( dir.getName().endsWith( CONF_FILE_EXTN ) )
            {
                return dir.isFile();
            }
            else
            {
                return false;
            }
        }
    };


    /**
     * Creates a new instance of LdifPartition.
     */
    public LdifPartition( SchemaManager schemaManager, DnFactory dnFactory )
    {
        super( schemaManager, dnFactory );
    }


    /**
     * {@inheritDoc}
     */
    protected void doInit() throws Exception
    {
        if ( !initialized )
        {
            File partitionDir = new File( getPartitionPath() );

            // Initialize the suffixDirectory : it's a composition
            // of the workingDirectory followed by the suffix
            if ( ( suffixDn == null ) || ( suffixDn.isEmpty() ) )
            {
                String msg = I18n.err( I18n.ERR_150 );
                LOG.error( msg );
                throw new LdapInvalidDnException( msg );
            }

            suffixDn.apply( schemaManager );

            String suffixDirName = getFileName( suffixDn );
            suffixDirectory = new File( partitionDir, suffixDirName );

            super.doInit();

            // Create the context entry now, if it does not exists, or load the
            // existing entries
            if ( suffixDirectory.exists() )
            {
                loadEntries( partitionDir );
            }
            else
            {
                // The partition directory does not exist, we have to create it, including parent directories
                try
                {
                    suffixDirectory.mkdirs();
                }
                catch ( SecurityException se )
                {
                    String msg = I18n.err( I18n.ERR_151, suffixDirectory.getAbsolutePath(), se.getLocalizedMessage() );
                    LOG.error( msg );
                    throw se;
                }

                // And create the context entry too
                File contextEntryFile = new File( suffixDirectory + CONF_FILE_EXTN );

                LOG.info( "ldif file doesn't exist {}, creating it.", contextEntryFile.getAbsolutePath() );

                if ( contextEntry == null )
                {
                    if ( contextEntryFile.exists() )
                    {
                        LdifReader reader = new LdifReader( contextEntryFile );
                        contextEntry = new DefaultEntry( schemaManager, reader.next().getEntry() );
                        reader.close();
                    }
                    else
                    {
                        // No context entry and no LDIF file exists.
                        // Skip initialization of context entry here, it will be added later.
                        return;
                    }
                }

                // Initialization of the context entry
                if ( ( suffixDn != null ) && ( contextEntry != null ) )
                {
                    Dn contextEntryDn = contextEntry.getDn();

                    // Checking if the context entry DN is schema aware
                    if ( !contextEntryDn.isSchemaAware() )
                    {
                        contextEntryDn.apply( schemaManager );
                    }

                    // We're only adding the entry if the two DNs are equal
                    if ( suffixDn.equals( contextEntryDn ) )
                    {
                        // Looking for the current context entry
                        Entry suffixEntry = lookup( new LookupOperationContext( null, suffixDn ) );

                        // We're only adding the context entry if it doesn't already exist
                        if ( suffixEntry == null )
                        {
                            // Checking of the context entry is schema aware
                            if ( !contextEntry.isSchemaAware() )
                            {
                                // Making the context entry schema aware
                                contextEntry = new DefaultEntry( schemaManager, contextEntry );
                            }

                            // Adding the 'entryCsn' attribute
                            if ( contextEntry.get( SchemaConstants.ENTRY_CSN_AT ) == null )
                            {
                                contextEntry.add( SchemaConstants.ENTRY_CSN_AT, new CsnFactory( 0 ).newInstance()
                                    .toString() );
                            }

                            // Adding the 'entryUuid' attribute
                            if ( contextEntry.get( SchemaConstants.ENTRY_UUID_AT ) == null )
                            {
                                String uuid = UUID.randomUUID().toString();
                                contextEntry.add( SchemaConstants.ENTRY_UUID_AT, uuid );
                            }

                            // And add this entry to the underlying partition
                            add( new AddOperationContext( null, contextEntry ) );
                        }
                    }
                }
            }
        }
    }


    //-------------------------------------------------------------------------
    // Operations
    //-------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    public void add( AddOperationContext addContext ) throws LdapException
    {
        super.add( addContext );

        addEntry( addContext.getEntry() );
    }


    /**
     * {@inheritDoc}
     */
    public Entry delete( String id ) throws LdapException
    {
        Entry deletedEntry = super.delete( id );

        if ( deletedEntry != null )
        {
            File ldifFile = getFile( deletedEntry.getDn(), DELETE );

            boolean deleted = deleteFile( ldifFile );

            LOG.debug( "deleted file {} {}", ldifFile.getAbsoluteFile(), deleted );

            // Delete the parent if there is no more children
            File parentFile = ldifFile.getParentFile();

            if ( parentFile.listFiles().length == 0 )
            {
                deleteFile( parentFile );

                LOG.debug( "deleted file {} {}", parentFile.getAbsoluteFile(), deleted );
            }
        }

        return deletedEntry;
    }


    /**
     * {@inheritDoc}
     */
    public void modify( ModifyOperationContext modifyContext ) throws LdapException
    {
        String id = getEntryId( modifyContext.getDn() );

        try
        {
            super.modify( modifyContext.getDn(), modifyContext.getModItems().toArray( new Modification[]
                {} ) );
        }
        catch ( Exception e )
        {
            throw new LdapOperationException( e.getMessage(), e );
        }

        // Get the modified entry and store it in the context for post usage
        Entry modifiedEntry = fetch( id, modifyContext.getDn() );
        modifyContext.setAlteredEntry( modifiedEntry );

        // Remove the EntryDN
        modifiedEntry.removeAttributes( entryDnAT );

        // just overwrite the existing file
        Dn dn = modifyContext.getDn();

        // And write it back on disk
        try
        {
            FileWriter fw = new FileWriter( getFile( dn, DELETE ) );
            fw.write( LdifUtils.convertToLdif( modifiedEntry, true ) );
            fw.close();
        }
        catch ( IOException ioe )
        {
            throw new LdapOperationException( ioe.getMessage(), ioe );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void move( MoveOperationContext moveContext ) throws LdapException
    {
        Dn oldDn = moveContext.getDn();
        String id = getEntryId( oldDn );

        super.move( moveContext );

        // Get the modified entry
        Entry modifiedEntry = fetch( id, moveContext.getNewDn() );

        try
        {
            entryMoved( oldDn, modifiedEntry, id );
        }
        catch ( Exception e )
        {
            throw new LdapOperationErrorException( e.getMessage(), e );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void moveAndRename( MoveAndRenameOperationContext moveAndRenameContext ) throws LdapException
    {
        Dn oldDn = moveAndRenameContext.getDn();
        String id = getEntryId( oldDn );

        super.moveAndRename( moveAndRenameContext );

        // Get the modified entry and store it in the context for post usage
        Entry modifiedEntry = fetch( id, moveAndRenameContext.getNewDn() );
        moveAndRenameContext.setModifiedEntry( modifiedEntry );

        try
        {
            entryMoved( oldDn, modifiedEntry, id );
        }
        catch ( Exception e )
        {
            throw new LdapOperationErrorException( e.getMessage(), e );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void rename( RenameOperationContext renameContext ) throws LdapException
    {
        Dn oldDn = renameContext.getDn();
        String id = getEntryId( oldDn );

        // Create the new entry
        super.rename( renameContext );

        // Get the modified entry and store it in the context for post usage
        Dn newDn = oldDn.getParent().add( renameContext.getNewRdn() );
        Entry modifiedEntry = fetch( id, newDn );
        renameContext.setModifiedEntry( modifiedEntry );

        // Now move the potential children for the old entry
        // and remove the old entry
        try
        {
            entryMoved( oldDn, modifiedEntry, id );
        }
        catch ( Exception e )
        {
            throw new LdapOperationErrorException( e.getMessage(), e );
        }
    }


    /**
     * rewrites the moved entry and its associated children
     * Note that instead of moving and updating the existing files on disk
     * this method gets the moved entry and its children and writes the LDIF files
     *
     * @param oldEntryDn the moved entry's old Dn
     * @param entryId the moved entry's master table ID
     * @param deleteOldEntry a flag to tell whether to delete the old entry files
     * @throws Exception
     */
    private void entryMoved( Dn oldEntryDn, Entry modifiedEntry, String entryIdOld ) throws Exception
    {
        // First, add the new entry
        addEntry( modifiedEntry );

        String baseId = getEntryId( modifiedEntry.getDn() );

        ParentIdAndRdn parentIdAndRdn = getRdnIndex().reverseLookup( baseId );
        IndexEntry indexEntry = new IndexEntry();

        indexEntry.setId( baseId );
        indexEntry.setKey( parentIdAndRdn );

        Cursor<IndexEntry<ParentIdAndRdn, String>> cursor = new SingletonIndexCursor<ParentIdAndRdn>(
            indexEntry );
        String parentId = parentIdAndRdn.getParentId();

        Cursor<IndexEntry<String, String>> scopeCursor = new DescendantCursor( this, baseId, parentId, cursor );

        // Then, if there are some children, move then to the new place
        try
        {
            while ( scopeCursor.next() )
            {
                IndexEntry<String, String> entry = scopeCursor.get();

                // except the parent entry add the rest of entries
                if ( entry.getId() != entryIdOld )
                {
                    addEntry( fetch( entry.getId() ) );
                }
            }

            scopeCursor.close();
        }
        catch ( Exception e )
        {
            throw new LdapOperationException( e.getMessage(), e );
        }

        // And delete the old entry's LDIF file
        File file = getFile( oldEntryDn, DELETE );
        boolean deleted = deleteFile( file );
        LOG.warn( "move operation: deleted file {} {}", file.getAbsoluteFile(), deleted );

        // and the associated directory ( the file's name's minus ".ldif")
        String dirName = file.getAbsolutePath();
        dirName = dirName.substring( 0, dirName.indexOf( CONF_FILE_EXTN ) );
        deleted = deleteFile( new File( dirName ) );
        LOG.warn( "move operation: deleted dir {} {}", dirName, deleted );
    }


    /**
     * loads the configuration into the DIT from the file system
     * Note that it assumes the presence of a directory with the partition suffix's upname
     * under the partition's base dir
     *
     * for ex. if 'config' is the partition's id and 'ou=config' is its suffix it looks for the dir with the path
     *
     * <directory-service-working-dir>/config/ou=config
     * e.x example.com/config/ou=config
     *
     * NOTE: this dir setup is just to ease the testing of this partition, this needs to be
     * replaced with some kind of bootstrapping the default config from a jar file and
     * write to the FS in LDIF format
     *
     * @throws Exception
     */
    private void loadEntries( File entryDir ) throws Exception
    {
        LOG.debug( "Processing dir {}", entryDir.getName() );

        // First, load the entries
        File[] entries = entryDir.listFiles( entryFilter );

        if ( ( entries != null ) && ( entries.length != 0 ) )
        {
            LdifReader ldifReader = new LdifReader();

            for ( File entry : entries )
            {
                LOG.debug( "parsing ldif file {}", entry.getName() );
                List<LdifEntry> ldifEntries = ldifReader.parseLdifFile( entry.getAbsolutePath() );
                ldifReader.close();

                if ( ( ldifEntries != null ) && !ldifEntries.isEmpty() )
                {
                    // this ldif will have only one entry
                    LdifEntry ldifEntry = ldifEntries.get( 0 );
                    LOG.debug( "Adding entry {}", ldifEntry );

                    Entry serverEntry = new DefaultEntry( schemaManager, ldifEntry.getEntry() );

                    if ( !serverEntry.containsAttribute( SchemaConstants.ENTRY_CSN_AT ) )
                    {
                        serverEntry.put( SchemaConstants.ENTRY_CSN_AT, defaultCSNFactory.newInstance().toString() );
                    }

                    if ( !serverEntry.containsAttribute( SchemaConstants.ENTRY_UUID_AT ) )
                    {
                        serverEntry.put( SchemaConstants.ENTRY_UUID_AT, UUID.randomUUID().toString() );
                    }

                    // call add on the wrapped partition not on the self
                    AddOperationContext addContext = new AddOperationContext( null, serverEntry );
                    super.add( addContext );
                }
            }

        }
        else
        {
            // If we don't have ldif files, we won't have sub-directories
            return;
        }

        // Second, recurse on the sub directories
        File[] dirs = entryDir.listFiles( dirFilter );

        if ( ( dirs != null ) && ( dirs.length != 0 ) )
        {
            for ( File f : dirs )
            {
                loadEntries( f );
            }
        }
    }


    /**
     * Create the file name from the entry Dn.
     */
    private File getFile( Dn entryDn, boolean create ) throws LdapException
    {
        String parentDir = null;
        String rdnFileName = null;

        if ( entryDn.equals( suffixDn ) )
        {
            parentDir = suffixDirectory.getParent() + File.separator;
            rdnFileName = suffixDn.getName() + CONF_FILE_EXTN;
        }
        else
        {
            StringBuilder filePath = new StringBuilder();
            filePath.append( suffixDirectory ).append( File.separator );

            Dn baseDn = entryDn.getDescendantOf( suffixDn );
            int size = baseDn.size();

            for ( int i = 0; i < size - 1; i++ )
            {
                rdnFileName = getFileName( baseDn.getRdn( size - 1 - i ) );

                filePath.append( rdnFileName ).append( File.separator );
            }

            rdnFileName = getFileName( entryDn.getRdn() ) + CONF_FILE_EXTN;
            parentDir = filePath.toString();
        }

        File dir = new File( parentDir );

        if ( !dir.exists() && create )
        {
            // We have to create the entry if it does not have a parent
            if ( !dir.mkdir() )
            {
                throw new LdapException( I18n.err( I18n.ERR_112_COULD_NOT_CREATE_DIRECORY, dir ) );
            }
        }

        File ldifFile = new File( parentDir + rdnFileName );

        if ( ldifFile.exists() && create )
        {
            // The entry already exists
            throw new LdapException( I18n.err( I18n.ERR_633 ) );
        }

        return ldifFile;
    }


    /**
     * Compute the real name based on the Rdn, assuming that depending on the underlying
     * OS, some characters are not allowed.
     *
     * We don't allow filename which length is > 255 chars.
     */
    private String getFileName( Rdn rdn ) throws LdapException
    {
        StringBuilder fileName = new StringBuilder( "" );

        Iterator<Ava> iterator = rdn.iterator();

        while ( iterator.hasNext() )
        {
            Ava ava = iterator.next();

            // First, get the AT name, or OID
            String normAT = ava.getNormType();
            AttributeType at = schemaManager.lookupAttributeTypeRegistry( normAT );

            String atName = at.getName();

            // Now, get the normalized value
            String normValue = ava.getNormValue().getString();

            fileName.append( atName ).append( "=" ).append( normValue );

            if ( iterator.hasNext() )
            {
                fileName.append( "+" );
            }
        }

        return getOSFileName( fileName.toString() );
    }


    /**
     * Compute the real name based on the Dn, assuming that depending on the underlying
     * OS, some characters are not allowed.
     *
     * We don't allow filename which length is > 255 chars.
     */
    private String getFileName( Dn dn ) throws LdapException
    {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( Rdn rdn : dn.getRdns() )
        {
            // First, get the AT name, or OID
            String normAT = rdn.getNormType();
            AttributeType at = schemaManager.lookupAttributeTypeRegistry( normAT );

            String atName = at.getName();

            // Now, get the normalized value
            String normValue = rdn.getNormValue().getString();

            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( "," );
            }

            sb.append( atName ).append( "=" ).append( normValue );
        }

        return getOSFileName( sb.toString() );
    }


    /**
     * Get a OS compatible file name. We URL encode all characters that may cause trouble
     * according to http://en.wikipedia.org/wiki/Filenames. This includes C0 control characters
     * [0x00-0x1F] and 0x7F, see http://en.wikipedia.org/wiki/Control_characters.
     */
    private String getOSFileName( String fileName )
    {
        StringBuilder sb = new StringBuilder();

        for ( char c : fileName.toCharArray() )
        {
            switch ( c )
            {
                case 0x00:
                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                case 0x08:
                case 0x09:
                case 0x0A:
                case 0x0B:
                case 0x0C:
                case 0x0D:
                case 0x0E:
                case 0x0F:
                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                case 0x1A:
                case 0x1B:
                case 0x1C:
                case 0x1D:
                case 0x1E:
                case 0x1F:
                case 0x7F:
                case ' ': // 0x20
                case '"': // 0x22
                case '%': // 0x25
                case '&': // 0x26
                case '(': // 0x28
                case ')': // 0x29
                case '*': // 0x2A
                case '+': // 0x2B
                case '/': // 0x2F
                case ':': // 0x3A
                case ';': // 0x3B
                case '<': // 0x3C
                case '>': // 0x3E
                case '?': // 0x3F
                case '[': // 0x5B
                case '\\': // 0x5C
                case ']': // 0x5D
                case '|': // 0x7C
                    sb.append( "%" ).append( Strings.dumpHex( ( byte ) ( c >> 4 ) ) )
                        .append( Strings.dumpHex( ( byte ) ( c & 0xF ) ) );
                    break;

                default:
                    sb.append( c );
                    break;
            }
        }

        return Strings.toLowerCase( sb.toString() );
    }


    /**
     * Write the new entry on disk. It does not exist, as this has been checked
     * by the ExceptionInterceptor.
     */
    private void addEntry( Entry entry ) throws LdapException
    {
        // Remove the EntryDN
        entry.removeAttributes( entryDnAT );

        try
        {
            FileWriter fw = new FileWriter( getFile( entry.getDn(), CREATE ) );
            fw.write( LdifUtils.convertToLdif( entry ) );
            fw.close();
        }
        catch ( IOException ioe )
        {
            throw new LdapOperationException( ioe.getMessage(), ioe );
        }
    }


    /**
     * Recursively delete an entry and all of its children. If the entry is a directory,
     * then get into it, call the same method on each of the contained files,
     * and delete the directory.
     */
    private boolean deleteFile( File file )
    {
        if ( file.isDirectory() )
        {
            File[] files = file.listFiles();

            // Process the contained files
            for ( File f : files )
            {
                deleteFile( f );
            }

            // then delete the directory itself
            return file.delete();
        }
        else
        {
            return file.delete();
        }
    }
}