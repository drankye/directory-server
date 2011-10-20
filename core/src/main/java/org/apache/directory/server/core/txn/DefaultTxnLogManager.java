
package org.apache.directory.server.core.txn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Comparator;

import org.apache.directory.server.core.log.UserLogRecord;
import org.apache.directory.server.core.log.Log;
import org.apache.directory.server.core.log.InvalidLogException;
import org.apache.directory.server.core.api.partition.index.IndexCursor;
import org.apache.directory.server.core.api.partition.index.IndexComparator;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.name.Dn;

import org.apache.directory.server.core.txn.logedit.LogEdit;


public class DefaultTxnLogManager<ID> implements TxnLogManager<ID>
{
    /** Write ahea log */
    Log wal;
    
    /** Txn Manager */
    TxnManagerInternal<ID> txnManager;
    
    public void init( Log logger, TxnManagerInternal<ID> txnManager )
    {
        this.wal = logger;
        this.txnManager = txnManager;
    }
    /**
     * {@inheritDoc}
     */
   public void log( LogEdit<ID> logEdit, boolean sync ) throws IOException
   {
       Transaction<ID> curTxn = txnManager.getCurTxn();
       
       if ( ( curTxn == null ) || ( ! ( curTxn instanceof ReadWriteTxn ) ) )
       {
           throw new IllegalStateException( "Trying to log logedit without ReadWriteTxn" );
       }
       
       ReadWriteTxn<ID> txn = (ReadWriteTxn<ID>)curTxn;
       UserLogRecord logRecord = txn.getUserLogRecord();
       
       
       ObjectOutputStream out = null;
       ByteArrayOutputStream bout = null;
       byte[] data;

       try
       {
           bout = new ByteArrayOutputStream();
           out = new ObjectOutputStream( bout );
           out.writeObject( logEdit );
           out.flush();
           data = bout.toByteArray();
       }
       finally
       {
           if ( bout != null )
           {
               bout.close();
           }
           
           if ( out != null )
           {
               out.close();
           }
       }
       
       logRecord.setData( data, data.length );
       
       this.log( logRecord, sync );
       
       logEdit.getLogAnchor().resetLogAnchor( logRecord.getLogAnchor() );
       txn.addLogEdit( logEdit );
   }
    
   /**
    * {@inheritDoc}
    */
   public void log( UserLogRecord logRecord, boolean sync ) throws IOException
   {
       try
       {
           wal.log( logRecord, sync );
       }
       catch ( InvalidLogException e )
       {
           throw new IOException(e);
       }
   }
   
   
   /**
    * {@inheritDoc}
    */
   public Entry mergeUpdates(Dn partitionDn, ID entryID,  Entry entry )
   {
       Transaction<ID> curTxn = txnManager.getCurTxn();
       
       if ( ( curTxn == null ) )
       {
           throw new IllegalStateException( "Trying to merge with log wihout txn" );
       }
       
       return curTxn.mergeUpdates( partitionDn, entryID, entry );
   }
   
   /**
    * {@inheritDoc}
    */
   public IndexCursor<Object, Entry, ID> wrap( Dn partitionDn, IndexCursor<Object, Entry, ID> wrappedCursor, IndexComparator<Object,ID> comparator, String attributeOid, boolean forwardIndex, Object onlyValueKey, ID onlyIDKey ) throws Exception
   {
       return new IndexCursorWrapper<ID>( partitionDn, wrappedCursor, comparator, attributeOid, forwardIndex, onlyValueKey, onlyIDKey );
   }
}