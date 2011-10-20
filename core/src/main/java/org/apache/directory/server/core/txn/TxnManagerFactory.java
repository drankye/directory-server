
package org.apache.directory.server.core.txn;

import java.util.Comparator;
import org.apache.directory.server.core.api.partition.index.Serializer;

public class TxnManagerFactory
{
    private static TxnManagerInternal<?> txnManager;
    
    private static TxnLogManager<?> txnLogManager;
    
    public static <ID> void 
        init(Comparator<ID> idComparator, Serializer idSerializer)
    {
        DefaultTxnManager<ID> dTxnManager;
        dTxnManager = new DefaultTxnManager<ID>();
        txnManager = dTxnManager;
        
        DefaultTxnLogManager<ID> dTxnLogManager;
        dTxnLogManager = new DefaultTxnLogManager<ID>();
        txnLogManager = dTxnLogManager;
        
        // TODO init txn manager and log manager
        
        dTxnManager.init( dTxnLogManager, idComparator, idSerializer );
    }
    
    
    public static <ID> TxnManager<ID> txnManagerInstance()
    {
        return ( (TxnManager<ID>) txnManager );
    }
    
    public static <ID> TxnLogManager<ID> txnLogManagerInstance()
    {
        return ( (TxnLogManager<ID>) txnLogManager );
    }
    
    static <ID> TxnManagerInternal<ID> txnManagerInternalInstance()
    {
        return ( (TxnManagerInternal<ID>) txnManager );
    }
}